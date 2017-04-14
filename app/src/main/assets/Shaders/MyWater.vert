uniform mat4 g_ViewMatrix;
uniform mat4 g_ProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform mat4 g_WorldViewProjectionMatrix;
uniform float g_Time;

attribute vec3 inPosition;
attribute vec2 inTexCoord;

varying vec2 texCoord1;

//const float scrollSpeed = 0.2;

void main(){


texCoord1 = inTexCoord;

vec4 modelSpacePos = vec4(inPosition, 1.0);

#ifdef Animation_Type
      modelSpacePos.x += sin((g_Time + modelSpacePos.x))/10.0;
      modelSpacePos.y = sin(g_Time * modelSpacePos.z)/20.0;
      modelSpacePos.z += sin(g_Time + modelSpacePos.z)/20.0;
    //vec3 b = g_Time * .1 * inPosition;
    //modelSpacePos = vec4(b,1.0);
#endif
gl_Position = g_WorldViewProjectionMatrix * modelSpacePos;
}