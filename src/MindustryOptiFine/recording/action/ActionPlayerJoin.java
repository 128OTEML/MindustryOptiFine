package MindustryOptiFine.recording.action;

import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.gen.*;
import mindustry.io.TypeIO;

public class ActionPlayerJoin implements Action{
    public String name;
    public String uuid;
    public int teamId;

    public ActionPlayerJoin(){}

    public ActionPlayerJoin(Player player){
        this.name = player.name;
        this.uuid = player.uuid();
        this.teamId = player.team().id;
    }

    @Override
    public void write(Writes write){
        TypeIO.writeString(write, name);
        TypeIO.writeString(write, uuid);
        write.i(teamId);
    }

    @Override
    public void read(Reads read){
        name = TypeIO.readString(read);
        uuid = TypeIO.readString(read);
        teamId = read.i();
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.playerJoin;
    }
}