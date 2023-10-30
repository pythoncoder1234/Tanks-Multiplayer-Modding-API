package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenCrusadeDescription extends Screen
{
    public Crusade crusade;

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenCrusadeEditor(crusade));

    public ScreenCrusadeDescription(Crusade c)
    {
        this.music = "menu_5.ogg";
        this.musicID = "menu";

        this.crusade = c;
        this.description.maxChars = 80;
        this.description.enablePunctuation = true;
        this.description.enableCaps = true;

        if (c.description != null)
            this.description.inputText = c.description;
    }    public TextBox description = new TextBox(this.centerX, this.centerY, this.objWidth * 2.5, this.objHeight, "Description", new Runnable()
    {
        @Override
        public void run()
        {
            if (!description.inputText.isBlank())
                crusade.description = description.inputText;
            else
                crusade.description = null;
        }
    }, "");

    @Override
    public void update()
    {
        this.back.update();
        this.description.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "Enter crusade description");

        this.back.draw();
        this.description.draw();
    }


}
