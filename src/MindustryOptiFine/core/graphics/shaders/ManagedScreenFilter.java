package MindustryOptiFine.core.graphics.shaders;

import arc.*;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.util.Time;
import MindustryOptiFine.core.graphics.Disposable;

public class ManagedScreenFilter implements Disposable {
    private final ObjectMap<Integer, DeferredTexture> deferredTextures = new ObjectMap<>();

    private final ObjectMap<String, Object> parameterCache = new ObjectMap<>();

    public Shader shader;

    public boolean disposed = false;

    public boolean isActive = false;

    public float opacity = 0f;

    public Vec2 focusPosition = new Vec2();

    public static final String textureSizeParameterPrefix = "textureSize";

    public static class DeferredTexture {
        public final Texture texture;
        public final int index;
        public final Texture.TextureWrap samplerState;

        public DeferredTexture(Texture texture, int index, Texture.TextureWrap samplerState) {
            this.texture = texture;
            this.index = index;
            this.samplerState = samplerState;
        }
    }

    public ManagedScreenFilter(Shader shader) {
        this.shader = shader;
    }

    public ManagedScreenFilter setMainColor(Color color) {
        trySetParameter("mainColor", color);
        return this;
    }

    public ManagedScreenFilter setSecondaryColor(Color color) {
        trySetParameter("secondaryColor", color);
        return this;
    }

    public ManagedScreenFilter setFocusPosition(Vec2 worldPosition) {
        focusPosition.set(worldPosition);
        return this;
    }

    public boolean parameterIsCachedAsValue(String parameterName, Object value) {
        if (!parameterCache.containsKey(parameterName)) {
            return false;
        }
        Object parameter = parameterCache.get(parameterName);
        return parameter != null && parameter.equals(value);
    }

    public boolean trySetParameter(String parameterName, Object value) {
        if (shader == null) return false;

        if (parameterIsCachedAsValue(parameterName, value)) {
            return false;
        }

        parameterCache.put(parameterName, value);
        return true;
    }

    /** Apply cached uniforms. Must be called while the shader program is bound. */
    private void applyCachedUniforms() {
        if (shader == null) return;

        for (ObjectMap.Entry<String, Object> entry : parameterCache) {
            String key = entry.key;
            Object value = entry.value;

            if (value instanceof Boolean b) {
                shader.setUniformi(key, b ? 1 : 0);
            } else if (value instanceof Boolean[] b2) {
                for (int i = 0; i < b2.length; i++) {
                    shader.setUniformi(key + "_" + i, b2[i] ? 1 : 0);
                }
            } else if (value instanceof Integer i) {
                shader.setUniformi(key, i);
            } else if (value instanceof Integer[] i2) {
                for (int i = 0; i < i2.length; i++) {
                    shader.setUniformi(key + "_" + i, i2[i]);
                }
            } else if (value instanceof Float f) {
                shader.setUniformf(key, f);
            } else if (value instanceof Float[] f2) {
                for (int i = 0; i < f2.length; i++) {
                    shader.setUniformf(key + "_" + i, f2[i]);
                }
            } else if (value instanceof Vec2 v2) {
                shader.setUniformf(key, v2.x, v2.y);
            } else if (value instanceof Vec2[] v22) {
                for (int i = 0; i < v22.length; i++) {
                    shader.setUniformf(key + "_" + i, v22[i].x, v22[i].y);
                }
            } else if (value instanceof Color c) {
                shader.setUniformf(key, c.r, c.g, c.b);
            } else if (value instanceof Texture t) {
                shader.setUniformi(key, t.getTextureObjectHandle());
            }
        }
    }

    public void setTexture(Texture texture, int textureIndex) {
        setTexture(texture, textureIndex, null);
    }

    public void setTexture(Texture texture, int textureIndex, Texture.TextureWrap samplerStateOverride) {
        DeferredTexture deferredTexture = new DeferredTexture(texture, textureIndex, samplerStateOverride);
        deferredTextures.put(textureIndex, deferredTexture);
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    public void update() {
        if (isActive) {
            opacity = Mathf.clamp(opacity + 0.015f, 0f, 1f);
        } else {
            opacity = Mathf.clamp(opacity - 0.015f, 0f, 1f);
        }
    }

    public void apply() {
        apply(true, null);
    }

    public void apply(boolean setCommonParams, String pass) {
        if (shader == null) return;

        shader.bind();

        if (setCommonParams) {
            setCommonParameters();
        }

        applyCachedUniforms();

        supplyDeferredTextures();
    }

    private void supplyDeferredTextures() {
        for (DeferredTexture textureWrapper : deferredTextures.values()) {
            int textureIndex = textureWrapper.index;
            Texture texture = textureWrapper.texture;
            Texture.TextureWrap samplerStateOverride = textureWrapper.samplerState;

            if (shader != null) {
                shader.setUniformf(textureSizeParameterPrefix + textureIndex, texture.width, texture.height);
            }

            if (textureIndex >= 0 && textureIndex < 16) {
                Core.gl.glActiveTexture(Core.gl.GL_TEXTURE0 + textureIndex);
                texture.bind();
            }
        }
    }

    private void setCommonParameters() {
        if (shader == null) return;

        shader.setUniformf("globalTime", Time.time);
        shader.setUniformf("opacity", opacity);
        shader.setUniformf("focusPosition", focusPosition.x, focusPosition.y);
        shader.setUniformf("screenPosition", Core.camera.position.x, Core.camera.position.y);
        shader.setUniformf("screenSize", Core.graphics.getWidth(), Core.graphics.getHeight());
    }

    @Override
    public void dispose() {
        if (disposed) return;

        disposed = true;
        if (shader != null) {
            shader.dispose();
        }
        parameterCache.clear();
    }
}
