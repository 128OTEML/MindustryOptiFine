varying vec4 v_color;
varying vec3 v_texCoord;

uniform sampler2D image;

void main() {
    gl_FragColor = v_color * texture2D(image, v_texCoord.xy);
}