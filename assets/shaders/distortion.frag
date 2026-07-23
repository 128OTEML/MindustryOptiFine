varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_strength;
uniform float u_speed;
uniform float u_time;
uniform vec2 u_resolution;

void main(){
    vec2 uv = v_texCoord;
    float offset = sin(uv.y * 10.0 + u_time * u_speed) * u_strength * 0.1;
    float offset2 = cos(uv.x * 10.0 + u_time * u_speed * 0.7) * u_strength * 0.1;
    
    uv.x += offset;
    uv.y += offset2;
    
    gl_FragColor = texture2D(u_texture, uv);
}