package MindustryOptiFine.core.graphics.primitives;

import arc.graphics.Color;
import arc.math.geom.Vec2;
import MindustryOptiFine.core.graphics.shaders.ManagedShader;

public class PrimitiveSettings implements IPrimitiveSettings {
    public final VertexWidthFunction widthFunction;
    public final VertexColorFunction colorFunction;
    public final VertexOffsetFunction offsetFunction;
    public final boolean smoothen;
    public final boolean pixelate;
    public final ManagedShader shader;
    public final Integer projectionAreaWidth;
    public final Integer projectionAreaHeight;
    public final boolean useUnscaledMatrix;
    public final VertexPositionOverride initialVertexPositionsOverride;

    public PrimitiveSettings(VertexWidthFunction widthFunction, VertexColorFunction colorFunction) {
        this(widthFunction, colorFunction, null);
    }

    public PrimitiveSettings(VertexWidthFunction widthFunction, VertexColorFunction colorFunction, VertexOffsetFunction offsetFunction) {
        this(widthFunction, colorFunction, offsetFunction, true);
    }

    public PrimitiveSettings(VertexWidthFunction widthFunction, VertexColorFunction colorFunction, VertexOffsetFunction offsetFunction, boolean smoothen) {
        this(widthFunction, colorFunction, offsetFunction, smoothen, false);
    }

    public PrimitiveSettings(VertexWidthFunction widthFunction, VertexColorFunction colorFunction, VertexOffsetFunction offsetFunction, boolean smoothen, boolean pixelate) {
        this(widthFunction, colorFunction, offsetFunction, smoothen, pixelate, null);
    }

    public PrimitiveSettings(VertexWidthFunction widthFunction, VertexColorFunction colorFunction, VertexOffsetFunction offsetFunction, boolean smoothen, boolean pixelate, ManagedShader shader) {
        this(widthFunction, colorFunction, offsetFunction, smoothen, pixelate, shader, null, null);
    }

    public PrimitiveSettings(VertexWidthFunction widthFunction, VertexColorFunction colorFunction, VertexOffsetFunction offsetFunction, boolean smoothen, boolean pixelate, ManagedShader shader, Integer projectionAreaWidth, Integer projectionAreaHeight) {
        this(widthFunction, colorFunction, offsetFunction, smoothen, pixelate, shader, projectionAreaWidth, projectionAreaHeight, false);
    }

    public PrimitiveSettings(VertexWidthFunction widthFunction, VertexColorFunction colorFunction, VertexOffsetFunction offsetFunction, boolean smoothen, boolean pixelate, ManagedShader shader, Integer projectionAreaWidth, Integer projectionAreaHeight, boolean useUnscaledMatrix) {
        this(widthFunction, colorFunction, offsetFunction, smoothen, pixelate, shader, projectionAreaWidth, projectionAreaHeight, useUnscaledMatrix, null);
    }

    public PrimitiveSettings(VertexWidthFunction widthFunction, VertexColorFunction colorFunction, VertexOffsetFunction offsetFunction, boolean smoothen, boolean pixelate, ManagedShader shader, Integer projectionAreaWidth, Integer projectionAreaHeight, boolean useUnscaledMatrix, VertexPositionOverride initialVertexPositionsOverride) {
        this.widthFunction = widthFunction;
        this.colorFunction = colorFunction;
        this.offsetFunction = offsetFunction;
        this.smoothen = smoothen;
        this.pixelate = pixelate;
        this.shader = shader;
        this.projectionAreaWidth = projectionAreaWidth;
        this.projectionAreaHeight = projectionAreaHeight;
        this.useUnscaledMatrix = useUnscaledMatrix;
        this.initialVertexPositionsOverride = initialVertexPositionsOverride;
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

    public interface VertexWidthFunction {
        float get(float trailLengthInterpolant);
    }

    public interface VertexColorFunction {
        Color get(float trailLengthInterpolant);
    }

    public interface VertexOffsetFunction {
        Vec2 get(float trailLengthInterpolant);
    }

    public static class VertexPositionOverride {
        public final Vec2 left;
        public final Vec2 right;

        public VertexPositionOverride(Vec2 left, Vec2 right) {
            this.left = left;
            this.right = right;
        }

        public boolean isValid() {
            return left != null && right != null && !left.isZero() && !right.isZero();
        }
    }
}