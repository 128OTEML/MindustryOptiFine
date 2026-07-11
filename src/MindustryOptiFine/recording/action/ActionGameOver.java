package MindustryOptiFine.recording.action;

import arc.util.io.*;

public class ActionGameOver implements Action{
    public int winnerTeamId;

    public ActionGameOver(){}

    public ActionGameOver(int winnerTeamId){
        this.winnerTeamId = winnerTeamId;
    }

    @Override
    public void write(Writes write){
        write.i(winnerTeamId);
    }

    @Override
    public void read(Reads read){
        winnerTeamId = read.i();
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.gameOver;
    }
}