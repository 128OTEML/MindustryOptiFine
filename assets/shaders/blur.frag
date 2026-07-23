varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform vec2 u_blurDirection;

void main() {
    vec2 texelSize = 1.0 / vec2(textureSize(u_texture, 0));
    vec4 color = vec4(0.0);
    
    float blurAmount = length(u_blurDirection);
    
    for (float i = -3.0; i <= 3.0; i++) {
        float weight = (4.0 - abs(i)) / 4.0;
        color += texture2D(u_texture, v_texCoord + u_blurDirection * i * texelSize) * weight;
    }
    
    color /= 16.0;
    
    gl_FragColor = color;
}