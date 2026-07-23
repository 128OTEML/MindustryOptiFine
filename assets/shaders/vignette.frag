varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_intensity;
uniform float u_smoothness;
uniform vec4 u_color;
uniform vec2 u_resolution;

void main(){
    vec2 center = vec2(0.5);
    vec2 dist = v_texCoord - center;
    float len = length(dist * u_resolution / max(u_resolution.x, u_resolution.y));
    
    float vignette = smoothstep(u_smoothness, 0.0, len);
    vignette = mix(1.0, vignette, u_intensity);
    
    vec4 color = texture2D(u_texture, v_texCoord);
    color.rgb = mix(color.rgb * u_color.rgb, color.rgb, vignette);
    
    gl_FragColor = color;
}