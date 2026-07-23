varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_threshold;
uniform float u_intensity;

void main(){
    vec4 color = texture2D(u_texture, v_texCoord);
    float brightness = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    
    if(brightness > u_threshold){
        color.rgb *= u_intensity;
    }
    
    gl_FragColor = color;
}