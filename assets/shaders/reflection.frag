uniform sampler2D u_texture;
uniform float u_reflectionStrength;
uniform float u_time;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);
    // 简单的反射效果 - 稍微模糊和翻转
    vec2 reflectedCoords = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
    vec4 reflectedColor = texture2D(u_texture, reflectedCoords);
    
    // 混合
    vec4 result = mix(color, reflectedColor, u_reflectionStrength * 0.5);
    gl_FragColor = result;
}