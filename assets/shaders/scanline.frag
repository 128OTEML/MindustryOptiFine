varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_lineHeight;
uniform float u_opacity;
uniform vec2 u_resolution;

void main(){
    vec4 color = texture2D(u_texture, v_texCoord);
    
    float scanline = mod(v_texCoord.y * u_resolution.y, u_lineHeight);
    scanline = step(u_lineHeight * 0.5, scanline);
    
    color.rgb *= mix(1.0 - u_opacity, 1.0, scanline);
    
    gl_FragColor = color;
}