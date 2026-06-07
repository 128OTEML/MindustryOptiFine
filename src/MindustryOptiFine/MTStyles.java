package MindustryOptiFine;

import arc.func.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.ui.*;

public class MTStyles{
    public static Drawable titleBarbgNormal, titleBarbgSnapped, white, gray2;
    public static float buttonSize = 32f;
    public static TextButton.TextButtonStyle textb = Styles.flatt, textbtoggle = Styles.flatTogglet;
    public static Cons<TextButton> funcSetTextb = c -> {
        c.getLabel().setAlignment(Align.center);
        c.getLabel().setWrap(false);
        c.margin(6f);
    };

    public static void load(){

    }
}
