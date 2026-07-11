package MindustryOptiFine.recording;

import MindustryOptiFine.recording.action.ActionUnitMove;
import arc.Events;
import arc.struct.*;
import mindustry.game.EventType;
import mindustry.gen.*;

public class EntityTracker{
    private final Recorder recorder;
    private final IntMap<Position> lastPositions = new IntMap<>();
    private Runnable listener;

    public EntityTracker(Recorder recorder){
        this.recorder = recorder;
    }

    public void start(){
        listener = () -> Events.run(EventType.Trigger.update, this::trackEntities);
        listener.run();
    }

    public void stop(){
        if(listener != null){
            listener = null;
        }
    }

    private void trackEntities(){
        if(!recorder.isRecording()) return;

        Groups.unit.each(unit -> {
            int id = unit.id();
            Position last = lastPositions.get(id);

            if(last == null || 
               Math.abs(unit.x() - last.x) > 0.01f || 
               Math.abs(unit.y() - last.y) > 0.01f ||
               Math.abs(unit.rotation() - last.rotation) > 0.1f){

                recorder.write(new ActionUnitMove(unit));
                lastPositions.put(id, new Position(unit.x(), unit.y(), unit.rotation()));
            }
        });
    }

    private static class Position{
        float x, y, rotation;

        Position(float x, float y, float rotation){
            this.x = x;
            this.y = y;
            this.rotation = rotation;
        }
    }
}