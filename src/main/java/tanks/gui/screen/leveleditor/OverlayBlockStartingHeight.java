package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.screen.Screen;

public class OverlayBlockStartingHeight extends ScreenLevelEditorOverlay
{
    public Button increaseHeight = new Button(this.centerX + 100, this.centerY, 60, 60, "+", () -> editor.mouseObstacleStartHeight += 0.5);

    public Button decreaseHeight = new Button(this.centerX - 100, this.centerY, 60, 60, "-", () -> editor.mouseObstacleStartHeight -= 0.5);

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", this::escape);

    public Button staggering = new Button(this.centerX + 200, this.centerY, 60, 60, "", () ->
    {
        if (!editor.stagger)
        {
            editor.mouseObstacleStartHeight = Math.max(editor.mouseObstacleStartHeight, 0.5);
            editor.stagger = true;
        }
        else if (!editor.oddStagger)
        {
            editor.mouseObstacleStartHeight = Math.max(editor.mouseObstacleStartHeight, 0.5);
            editor.oddStagger = true;
        }
        else
        {
            editor.oddStagger = false;
            editor.stagger = false;
        }
    }, " --- "
    );

    public OverlayBlockStartingHeight(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        staggering.imageSizeX = 40;
        staggering.imageSizeY = 40;
        staggering.fullInfo = true;

        increaseHeight.textOffsetX = 1.5;
        increaseHeight.textOffsetY = 1.5;

        decreaseHeight.textOffsetX = 1.5;
        decreaseHeight.textOffsetY = 1.5;
    }

    public void update()
    {
        this.increaseHeight.enabled = editor.mouseObstacleStartHeight < 100;
        this.decreaseHeight.enabled = editor.mouseObstacleStartHeight > -1;

        if (editor.stagger)
            this.decreaseHeight.enabled = editor.mouseObstacleStartHeight > -0.5;

        this.increaseHeight.update();
        this.decreaseHeight.update();
        this.staggering.update();

        if (!editor.stagger)
        {
            this.staggering.image = "icons/nostagger.png";
            this.staggering.setHoverText("Blocks will all be placed---with the same height");
        }
        else if (editor.oddStagger)
        {
            this.staggering.image = "icons/oddstagger.png";
            this.staggering.setHoverText("Every other block on the grid---will be half a block shorter");
        }
        else
        {
            this.staggering.image = "icons/evenstagger.png";
            this.staggering.setHoverText("Every other block on the grid---will be half a block shorter");
        }

        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();
        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Starting Height");

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 500, 150);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(36);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, editor.mouseObstacleStartHeight + "");

        this.increaseHeight.draw();
        this.decreaseHeight.draw();
        this.staggering.draw();

        this.back.draw();

        Drawing.drawing.setInterfaceFontSize(12);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(staggering.posX, staggering.posY - 40, "Staggering");
    }
}
