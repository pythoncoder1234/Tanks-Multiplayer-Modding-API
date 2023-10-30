package tanks.gui.screen.leveleditor;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayLevelOptions extends ScreenLevelEditorOverlay
{
    public TextBox levelName;

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Back", this::escape);

    public Button crusadeImport = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Import from crusade", () -> Game.screen = new OverlayCrusadeImport(Game.screen, this.editor));

    public Button undoImport = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Undo import", () -> Game.screen = new OverlayCrusadeImport(Game.screen, this.editor, true));

    public Button colorOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Background colors", () -> Game.screen = new OverlayLevelOptionsColor(Game.screen, editor));

    public Button sizeOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 1, this.objWidth, this.objHeight, "Level size", () -> Game.screen = new OverlayLevelOptionsSize(Game.screen, editor));

    public Button timerOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Time limit", () -> Game.screen = new OverlayLevelOptionsTimer(Game.screen, editor));

    public Button lightingOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Lighting", () -> Game.screen = new OverlayLevelOptionsLighting(Game.screen, editor));

    public Button teamsOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Teams", () -> Game.screen = new OverlayLevelOptionsTeams(Game.screen, editor));

    public Button itemOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Items", () -> Game.screen = new OverlayLevelOptionsItems(Game.screen, editor));

    public OverlayLevelOptions(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        levelName = new TextBox(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Level name", () ->
        {
            BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + screenLevelEditor.name);

            String input = levelName.inputText.replace(" ", "_");
            if (!levelName.inputText.isEmpty() && !Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + input + ".tanks").exists())
            {
                if (file.exists())
                    file.renameTo(Game.homedir + Game.levelDir + "/" + input + ".tanks");

                while (file.exists())
                    file.delete();

                screenLevelEditor.name = input + ".tanks";

                //screenLevelBuilder.reload(false);
            }
            else
                levelName.inputText = screenLevelEditor.name.split("\\.")[0].replace("_", " ");

        },  screenLevelEditor.name.split("\\.")[0].replace("_", " "));

        levelName.maxChars = 35;
        levelName.enableCaps = true;
        screenLevelEditor.modified = true;

        lightingOptions.enabled = Game.framework != Game.Framework.libgdx;
    }

    public void update()
    {
        this.levelName.update();
        this.crusadeImport.update();
        this.undoImport.update();
        this.back.update();

        this.sizeOptions.update();
        this.colorOptions.update();
        this.lightingOptions.update();

        this.teamsOptions.update();
        this.itemOptions.update();
        this.timerOptions.update();

        if (Game.game.input.editorCopy.isValid() && Game.game.window.shift)
        {
            Game.game.input.editorCopy.invalidate();
            Game.game.window.setClipboard(this.editor.level.levelString);
            Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
        }

        super.update();
    }

    public void draw()
    {
        super.draw();

        this.levelName.draw();
        this.crusadeImport.draw();
        this.undoImport.draw();
        this.back.draw();

        this.timerOptions.draw();
        this.itemOptions.draw();
        this.teamsOptions.draw();

        this.lightingOptions.draw();
        this.colorOptions.draw();
        this.sizeOptions.draw();

        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Level options");
    }
}
