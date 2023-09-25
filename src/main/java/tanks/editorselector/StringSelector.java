package tanks.editorselector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectString;

public class StringSelector<T extends GameObject> extends LevelEditorSelector<T>
{
    // Easiest selector to write so far, nice.
    // Mod API 1.2.0a

    public String string;

    @Override
    public void baseInit()
    {
        this.id = "string";
        this.title = "String Selector";
        this.property = "string";

        super.baseInit();
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlaySelectString(Game.screen, editor, this);
    }

    @Override
    public String getMetadata()
    {
        return string;
    }

    @Override
    public void setMetadata(String data)
    {
        this.string = data;
    }

    @Override
    public void changeMetadata(int add)
    {
        // meow
    }
}
