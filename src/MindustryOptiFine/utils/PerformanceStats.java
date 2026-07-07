package MindustryOptiFine.utils;

import arc.struct.*;
import arc.util.*;

public class PerformanceStats{
    static ObjectMap<String, Long> timings = new ObjectMap<>();
    static ObjectMap<String, Long> totals = new ObjectMap<>();
    static ObjectMap<String, Integer> counts = new ObjectMap<>();
    static int frameCount = 0;
    static final int reportInterval = 60;
    
    public static void start(String name){
        timings.put(name, Time.millis());
    }
    
    public static void end(String name){
        long start = timings.get(name, 0L);
        long elapsed = Time.millis() - start;
        
        totals.put(name, totals.get(name, 0L) + elapsed);
        counts.put(name, counts.get(name, 0) + 1);
    }
    
    public static void frameEnd(){
        frameCount++;
        if(frameCount >= reportInterval){
            Log.info("=== Performance Stats (last " + reportInterval + " frames) ===");
            for(String name : totals.keys()){
                long total = totals.get(name, 0L);
                int count = counts.get(name, 0);
                long avg = count > 0 ? total / count : 0;
                Log.info(name + ": total=" + total + "ms, count=" + count + ", avg=" + avg + "ms");
            }
            totals.clear();
            counts.clear();
            frameCount = 0;
            Log.info("==============================");
        }
    }
    
    public static void reset(){
        timings.clear();
        totals.clear();
        counts.clear();
        frameCount = 0;
    }
}