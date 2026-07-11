package MindustryOptiFine.recording.action;

import arc.util.io.*;

public class ActionWave implements Action{
    public int wave;

    public ActionWave(){}

    public ActionWave(int wave){
        this.wave = wave;
    }

    @Override
    public void write(Writes write){
        write.i(wave);
    }

    @Override
    public void read(Reads read){
        wave = read.i();
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.wave;
    }
}