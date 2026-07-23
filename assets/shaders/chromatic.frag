varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_intensity;
uniform float u_angle;
uniform vec2 u_resolution;

void main(){
    vec2 center = vec2(0.5);
    vec2 dir = v_texCoord - center;
    float dist = length(dir);
    
    vec2 offset = vec2(cos(u_angle), sin(u_angle)) * dist * u_intensity;
    
    float r = texture2D(u_texture, v_texCoord + offset).r;
    float g = texture2D(u_texture, v_texCoord).g;
    float b = texture2D(u_texture, v_texCoord - offset).b;
    
    gl_FragColor = vec4(r, g, b, 1.0);
}