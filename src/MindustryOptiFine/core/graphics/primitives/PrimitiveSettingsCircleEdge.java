package MindustryOptiFine.core.graphics.primitives;

import arc.graphics.Color;
import MindustryOptiFine.core.graphics.shaders.ManagedShader;

public class PrimitiveSettingsCircleEdge implements IPrimitiveSettings {
    public final PrimitiveSettings.VertexWidthFunction edgeWidthFunction;
    public final PrimitiveSettings.VertexColorFunction colorFunction;
    public final PrimitiveSettings.VertexWidthFunction radiusFunction;
    public final boolean pixelate;
    public final ManagedShader shader;
    public final Integer projectionAreaWidth;
    public final Integer projectionAreaHeight;
    public final boolean useUnscaledMatrix;

    public PrimitiveSettingsCircleEdge(PrimitiveSettings.VertexWidthFunction edgeWidthFunction, PrimitiveSettings.VertexColorFunction colorFunction, PrimitiveSettings.VertexWidthFunction radiusFunction) {
        this(edgeWidthFunction, colorFunction, radiusFunction, false);
    }

    public PrimitiveSettingsCircleEdge(PrimitiveSettings.VertexWidthFunction edgeWidthFunction, PrimitiveSettings.VertexColorFunction colorFunction, PrimitiveSettings.VertexWidthFunction radiusFunction, boolean pixelate) {
        this(edgeWidthFunction, colorFunction, radiusFunction, pixelate, null);
    }

    public PrimitiveSettingsCircleEdge(PrimitiveSettings.VertexWidthFunction edgeWidthFunction, PrimitiveSettings.VertexColorFunction colorFunction, PrimitiveSettings.VertexWidthFunction radiusFunction, boolean pixelate, ManagedShader shader) {
        this(edgeWidthFunction, colorFunction, radiusFunction, pixelate, shader, null, null);
    }

    public PrimitiveSettingsCircleEdge(PrimitiveSettings.VertexWidthFunction edgeWidthFunction, PrimitiveSettings.VertexColorFunction colorFunction, PrimitiveSettings.VertexWidthFunction radiusFunction, boolean pixelate, ManagedShader shader, Integer projectionAreaWidth, Integer projectionAreaHeight) {
        this(edgeWidthFunction, colorFunction, radiusFunction, pixelate, shader, projectionAreaWidth, projectionAreaHeight, false);
    }

    public PrimitiveSettingsCircleEdge(PrimitiveSettings.VertexWidthFunction edgeWidthFunction, PrimitiveSettings.VertexColorFunction colorFunction, PrimitiveSettings.VertexWidthFunction radiusFunction, boolean pixelate, ManagedShader shader, Integer projectionAreaWidth, Integer projectionAreaHeight, boolean useUnscaledMatrix) {
        this.edgeWidthFunction = edgeWidthFunction;
        this.colorFunction = colorFunction;
        this.radiusFunction = radiusFunction;
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