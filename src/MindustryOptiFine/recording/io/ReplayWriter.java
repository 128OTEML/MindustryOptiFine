package MindustryOptiFine.recording.io;

import MindustryOptiFine.recording.action.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.Vars;

import java.io.*;
import java.util.concurrent.*;
import java.util.zip.*;

public class ReplayWriter{
    private final Fi file;
    private final ReplayMeta meta;
    private final Seq<Action> actions = new Seq<>();
    private boolean closed = false;

    public ReplayWriter(Fi file){
        this.file = file;
        this.meta = new ReplayMeta();
    }

    public void setMeta(ReplayMeta meta){
        this.meta.mapWidth = meta.mapWidth;
        this.meta.mapHeight = meta.mapHeight;
        this.meta.mapName = meta.mapName;
        this.meta.defaultTeamId = meta.defaultTeamId;
        this.meta.waveTeamId = meta.waveTeamId;
        this.meta.snapshotInterval = meta.snapshotInterval;
    }

    public void write(Action action){
        if(closed) return;
        actions.add(action);
        meta.frames++;
    }

    public void flush(){
        if(actions.isEmpty()) return;

        Vars.mainExecutor.submit(() -> {
            try{
                writeChunk(actions);
            }catch(Exception e){
                Log.err("ReplayWriter flush error: " + e.getMessage());
            }
        });

        actions.clear();
    }

    private void writeChunk(Seq<Action> chunk) throws IOException{
        ByteArrayOutputStream fbaos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(new DeflaterOutputStream(fbaos));
        Writes write = new Writes(dos);

        write.i(chunk.size);
        for(Action action : chunk){
            write.b((byte)action.type().ordinal());
            action.write(write);
        }

        dos.close();

        synchronized(this){
            if(!file.exists()){
                writeHeader();
            }

            DataOutputStream out = new DataOutputStream(new FileOutputStream(file.file(), true));
            byte[] data = fbaos.toByteArray();
            out.writeInt(data.length);
            out.write(data);
            out.close();
        }
    }

    private void writeHeader() throws IOException{
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file.file()));
        Writes write = new Writes(out);

        write.b(new byte[]{'M', 'R', 'P', 'L'});
        write.i(1);
        meta.write(write);

        out.close();
    }

    public void close(){
        if(closed) return;
        closed = true;

        flush();

        meta.duration = (long)(meta.frames / 60.0 * 1000);

        try{
            RandomAccessFile raf = new RandomAccessFile(file.file(), "rw");
            raf.seek(4);
            raf.writeInt(1);
            Writes write = new Writes(new DataOutputStream(new FileOutputStream(raf.getFD())));
            meta.write(write);
            raf.close();
        }catch(Exception e){
            Log.err("ReplayWriter close error: " + e.getMessage());
        }
    }

    public ReplayMeta getMeta(){
        return meta;
    }
}