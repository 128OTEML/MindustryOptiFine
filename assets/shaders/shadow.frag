#define HIGHP
#define LIGHTH 1.0
#define MAX_LIGHTS 400
uniform float u_EDGE_PRECISION;
uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform vec2 u_offset;
uniform float u_ambientLight;
uniform vec3 u_ambientColor;
uniform int u_lightcount;
uniform vec2 u_lights[MAX_LIGHTS];

varying vec2 v_texCoords;
vec4 unpack(vec2 value){
    vec4 light;
    if(value.x < 0.0){
        light.x = -100000.0;
        return light;
    }
    light.x = mod(value.x, 50000.0) / 5.0 - 100.0;
    light.y = mod(value.y, 50000.0) / 5.0 - 100.0;
    light.z = floor(value.x / 50000.0);     //source size
    light.w = floor(value.y / 50000.0);     //radius
    return light;
}

void main(){
    vec2 T = v_texCoords.xy;
    vec2 worldxy = (T * u_texsize) + u_offset;
    vec4 shadow0 = texture2D(u_texture, T);

    float lightness = 0.0;
    float shadowness = 0.0;

    //source light
    for(int i = -1; i < u_lightcount; i++){
        vec4 light;
        if(i == -1){
            //ambientLight
            light.xy = worldxy + 24.0;
            light.w = 48.0;
        }else{
            light = unpack(u_lights[i]);
        }


        if(light.x < -10000.0) continue;
        float radius = light.w * 0.8 + 20.0;

        float dst = distance(worldxy, light.xy);
        if(dst < radius){
            bool isShadow = false;
            float shadowLen = dst / LIGHTH;
            vec2 normali = normalize(light.xy - worldxy) * u_invsize;
            for(float j = 0.0; j < shadowLen; j += u_EDGE_PRECISION){
                vec4 shadow = texture2D(u_texture, T + normali * j);//blockscreenxy
                if((shadow.b - shadow0.b) > (j / shadowLen)){
                    if(dst - j < light.z) break;
                    shadowness = max(shadowness, 1.0 - dst / radius);
                    isShadow = true;
                    break;
                }
            }

            if(!isShadow){
                lightness = max(lightness, 1.0 - dst / radius);
            }
        }
    }

    float shadowAlpha = clamp(shadowness * (1.0 - lightness) * sqrt(u_ambientLight * 0.75 + 0.25), 0.0, 0.9);
    // 使用环境光颜色影响阴影颜色，创造更真实的视觉效果
    vec3 shadowColor = mix(vec3(0.0, 0.0, 0.0), u_ambientColor * 0.5, u_ambientLight);
    vec4 result = vec4(shadowColor, shadowAlpha);
    gl_FragColor = result;
}