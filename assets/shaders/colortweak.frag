varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_brightness;
uniform float u_contrast;
uniform float u_saturation;
uniform float u_gamma;

void main(){
    vec4 color = texture2D(u_texture, v_texCoord);
    
    color.rgb *= u_brightness;
    
    color.rgb = ((color.rgb - 0.5) * u_contrast) + 0.5;
    
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(vec3(luminance), color.rgb, u_saturation);
    
    color.rgb = pow(color.rgb, vec3(u_gamma));
    
    gl_FragColor = color;
}