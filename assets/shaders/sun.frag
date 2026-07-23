varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_hoverIntensity;
uniform float u_speed;
uniform float u_pixel;
uniform vec3 u_inColor;

#define PI 3.14159265359

float hash11(float p){
    p = fract(p * 0.1031);
    p *= p + 34.32;
    p *= p + p;
    return fract(p);
}

vec3 rayGun(vec2 ray, vec2 uv, vec3 col, float delay, float size, float len){
    vec3 sunCol = vec3(1.0, 0.9, 0.8);
    float ang = dot(normalize(uv), normalize(ray));
    ang = 1.0 - ang;
    ang = ang / size;
    ang = clamp(ang, 0.0, 1.0);
    
    float v = smoothstep(0.0, 1.0, (1.0 - ang) * (1.0 - ang));
    v *= sin(u_time / 2.0 * u_speed + delay) / 2.0 + 0.5;
    
    float l = length(uv) * len;
    l = clamp(l, 0.0, 1.0);
    
    vec3 o = mix(col, mix(sunCol, col, l), smoothstep(1.0, 4.0, u_time * u_speed) * v);
    return o;
}

void main(){
    vec2 uv = v_texCoord;
    uv -= 0.5;
    
    float t = u_time / 15.0 * u_speed;
    float cosT = cos(t);
    float sinT = sin(t);
    
    vec3 col = vec3(0.0);
    
    for(float x = 0.0; x < 2.0 * PI; x += PI / 8.0){
        float c = cos(x);
        float s = sin(x);
        float rCos = cosT * c - sinT * s;
        float rSin = sinT * c + cosT * s;
        vec2 ray = vec2(rCos, rSin);
        col = rayGun(ray, uv, col, hash11(x * 15.0) * 7003.0, hash11(x * 234.0) * 0.2, 2.0);
    }
    
    vec4 texColor = texture2D(u_texture, v_texCoord);
    vec3 baseColor = texColor.rgb;
    float alpha = texColor.a;
    
    vec3 colScaled = col * 1.5;
    vec3 colPow3 = colScaled * colScaled * colScaled;
    vec3 colPow2 = colScaled * colScaled;
    
    vec3 colorLerp = mix(u_inColor, baseColor, length(colPow3));
    
    float intensity = smoothstep(0.5, 1.0, length(col * 3.0)) * length(colPow2);
    intensity = mix(1.0, intensity, u_hoverIntensity);
    
    gl_FragColor = vec4(colorLerp, alpha * intensity) * v_color;
}