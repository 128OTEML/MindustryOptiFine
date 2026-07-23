varying vec2 v_texCoord;

uniform sampler2D u_metaballContents;
uniform sampler2D u_overlayTexture;

uniform vec2 u_screenSize;
uniform vec2 u_layerSize;
uniform vec2 u_layerOffset;
uniform vec4 u_edgeColor;
uniform vec2 u_singleFrameScreenOffset;

vec2 convertToScreenCoords(vec2 coords) {
    return coords * u_screenSize;
}

vec2 convertFromScreenCoords(vec2 coords) {
    return coords / u_screenSize;
}

void main() {
    vec4 baseColor = texture2D(u_metaballContents, v_texCoord);
    
    float alphaOffset = (1.0 - (baseColor.a > 0.0 ? 1.0 : 0.0));
    
    float left = texture2D(u_metaballContents, convertFromScreenCoords(convertToScreenCoords(v_texCoord) + vec2(-2.0, 0.0))).a + alphaOffset;
    float right = texture2D(u_metaballContents, convertFromScreenCoords(convertToScreenCoords(v_texCoord) + vec2(2.0, 0.0))).a + alphaOffset;
    float top = texture2D(u_metaballContents, convertFromScreenCoords(convertToScreenCoords(v_texCoord) + vec2(0.0, -2.0))).a + alphaOffset;
    float bottom = texture2D(u_metaballContents, convertFromScreenCoords(convertToScreenCoords(v_texCoord) + vec2(0.0, 2.0))).a + alphaOffset;
    
    float leftHasNoAlpha = step(left, 0.0);
    float rightHasNoAlpha = step(right, 0.0);
    float topHasNoAlpha = step(top, 0.0);
    float bottomHasNoAlpha = step(bottom, 0.0);
    
    float conditionOpacityFactor = 1.0 - clamp(leftHasNoAlpha + rightHasNoAlpha + topHasNoAlpha + bottomHasNoAlpha, 0.0, 1.0);
    
    vec4 layerColor = texture2D(u_overlayTexture, (v_texCoord + u_layerOffset + u_singleFrameScreenOffset) * u_screenSize / u_layerSize);
    vec4 defaultColor = layerColor * texture2D(u_metaballContents, v_texCoord);
    
    gl_FragColor = (defaultColor * conditionOpacityFactor) + (u_edgeColor * (1.0 - conditionOpacityFactor));
}