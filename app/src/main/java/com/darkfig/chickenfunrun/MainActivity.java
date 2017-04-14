package com.darkfig.chickenfunrun;


import com.jme3.app.DefaultAndroidProfiler;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import audiosystem.AudioManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.jme3.app.AndroidHarnessFragment;
import com.jme3.input.event.TouchEvent;

import com.mycompany.mygame.util.IabHelper;
import com.mycompany.mygame.util.IabHelper.OnIabSetupFinishedListener;
import com.mycompany.mygame.util.IabResult;
import com.mycompany.mygame.util.Inventory;
import com.mycompany.mygame.util.Purchase;
import guisystem.Gui;
import guisystem.PopupsManager;
import guisystem.Storage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import maincharacter.LifeManager;
import mygame.Communication;
import mygame.GameState;
import mygame.GameStateManager;
import mygame.Main;
import mygame.PlayState;
import mygame.PurchaseItems;
 
public class MainActivity extends Activity implements SensorEventListener, Communication{
    /*
     * Note that you can ignore the errors displayed in this file,
     * the android project will build regardless.
     * Install the 'Android' plugin under Tools->Plugins->Available Plugins
     * to get error checks and code completion for the Android project files.
     */
    
    Main main;
    SavedSettings savedSettings;
    boolean userLeaving = false;
    SensorManager sensorManager;
    Sensor accelerometer;
    InterstitialAd mInterstitialAd;
    boolean isAdsDisabled = false;
    private static final String TAG = "NoAds";
    IabHelper mHelper;
    static final String SKU_REMOVE_ADS = "com.darkfig.noads";
    PurchaseItems activePurchaseItem;
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener 
	= new IabHelper.OnIabPurchaseFinishedListener() {
	public void onIabPurchaseFinished(IabResult result, 
                    Purchase purchase) 
	{
	   if (result.isFailure()) {
	      if(activePurchaseItem.equals(PurchaseItems.SMALLPACKGAMEOVER))
                  PopupsManager.closeBuyChickensPopup("true");
                  
                  
	      return;
	 }      
	 else if (purchase.getSku().equals(PurchaseItems.NOADS.getSku())) {
            // we dont call consumeitem(); because remove ads is a permanent purchase!
	     isAdsDisabled = true;
             main.setIsAdsDisabled(isAdsDisabled);
	    
	}
         else if(!purchase.getSku().equals(PurchaseItems.NOADS.getSku())){
             consumeItem();
         }
	      
   }
};
    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener 
   = new IabHelper.QueryInventoryFinishedListener() {
	   public void onQueryInventoryFinished(IabResult result,
	      Inventory inventory) {

		   		   
	      if (result.isFailure()) {
		  // Handle failure
                  return;
	      } else {
              if(activePurchaseItem == null)
                  return;

              Purchase purchase;

              purchase = inventory.getPurchase(activePurchaseItem.getSku());

              //use activepuschaseitem to query inventory
              if(purchase != null)
                  mHelper.consumeAsync(inventory.getPurchase(purchase.getSku()),
                          mConsumeFinishedListener);
	      }
    }
};
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
	  new IabHelper.OnConsumeFinishedListener() {
	   public void onConsumeFinished(Purchase purchase, 
             IabResult result) {

	 if (result.isSuccess()) {
             if(activePurchaseItem.equals(PurchaseItems.SMALLPACKGAMEOVER)){
                    //movi this to oncomsumefinishedlistener
                 PlayState.main.enqueue(new Callable(){

                     public Object call() throws Exception {
                         PopupsManager.closeBuyChickensPopup("false");
                         LifeManager.buyFromGameOver();
                         return null;
                     }
                     
                 });
                  
	   	  //clickButton.setEnabled(true);
             }
             else {
                 PlayState.main.enqueue(new Callable(){

                     public Object call() throws Exception {
                         LifeManager.buyFromStore(activePurchaseItem);
                         return null;
                     }
                     
                 });
	 } 
  }
         else {
	         // handle error
	 }
}
 };
    public MainActivity(){
        // Set the default logging level (default=Level.INFO, Level.ALL=All Debug Info)
        LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
        
    }
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set window fullscreen and remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        //load Saved files
        savedSettings = new SavedSettings(this);
        savedSettings.load();
        
