attribute vec2 a_position;
attribute vec4 a_color;
attribute vec3 a_texCoord;

uniform mat4 uWorldViewProjection;

varying vec4 v_color;
varying vec2 v_texCoord;

void main() {
    gl_Position = uWorldViewProjection * vec4(a_position, 0.0, 1.0);
    gl_Position.z = 0.0;
    v_color = a_color;
    v_texCoord = a_texCoord.xy;
    v_texCoord.y = (v_texCoord.y - 0.5) / a_texCoord.z + 0.5;
}