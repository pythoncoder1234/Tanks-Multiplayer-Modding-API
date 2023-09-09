package tanks;

import tanks.gui.TextBox;

public class TextInput extends TextBox
{
    public double textColorR = 255;
    public double textColorG = 255;
    public double textColorB = 255;

    public TextInput(double x, double y, double sX, double sY, Runnable f, String defaultText)
    {
        super(x, y, sX, sY, "", f, defaultText);
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(textColorR, textColorG, textColorB);
        Drawing.drawing.setFontSize(this.sizeY * 0.6);
        Drawing.drawing.drawText(this.posX, this.posY, this.inputText);
    }
}
