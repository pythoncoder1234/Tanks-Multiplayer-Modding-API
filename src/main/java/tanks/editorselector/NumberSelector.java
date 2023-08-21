package tanks.editorselector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlayNumberSelector;

public abstract class NumberSelector<T extends GameObject> extends LevelEditorSelector<T>
{
    public String format = "%.1f";
    public double min = -99999999;
    public double max = 99999999;
    public double step = 1;

    /**
     * When inputted from a text box, rounds it to the nearest number divisible to <code>step</code>.
     */
    public boolean forceStep = true;
    public boolean allowDecimals = false;
    public double number;

    @Override
    public void onSelect()
    {
        Game.screen = new OverlayNumberSelector(Game.screen, editor, this);
    }

    public String numberString()
    {
        return String.format(format, number);
    }

    public void changeMetadata(int add)
    {
        this.number += add;
        syncProperties();
    }

    @Override
    public void load()
    {
        this.button.setText(buttonText, number);
    }

    public String getMetadata()
    {
        return String.format(this.format, this.number);
    }

    public void setMetadata(String d)
    {
        this.number = Double.parseDouble(d);
    }
}
