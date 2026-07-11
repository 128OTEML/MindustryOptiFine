package MindustryOptiFine.recording.action;

import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.io.TypeIO;

public class ActionPlayerLeave implements Action{
    public String uuid;

    public ActionPlayerLeave(){}

    public ActionPlayerLeave(String uuid){
        this.uuid = uuid;
    }

    @Override
    public void write(Writes write){
        TypeIO.writeString(write, uuid);
    }

    @Override
    public void read(Reads read){
        uuid = TypeIO.readString(read);
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.playerLeave;
    }
}