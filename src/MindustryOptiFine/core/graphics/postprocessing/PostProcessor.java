package MindustryOptiFine.core.graphics.postprocessing;

import arc.graphics.gl.FrameBuffer;
import arc.graphics.g2d.TextureRegion;

public interface PostProcessor {
    void apply(FrameBuffer source);

    boolean isEnabled();

    void setEnabled(boolean enabled);
}