package MindustryOptiFine.recording.action;

import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.gen.*;
import mindustry.io.TypeIO;

public class ActionPlayerData implements Action{
    public String uuid;
    public float x, y;
    public int unitId;
    public int teamId;

    public ActionPlayerData(){}

    public ActionPlayerData(Player player){
        this.uuid = player.uuid();
        this.x = player.x();
        this.y = player.y();
        this.unitId = player.unit() == null ? -1 : player.unit().id();
        this.teamId = player.team().id;
    }

    @Override
    public void write(Writes write){
        TypeIO.writeString(write, uuid);
        write.f(x);
        write.f(y);
        write.i(unitId);
        write.i(teamId);
    }

    @Override
    public void read(Reads read){
        uuid = TypeIO.readString(read);
        x = read.f();
        y = read.f();
        unitId = read.i();
        teamId = read.i();
    }

    @Override
    public void execute(){
        Player player = Groups.player.find(p -> p.uuid().equals(uuid));
        if(player != null){
            player.set(x, y);
            if(unitId != -1){
                Unit unit = Groups.unit.getByID(unitId);
                if(unit != null){
                    player.unit(unit);
                }
            }
        }
    }

    @Override
    public ActionType type(){
        return ActionType.playerData;
    }
}