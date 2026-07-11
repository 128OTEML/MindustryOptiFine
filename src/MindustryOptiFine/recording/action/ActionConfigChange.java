package MindustryOptiFine.recording.action;

import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.io.TypeIO;
import mindustry.world.*;

public class ActionConfigChange implements Action{
    public int x, y;
    public String config;

    public ActionConfigChange(){}

    public ActionConfigChange(Building build, Object config){
        this.x = build.tile.x;
        this.y = build.tile.y;
        this.config = config == null ? "" : config.toString();
    }

    @Override
    public void write(Writes write){
        write.i(x);
        write.i(y);
        TypeIO.writeString(write, config);
    }

    @Override
    public void read(Reads read){
        x = read.i();
        y = read.i();
        config = TypeIO.readString(read);
    }

    @Override
    public void execute(){
        Building build = Vars.world.build(x, y);
        if(build != null){
            build.configure(config);
        }
    }

    @Override
    public ActionType type(){
        return ActionType.configChange;
    }
}