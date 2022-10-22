package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayLevelOptionsSize extends ScreenLevelEditorOverlay
{
    public TextBox sizeX;
    public TextBox sizeY;
    double resizeCounter = 0;

    public Button back3 = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2), this.objWidth, this.objHeight, "Back", this::escape);

    public OverlayLevelOptionsSize(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        sizeX = new TextBox(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Width", () ->
        {
            if (sizeX.inputText.length() <= 0)
                sizeX.inputText = screenLevelEditor.level.sizeX + "";
            else
            {
                screenLevelEditor.level.sizeX = Integer.parseInt(sizeX.inputText);
                Game.currentSizeX = screenLevelEditor.level.sizeX;
            }

            screenLevelEditor.level.reloadTiles();
            resizeCounter = 50;
        }
                , screenLevelEditor.level.sizeX + "");

        sizeX.allowLetters = false;
        sizeX.allowSpaces = false;
        sizeX.maxChars = 3;
        sizeX.maxValue = 400;
        sizeX.minValue = 1;
        sizeX.checkMaxValue = true;
        sizeX.checkMinValue = true;

        sizeY = new TextBox(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Height", () ->
        {
            if (sizeY.inputText.length() <= 0)
                sizeY.inputText = screenLevelEditor.level.sizeY + "";
            else
            {
                screenLevelEditor.level.sizeY = Integer.parseInt(sizeY.inputText);
                Game.currentSizeY = screenLevelEditor.level.sizeY;
            }

            screenLevelEditor.level.reloadTiles();
            resizeCounter = 50;
        }
                , screenLevelEditor.level.sizeY + "");

        sizeY.allowLetters = false;
        sizeY.allowSpaces = false;
        sizeY.maxChars = 3;
        sizeY.maxValue = 400;
        sizeY.minValue = 1;
        sizeY.checkMaxValue = true;
        sizeY.checkMinValue = true;
    }

    public void update()
    {
        this.sizeX.update();
        this.sizeY.update();
        this.back3.update();

        if (resizeCounter > 0)
            resizeCounter -= Panel.frameFrequency;
        else if (resizeCounter < 0) {
            Drawing.drawing.forceRedrawTerrain();
            resizeCounter = 0;
        }

        super.update();
    }

    public void draw()
    {
        super.draw();
        this.sizeY.draw();
        this.sizeX.draw();
        this.back3.draw();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Level size");
    }
}
