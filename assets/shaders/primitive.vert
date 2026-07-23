attribute vec2 a_position;
attribute vec2 a_texCoord;
attribute vec4 a_color;

varying vec2 v_texCoord;
varying vec4 v_color;

void main(){
    v_texCoord = a_texCoord;
    v_color = a_color;
    gl_Position = vec4(a_position, 0.0, 1.0);
}