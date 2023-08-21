package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.obstacle.Obstacle;

public class OverlayBlockHeight extends ScreenLevelEditorOverlay
{
    public Button increaseHeight = new Button(this.centerX + 100, this.centerY, 60, 60, "+", () -> editor.mouseObstacle.stackHeight += 0.5);

    public Button decreaseHeight = new Button(this.centerX - 100, this.centerY, 60, 60, "-", () -> editor.mouseObstacle.stackHeight -= 0.5);

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", this::escape);

    public Button staggering = new Button(this.centerX + 200, this.centerY, 60, 60, "", () ->
    {
        if (!editor.stagger)
        {
            editor.mouseObstacle.stackHeight = Math.max(editor.mouseObstacle.stackHeight, 1);
            editor.stagger = true;
        }
        else if (!editor.oddStagger)
        {
            editor.mouseObstacle.stackHeight = Math.max(editor.mouseObstacle.stackHeight, 1);
            editor.oddStagger = true;
        }
        else
        {
            editor.oddStagger = false;
            editor.stagger = false;
        }
    }, " --- "
    );

    public OverlayBlockHeight(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        staggering.imageSizeX = 40;
        staggering.imageSizeY = 40;
        staggering.fullInfo = true;
    }

    public void update()
    {
        this.increaseHeight.enabled = editor.mouseObstacle.stackHeight < Obstacle.default_max_height / (Game.debug ? 1 : 2.);
        this.decreaseHeight.enabled = editor.mouseObstacle.stackHeight > 0.5;

        if (editor.stagger)
            this.decreaseHeight.enabled = editor.mouseObstacle.stackHeight > 1;

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
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Block height");

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 500, 150);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(36);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, editor.mouseObstacle.stackHeight + "");

        this.increaseHeight.draw();
        this.decreaseHeight.draw();
        this.staggering.draw();

        this.back.draw();

        Drawing.drawing.setInterfaceFontSize(12);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(staggering.posX, staggering.posY - 40, "Staggering");
    }
}
