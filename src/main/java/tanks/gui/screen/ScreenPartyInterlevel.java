package tanks.gui.screen;

import tanks.*;
import tanks.generator.LevelGeneratorVersus;
import tanks.gui.Button;
import tanks.gui.Firework;
import tanks.gui.SpeedrunTimer;

import java.util.ArrayList;

public class ScreenPartyInterlevel extends Screen implements IDarkScreen
{
    boolean odd = false;

    ArrayList<Firework> fireworks1 = new ArrayList<>();
    ArrayList<Firework> fireworks2 = new ArrayList<>();

    Button newLevel = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Generate a new level", () ->
    {
        if (ScreenGame.versus)
        {
            Game.cleanUp();
            new Level(LevelGeneratorVersus.generateLevelString()).loadLevel();
            Game.screen = new ScreenGame();
        }
        else
        {
            Game.cleanUp();
            Game.loadRandomLevel();
            Game.screen = new ScreenGame();
        }
    }
    );

    Button replay = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Replay the level", () ->
    {
        Level level = new Level(Game.currentLevelString);
        level.loadLevel();
        Game.screen = new ScreenGame();
    }
    );

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Back to party", () ->
    {
        Game.resetTiles();
        Game.screen = ScreenPartyHost.activeScreen;
        ScreenGame.versus = false;
        ScreenInterlevel.fromSavedLevels = false;
        ScreenInterlevel.fromMinigames = false;
    }
    );

    Button replayHigherPos = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Replay the level", () ->
    {
        Level level = new Level(Game.currentLevelString);
        level.loadLevel();
        Game.screen = new ScreenGame();
    }
    );

    Button replayMinigame = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Play again", () ->
    {
        ScreenInterlevel.fromMinigames = false;
        try
        {
            assert Game.currentGame != null;
            Game.currentGame = Game.currentGame.getClass().getConstructor().newInstance();
            Game.currentGame.start();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e.getCause());
        }
    });

    Button quitHigherPos = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Back to party", () ->
    {
        Game.resetTiles();
        Game.screen = ScreenPartyHost.activeScreen;
        ScreenGame.versus = false;
        ScreenInterlevel.fromSavedLevels = false;
        ScreenInterlevel.fromMinigames = false;
    }
    );

    Button next = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Continue", () ->
    {
        Game.resetTiles();
        Game.screen = new ScreenPartyLobby();
        ScreenGame.versus = false;
    }
    );

    Button save = new Button(0, 0, this.objHeight * 1.5, this.objHeight * 1.5, "", () ->
    {
        ScreenSaveLevel sc = new ScreenSaveLevel(System.currentTimeMillis() + "", Game.currentLevelString, Game.screen);
        Level lev = new Level(Game.currentLevelString);
        lev.preview = true;
        lev.loadLevel(sc);
        Game.screen = sc;

        sc.fromInterlevel = true;
        sc.music = music;
        sc.musicID = musicID;
        sc.updateDownloadButton();
    }
    );

    public ScreenPartyInterlevel()
    {
        Game.player.hotbar.percentHidden = 100;

        if (Panel.win)
        {
            //Drawing.drawing.playSound("win.ogg");
            this.music = "win_music.ogg";
        }
        else
        {
            //Drawing.drawing.playSound("lose.ogg");
            this.music = "lose_music.ogg";
        }

        if (Panel.win && Game.effectsEnabled)
        {
            for (int i = 0; i < 5; i++)
            {
                Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, getFireworkArray());
                f.setRandomColor();
                f.setVelocity();
                getFireworkArray().add(f);
            }
        }

        Game.reset();

        save.posX = Drawing.drawing.interfaceSizeX - Drawing.drawing.interfaceScaleZoom * 40;
        save.posY = Drawing.drawing.interfaceSizeY - 50 - Drawing.drawing.interfaceScaleZoom * 40;
        save.image = "icons/save.png";

        save.imageSizeX = this.objHeight;
        save.imageSizeY = this.objHeight;
    }


    @Override
    public void update()
    {
        if (Panel.win && Game.effectsEnabled)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (ScreenPartyLobby.isClient)
        {
            next.update();
        }
        else if (ScreenInterlevel.fromSavedLevels)
        {
            quitHigherPos.update();
            replayHigherPos.update();
        }
        else if (ScreenInterlevel.fromMinigames)
        {
            replayMinigame.update();
            quitHigherPos.update();
        }
        else
        {
            quit.update();
            replay.update();
            newLevel.update();
        }

        if (!ScreenInterlevel.fromMinigames)
            save.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (Panel.win && Game.effectsEnabled && !Game.game.window.drawingShadow)
        {
            ArrayList<Firework> fireworks = getFireworkArray();
            if (Math.random() < Panel.frameFrequency * ScreenInterlevel.firework_frequency * Game.effectMultiplier)
            {
                Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, fireworks);
                f.setRandomColor();
                f.setVelocity();
                getFireworkArray().add(f);
            }

            for (int i = 0; i < getFireworkArray().size(); i++)
            {
                fireworks.get(i).drawUpdate(fireworks, getOtherFireworkArray());
            }

            if (Game.glowEnabled)
            {
                for (int i = 0; i < getFireworkArray().size(); i++)
                {
                    fireworks.get(i).drawGlow();
                }
            }

            //A fix to some glitchiness on ios
            Drawing.drawing.setColor(0, 0, 0, 0);
            Drawing.drawing.fillInterfaceRect(0, 0, 0, 0);

            fireworks.clear();
            odd = !odd;
        }

        if (Panel.win && Game.effectsEnabled)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (ScreenPartyLobby.isClient)
        {
            next.draw();
        }
        else if (ScreenInterlevel.fromSavedLevels)
        {
            quitHigherPos.draw();
            replayHigherPos.draw();
        }
        else if (ScreenInterlevel.fromMinigames)
        {
            quitHigherPos.draw();
            replayMinigame.draw();
        }
        else
        {
            quit.draw();
            replay.draw();
            newLevel.draw();
        }

        if (Game.showSpeedrunTimer)
            SpeedrunTimer.draw();

        if ((Panel.win && Game.effectsEnabled) || Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, Panel.winlose);

        if (!ScreenInterlevel.fromMinigames)
           save.draw();
    }

    public ArrayList<Firework> getFireworkArray()
    {
        if (odd)
            return fireworks2;
        else
            return fireworks1;
    }

    public ArrayList<Firework> getOtherFireworkArray()
    {
        if (odd)
            return fireworks1;
        else
            return fireworks2;
    }
}
