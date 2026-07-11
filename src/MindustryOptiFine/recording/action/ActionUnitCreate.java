package MindustryOptiFine.recording.action;

import arc.util.io.*;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.*;

public class ActionUnitCreate implements Action{
    public int unitId;
    public int typeId;
    public float x, y;
    public float rotation;
    public int teamId;

    public ActionUnitCreate(){}

    public ActionUnitCreate(Unit unit){
        this.unitId = unit.id();
        this.typeId = unit.type.id;
        this.x = unit.x();
        this.y = unit.y();
        this.rotation = unit.rotation();
        this.teamId = unit.team().id;
    }

    @Override
    public void write(Writes write){
        write.i(unitId);
        write.i(typeId);
        write.f(x);
        write.f(y);
        write.f(rotation);
        write.i(teamId);
    }

    @Override
    public void read(Reads read){
        unitId = read.i();
        typeId = read.i();
        x = read.f();
        y = read.f();
        rotation = read.f();
        teamId = read.i();
    }

    @Override
    public void execute(){
        Unit unit = Vars.content.unit(typeId).create(Team.get(teamId));
        unit.set(x, y);
        unit.rotation(rotation);
    }

    @Override
    public ActionType type(){
        return ActionType.unitCreate;
    }
}