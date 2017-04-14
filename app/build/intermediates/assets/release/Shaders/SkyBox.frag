uniform samplerCube m_Texture;
uniform vec4 m_Color;

varying vec3 direction;


const float lowerLimit = 0.0;
const float upperLimit = 0.3;

void main(){
    vec3 dir = normalize(direction);
    vec4 finalColor = textureCube(m_Texture, direction);
    float factor = ((dir.y * -1.0) / (upperLimit - lowerLimit));
    factor = clamp(factor, 0.0, 1.0);
    gl_FragColor = mix(m_Color,finalColor,factor);
      //gl_FragColor = m_Color;
}