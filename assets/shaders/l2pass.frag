varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_seed;
uniform vec2 u_resolution;
uniform int u_k;

void main(){
    vec2 uv = v_texCoord;
    vec4 col;

    if(u_k == 0){
        //procedural seed pass: no input texture, pure math
        vec2 p = uv * 2.0 - 1.0;
        float d = length(p);
        float pat = 0.5 + 0.5 * sin(uv.x * 40.0 * u_seed + u_time) * cos(uv.y * 40.0 * u_seed - u_time);
        float rings = 0.5 + 0.5 * sin(d * 18.0 - u_time * 3.0);
        col = vec4(pat * rings, 0.25, 1.0 - pat * rings, 1.0);
    }else{
        //feedback pass: sample the previous render target with a slight distortion
        vec2 texel = 1.0 / u_resolution;
        vec2 off = vec2(sin(uv.y * 30.0 + u_time * 2.0) * 0.008, cos(uv.x * 30.0 - u_time * 2.0) * 0.008);
        col = texture2D(u_texture, uv + off);
        col.rgb = clamp(col.rgb * 1.05, 0.0, 1.0);
    }

    gl_FragColor = col;
}