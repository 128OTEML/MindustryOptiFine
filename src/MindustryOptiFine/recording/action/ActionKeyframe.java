package MindustryOptiFine.recording.action;

import arc.util.io.*;
import mindustry.io.TypeIO;

public class ActionKeyframe implements Action {
    public int tick;
    public String name;
    public float cameraX, cameraY, cameraZoom;

    public ActionKeyframe(){}

    public ActionKeyframe(int tick, String name, float cameraX, float cameraY, float cameraZoom){
        this.tick = tick;
        this.name = name;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZoom = cameraZoom;
    }

    @Override
    public void write(Writes write){
        write.i(tick);
        TypeIO.writeString(write, name);
        write.f(cameraX);
        write.f(cameraY);
        write.f(cameraZoom);
    }

    @Override
    public void read(Reads read){
        tick = read.i();
        name = TypeIO.readString(read);
        cameraX = read.f();
        cameraY = read.f();
        cameraZoom = read.f();
    }

    @Override
    public void execute(){
    }

    @Override
    public ActionType type(){
        return ActionType.keyframe;
    }
}