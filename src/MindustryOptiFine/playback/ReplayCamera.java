package MindustryOptiFine.playback;

import arc.Core;
import arc.Events;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.*;

public class ReplayCamera{
    private final ReplayServer server;
    private Mode mode = Mode.free;
    private Player targetPlayer;
    private float targetX, targetY;
    private float targetZoom = 1f;
    private Runnable listener;

    public enum Mode{
        free,
        followPlayer,
        fixed
    }

    public ReplayCamera(ReplayServer server){
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
        if(!server.isPlaying()) return;

        switch(mode){
            case followPlayer -> updateFollowPlayer();
            case fixed -> {}
            case free -> updateFree();
        }

        updateZoom();
    }

    private void updateFollowPlayer(){
        if(targetPlayer != null && targetPlayer.unit() != null){
            targetX = targetPlayer.unit().x();
            targetY = targetPlayer.unit().y();
        }else if(targetPlayer != null){
            targetX = targetPlayer.x();
            targetY = targetPlayer.y();
        }

        applyTarget();
    }

    private void updateFree(){
        applyTarget();
    }

    private void applyTarget(){
        Core.camera.position.lerp(targetX, targetY, 0.1f);
    }

    private void updateZoom(){
        if(Vars.renderer != null){
            float currentZoom = Vars.renderer.getDisplayScale();
            float diff = targetZoom - currentZoom;
            Vars.renderer.scaleCamera(diff * 2f);
        }
    }

    public void setMode(Mode mode){
        this.mode = mode;
    }

    public Mode getMode(){
        return mode;
    }

    public void setTargetPlayer(Player player){
        this.targetPlayer = player;
        this.mode = Mode.followPlayer;
    }

    public void setTargetPosition(float x, float y){
        this.targetX = x;
        this.targetY = y;
        this.mode = Mode.fixed;
    }

    public void setFreePosition(float x, float y){
        this.targetX = x;
        this.targetY = y;
        this.mode = Mode.free;
    }

    public void setZoom(float zoom){
        this.targetZoom = Mathf.clamp(zoom, 0.1f, 10f);
    }

    public float getZoom(){
        return targetZoom;
    }

    public void zoomIn(){
        setZoom(targetZoom * 1.2f);
    }

    public void zoomOut(){
        setZoom(targetZoom / 1.2f);
    }

    public Vec2 getPosition(){
        return new Vec2(targetX, targetY);
    }
}