        // find the fragment
        FragmentManager fm = getFragmentManager();
        AndroidHarnessFragment jmeFragment =
                (AndroidHarnessFragment) fm.findFragmentById(R.id.jmeFragment);
        
        main = (Main) jmeFragment.getJmeApplication();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        
       
        
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6661079999208569/6015359932");
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                
            }

            @Override
            public void onAdClosed() {
                requestInterstitial();
            }
        });
        requestNewInterstitial();
        
        String base64EncodedPublicKey = 
                                       "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgK"
                + "CAQEAsO36vCCfHedvTddfcHbk1q02LkKQrK+VSibVi83EcL8+MMhEiAwgyUbQ"
                + "FT+r+Z3JsyPn/jUsqq/B2O6GU0wLxHMz7BbcLvbwQciDc7V4DofdWEQx1GUMs"
                + "leEA0g3E3k96XqjUm+60u+uZTWVt/oC+FCF3o2rieehHioy4PcHXEX03co7fLq"
                + "UTaYprSZLm934pliC1ofBKJFddqgUhLqNBRiXN2Jg2bYHyLZmo7pU3t0wMVeGL"
                + "/SPoM89C3uEyyG1X7SgDXn25UV/waNlQb5ZQICe989WktDdcvAEP47darlYJumg"
                + "XnhVY0HMhbq+RA3dF3tfszrl7QkD2ifADx5dMwIDAQAB";
       
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new OnIabSetupFinishedListener(){

            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
             
        	             Log.d(TAG, "In-app Billing setup failed: " + 
					result);
        	           } 
                else {
                    try {
            Bundle ownedItems = mHelper.getServiceConnection().getPurchases(3, getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                 ArrayList<String> ownedSkus =
                 ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                 if(ownedSkus != null){
                     for(String item: ownedSkus)
                         if(item.equalsIgnoreCase(PurchaseItems.NOADS.getSku()))
                             isAdsDisabled = true;
                         else{
                             if(item.equalsIgnoreCase(PurchaseItems.SMALLPACK.getSku())&& !mHelper.isASyncInProgress()){

                                 activePurchaseItem = PurchaseItems.SMALLPACK;
                                 consumeItem();
                             }
                             else if(item.equalsIgnoreCase(PurchaseItems.HIGHPACK.getSku())&& !mHelper.isASyncInProgress()){
                                 activePurchaseItem = PurchaseItems.HIGHPACK;
                                 consumeItem();
                             }
                         }
                 }
            }
        } catch (RemoteException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
                     main.setIsAdsDisabled(isAdsDisabled);
        	      	     Log.d(TAG, "In-app Billing is set up OK");
		           }
            }

        });
        // uncomment the next line to add the default android profiler to the project
        //jmeFragment.getJmeApplication().setAppProfiler(new DefaultAndroidProfiler());
        


       
        
        //Now finally pass all values needed by main application
         DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        main.setScreenDensity(metrics.density);
        
        main.setCommunication(this);
        
    }
    
    public void buyItem(PurchaseItems item){
        if(item.equals(PurchaseItems.NOADS) && isAdsDisabled)
            return;
        activePurchaseItem = item;
        //replace SKU_REMOVE_ADS FROM SAMPLECODE WITH ITEM.GETSKU()
         mHelper.launchPurchaseFlow(this, item.getSku(), 10001,   
     			   mPurchaseFinishedListener, "mypurchasetoken");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, 
              resultCode, data)) {     
    	super.onActivityResult(requestCode, resultCode, data);
      }
    }
    
    public void consumeItem() {
	mHelper.queryInventoryAsync(mReceivedInventoryListener);
}
    
