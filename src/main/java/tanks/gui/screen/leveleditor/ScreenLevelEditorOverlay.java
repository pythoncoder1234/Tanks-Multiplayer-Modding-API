package tanks.gui.screen.leveleditor;

import tanks.Game;
import tanks.editorselector.LevelEditorSelector;
import tanks.gui.screen.ILevelPreviewScreen;
import tanks.gui.screen.Screen;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public abstract class ScreenLevelEditorOverlay extends Screen implements ILevelPreviewScreen
{
    public Screen previous;
    public ScreenLevelEditor editor;
    public boolean musicInstruments = false;
    public boolean keepTitle = true;

    public ScreenLevelEditorOverlay(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        this.previous = previous;
        this.editor = screenLevelEditor;

        this.music = previous.music;
        this.musicID = previous.musicID;

        this.enableMargins = false;

        if (previous instanceof ScreenLevelEditorOverlay)
            this.musicInstruments = ((ScreenLevelEditorOverlay) previous).musicInstruments;
    }

    public void escape()
    {
        Game.screen = previous;

        if (previous instanceof ScreenLevelEditorOverlay)
            ((ScreenLevelEditorOverlay) previous).load();

        if (previous == editor)
        {
            if (editor.initialized)
            {
                if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
                    editor.mouseObstacle.forAllSelectors(LevelEditorSelector::load);
                else
                    editor.mouseTank.forAllSelectors(LevelEditorSelector::load);
            }

            OverlayObjectMenu.saveSelectors(editor);
            editor.clickCooldown = 20;
            editor.paused = false;
        }
    }

    public void load()
    {

    }

    @Override
    public void update()
    {
        this.editor.updateMusic(this.musicInstruments);

        if (Game.game.input.editorPause.isValid())
        {
            Game.game.input.editorPause.invalidate();
            this.escape();
        }

        if (Game.game.input.editorObjectMenu.isValid() && editor.objectMenu)
        {
            Game.game.input.editorObjectMenu.invalidate();
            Game.screen = editor;

            editor.clickCooldown = 20;
            editor.paused = false;
            OverlayObjectMenu.saveSelectors(editor);
        }

        allowClose = editor.undoActions.isEmpty() && !editor.modified;
    }

    @Override
    public void draw()
    {
        this.editor.draw();

        if (keepTitle)
            windowTitle = editor.windowTitle;
    }

    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return editor.spawns;
    }

    @Override
    public double getOffsetX()
    {
        return editor.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        return editor.getOffsetY();
    }

    @Override
    public double getScale()
    {
        return editor.getScale();
    }

    @Override
    public void onAttemptClose()
    {
        Game.screen = new OverlayConfirmSave(Game.screen, this.editor);
    }
}
