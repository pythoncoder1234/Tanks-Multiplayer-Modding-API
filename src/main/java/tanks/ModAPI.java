package tanks;

import basewindow.BaseFile;
import basewindow.BaseFontRenderer;
import basewindow.BaseShapeRenderer;
import tanks.gui.menus.FixedMenu;
import tanks.gui.menus.FixedText;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.minigames.TeamDeathmatch;
import tanks.network.EventMinigameStart;
import tanks.network.NetworkEventMap;
import tanks.network.event.*;
import tanks.tank.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ModAPI
{
    public static boolean sendEvents = true;
    public static ArrayList<FixedMenu> fixedMenus = new ArrayList<>();
    public static HashMap<Double, FixedMenu> ids = new HashMap<>();
    public static ArrayList<FixedMenu> removeMenus = new ArrayList<>();

    // Directions in radians in terms of tank
    public static final double up = Math.toRadians(-90);
    public static final double down = Math.toRadians(90);
    public static final double left = Math.toRadians(180);
    public static final double right = Math.toRadians(0);

    /**
     * To add a new mod, add {@code registerMod(yourMod.class)} to this function. Of course, type the name of your mod instead of "yourMod".<br><br>
     * You can also use other functions here, like the {@code ModAPI.printLevelString(levelPath)) function.<br><br>
     * However, keep in mind that this will only be called on the first frame of the game launch.
     */
    public static void registerMods()
    {
        Game.registerMinigame(TeamDeathmatch.class);
        Game.registerMinigame(Minecraft.class);
//        Game.registerMinigame(GameMap.class);
    }

    public static void setUp()
    {
        registerMods();

        NetworkEventMap.register(EventAddCustomMovable.class);
        NetworkEventMap.register(EventAddCustomShape.class);
        NetworkEventMap.register(EventCreateTankNPC.class);
        NetworkEventMap.register(EventAddNPCShopItem.class);
        NetworkEventMap.register(EventAddObstacle.class);
        NetworkEventMap.register(EventAddObstacleText.class);
        NetworkEventMap.register(EventCreateScoreboard.class);
        NetworkEventMap.register(EventChangeBackgroundColor.class);
        NetworkEventMap.register(EventChangeNPCMessage.class);
        NetworkEventMap.register(EventClearMenuGroup.class);
        NetworkEventMap.register(EventClearMovables.class);
        NetworkEventMap.register(EventClearNPCShop.class);
        NetworkEventMap.register(EventCustomLevelEndCondition.class);
        NetworkEventMap.register(EventDisableMinimap.class);
        NetworkEventMap.register(EventDisplayText.class);
        NetworkEventMap.register(EventMinigameStart.class);
        NetworkEventMap.register(EventLoadMapLevel.class);
        NetworkEventMap.register(EventOverrideNPCState.class);
        NetworkEventMap.register(EventPurchaseNPCItem.class);
        NetworkEventMap.register(EventSetHotbar.class);
        NetworkEventMap.register(EventSetObstacle.class);
        NetworkEventMap.register(EventScoreboardUpdateScore.class);
        NetworkEventMap.register(EventSortNPCShopButtons.class);
        NetworkEventMap.register(EventSkipCountdown.class);

        fixedShapes = Game.game.window.shapeRenderer;
        fixedText = Game.game.window.fontRenderer;
    }

    /** Skips the countdown before a level starts. */
    public static void skipCountdown()
    {
        if (!(Game.screen instanceof ScreenGame))
            return;

        EventSkipCountdown e = new EventSkipCountdown();
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    /**
     * Returns the level or crusade string of a level file.
     */
    public static String getLevelString(String levelName)
    {
        return getLevelString(levelName, false);
    }

    /**
     * Returns (or prints) the level or crusade string of a level file.
     */
    public static String getLevelString(String levelName, boolean print)
    {
        StringBuilder levelString = new StringBuilder();

        BaseFile level = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName.replace(' ', '_') + ".tanks");
        try
        {
            level.startReading();

            while (level.hasNextLine())
            {
                String line = level.nextLine();

                if (print)
                    System.out.println(line);

                levelString.append(line).append("\n");
            }

            level.stopReading();

            return levelString.toString();
        }

        catch (IOException e)
        {
            throw new RuntimeException("Level \"" + levelName + "\" not found");
        }
    }


    /**
     * Coordinates for top left or top right corner (to put gui elements)
     */
    public static double[] topCoords(boolean left)
    {
        double[] output = new double[2];
        output[0] = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2 + Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50;
        output[1] = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;

        if (!Game.showSpeedrunTimer || (Game.currentGame != null && Game.currentGame.hideSpeedrunTimer))
            output[1] -= 30;

        if (!left)
        {
            output[0] = Game.game.window.absoluteWidth - output[0];
            output[1] = Game.game.window.absoluteHeight - output[1];
        }

        return output;
    }

    public static void setLevelTimer(int minutes, int seconds)
    {
        setLevelTimer(minutes * 60 + seconds);
    }

    /**
     * Set to 0 to clear the level timer
     */
    public static void setLevelTimer(int seconds)
    {
        if (Game.currentLevel == null)
        {
            System.err.println("WARNING: No level found");
            return;
        }

        EventSetLevelTimer e = new EventSetLevelTimer(seconds);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    public static void setScreenMusic(String music)
    {
        if (music.equals(Game.screen.music))
            return;

        EventSetMusic e = new EventSetMusic(music);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    public static void setScreenMusic(MusicState state)
    {
        if (Objects.equals(state.music, Game.screen.music) && !(Game.screen instanceof ScreenGame && !Objects.equals(state.intro, ((ScreenGame) Game.screen).introMusic)))
            return;

        EventSetMusic e = new EventSetMusic(state);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    public static void sendChatMessage(String message)
    {
        if (!ScreenPartyHost.isServer)
            return;

        EventServerChat e = new EventServerChat(message);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    public static void sendChatMessage(String message, int colorR, int colorG, int colorB)
    {
        sendChatMessage(message, colorR, colorG, colorB, (int) Turret.calculateSecondaryColor(colorR), (int) Turret.calculateSecondaryColor(colorG), (int) Turret.calculateSecondaryColor(colorB));
    }

    public static void sendChatMessage(String message, int r1, int g1, int b1, int r2, int g2, int b2)
    {
        if (!ScreenPartyHost.isServer)
            return;

        EventServerChat e = new EventServerChat(message, r1, g1, b1, r2, g2, b2);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    public static void clearMenuGroup()
    {
        EventClearMenuGroup e = new EventClearMenuGroup();
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    public static void displayText(FixedText text)
    {
        ModAPI.fixedMenus.add(text);

        if (sendEvents)
            Game.eventsOut.add(new EventDisplayText(text));
    }


    /** Draws a tank model at a position on the screen. */
    public static void drawTank(double x, double y, double size, double angle, double r1, double g1, double b1, double r2, double g2, double b2)
    {
        Drawing.drawing.setColor(r2, g2, b2);
        TankModels.tank.base.draw(x, y, size, size, angle);

        Drawing.drawing.setColor(r1, g1, b1);
        TankModels.tank.color.draw(x, y, size, size, angle);

        Drawing.drawing.setColor(r2, g2, b2);
        TankModels.tank.turret.draw(x, y, size, size, angle);

        Drawing.drawing.setColor((r1 + r2) / 2, (g1 + g2) / 2, (b1 + b2) / 2);
        TankModels.tank.turretBase.draw(x, y, size, size, angle);
    }

    /**
     * Tests if any Tank is within range of an area (in tiles)
     *
     * @return an ArrayList of Tanks
     */
    public static ArrayList<Tank> withinRange(double x, double y, double size)
    {
        ArrayList<Tank> output = new ArrayList<>();

        for (Movable m : Game.movables)
        {
            if (m instanceof Tank)
            {
                Tank t = (Tank) m;

                if (Math.pow(t.posX - (x * 50 + 25), 2) + Math.pow(t.posY - (y * 50 + 25), 2) <= (size * 50) * (size * 50))
                    output.add(t);
            }
        }

        return output;
    }

    /**
     *  <b>WIP Function</b><br>
     *  Respawns a player in one of its team's spawn points.
     *  */
    public static void respawnPlayer(Player p)
    {
        respawnPlayer(p.tank);
    }

    /**
     *  <b>WIP Function</b><br>
     *  Respawns a player in one of its team's spawn points.
     *  */
    public static void respawnPlayer(Tank t)
    {
        Game.removeMovables.add(t);

        for (int attemptNo = 0; attemptNo < 10; attemptNo++)
        {
            int spawnIndex = (int) (Math.random() * Game.currentLevel.playerSpawnsX.size());

            if (!Game.currentLevel.playerSpawnsTeam.get(spawnIndex).equals(t.team))
                continue;

            double x = Game.currentLevel.playerSpawnsX.get(spawnIndex);
            double y = Game.currentLevel.playerSpawnsY.get(spawnIndex);
            double angle = Game.currentLevel.playerSpawnsAngle.get(spawnIndex);

            TankPlayer p = new TankPlayer(x, y, angle);
            p.team = t.team;
            Game.movables.add(new Crate(p));
            Game.playerTank = p;

            break;
        }
    }

    public static Crate spawnTankCrate(Tank t)
    {
        Crate c = new Crate(t);
        Game.movables.add(c);
        return c;
    }

    /**
     * Converts a number to a string, and removes the <code>.0</code>.<br>
     * 100.0 -> 100, 100.1 -> 100.1
     */
    public static String convertToString(double number)
    {
        if (number % 1 != 0)
            return "" + number;
        else
            return "" + (int) number;
    }

    public static String capitalize(String s)
    {
        if (s.length() == 0)
            throw new RuntimeException("Capitalizing string without letters in it or of size 0");

        if (!(Game.lessThan('A', s.charAt(1), 'Z') || Game.lessThan('a', s.charAt(1), 'z')))
            return s.charAt(0) + capitalize(s.substring(1));

        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // Drawing functions added in this mod api

    /**
     * Abbreviations of renderers to draw fixed stuff
     */
    public static BaseShapeRenderer fixedShapes;
    public static BaseFontRenderer fixedText;

    /** Places an obstacle on a tile. */
    public static void setObstacle(int x, int y, String registryName)
    {
        setObstacle(x, y, registryName, 1, 0);
    }

    /** Places an obstacle on a tile. */
    public static void setObstacle(int x, int y, String registryName, double stackHeight)
    {
        setObstacle(x, y, registryName, stackHeight, 0);
    }

    /** Places an obstacle on a tile. */
    public static void setObstacle(int x, int y, String registryName, double stackHeight, double startHeight)
    {
        EventSetObstacle e = new EventSetObstacle(x, y, registryName, stackHeight, startHeight);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    /** Fills a rectangular area with obstacles. */
    public static void fillObstacle(int startX, int startY, int endX, int endY, String registryName)
    {
        fillObstacle(startX, startY, endX, endY, registryName, 1, 0);
    }

    /** Fills a rectangular area with obstacles. */
    public static void fillObstacle(int startX, int startY, int endX, int endY, String registryName, double stackHeight)
    {
        fillObstacle(startX, startY, endX, endY, registryName, stackHeight, 0);
    }

    /** Fills a rectangular area with obstacles. */
    public static void fillObstacle(int startX, int startY, int endX, int endY, String registryName, double stackHeight, double startHeight)
    {
        EventFillObstacle e = new EventFillObstacle(startX, startY, endX, endY, registryName, stackHeight, startHeight);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    /** Places a tree, centered on a specified tile.
     * @param height Height of the tree, including leaves
     * @param sizeFactor How big the tree will be.
     * */
    public static void placeTree(int x, int y, int height, double sizeFactor)
    {
        if (sizeFactor > 3)
        {
            System.err.println("Warning (ModAPI.placeTree): Size Factor greater than 3, canceling");
            return;
        }

        ModAPI.fillObstacle((int) Math.round(x - sizeFactor / 4), (int) Math.round(y - sizeFactor / 4), (int) Math.round(x + sizeFactor / 4), (int) Math.round(y + sizeFactor / 4), "normal", height * sizeFactor);

        sizeFactor *= 4;
        x -= sizeFactor / 2;
        y -= sizeFactor / 2;

        for (int i = 0; i < sizeFactor / 2; i++)
        {
            for (int j = 0; j < sizeFactor / 4; j++)
                ModAPI.fillObstacle(x + i + j, y + i + j, (int) (x + sizeFactor - i - j), (int) (y + sizeFactor - i - j), "shrub", sizeFactor, height + i + j * (sizeFactor / 4));
        }
    }

    /** Places an {@link tanks.obstacle.ObstacleText ObstacleText} at the specified location. */
    public static void addTextObstacle(double x, double y, String text)
    {
        addTextObstacle(x, y, text, 0);
    }

    /**
     * Places an {@link tanks.obstacle.ObstacleText ObstacleText} at the specified location.
     *
     * @param duration The duration of time before the ObstacleText it gets removed.
     */
    public static void addTextObstacle(double x, double y, String text, long duration)
    {
        EventAddObstacleText e = new EventAddObstacleText((int) (Math.random() * Integer.MAX_VALUE), text, x, y, Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, duration);

        if (sendEvents)
            Game.eventsOut.add(e);
        e.execute();
    }

    public static void loadLevel(Level l, boolean forceNotRemote)
    {
        l.loadLevel(!forceNotRemote && ScreenPartyLobby.isClient);
        Game.screen = new ScreenGame();
    }

    public static void loadLevel(Level l)
    {
        loadLevel(l, false);
    }

    public static Level loadLevel(String levelString)
    {
        Level l = new Level(levelString);
        loadLevel(l, false);

        if (Game.currentGame != null)
            Game.currentGame.level = l;

        return l;
    }

    /** Change the color of a level background. */
    public static void changeBackgroundColor(int r, int g, int b)
    {
        changeBackgroundColor(r, g, b, -1, -1, -1);
    }

    public static void changeBackgroundColor(int r, int g, int b, int noiseR, int noiseG, int noiseB)
    {
        EventChangeBackgroundColor e = new EventChangeBackgroundColor(r, g, b, noiseR, noiseG, noiseB);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }
}
