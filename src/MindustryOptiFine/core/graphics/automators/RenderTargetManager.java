package MindustryOptiFine.core.graphics.automators;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType;

public class RenderTargetManager {
    public static final Seq<ManagedRenderTarget> managedTargets = new Seq<>();

    public interface RenderTargetUpdateDelegate {
        void invoke();
    }

    public static RenderTargetUpdateDelegate renderTargetUpdateLoopEvent;

    public static final int timeUntilUnusedTargetsAreDisposed = 600;

    public static void load() {
        Events.on(EventType.ResizeEvent.class, e -> {
            resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        });
    }

    public static void unload() {
        disposeOfTargets();
        renderTargetUpdateLoopEvent = null;
    }

    public static void register(ManagedRenderTarget target) {
        if (!managedTargets.contains(target)) {
            managedTargets.add(target);
        }
    }

    public static void unregister(ManagedRenderTarget target) {
        managedTargets.remove(target);
    }

    public static void disposeOfTargets() {
        for (ManagedRenderTarget target : managedTargets) {
            if (target != null) {
                target.dispose();
            }
        }
        managedTargets.clear();
    }

    public static void handleTargetUpdateLoop() {
        if (renderTargetUpdateLoopEvent != null) {
            renderTargetUpdateLoopEvent.invoke();
        }

        for (int i = 0; i < managedTargets.size; i++) {
            ManagedRenderTarget target = managedTargets.get(i);

            if (!target.subjectToGarbageCollection || target.isUninitialized()) {
                continue;
            }

            target.timeSinceLastUsage++;
            if (target.timeSinceLastUsage >= timeUntilUnusedTargetsAreDisposed) {
                target.dispose();
            }
        }
    }

    public static void resize(int width, int height) {
        for (ManagedRenderTarget target : managedTargets) {
            if (target != null && !target.waitingForFirstInitialization) {
                target.recreate(width, height);
            }
        }
    }
}