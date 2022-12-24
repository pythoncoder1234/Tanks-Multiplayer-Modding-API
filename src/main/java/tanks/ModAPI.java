package tanks;

import basewindow.BaseFile;
import basewindow.BaseFontRenderer;
import basewindow.BaseShapeRenderer;
import tanks.event.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.menus.CustomShape;
import tanks.menus.FixedMenu;
import tanks.menus.FixedText;
import tanks.menus.TransitionEffect;
import tanks.modlevels.Arcade.java.ArcadeMain;
import tanks.modlevels.GameMap;
import tanks.network.NetworkEventMap;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModAPI
{
    /**
     * Stores registered mods
     */
    public static ArrayList<Class<? extends ModGame>> registeredCustomGames = new ArrayList<>();
    public static ArrayList<Class<? extends ModLevel>> registeredCustomLevels = new ArrayList<>();
    public static ArrayList<FixedMenu> menuGroup = new ArrayList<>();
    public static HashMap<Double, FixedMenu> ids = new HashMap<>();
    public static ArrayList<FixedMenu> removeMenus = new ArrayList<>();

    // Directions in radians
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
        registerGame(ArcadeMain.class);
        registerGame(GameMap.class);
    }

    public static void setUp()
    {
        registerMods();

        NetworkEventMap.register(EventAddCustomMovable.class);
        NetworkEventMap.register(EventAddCustomShape.class);
        NetworkEventMap.register(EventAddNPC.class);
        NetworkEventMap.register(EventAddNPCShopItem.class);
        NetworkEventMap.register(EventAddObstacle.class);
        NetworkEventMap.register(EventAddObstacleText.class);
        NetworkEventMap.register(EventAddScoreboard.class);
        NetworkEventMap.register(EventAddTransitionEffect.class);
        NetworkEventMap.register(EventChangeBackgroundColor.class);
        NetworkEventMap.register(EventChangeNPCMessage.class);
        NetworkEventMap.register(EventChangeScoreboardAttribute.class);
        NetworkEventMap.register(EventClearMenuGroup.class);
        NetworkEventMap.register(EventClearNPCShop.class);
        NetworkEventMap.register(EventCustomLevelEndCondition.class);
        NetworkEventMap.register(EventDisableMinimap.class);
        NetworkEventMap.register(EventDisplayText.class);
        NetworkEventMap.register(EventFillObstacle.class);
        NetworkEventMap.register(EventLoadMapLevel.class);
        NetworkEventMap.register(EventOverrideNPCState.class);
        NetworkEventMap.register(EventPurchaseNPCItem.class);
        NetworkEventMap.register(EventScoreboardUpdateScore.class);
        NetworkEventMap.register(EventSortNPCShopButtons.class);
        NetworkEventMap.register(EventSkipCountdown.class);

        fixedShapes = Game.game.window.shapeRenderer;
        fixedText = Game.game.window.fontRenderer;
    }

    public static void registerGame(Class<? extends ModGame> m)
    {
        registeredCustomGames.add(m);
    }

    public static void registerMod(Class<? extends ModLevel> m)
    {
        registeredCustomLevels.add(m);
    }

    /** Skips the countdown before a level starts. */
    public static void skipCountdown()
    {
        if (!(Game.screen instanceof ScreenGame))
            return;

        EventSkipCountdown e = new EventSkipCountdown();
        e.execute();
        Game.eventsOut.add(e);
    }

    /**
     * Returns the level or crusade string of a level file.
     */
    public static String getLevelString(String levelName) throws FileNotFoundException
    {
        return getLevelString(levelName, false);
    }

    /**
     * Returns (or prints) the level or crusade string of a level file.
     */
    public static String getLevelString(String levelName, boolean print) throws FileNotFoundException
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
                    System.out.print(line + "\\n");

                levelString.append(line).append("\n");
            }

            level.stopReading();

            return levelString.toString();
        }

        catch (IOException e)
        {
            throw new FileNotFoundException("Level \"" + levelName + "\" not found");
        }
    }

    public static void setLevelTimer(int minutes, int seconds)
    {
        setLevelTimer(minutes * 60 + seconds);
    }

    public static void setLevelTimer(int seconds)
    {
        if (Game.currentLevel == null)
        {
            System.err.println("WARNING: No level found");
            return;
        }

        EventSetLevelTimer e = new EventSetLevelTimer(seconds);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void setMusic(String music)
    {
        if (music.equals(Game.screen.music))
            return;

        EventSetMusic e = new EventSetMusic(music);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void sendChatMessage(String message)
    {
        if (!ScreenPartyHost.isServer)
            return;

        EventServerChat e = new EventServerChat(message);
        e.execute();
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
        Game.eventsOut.add(e);
    }

    /**
     * Function to add an object to the correct list
     * (<code>Game.obstacles</code>, <code>Game.movables</code>, etc.)
     *  */
    public static void addObject(Object o)
    {
        if (o instanceof Movable)
        {
            ((Movable) o).posX = ((Movable) o).posX * 50 + 25;
            Game.movables.add((Movable) o);

            if (o instanceof TankNPC)
                Game.eventsOut.add(new EventAddNPC((TankNPC) o));

            else if (o instanceof Tank)
                Game.eventsOut.add(new EventCreateCustomTank((Tank) o));

            else if (o instanceof CustomMovable)
                Game.eventsOut.add(new EventAddCustomMovable((CustomMovable) o));
        }
        else if (o instanceof Obstacle)
        {
            EventAddObstacle e = new EventAddObstacle((Obstacle) o);
            e.execute();
            Game.eventsOut.add(e);
        }
        else if (o instanceof FixedMenu)
            ModAPI.menuGroup.add((FixedMenu) o);

        else
            System.err.println("Warning: Invalid item given to ModAPI.addObject()");
    }

    public static void clearMenuGroup()
    {
        EventClearMenuGroup e = new EventClearMenuGroup();
        e.execute();
        Game.eventsOut.add(e);
    }

    /** Displays text on a player's screen, with custom positions. Works with multiplayer. */
    public static FixedText displayText(double x, double y, String text, Animations.Animation... animations)
    {
        int brightness = (Game.screen instanceof ScreenGame && !Level.isDark()) ? 255 : 0;
        return displayText(x, y, text, brightness, brightness, brightness, 24, animations);
    }

    /** Displays text on a player's screen, with custom positions. Works with multiplayer. */
    public static FixedText displayText(double x, double y, String text, double r, double g, double b, double fontSize, Animations.Animation... animations)
    {
        FixedText t = new FixedText(x, y, text, r, g, b, fontSize);

        if (!t.animations.isEmpty())
            t.animations = new ArrayList<>(List.of(animations));

        ModAPI.menuGroup.add(t);
        Game.eventsOut.add(new EventDisplayText(t));
        return t;
    }

    /** Displays text on a player's screen. Works with multiplayer. */
    public static FixedText displayText(FixedText.types location, String text, Animations.Animation... animations)
    {
        int brightness = (Game.screen instanceof ScreenGame && !Level.isDark()) ? 255 : 0;
        return displayText(location, text, false, 0, brightness, brightness, brightness, animations);
    }

    /** Displays text on a player's screen, with adjustable colors. Works with multiplayer. */
    public static FixedText displayText(FixedText.types location, String text, double r, double g, double b, Animations.Animation... animations)
    {
        return displayText(location, text, false, 0, r, g, b, animations);
    }

    /** Displays text on a player's screen, with adjustable display duration and colors. Works with multiplayer. */
    public static FixedText displayText(FixedText.types location, String text, int duration, Animations.Animation... animations)
    {
        int brightness = (Game.screen instanceof ScreenGame && !Level.isDark()) ? 255 : 0;

        FixedText t = new FixedText(location, text, duration, brightness, brightness, brightness);

        if (!t.animations.isEmpty())
            t.animations = new ArrayList<>(List.of(animations));

        ModAPI.menuGroup.add(t);
        Game.eventsOut.add(new EventDisplayText(t));
        return t;
    }

    /** Displays text on a player's screen, with adjustable display duration and colors. Works with multiplayer. */
    public static FixedText displayText(FixedText.types location, String text, int duration, double r, double g, double b, Animations.Animation... animations)
    {
        FixedText t = new FixedText(location, text, duration, r, g, b);

        if (!t.animations.isEmpty())
            t.animations = new ArrayList<>(List.of(animations));

        ModAPI.menuGroup.add(t);
        Game.eventsOut.add(new EventDisplayText(t));
        return t;
    }

    /**
     * Displays text on a player's screen. Works with multiplayer.
     * @param afterGameStarted Displays the text after the level starts.
     * @param duration The duration (in centiseconds) for the text to be displayed before fading out. Set to
     *                     0 for infinite duration.
     *  */
    public static FixedText displayText(FixedText.types location, String text, boolean afterGameStarted, int duration, double r, double g, double b, Animations.Animation... animations)
    {
        FixedText t = new FixedText(location, text, duration, r, g, b);

        if (!t.animations.isEmpty())
            t.animations = new ArrayList<>(List.of(animations));

        t.afterGameStarted = afterGameStarted;
        ModAPI.menuGroup.add(t);
        Game.eventsOut.add(new EventDisplayText(t));
        return t;
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

        for (int attemptNo = 0; attemptNo < 100; attemptNo++)
        {
            int spawnIndex = (int) (Math.random() * Game.currentLevel.playerSpawnsX.size());

            if (!Game.currentLevel.playerSpawnsTeam.get(spawnIndex).equals(t.team))
                continue;

            double x = Game.currentLevel.playerSpawnsX.get(spawnIndex);
            double y = Game.currentLevel.playerSpawnsY.get(spawnIndex);
            double angle = Game.currentLevel.playerSpawnsAngle.get(spawnIndex);

            TankPlayer p = new TankPlayer(x, y, angle);
            p.team = t.team;
            Game.movables.add(p);
            Game.playerTank = p;

            break;
        }
    }

    /** Converts a number to a string, and removes the <code>.0</code>.<br>
     * 100.0 -> 100, 100.1 -> 100.1
     * */
    public static String convertToString(double number)
    {
        if (number != (int) number)
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
        try
        {
            Obstacle o = Game.registryObstacle.getEntry(registryName).obstacle
                    .getConstructor(String.class, double.class, double.class)
                    .newInstance(registryName, x, y);
            o.stackHeight = stackHeight;
            o.startHeight = startHeight;

            ModAPI.addObject(o);

            if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
            {
                if (o.bulletCollision)
                    Game.game.solidGrid[x][y] = true;

                if (o.bulletCollision && !o.shouldShootThrough)
                    Game.game.unbreakableGrid[x][y] = true;

                Game.obstacleMap[x][y] = o;
            }
        }
        catch (Exception e)
        {
            System.err.println("Warning: Bad obstacle provided to setObstacle");
            e.printStackTrace();
        }
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
        Game.eventsOut.add(e);
    }

    /** Places a tree, centered on a specified tile.
     * @param height Height of the tree, leaves and all.
     * @param sizeFactor How big the tree will be.
     * */
    public static void placeTree(int x, int y, int height, int sizeFactor)
    {
        int endX = x + sizeFactor / 2;
        int endY = y + sizeFactor / 2;

        ModAPI.fillObstacle(x, y, endX, endY, "normal", height * sizeFactor - sizeFactor);

        for (int i = 0; i < sizeFactor; i++)
        {
            int placeX = x + i;
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
        Game.eventsOut.add(e);
        e.execute();
    }

    public static void addTransitionEffect(TransitionEffect.types type)
    {
        addTransitionEffect(type, 1, 0, 0, 0);
    }

    public static void addTransitionEffect(TransitionEffect.types type, int colR, int colG, int colB)
    {
        addTransitionEffect(type, 1, colR, colG, colB);
    }

    public static void addTransitionEffect(TransitionEffect.types type, float speed, int colR, int colG, int colB)
    {
        EventAddTransitionEffect e = new EventAddTransitionEffect(type, speed, colR, colG, colB);
        e.execute();
        Game.eventsOut.add(e);
    }

    /** Adds a shape (rectangle, oval) to the screen. Works in multiplayer. */
    public static void addCustomShape(boolean all, CustomShape.types type, int x, int y, int sizeX, int sizeY, int r, int g, int b)
    {
        addCustomShape(all, type, x, y, sizeX, sizeY, 0, r, g, b, 255);
    }

    /** Adds a shape (rectangle, oval) to the screen. Works in multiplayer. */
    public static void addCustomShape(boolean all, CustomShape.types type, int x, int y, int sizeX, int sizeY, int r, int g, int b, int a)
    {
        addCustomShape(all, type, x, y, sizeX, sizeY, 0, r, g, b, a);
    }

    /** Adds a shape (rectangle, oval) to the screen. Works in multiplayer. */
    public static void addCustomShape(boolean all, CustomShape.types type, int x, int y, int sizeX, int sizeY, int duration, int r, int g, int b, int a)
    {
        EventAddCustomShape e = new EventAddCustomShape(type, x, y, sizeX, sizeY, duration, r, g, b, a);
        e.execute();

        if (all)
            Game.eventsOut.add(e);
    }

    public static void loadLevel(Level l)
    {
        l.loadLevel();
        Game.screen = new ScreenGame();
    }

    public static void loadLevel(String levelString)
    {
        loadLevel(new Level(levelString));
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
        Game.eventsOut.add(e);
    }
}
