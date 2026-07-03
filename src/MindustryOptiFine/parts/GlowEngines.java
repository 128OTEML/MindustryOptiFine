package MindustryOptiFine.parts;

import arc.*;
import arc.struct.*;
import MindustryOptiFine.*;
import MindustryOptiFine.graphics.*;
import mindustry.gen.*;
import mindustry.type.UnitType.*;

public class GlowEngines extends UnitEngine{
    public Seq<UnitEngine> engines = new Seq<>();

    @Override
    public void draw(Unit unit){
        if(Core.batch == MindustryOptiFine.batch){
            AltLightBatch b = MindustryOptiFine.batch;
            b.setGlow(true);
            for(UnitEngine e : engines){
                e.draw(unit);
            }
            b.setGlow(false);
        }else{
            for(UnitEngine e : engines){
                e.draw(unit);
            }
        }
    }
}
