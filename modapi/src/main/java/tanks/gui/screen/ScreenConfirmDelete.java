package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenConfirmDelete extends Screen
{
    public String levelName;
    public Screen previous = Game.screen;

    public Button cancelDelete = new Button(this.centerX, (int) (this.centerY + this.objYSpace), this.objWidth, this.objHeight, "No", () -> Game.screen = previous);

    public Button confirmDelete = new Button(this.centerX, (int) (this.centerY), this.objWidth, this.objHeight, "Yes", () ->
    {
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName);

        Game.cleanUp();

        while (file.exists())
        {
            file.delete();
        }

        Game.screen = new ScreenSavedLevels();
    }
    );

    public ScreenConfirmDelete(String levelName)
    {
        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.levelName = levelName;
    }

    public void update()
    {
        this.cancelDelete.update();
        this.confirmDelete.update();
    }

    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Are you sure you want to delete the level?");

        this.cancelDelete.draw();
        this.confirmDelete.draw();
    }
}
