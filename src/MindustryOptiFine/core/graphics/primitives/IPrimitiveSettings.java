package MindustryOptiFine.core.graphics.primitives;

import MindustryOptiFine.core.graphics.shaders.ManagedShader;

public interface IPrimitiveSettings {
    boolean getPixelate();

    ManagedShader getShader();

    Integer getProjectionAreaWidth();

    Integer getProjectionAreaHeight();

    boolean getUseUnscaledMatrix();
}