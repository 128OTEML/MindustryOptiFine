package MindustryOptiFine.playback;

import MindustryOptiFine.recording.action.*;
import MindustryOptiFine.recording.io.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.*;
import mindustry.gen.*;

public class ReplayServer{
    private boolean playing = false;
    private boolean paused = false;
    private boolean frozen = false;
    private ReplayReader reader;
    private ReplayMeta meta;
    private ActionExecutor executor;
    private ReplayTimer timer;
    private ReplayCamera camera;
    private int currentTick = 0;
    private Seq<Action> currentChunk = new Seq<>();
    private int chunkIndex = 0;

    public void loadReplay(Fi file){
        stop();

        if(!file.exists()){
            Log.err("Replay file not found: @", file.name());
            return;
        }

        if(file.length() < 8){
            Log.err("Replay file is too small: @", file.name());
            return;
        }

        reader = new ReplayReader(file);
        if(!reader.open()){
            Log.err("Failed to open replay file: @", file.name());
            reader = null;
            return;
        }

        meta = reader.getMeta();

        executor = new ActionExecutor(this);
        timer = new ReplayTimer(this);
        camera = new ReplayCamera(this);

        Log.info("Replay loaded: @, frames: @, duration: @s", file.name(), meta.frames, meta.duration / 1000);
    }

    public void start(){
        if(reader == null || playing) return;

        playing = true;
        paused = false;
        frozen = false;
        currentTick = 0;
        chunkIndex = 0;

        loadNextChunk();

        timer.start();
        camera.start();

        Log.info("Replay started");
    }

    public void pause(){
        paused = !paused;
    }

    public void stop(){
        playing = false;
        paused = false;
        frozen = false;

        if(reader != null){
            reader.close();
            reader = null;
        }

        if(timer != null){
            timer.stop();
        }

        if(camera != null){
            camera.stop();
        }

        currentTick = 0;
        chunkIndex = 0;
        currentChunk.clear();
    }

    public void jumpToTick(int tick){
        if(reader == null) return;

        currentTick = tick;
        chunkIndex = 0;
        currentChunk.clear();

        while(reader.hasMore() && currentTick > meta.snapshotInterval * chunkIndex){
            chunkIndex++;
            reader.readChunk();
        }

        loadNextChunk();
    }

    public void update(){
        if(!playing || paused || frozen) return;

        while(chunkIndex < currentChunk.size){
            Action action = currentChunk.get(chunkIndex);
            if(action instanceof ActionNextTick){
                currentTick++;
                chunkIndex++;

                if(timer.shouldUpdate()){
                    executor.execute(action);
                    break;
                }
            }else{
                executor.execute(action);
                chunkIndex++;
            }
        }

        if(chunkIndex >= currentChunk.size && reader.hasMore()){
            loadNextChunk();
        }

        timer.update();
        camera.update();
    }

    private void loadNextChunk(){
        if(reader != null && reader.hasMore()){
            currentChunk = reader.readChunk();
            chunkIndex = 0;
        }
    }

    public boolean isPlaying(){
        return playing;
    }

    public boolean isPaused(){
        return paused;
    }

    public boolean isFrozen(){
        return frozen;
    }

    public int getCurrentTick(){
        return currentTick;
    }

    public int getTotalFrames(){
        return meta != null ? meta.frames : 0;
    }

    public ReplayMeta getMeta(){
        return meta;
    }

    public ReplayTimer getTimer(){
        return timer;
    }

    public ReplayCamera getCamera(){
        return camera;
    }
}