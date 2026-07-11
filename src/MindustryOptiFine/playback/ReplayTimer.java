package MindustryOptiFine.playback;

import arc.Events;
import arc.util.*;
import mindustry.game.EventType;

public class ReplayTimer{
    private final ReplayServer server;
    private float tickRate = 1f;
    private float accumulator = 0f;
    private boolean fastForwarding = false;
    private Runnable listener;

    public ReplayTimer(ReplayServer server){
        this.server = server;
    }

    public void start(){
        listener = () -> Events.run(EventType.Trigger.update, this::update);
        listener.run();
    }

    public void stop(){
        if(listener != null){
            listener = null;
        }
    }

    public void update(){
        if(!server.isPlaying() || server.isPaused()) return;

        if(fastForwarding){
            accumulator += Time.delta * tickRate * 10f;
        }else{
            accumulator += Time.delta * tickRate;
        }
    }

    public boolean shouldUpdate(){
        if(accumulator >= 1f){
            accumulator -= 1f;
            return true;
        }
        return false;
    }

    public void setTickRate(float rate){
        this.tickRate = rate;
    }

    public float getTickRate(){
        return tickRate;
    }

    public void setFastForwarding(boolean fastForwarding){
        this.fastForwarding = fastForwarding;
    }

    public boolean isFastForwarding(){
        return fastForwarding;
    }

    public float getProgress(){
        if(server.getTotalFrames() == 0) return 0f;
        return (float)server.getCurrentTick() / server.getTotalFrames();
    }

    public float getTime(){
        return server.getCurrentTick() / 60f;
    }

    public float getDuration(){
        return server.getTotalFrames() / 60f;
    }
}