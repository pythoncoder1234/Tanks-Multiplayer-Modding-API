package tanks.gui.screen.leveleditor;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SpeedrunTimer;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenSavedLevels;

@SuppressWarnings("unchecked")
public class OverlayEditorMenu extends ScreenLevelEditorOverlay
{
    public boolean showTime = false;

    public Button resume = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Edit", this::escape);

    public Button play = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 2), this.objWidth, this.objHeight, "Play", () -> editor.play()
    );

    public Button playUnavailable = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 2), this.objWidth, this.objHeight, "Play", "You must add a player---spawn point to play!");

    public Button options = new Button(this.centerX, (int) (this.centerY + 0), this.objWidth, this.objHeight, "Options", () -> Game.screen = new OverlayLevelOptions(Game.screen, editor)
    );

    public Button quit = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2), this.objWidth, this.objHeight, "Exit", () ->
    {
        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
            editor.modified = true;

        editor.save();

        Game.cleanUp();
        Game.screen = new ScreenSavedLevels();
    },
            "Shift click to force save---with no tank references"
    );

    public Button delete = new Button(this.centerX, (int) (this.centerY + this.objYSpace), this.objWidth, this.objHeight, "Delete level", () -> Game.screen = new OverlayConfirmDelete(Game.screen, editor)
    );

    public OverlayEditorMenu(Screen previous, ScreenLevelEditor editor)
    {
        super(previous, editor);

        this.allowClose = false;

        if (!editor.level.editable)
        {
            play.posY += 60;
            delete.posY -= 60;
            quit.posY -= 60;
        }
    }

    public void update()
    {
        if (!editor.initialized)
            editor.initialize();

        if (editor.level.editable)
        {
            resume.update();
            options.update();
            super.update();
        }

        quit.enableHover = Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT);

        delete.update();
        quit.update();

        if (!editor.spawns.isEmpty())
            play.update();
        else
            playUnavailable.update();

        if (Game.game.input.editorPlay.isValid() && !editor.spawns.isEmpty())
        {
            editor.play();
            Game.game.input.play.invalidate();
        }
    }

    public void draw()
    {
        super.draw();

        if (editor.level.editable)
        {
            resume.draw();
            options.draw();
        }

        delete.draw();
        quit.draw();

        if (!editor.spawns.isEmpty())
            play.draw();
        else
            playUnavailable.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Level menu");

        if (Game.showSpeedrunTimer && showTime)
            SpeedrunTimer.draw();
    }
}
