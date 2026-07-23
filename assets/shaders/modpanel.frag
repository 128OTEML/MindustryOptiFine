varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_hoverIntensity;
uniform float u_speed;
uniform vec2 u_source;

float rand(vec2 uv){
    float x = dot(uv, vec2(4371.321, -9137.327));
    return 2.0 * fract(sin(x) * 17381.94472) - 1.0;
}

float noise(vec2 uv){
    vec2 id = floor(uv);
    vec2 f = fract(uv);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(rand(id + vec2(0.0, 0.0)), rand(id + vec2(1.0, 0.0)), u.x),
               mix(rand(id + vec2(0.0, 1.0)), rand(id + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 uv){
    float f = 0.0;
    float gat = 0.0;
    for(float octave = 0.0; octave < 5.0; octave++){
        float la = pow(2.0, octave);
        float ga = pow(0.5, octave + 1.0);
        f += ga * noise(la * uv);
        gat += ga;
    }
    return f / gat;
}

vec3 hash33(vec3 p3){
    vec3 uv = fract(p3 * vec3(0.1031, 0.11369, 0.13787));
    uv += dot(uv, uv.yxz + 19.19);
    return -1.0 + 2.0 * fract(vec3((uv.x + uv.y) * uv.z, (uv.x + uv.z) * uv.y, (uv.y + uv.z) * uv.x));
}

float worley(vec3 uv, float scale){
    vec3 id = floor(uv);
    vec3 fd = fract(uv);
    float minimalDist = 1.0;
    for(float x = -2.0; x <= 2.0; x++){
        for(float y = -2.0; y <= 2.0; y++){
            for(float z = -2.0; z <= 2.0; z++){
                vec3 coord = vec3(x, y, z);
                vec3 rId = hash33(mod(abs(id + coord), scale)) * 0.33;
                vec3 r = coord + rId - fd;
                float d = dot(r, r);
                if(d < minimalDist){
                    minimalDist = d;
                }
            }
        }
    }
    return minimalDist;
}

float fbm3(vec3 uv, float scale){
    float G = exp(-0.3);
    float amp = 1.0;
    float freq = 1.0;
    float n = 0.0;
    for(int i = 0; i < 5; i++){
        n += worley(uv * freq, scale * freq) * amp;
        freq *= 2.0;
        amp *= G;
    }
    return n * n;
}

float fbm_warped(vec2 uv){
    float h = fbm(0.09 * u_time * u_speed + uv + fbm(0.065 * u_time * u_speed + 2.0 * uv - 5.0 * fbm(4.0 * uv)));
    return fbm3(vec3(h, h, h), 1.0);
}

float border(vec2 uv, float epsilon){
    float f = fbm_warped(uv);
    float left = fbm_warped(uv - vec2(0.0, epsilon));
    float up = fbm_warped(uv - vec2(epsilon, 0.0));
    float right = fbm_warped(uv + vec2(0.0, epsilon));
    float down = fbm_warped(uv + vec2(epsilon, 0.0));
    return clamp(abs(4.0 * f - left - down - up - right), 0.0, 1.0);
}

float mySmoothstep(float edge0, float edge1, float x){
    x = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return x * x * (3.0 - 2.0 * x);
}

void main(){
    vec2 uv = v_texCoord;
    float f = fbm3(vec3(uv, 2.0), 2.0);
    float a2 = smoothstep(-0.5, 0.5, f);
    float a1 = smoothstep(-1.0, 1.0, fbm(uv));
    
    vec3 pink = vec3(254.0 / 255.0, 18.0 / 255.0, 97.0 / 255.0);
    vec3 yellow = vec3(253.0 / 255.0, 248.0 / 255.0, 27.0 / 255.0);
    vec3 orange = vec3(255.0 / 255.0, 122.0 / 255.0, 2.0 / 255.0);
    
    vec3 finalCol = mix(mix(pink, yellow, a1), orange, a2);
    vec3 outline = vec3(177.0 / 255.0, 100.0 / 255.0, 100.0 / 255.0) * border(uv, 0.00025);
    finalCol += outline;
    
    finalCol = mix(outline, finalCol, mySmoothstep(0.0, 1.0, u_hoverIntensity));
    
    gl_FragColor = vec4(finalCol, 1.0) * v_color;
}