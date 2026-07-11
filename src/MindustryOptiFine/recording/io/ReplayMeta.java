package MindustryOptiFine.recording.io;

import arc.util.io.*;
import mindustry.io.TypeIO;

public class ReplayMeta{
    public String version = "1.0";
    public String gameVersion = "159.2";
    public long duration = 0;
    public int frames = 0;
    public int mapWidth = 0;
    public int mapHeight = 0;
    public String mapName = "";
    public int defaultTeamId = 0;
    public int waveTeamId = 1;
    public int snapshotInterval = 1200;

    public void write(Writes write){
        TypeIO.writeString(write, version);
        TypeIO.writeString(write, gameVersion);
        write.l(duration);
        write.i(frames);
        write.i(mapWidth);
        write.i(mapHeight);
        TypeIO.writeString(write, mapName);
        write.i(defaultTeamId);
        write.i(waveTeamId);
        write.i(snapshotInterval);
    }

    public void read(Reads read){
        version = TypeIO.readString(read);
        gameVersion = TypeIO.readString(read);
        duration = read.l();
        frames = read.i();
        mapWidth = read.i();
        mapHeight = read.i();
        mapName = TypeIO.readString(read);
        defaultTeamId = read.i();
        waveTeamId = read.i();
        snapshotInterval = read.i();
    }
}