@Override
public void onDestroy() {
	super.onDestroy();
	if (mHelper != null) mHelper.dispose();
	mHelper = null;
}

    public void onSensorChanged(SensorEvent event) {
        if(main == null)
            return;
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            main.handleAccelerometer(event.values);
        
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }

    @Override
    protected void onPause() {
        super.onPause(); 
        savedSettings.save();
        userLeaving = false;
    }

    @Override
    protected void onStop() {
        super.onStop(); 
        //leaving();
    }
    
    
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint(); 
        leaving();
        AudioManager.pauseGameMusic();
        userLeaving =true;
    }
    
    public static void leaving(){
        if(GameStateManager.currentGameState.equals(GameState.RUN)
                || GameStateManager.currentGameState.equals(GameState.RESUMEPLAY)
                || GameStateManager.currentGameState.equals(GameState.TUTORIAL)
                || GameStateManager.currentGameState.equals(GameState.RESUMETUTORIAL))
            PlayState.main.gui.pause("false");
        else if(GameStateManager.getCurrentState().equals(GameState.INTRO)
                || GameStateManager.getCurrentState().equals(GameState.TUTORIALINTRO))
            GameStateManager.setState(GameState.HOME);
        
    }

  
    
   
    

    @Override
    public void finishAffinity() {
        super.finishAffinity(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    

    @Override
    protected void onResume() {
        super.onResume(); 
        AudioManager.resumeGameMusic();
        if(activePurchaseItem != null)
            if(!activePurchaseItem.equals(PurchaseItems.NOADS))
                if(!mHelper.isASyncInProgress())
                    consumeItem();
    }
    
    public void requestInterstitial(){
        requestNewInterstitial();
    }

    public void requestNewInterstitial() {
          AdRequest adRequest = new AdRequest.Builder().build();
          mInterstitialAd.loadAd(adRequest);
    }

    public void showInterstitial() {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                doShowInterstitial();
            }
        });
    }
 
    
    public void doShowInterstitial(){
       if(mInterstitialAd.isLoaded())
       mInterstitialAd.show();  
    }
    
    @Override
    public Storage loadSavedSettings() {
        return savedSettings;
    }
  
    @Override 
    public void goToWebsite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=DarkFig")); 
        startActivity(intent);
    }
            

   
 
    public static class JmeFragment extends AndroidHarnessFragment {
        public JmeFragment() {
            // Set main project class (fully qualified path)
            appClass = "mygame.Main";
 
            // Set the desired EGL configuration
            eglBitsPerPixel = 24;
            eglAlphaBits = 0;
            eglDepthBits = 16;
            eglSamples = 0;
            eglStencilBits = 0;
 
            // Set the maximum framerate
            // (default = -1 for unlimited)
            frameRate = -1;
 
            // Set the maximum resolution dimension
            // (the smaller side, height or width, is set automatically
            // to maintain the original device screen aspect ratio)
            // (default = -1 to match device screen resolution)
            maxResolutionDimension = -1;
 
            // Set input configuration settings
            joystickEventsEnabled = false;
            keyEventsEnabled = true;
            mouseEventsEnabled = true;
 
            // Set application exit settings
            finishOnAppStop = true;
            handleExitHook = true;
            exitDialogTitle = "Do you want to exit?";
            exitDialogMessage = "Use your home key to bring this app into the background or exit to terminate it.";
 
            // Set splash screen resource id, if used
            // (default = 0, no splash screen)
            // For example, if the image file name is "splash"...
            //     splashPicID = R.drawable.splash;
            splashPicID = 0;
            splashPicID = R.drawable.darkfiglogo;
        }

        @Override
        public void onTouch(String name, TouchEvent evt, float tpf) {
            //super.onTouch(name, evt, tpf); //To change body of generated methods, choose Tools | Templates.
            
        if (name.equals("TouchEscape")) {
            switch (evt.getType()) {
                case KEY_UP:
                   MainActivity.leaving();
                    break;
                default:
                    break;
            }
        }
    }
  
        
    }
}
