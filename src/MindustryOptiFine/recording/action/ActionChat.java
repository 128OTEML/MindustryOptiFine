package MindustryOptiFine.recording.action;

import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.io.TypeIO;

public class ActionChat implements Action{
    public String playerName;
    public String message;

    public ActionChat(){}

    public ActionChat(String playerName, String message){
        this.playerName = playerName;
        this.message = message;
    }

    @Override
    public void write(Writes write){
        TypeIO.writeString(write, playerName);
        TypeIO.writeString(write, message);
    }

    @Override
    public void read(Reads read){
        playerName = TypeIO.readString(read);
        message = TypeIO.readString(read);
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.chat;
    }
}