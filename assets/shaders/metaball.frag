varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_threshold;
uniform vec2 u_resolution;

void main(){
    vec4 metaballColor = texture2D(u_texture, v_texCoord);
    
    float value = length(metaballColor.rgb);
    float alpha = smoothstep(u_threshold, 0.8, value);
    
    gl_FragColor = vec4(metaballColor.rgb, alpha);
}