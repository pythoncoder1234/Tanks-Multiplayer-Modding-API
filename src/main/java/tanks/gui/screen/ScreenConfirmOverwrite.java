package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;

public class ScreenConfirmOverwrite extends Screen
{
    public Screen previous = Game.screen;
    public String message;
    public Runnable onConfirm;

    public Button overwrite = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Overwrite", () ->
    {
        onConfirm.run();
        Game.screen = previous;
    });

    public Button cancel = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Cancel", () -> Game.screen = previous);

    public ScreenConfirmOverwrite(String message, Runnable confirm)
    {
        this.music = previous.music;
        this.musicID = previous.musicID;

        this.message = message;
        this.onConfirm = confirm;
    }

    @Override
    public void update()
    {
        overwrite.update();
        cancel.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        int b = Level.isDark(true) ? 255 : 0;
        Drawing.drawing.setColor(b, b, b);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, this.message);

        overwrite.draw();
        cancel.draw();
    }
}
