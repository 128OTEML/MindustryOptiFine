varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform vec2 pixelationFactor;

void main() {
    vec2 coords = v_texCoord;
    coords = floor(coords / pixelationFactor) * pixelationFactor;
    
    vec4 color = texture2D(u_texture, coords);
    gl_FragColor = color;
}