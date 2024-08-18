package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.gui.SpeedrunTimer;

public abstract class ScreenOptionsOverlay extends Screen implements IPartyGameScreen
{
    public ScreenGame game;
    public Screen prevScreen;
    public int brightness;

    public ScreenOptionsOverlay()
    {
        game = ScreenGame.getInstance();

        if (game != null)
        {
            this.music = game.music;
            this.musicID = game.musicID;
            this.brightness = Game.currentLevel != null && Level.isDark(true) ? 255 : 0;
            game.screenshotMode = true;
            game.paused = true;
        }
        else
        {
            this.music = "menu_options.ogg";
            this.musicID = "menu";
        }

        prevScreen = Game.screen;
    }

    @Override
    public void update()
    {
        if (game != null)
        {
            if (Game.game.input.pause.isValid())
            {
                Game.game.input.pause.invalidate();
                if (Game.game.window.shift && game != null)
                    prevScreen = game;

                Game.screen = prevScreen;

                if (prevScreen == game)
                {
                    game.screenshotMode = false;
                    Panel.panel.redrawOnChange = false;
                }
            }

            game.update();
            this.music = game.music;
            this.musicID = game.musicID;
        }

        if (Panel.selectedTextBox == null && game != null &&
                Game.game.window.textValidPressedKeys.contains(InputCodes.KEY_D) && Game.game.window.shift)
        {
            Game.game.window.textValidPressedKeys.remove((Integer) InputCodes.KEY_D);
            Game.screen = new ScreenDebug();
        }
    }

    @Override
    public void drawDefaultBackground(double size)
    {
        if (game != null)
        {
            game.paused = true;
            game.screenshotMode = true;
            game.draw();

            if (Game.showSpeedrunTimer && !(Game.currentGame != null && Game.currentGame.hideSpeedrunTimer))
                SpeedrunTimer.draw();

            Drawing.drawing.setColor(127, 178, 228, 64);
            Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);
        }
        else
            super.drawDefaultBackground(size);
    }
}
