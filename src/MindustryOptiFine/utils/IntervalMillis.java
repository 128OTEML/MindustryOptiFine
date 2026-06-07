package MindustryOptiFine.utils;

import arc.util.*;

import java.util.*;

public class IntervalMillis{
    long[] times;

    public IntervalMillis(int capacity){
        times = new long[capacity];
    }

    public IntervalMillis(){
        this(1);
    }

    public boolean get(long time){
        return get(0, time);
    }

    public boolean get(int id, long time){
        if(id >= times.length) throw new RuntimeException("Out of bounds! Max timer size is " + times.length + "!");

        boolean got = check(id, time);
        if(got) times[id] = Time.millis();
        return got;
    }

    public boolean check(int id, long time){
        return Time.millis() - times[id] >= time || Time.millis() < times[id];
    }

    public void reset(int id, long time){
        times[id] = Time.millis() - time;
    }

    public void clear(){
        Arrays.fill(times, 0);
    }

    public long getTime(int id){
        return Time.millis() - times[id];
    }

    public long[] getTimes(){
        return times;
    }
}