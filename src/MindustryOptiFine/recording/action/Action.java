package MindustryOptiFine.recording.action;

import arc.util.io.*;

public interface Action{
    void write(Writes write);
    void read(Reads read);
    void execute();
    ActionType type();
}