package MindustryOptiFine.recording;

import MindustryOptiFine.recording.action.*;
import MindustryOptiFine.recording.io.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;

public class Recorder{
    private boolean recording = false;
    private ReplayWriter writer;
    private EventCapture eventCapture;
    private EntityTracker entityTracker;
    private SnapshotManager snapshotManager;
    private int frameCount = 0;

    public void startRecording(Fi file){
        if(recording) stopRecording();

        recording = true;
        frameCount = 0;

        writer = new ReplayWriter(file);
        eventCapture = new EventCapture(this);
        entityTracker = new EntityTracker(this);
        snapshotManager = new SnapshotManager(this);

        ReplayMeta meta = new ReplayMeta();
        meta.mapWidth = Vars.world.width();
        meta.mapHeight = Vars.world.height();
        meta.mapName = Vars.state.map != null ? Vars.state.map.name() : "";
        meta.defaultTeamId = Vars.state.rules.defaultTeam.id;
        meta.waveTeamId = Vars.state.rules.waveTeam.id;
        writer.setMeta(meta);

        eventCapture.start();
        entityTracker.start();
        snapshotManager.start();

        Log.info("Recording started: @", file.name());
    }

    public void stopRecording(){
        if(!recording) return;

        recording = false;

        eventCapture.stop();
        entityTracker.stop();
        snapshotManager.stop();

        if(writer != null){
            writer.close();
            Log.info("Recording stopped, saved to: @", writer.getMeta().frames + " frames");
        }

        writer = null;
    }

    public void write(Action action){
        if(recording && writer != null){
            writer.write(action);
            frameCount++;
        }
    }

    public void flush(){
        if(writer != null){
            writer.flush();
        }
    }

    public boolean isRecording(){
        return recording;
    }

    public int getFrameCount(){
        return frameCount;
    }

    public ReplayWriter getWriter(){
        return writer;
    }
}