package MindustryOptiFine.recording;

import MindustryOptiFine.recording.action.*;
import arc.Events;
import arc.struct.*;
import mindustry.Vars;
import mindustry.game.*;
import mindustry.gen.*;

public class EventCapture{
    private final Recorder recorder;
    private final Seq<Runnable> listeners = new Seq<>();

    public EventCapture(Recorder recorder){
        this.recorder = recorder;
    }

    public void start(){
        listeners.add(() -> Events.on(EventType.TileChangeEvent.class, this::onTileChange));
        listeners.add(() -> Events.on(EventType.UnitCreateEvent.class, this::onUnitCreate));
        listeners.add(() -> Events.on(EventType.UnitDestroyEvent.class, this::onUnitDestroy));
        listeners.add(() -> Events.on(EventType.PlayerJoin.class, this::onPlayerJoin));
        listeners.add(() -> Events.on(EventType.PlayerLeave.class, this::onPlayerLeave));
        listeners.add(() -> Events.on(EventType.WaveEvent.class, this::onWave));
        listeners.add(() -> Events.on(EventType.WinEvent.class, this::onWin));
        listeners.add(() -> Events.on(EventType.LoseEvent.class, this::onLose));
        listeners.add(() -> Events.on(EventType.PlayerChatEvent.class, this::onChat));
        listeners.add(() -> Events.on(EventType.BuildDamageEvent.class, this::onBuildDamage));
        listeners.add(() -> Events.on(EventType.UnitDamageEvent.class, this::onUnitDamage));
        listeners.add(() -> Events.on(EventType.ConfigEvent.class, this::onConfigChange));
        listeners.add(() -> Events.on(EventType.BuildTeamChangeEvent.class, this::onTeamChange));

        for(Runnable listener : listeners){
            listener.run();
        }

        listeners.add(() -> Events.run(EventType.Trigger.update, this::onUpdate));
        listeners.peek().run();
    }

    public void stop(){
        listeners.clear();
    }

    private void onTileChange(EventType.TileChangeEvent e){
        recorder.write(new ActionTileChange(e.tile));
    }

    private void onUnitCreate(EventType.UnitCreateEvent e){
        recorder.write(new ActionUnitCreate(e.unit));
    }

    private void onUnitDestroy(EventType.UnitDestroyEvent e){
        recorder.write(new ActionUnitDestroy(e.unit));
    }

    private void onPlayerJoin(EventType.PlayerJoin e){
        recorder.write(new ActionPlayerJoin(e.player));
    }

    private void onPlayerLeave(EventType.PlayerLeave e){
        recorder.write(new ActionPlayerLeave(e.player.uuid()));
    }

    private void onWave(EventType.WaveEvent e){
        recorder.write(new ActionWave(Vars.state.wave));
    }

    private void onWin(EventType.WinEvent e){
        recorder.write(new ActionGameOver(Vars.state.rules.defaultTeam.id));
    }

    private void onLose(EventType.LoseEvent e){
        recorder.write(new ActionGameOver(Vars.state.rules.waveTeam.id));
    }

    private void onChat(EventType.PlayerChatEvent e){
        recorder.write(new ActionChat(e.player.name, e.message));
    }

    private void onBuildDamage(EventType.BuildDamageEvent e){
        if(e.build != null){
            recorder.write(new ActionBuildDamage(e.build.tile.x, e.build.tile.y, 0, e.source != null ? e.source.type.id : -1));
        }
    }

    private void onUnitDamage(EventType.UnitDamageEvent e){
        recorder.write(new ActionUnitDamage(e.unit.id(), 0, e.bullet != null ? e.bullet.type.id : -1));
    }

    private void onConfigChange(EventType.ConfigEvent e){
        if(e.tile != null){
            recorder.write(new ActionConfigChange(e.tile, e.value));
        }
    }

    private void onTeamChange(EventType.BuildTeamChangeEvent e){
        if(e.build != null){
            recorder.write(new ActionBuildTeamChange(e.build.tile.x, e.build.tile.y, e.previous.id, e.build.team().id));
        }
    }

    private void onUpdate(){
        if(recorder.isRecording()){
            Groups.player.each(player -> {
                recorder.write(new ActionPlayerData(player));
            });

            recorder.write(new ActionNextTick(recorder.getFrameCount()));
        }
    }
}