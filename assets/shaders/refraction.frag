uniform sampler2D u_texture;
uniform float u_refractionStrength;
uniform float u_distortion;
uniform float u_time;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec2 uv = v_texCoords;
    
    // 扭曲效果
    float distortionX = sin(uv.y * 20.0 + u_time) * u_distortion * 0.02;
    float distortionY = cos(uv.x * 20.0 + u_time * 0.8) * u_distortion * 0.02;
    uv += vec2(distortionX, distortionY);
    
    vec4 color = texture2D(u_texture, uv);
    
    // 水下色调
    vec4 underwater = vec4(0.1, 0.3, 0.5, 0.3);
    vec4 result = mix(color, underwater, u_refractionStrength * 0.3);
    gl_FragColor = result;
}