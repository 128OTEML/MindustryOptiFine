uniform sampler2D u_texture;
uniform sampler2D u_reflection;
uniform sampler2D u_refraction;
uniform float u_waveSpeed;
uniform float u_waveAmplitude;
uniform float u_time;
uniform float u_reflectionStrength;
uniform float u_refractionStrength;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec2 uv = v_texCoords;
    
    // 波浪效果
    float wave = sin(uv.x * 15.0 + uv.y * 10.0 + u_time * u_waveSpeed) * u_waveAmplitude;
    uv.y += wave * 0.5;
    
    // 从纹理采样
    vec4 baseColor = texture2D(u_texture, uv);
    vec4 reflectionColor = texture2D(u_reflection, uv + vec2(wave * 0.3, 0.0));
    vec4 refractionColor = texture2D(u_refraction, uv + vec2(0.0, wave * 0.3));
    
    // 混合反射和折射
    vec4 mixed = mix(refractionColor, reflectionColor, u_reflectionStrength);
    mixed = mix(baseColor, mixed, 0.6);
    
    // 增加光泽
    float specular = pow(max(0.0, sin(uv.x * 30.0 + u_time) * 0.5 + 0.5), 4.0);
    mixed.rgb += vec3(0.2, 0.3, 0.4) * specular * 0.3;
    
    gl_FragColor = vec4(mixed.rgb, 0.9);
}