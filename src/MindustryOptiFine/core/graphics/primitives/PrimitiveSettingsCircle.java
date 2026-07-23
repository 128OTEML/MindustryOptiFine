package MindustryOptiFine.core.graphics.primitives;

import arc.graphics.Color;
import MindustryOptiFine.core.graphics.shaders.ManagedShader;

public class PrimitiveSettingsCircle implements IPrimitiveSettings {
    public final PrimitiveSettings.VertexWidthFunction radiusFunction;
    public final PrimitiveSettings.VertexColorFunction colorFunction;
    public final boolean pixelate;
    public final ManagedShader shader;
    public final Integer projectionAreaWidth;
    public final Integer projectionAreaHeight;
    public final boolean useUnscaledMatrix;

    public PrimitiveSettingsCircle(PrimitiveSettings.VertexWidthFunction radiusFunction, PrimitiveSettings.VertexColorFunction colorFunction) {
        this(radiusFunction, colorFunction, false);
    }

    public PrimitiveSettingsCircle(PrimitiveSettings.VertexWidthFunction radiusFunction, PrimitiveSettings.VertexColorFunction colorFunction, boolean pixelate) {
        this(radiusFunction, colorFunction, pixelate, null);
    }

    public PrimitiveSettingsCircle(PrimitiveSettings.VertexWidthFunction radiusFunction, PrimitiveSettings.VertexColorFunction colorFunction, boolean pixelate, ManagedShader shader) {
        this(radiusFunction, colorFunction, pixelate, shader, null, null);
    }

    public PrimitiveSettingsCircle(PrimitiveSettings.VertexWidthFunction radiusFunction, PrimitiveSettings.VertexColorFunction colorFunction, boolean pixelate, ManagedShader shader, Integer projectionAreaWidth, Integer projectionAreaHeight) {
        this(radiusFunction, colorFunction, pixelate, shader, projectionAreaWidth, projectionAreaHeight, false);
    }

    public PrimitiveSettingsCircle(PrimitiveSettings.VertexWidthFunction radiusFunction, PrimitiveSettings.VertexColorFunction colorFunction, boolean pixelate, ManagedShader shader, Integer projectionAreaWidth, Integer projectionAreaHeight, boolean useUnscaledMatrix) {
        this.radiusFunction = radiusFunction;
        this.colorFunction = colorFunction;
        this.pixelate = pixelate;
        this.shader = shader;
        this.projectionAreaWidth = projectionAreaWidth;
        this.projectionAreaHeight = projectionAreaHeight;
        this.useUnscaledMatrix = useUnscaledMatrix;
    }

    @Override
    public boolean getPixelate() {
        return pixelate;
    }

    @Override
    public ManagedShader getShader() {
        return shader;
    }

    @Override
    public Integer getProjectionAreaWidth() {
        return projectionAreaWidth;
    }

    @Override
    public Integer getProjectionAreaHeight() {
        return projectionAreaHeight;
    }

    @Override
    public boolean getUseUnscaledMatrix() {
        return useUnscaledMatrix;
    }
}