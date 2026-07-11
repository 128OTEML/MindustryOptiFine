package MindustryOptiFine.recording.io;

import MindustryOptiFine.recording.action.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;

import java.io.*;
import java.util.Arrays;
import java.util.zip.*;

public class ReplayReader{
    private final Fi file;
    private final ReplayMeta meta;
    private DataInputStream in;
    private boolean closed = false;

    public ReplayReader(Fi file){
        this.file = file;
        this.meta = new ReplayMeta();
    }

    public boolean open(){
        try{
            if(!file.exists()) return false;

            in = new DataInputStream(new FileInputStream(file.file()));

            byte[] header = new byte[4];
            in.readFully(header);
            if(!Arrays.equals(header, new byte[]{'M', 'R', 'P', 'L'})){
                Log.err("Invalid replay file header");
                close();
                return false;
            }

            int version = in.readInt();
            if(version != 1){
                Log.err("Unsupported replay version: " + version);
                close();
                return false;
            }

            meta.read(new Reads(in));

            return true;
        }catch(Exception e){
            Log.err("ReplayReader open error: " + e.getMessage());
            return false;
        }
    }

    public Seq<Action> readChunk(){
        if(closed || in == null) return new Seq<>();

        try{
            int length = in.readInt();
            if(length <= 0) return new Seq<>();

            byte[] data = new byte[length];
            in.readFully(data);

            InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(data));
            DataInputStream dis = new DataInputStream(iis);
            Reads read = new Reads(dis);

            int count = read.i();
            Seq<Action> actions = new Seq<>(count);

            for(int i = 0; i < count; i++){
                int typeOrdinal = read.b() & 0xFF;
                Action action = ActionRegistry.create(typeOrdinal);
                if(action != null){
                    action.read(read);
                    actions.add(action);
                }
            }

            dis.close();
            return actions;
        }catch(Exception e){
            return new Seq<>();
        }
    }

    public boolean hasMore(){
        if(closed || in == null) return false;
        try{
            return in.available() > 0;
        }catch(Exception e){
            return false;
        }
    }

    public void close(){
        if(closed) return;
        closed = true;

        try{
            if(in != null) in.close();
        }catch(Exception e){
            Log.err("ReplayReader close error: " + e.getMessage());
        }
    }

    public ReplayMeta getMeta(){
        return meta;
    }
}