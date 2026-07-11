package MindustryOptiFine.recording;

import MindustryOptiFine.recording.action.ActionSnapshot;
import arc.Events;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.io.TypeIO;
import mindustry.world.*;

import java.io.*;
import java.util.zip.*;

public class SnapshotManager{
    private final Recorder recorder;
    private final int interval;
    private Runnable listener;

    public SnapshotManager(Recorder recorder){
        this(recorder, 1200);
    }

    public SnapshotManager(Recorder recorder, int interval){
        this.recorder = recorder;
        this.interval = interval;
    }

    public void start(){
        listener = () -> Events.run(EventType.Trigger.update, this::checkSnapshot);
        listener.run();
    }

    public void stop(){
        if(listener != null){
            listener = null;
        }
    }

    private void checkSnapshot(){
        if(!recorder.isRecording()) return;

        if(recorder.getFrameCount() % interval == 0){
            byte[] snapshotData = createSnapshot();
            recorder.write(new ActionSnapshot(recorder.getFrameCount(), snapshotData));
        }
    }

    private byte[] createSnapshot(){
        ByteArrayOutputStream fbaos = new ByteArrayOutputStream();
        try{
            DeflaterOutputStream dos = new DeflaterOutputStream(fbaos);
            DataOutputStream out = new DataOutputStream(dos);
            Writes write = new Writes(out);

            writeTiles(write);
            writeUnits(write);
            writeBuildings(write);
            writePlayers(write);
            writeState(write);

            out.close();
        }catch(Exception e){
            Log.err("Snapshot creation error: @", e.getMessage());
        }

        return fbaos.toByteArray();
    }

    private void writeTiles(Writes write){
        int width = Vars.world.width();
        int height = Vars.world.height();
        write.i(width);
        write.i(height);

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                Tile tile = Vars.world.tile(x, y);
                if(tile != null && tile.block() != Blocks.air){
                    write.i(x);
                    write.i(y);
                    write.i(tile.block().id);
                    write.i(tile.build == null ? 0 : tile.build.rotation);
                    write.i(tile.build == null ? Team.derelict.id : tile.build.team().id);
                }
            }
        }

        write.i(-1);
    }

    private void writeUnits(Writes write){
        write.i(Groups.unit.size());
        Groups.unit.each(unit -> {
            write.i(unit.id());
            write.i(unit.type.id);
            write.f(unit.x());
            write.f(unit.y());
            write.f(unit.rotation());
            write.i(unit.team().id);
        });
    }

    private void writeBuildings(Writes write){
        write.i(Groups.build.size());
        Groups.build.each(build -> {
            write.i(build.tile.x);
            write.i(build.tile.y);
            write.i(build.block.id);
            write.i(build.rotation);
            write.i(build.team().id);
            write.f(build.health());
        });
    }

    private void writePlayers(Writes write){
        write.i(Groups.player.size());
        Groups.player.each(player -> {
            TypeIO.writeString(write, player.name);
            TypeIO.writeString(write, player.uuid());
            write.i(player.team().id);
            write.i(player.unit() == null ? -1 : player.unit().id());
        });
    }

    private void writeState(Writes write){
        write.i(Vars.state.wave);
        write.i(Vars.state.rules.defaultTeam.id);
        write.i(Vars.state.rules.waveTeam.id);
        write.b((byte)(Vars.state.gameOver ? 1 : 0));
        write.i(Vars.state.won ? Vars.state.rules.defaultTeam.id : -1);
    }
}