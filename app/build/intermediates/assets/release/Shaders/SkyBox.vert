uniform mat4 g_ViewMatrix;
uniform mat4 g_ProjectionMatrix;
uniform mat4 g_WorldMatrix;

attribute vec3 inPosition;
attribute vec3 inNormal;
uniform vec3 m_NormalScale;
varying vec3 direction;

void main(){
 vec4 pos = vec4(inPosition,0.0);
 pos = g_ViewMatrix * pos;
 
 pos.w = 1.0;
 gl_Position = g_ProjectionMatrix * pos;
 
 vec4 normal = vec4(inNormal * m_NormalScale, 0.0);
 direction = (g_WorldMatrix * normal).xyz;

 //gl_Position = g_ProjectionMatrix * g_ViewMatrix *  vec4(inPosition,1.0);
 //direction = inPosition;
}