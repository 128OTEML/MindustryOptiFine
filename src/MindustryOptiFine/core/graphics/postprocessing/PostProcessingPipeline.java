package MindustryOptiFine.core.graphics.postprocessing;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType;

public class PostProcessingPipeline {
    private static FrameBuffer target1;
    private static FrameBuffer target2;
    private static FrameBuffer currentTarget;
    private static FrameBuffer previousTarget;

    private static final Seq<PostProcessor> processors = new Seq<>();

    private static boolean enabled = true;

    public static void load() {
        if (Core.graphics != null && Core.graphics.getWidth() > 0 && Core.graphics.getHeight() > 0) {
            target1 = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
            target2 = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
            currentTarget = target1;
            previousTarget = target2;
        }

        Events.on(EventType.ResizeEvent.class, e -> {
            resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        });

        Events.run(EventType.Trigger.draw, PostProcessingPipeline::apply);
    }

    public static void unload() {
        if (target1 != null) {
            target1.dispose();
        }
        if (target2 != null) {
            target2.dispose();
        }
        processors.clear();
    }

    public static void resize(int width, int height) {
        if (target1 != null) {
            target1.dispose();
        }
        if (target2 != null) {
            target2.dispose();
        }
        target1 = new FrameBuffer(width, height);
        target2 = new FrameBuffer(width, height);
        currentTarget = target1;
        previousTarget = target2;
    }

    public static void addProcessor(PostProcessor processor) {
        if (!processors.contains(processor)) {
            processors.add(processor);
        }
    }

    public static void removeProcessor(PostProcessor processor) {
        processors.remove(processor);
    }

    public static void clearProcessors() {
        processors.clear();
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void beginRender() {
        if (!enabled) return;
        currentTarget.begin();
        Core.graphics.clear(Color.clear);
    }

    public static void endRender() {
        if (!enabled) return;
        currentTarget.end();
    }

    public static void apply() {
        if (!enabled || processors.isEmpty()) return;
        if (currentTarget == null || previousTarget == null) return;
        if (Vars.state == null || !Vars.state.isGame()) return;

        Log.info("PostProcessingPipeline: applying " + processors.size + " processors");

        FrameBuffer source = currentTarget;
        FrameBuffer destination = previousTarget;

        for (PostProcessor processor : processors) {
            if (!processor.isEnabled()) continue;

            Log.info("PostProcessingPipeline: applying processor: " + processor.getClass().getSimpleName());
            destination.begin();
            Core.graphics.clear(Color.clear);

            processor.apply(source);

            destination.end();

            FrameBuffer temp = source;
            source = destination;
            destination = temp;
        }

        Draw.rect(new TextureRegion(source.getTexture()), Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f, Core.graphics.getWidth(), Core.graphics.getHeight());
        Log.info("PostProcessingPipeline: apply completed");
    }

    public static Seq<PostProcessor> getProcessors() {
        return processors;
    }
}