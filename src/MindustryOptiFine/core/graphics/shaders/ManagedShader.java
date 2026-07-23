package MindustryOptiFine.core.graphics.shaders;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

public class ManagedShader implements Disposable {
    public final String name;

    public final Shader shader;

    public boolean disposed = false;

    /** Cached parameter values, applied when the batch calls shader.apply() */
    private final ObjectMap<String, Object> parameterCache = new ObjectMap<>();

    public ManagedShader(String name, Shader shader) {
        this.shader = shader;
        this.name = name;
    }

    /** Check if a cached parameter matches the given value */
    public boolean parameterIsCachedAsValue(String parameterName, Object value) {
        if (!parameterCache.containsKey(parameterName)) {
            return false;
        }
        Object parameter = parameterCache.get(parameterName);
        return parameter != null && parameter.equals(value);
    }

    public void resetCache() {
        parameterCache.clear();
    }

    /** Cache a uniform value. Will be applied by the batch when it calls shader.apply(). */
    public boolean trySetParameter(String parameterName, Object value) {
        if (shader == null) return false;

        if (parameterIsCachedAsValue(parameterName, value)) {
            return false;
        }

        parameterCache.put(parameterName, value);
        return true;
    }

    /** Called by the inner Shader's apply() — applies all cached uniforms */
    public void applyCachedUniforms() {
        if (disposed || shader == null) return;

        for (ObjectMap.Entry<String, Object> entry : parameterCache) {
            String key = entry.key;
            Object value = entry.value;

            if (value instanceof Boolean b) {
                shader.setUniformi(key, b ? 1 : 0);
            } else if (value instanceof Integer i) {
                shader.setUniformi(key, i);
            } else if (value instanceof Float f) {
                shader.setUniformf(key, f);
            } else if (value instanceof Vec2 v2) {
                shader.setUniformf(key, v2.x, v2.y);
            } else if (value instanceof Vec3 v3) {
                shader.setUniformf(key, v3.x, v3.y, v3.z);
            } else if (value instanceof Color c) {
                shader.setUniformf(key, c.r, c.g, c.b);
            }
        }
    }

    public void setTexture(Texture texture, int textureIndex) {
        setTexture(texture, textureIndex, null);
    }

    public void setTexture(Texture texture, int textureIndex, Texture.TextureWrap samplerStateOverride) {
        trySetParameter("textureSize" + textureIndex, new Vec2(texture.width, texture.height));

        if (textureIndex >= 0 && textureIndex < 16) {
            Core.gl.glActiveTexture(Core.gl.GL_TEXTURE0 + textureIndex);
            texture.bind();
        }
    }

    /** Apply cached parameters. Does NOT bind the shader — use Draw.shader() instead. */
    public void apply() {
        apply(null);
    }

    public void apply(String passName) {
        if (disposed || shader == null) return;

        trySetParameter("globalTime", Time.time);

        Log.info("ManagedShader: applying shader '" + name + "' parameters"
            + (passName != null ? " pass: " + passName : ""));
    }

    public void dispose() {
        if (disposed) return;

        disposed = true;
        if (shader != null) {
            shader.dispose();
        }
        parameterCache.clear();
    }
}
