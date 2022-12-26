package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenConfirmDeleteCrusade extends Screen
{
    public ScreenCrusadeLevels background;
    public int sY = 5;
    public int posY = -1;

    public Screen previous;
    public Crusade crusade;

    public Button cancelDelete = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "No", () -> Game.screen = previous);

    public Button confirmDelete = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Yes", () ->
    {
        Game.game.fileManager.getFile(crusade.fileName).delete();
        Game.screen = new ScreenCrusades();
    });

    public ScreenConfirmDeleteCrusade(Screen previous, Crusade crusade)
    {
        this.previous = previous;
        this.crusade = crusade;

        if (this.previous instanceof ScreenCrusadeDetails)
        {
            ScreenCrusadeDetails s = (ScreenCrusadeDetails) this.previous;
            this.background = s.background;
            this.posY = s.popupY;
            this.sY = s.popupSY;
        }

        this.music = previous.music;
        this.musicID = previous.musicID;
    }

    @Override
    public void update()
    {
        confirmDelete.update();
        cancelDelete.update();
    }

    @Override
    public void draw()
    {
        if (this.background != null)
        {
            this.background.draw();

            Drawing.drawing.drawPopup(this.centerX, this.centerY + this.objYSpace * posY,
                    Drawing.drawing.interfaceSizeX * 0.7, this.objYSpace * sY, 10, 10);
        }
        else
            this.drawDefaultBackground();

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Are you sure you want to delete the crusade?");

        confirmDelete.draw();
        cancelDelete.draw();
    }
}
