package MindustryOptiFine.recording.action;

import arc.util.io.*;

public class ActionSectorCapture implements Action{
    public int sectorId;
    public boolean initialCapture;

    public ActionSectorCapture(){}

    public ActionSectorCapture(int sectorId, boolean initialCapture){
        this.sectorId = sectorId;
        this.initialCapture = initialCapture;
    }

    @Override
    public void write(Writes write){
        write.i(sectorId);
        write.b((byte)(initialCapture ? 1 : 0));
    }

    @Override
    public void read(Reads read){
        sectorId = read.i();
        initialCapture = read.b() == 1;
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.sectorCapture;
    }
}