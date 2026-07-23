varying vec4 v_color;
varying vec2 v_texCoord;

uniform float globalTime;
uniform sampler2D overlayTexture;

void main() {
    vec4 color = v_color;
    vec2 uv = v_texCoord;
    
    gl_FragColor = mix(vec4(1.0, 0.0, 0.0, 1.0), vec4(0.0, 0.0, 1.0, 1.0), uv.x);
}