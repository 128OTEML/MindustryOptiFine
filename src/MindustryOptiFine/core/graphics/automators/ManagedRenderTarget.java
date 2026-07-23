package MindustryOptiFine.core.graphics.automators;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import MindustryOptiFine.core.graphics.Disposable;

public class ManagedRenderTarget implements Disposable {
    private FrameBuffer target;

    boolean waitingForFirstInitialization = true;

    RenderTargetInitializationAction initializationAction;

    public boolean isUninitialized() {
        return target == null || target.isDisposed();
    }

    public int timeSinceLastUsage = 0;

    public boolean isDisposed = false;

    public boolean shouldResetUponScreenResize = false;

    public boolean subjectToGarbageCollection = true;

    public FrameBuffer getTarget() {
        timeSinceLastUsage = 0;
        if (isUninitialized()) {
            if (Core.graphics == null || Core.graphics.getWidth() <= 0 || Core.graphics.getHeight() <= 0) {
                return null;
            }
            target = initializationAction.create(Core.graphics.getWidth(), Core.graphics.getHeight());
            waitingForFirstInitialization = false;
        }
        return target;
    }

    public int getWidth() {
        return getTarget().getWidth();
    }

    public int getHeight() {
        return getTarget().getHeight();
    }

    @FunctionalInterface
    public interface RenderTargetInitializationAction {
        FrameBuffer create(int screenWidth, int screenHeight);
    }

    public ManagedRenderTarget(boolean shouldResetUponScreenResize, RenderTargetInitializationAction creationCondition) {
        this(shouldResetUponScreenResize, creationCondition, true);
    }

    public ManagedRenderTarget(boolean shouldResetUponScreenResize, RenderTargetInitializationAction creationCondition, boolean subjectToGarbageCollection) {
        this.shouldResetUponScreenResize = shouldResetUponScreenResize;
        this.initializationAction = creationCondition;
        this.subjectToGarbageCollection = subjectToGarbageCollection;
        RenderTargetManager.register(this);
    }

    public static FrameBuffer createScreenSizedTarget(int screenWidth, int screenHeight) {
        return new FrameBuffer(screenWidth, screenHeight);
    }

    public void dispose() {
        if (isDisposed) return;

        isDisposed = true;
        if (target != null) {
            target.dispose();
        }
        timeSinceLastUsage = 0;
    }

    public void recreate(int screenWidth, int screenHeight) {
        dispose();
        isDisposed = false;
        timeSinceLastUsage = 0;
        target = initializationAction.create(screenWidth, screenHeight);
    }

    public Vec2 size() {
        Texture tex = getTarget().getTexture();
        return new Vec2(tex.width, tex.height);
    }

    public void swapToRenderTarget(Color flushColor) {
        FrameBuffer fb = getTarget();
        if (fb == null) return;
        fb.begin();
        if (flushColor != null) {
            Core.graphics.clear(flushColor);
        }
    }

    public void copyContentsFrom(FrameBuffer from) {
        FrameBuffer fb = getTarget();
        if (fb == null || from == null) return;
        fb.begin(Color.clear);

        Draw.rect(new TextureRegion(from.getTexture()), 0, 0, from.getWidth(), from.getHeight());

        fb.end();
        from.begin(Color.clear);
        from.end();
    }
}
