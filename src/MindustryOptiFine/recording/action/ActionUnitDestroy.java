package MindustryOptiFine.recording.action;

import arc.util.io.*;
import mindustry.Vars;
import mindustry.gen.*;

public class ActionUnitDestroy implements Action{
    public int unitId;

    public ActionUnitDestroy(){}

    public ActionUnitDestroy(int unitId){
        this.unitId = unitId;
    }

    public ActionUnitDestroy(Unit unit){
        this.unitId = unit.id();
    }

    @Override
    public void write(Writes write){
        write.i(unitId);
    }

    @Override
    public void read(Reads read){
        unitId = read.i();
    }

    @Override
    public void execute(){
        Unit unit = Groups.unit.getByID(unitId);
        if(unit != null){
            unit.remove();
        }
    }

    @Override
    public ActionType type(){
        return ActionType.unitDestroy;
    }
}