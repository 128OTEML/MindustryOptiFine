package MindustryOptiFine.ui;

import MindustryOptiFine.recording.*;
import arc.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;

public class RecordingIndicator{
    private final Recorder recorder;
    private Table indicator;
    private Label frameCount;

    public RecordingIndicator(Recorder recorder){
        this.recorder = recorder;
        setup();
    }

    private void setup(){
        indicator = new Table();
        indicator.setFillParent(false);
        indicator.top().right();
        indicator.margin(10f);

        indicator.add(new Image(Icon.refresh)).color(Color.red).size(32f);
        indicator.add(new Label("@replay.record.recording")).color(Color.red).left();
        frameCount = indicator.add("").color(Color.white).left().get();

        indicator.visible(() -> recorder.isRecording());
        indicator.update(() -> {
            frameCount.setText(recorder.getFrameCount() + " frames");
        });

        Core.scene.add(indicator);

        Events.run(Trigger.update, () -> {
            if(Core.scene != null){
                indicator.setPosition(Core.graphics.getWidth() - 10f, Core.graphics.getHeight() - 10f, Align.topRight);
            }
        });
    }

    public void dispose(){
        if(indicator != null){
            indicator.remove();
            indicator = null;
        }
    }
}