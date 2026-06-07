package MindustryOptiFine.ui;

import arc.func.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.ui.*;

public class UIUtils{
    //不想新建更多的utility类了就放这吧

    /**
     * 为元素添加一个移动端可用的tooltip
     */
    public static void tooltip(Element e, Cons<Table> builder){
        var tool = new Tooltip(tooltip -> tooltip.table(builder).margin(2f), Tooltip.Tooltips.getInstance());
        tool.container.setBackground(Styles.black3);
        tool.allowMobile = true;
        e.addListener(tool);
    }

    public static void tooltip(Element e, String text){
        tooltip(e, t -> t.add(text));
    }
}
