/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.darkfig.chickenfunrun;

import android.content.Context;
import guisystem.StatsManager;
import guisystem.Storage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel Martrinez
 */
public class SavedSettings extends Storage{
  public static MainActivity activity;
  

    public SavedSettings(MainActivity activity) {
        SavedSettings.activity = activity;
        initMap();
    }
  
  
  @Override
  public  void save() {
    saveMissions();
    savePoints(SCORE);
    savePoints(CHICKENCOUNT);
    savePoints(SINGLERUNHIGH);
    saveMission(MISSIONSCOMPLETED);
    saveMission(MISSIONSCREEN);
    saveMission(FIRSTMISSION);
    saveMission(SECONDMISSION);
    saveMission(THIRDMISSION);
  }
  
  @Override
  public void load(){
    loadPoints(SCORE);
    loadPoints(CHICKENCOUNT);
    loadPoints(SINGLERUNHIGH);
    loadMission(MISSIONSCOMPLETED);
    loadMission(MISSIONSCREEN);
    loadMission(FIRSTMISSION);
    loadMission(SECONDMISSION);
    loadMission(THIRDMISSION);
    loadMissions();
  }
  
  public void loadPoints(String type){
      
      try {
          FileInputStream fin = activity.openFileInput(type);
          try {
              int c;
              String string = ""; 
              while((c=fin.read()) != -1){
              string += Character.toString((char)c);
              
              }
              if(!string.isEmpty())
                    loadPoints(type, Integer.parseInt(string));
          } catch (IOException ex) {
              Logger.getLogger(SavedSettings.class.getName()).log(Level.SEVERE, null, ex);
              
          }
      } catch (FileNotFoundException ex) {
          Logger.getLogger(SavedSettings.class.getName()).log(Level.SEVERE, null, ex);
          
      }
  }
  
  public void loadMission(String file){
      try {
          FileInputStream fin = activity.openFileInput(file);
          try {
              mapValue(file, fin.read());
          } catch (IOException ex) {
              Logger.getLogger(SavedSettings.class.getName()).log(Level.SEVERE, null, ex);
          }
      } catch (FileNotFoundException ex) {
          Logger.getLogger(SavedSettings.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
  
  public void savePoints(String type){
      
      try {
          FileOutputStream out =activity.openFileOutput(type, Context.MODE_PRIVATE);
          try {
              out.write(String.valueOf(StatsManager.getPoints(type)).getBytes());
              out.close();
          } catch (IOException ex) {
              Logger.getLogger(SavedSettings.class.getName()).log(Level.SEVERE, null, ex);
          }
      } catch (FileNotFoundException ex) {
          Logger.getLogger(SavedSettings.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
  
  public void saveMission(String file){
      try {
          FileOutputStream out =activity.openFileOutput(file, Context.MODE_PRIVATE);
          try {
              out.write(getMissionValue(file)); 
              out.close();
          } catch (IOException ex) {
              Logger.getLogger(SavedSettings.class.getName()).log(Level.SEVERE, null, ex);
          }
         
      } catch (FileNotFoundException ex) {
          Logger.getLogger(SavedSettings.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
  
}
