uniform vec4 m_Color;
uniform sampler2D m_ColorMap;
uniform float g_Tpf;
uniform float m_ScrollSpeed;
uniform float m_Alpha;
uniform highp float g_Time;
varying vec2 texCoord1;

void main(){
vec4 color = vec4(1.0);

vec2 newCoords = texCoord1;


newCoords.x = newCoords.x + fract(g_Time * m_ScrollSpeed);
newCoords.x = fract(newCoords.x);

color *= texture2D(m_ColorMap, newCoords );
color.a = m_Alpha;
gl_FragColor = color;
}