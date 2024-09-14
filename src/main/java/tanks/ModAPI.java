package tanks;

import basewindow.BaseFontRenderer;
import basewindow.BaseShapeRenderer;
import tanks.gui.menus.FixedMenu;
import tanks.gui.menus.FixedText;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.minigames.TeamDeathmatch;
import tanks.network.NetworkEventMap;
import tanks.network.event.*;
import tanks.tank.*;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ModAPI
{
    public static String version = "Mod API v1.2.7";
    public static boolean autoLoadExtensions = false;
    public static boolean sendEvents = true;
    static ArrayList<Runnable> resetFunc = new ArrayList<>();

    public static ArrayList<FixedMenu> fixedMenus = new ArrayList<>();
    public static HashMap<Double, FixedMenu> ids = new HashMap<>();
    public static ArrayList<FixedMenu> removeMenus = new ArrayList<>();

    // Directions in radians in terms of tank (the model is rotated 90 degrees)
    public static final double up = Math.toRadians(-90);
    public static final double down = Math.toRadians(90);
    public static final double left = Math.toRadians(180);
    public static final double right = Math.toRadians(0);

    /**
     * To add a new mod, add {@code Game.registerMinigame(yourMod.class)} to this function.<br><br>
     * You can also use other functions here, like the {@code ModAPI.printLevelString(levelPath)) function.}<br><br>
     * However, keep in mind that this will only be called on the first frame of the game launch.
     */
    public static void registerGames()
    {
        Game.registerMinigame(TeamDeathmatch.class);
//        Game.registerMinigame(GameMap.class);
    }

    public static void setUp()
    {
        registerGames();

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
        NetworkEventMap.register(EventClearNPCShop.class);
        NetworkEventMap.register(EventCustomLevelEndCondition.class);
        NetworkEventMap.register(EventDisableMinimap.class);
        NetworkEventMap.register(EventDisplayText.class);
        NetworkEventMap.register(EventMinigameStart.class);
        NetworkEventMap.register(EventOverrideNPCState.class);
        NetworkEventMap.register(EventPurchaseNPCItem.class);
        NetworkEventMap.register(EventSetHotbar.class);
        NetworkEventMap.register(EventSetObstacle.class);
        NetworkEventMap.register(EventSetMusic.class);
        NetworkEventMap.register(EventServerChat.class);
        NetworkEventMap.register(EventSyncField.class);
        NetworkEventMap.register(EventUpdateScoreboard.class);
        NetworkEventMap.register(EventSortNPCShopButtons.class);
        NetworkEventMap.register(EventSkipCountdown.class);
        NetworkEventMap.register(EventUpdateLevelTime.class);
        NetworkEventMap.register(EventSwapItemBarSlot.class);
        NetworkEventMap.register(EventDropItem.class);

        fixedShapes = Game.game.window.shapeRenderer;
        fixedText = Game.game.window.fontRenderer;
    }

    public static void registerResetFunction(Runnable r)
    {
        resetFunc.add(r);
    }

    /**
     * Skips the countdown before a level starts.
     */
    public static void skipCountdown()
    {
        if (ScreenGame.getInstance() == null)
            return;

        EventSkipCountdown e = new EventSkipCountdown();
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    /**
     * Returns the level string of a level file.
     */
    public static String getLevelString(String levelName)
    {
        return getInternalLevelString(Game.homedir + Game.levelDir + "/" + levelName.replace(' ', '_') + ".tanks", false);
    }

    /**
     * Returns the level or crusade string of a .tanks file inside the project directory.
     */
    public static String getInternalLevelString(String filePath)
    {
        return getInternalLevelString(filePath, false);
    }

    /**
     * Returns (and prints, if the <code>print</code> parameter is set to <code>true</code>) the level or crusade string of a .tanks file.
     */
    public static String getInternalLevelString(String filePath, boolean print)
    {
        String s = String.join("\n", Game.game.fileManager.getInternalFileContents(filePath));
        if (print) System.out.println(s);
        return s;
    }

    /**
     * Respawns a player in one of its team's spawn points.
     * Stolen from aehmttw's arcade code.
     */
    public static void respawnPlayer(Player p)
    {
        if (ScreenPartyLobby.isClient)
            return;

        Game.removeMovables.add(p.tank);

        Level l = Game.currentLevel;
        int r;
        if (!l.availablePlayerSpawns.isEmpty())
            r = l.availablePlayerSpawns.remove((int) (Level.random.nextDouble() * l.availablePlayerSpawns.size()));
        else
            r = (int) (l.playerSpawnsX.size() * Level.random.nextDouble());

        TankPlayer t = new TankPlayer(l.playerSpawnsX.get(r), l.playerSpawnsY.get(r), l.playerSpawnsAngle.get(r));
        t.team = Game.playerTeamNoFF;
        t.player = p;
        t.colorR = p.colorR;
        t.colorG = p.colorG;
        t.colorB = p.colorB;
        t.secondaryColorR = p.turretColorR;
        t.secondaryColorG = p.turretColorG;
        t.secondaryColorB = p.turretColorB;
        Game.movables.add(new Crate(t));
        Game.eventsOut.add(new EventAirdropTank(t));
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
        ScreenGame g = ScreenGame.getInstance();
        if (Objects.equals(state.music, Game.screen.music) && !(g != null && !Objects.equals(state.intro, g.introMusic)))
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


    /**
     * Draws a tank model at a position on the screen.
     */
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

    public static double distanceBetween(double x, double y, Tank t)
    {
        return Math.sqrt((t.posX - x) * (t.posX - x) + (t.posY - y) * (t.posY - y));
    }

    @Deprecated
    public static ArrayList<Tank> withinRange(double x, double y, double radius)
    {
        return withinRange(x, y, radius, true).stream().map(Map.Entry::getKey).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Tests if any {@link Tank} is within the radius of an area (in pixels or tiles,
     * which is determined by the <code>isTileCoords</code> parameter).
     * Sorted in ascending order, by distance.
     *
     * @param isTileCoords Whether the radius is in pixels or game tiles, which are 50 pixels by 50 pixels.
     * @return A list of map entries. The key returns the <code>Tank</code>,
     * and the value returns the distance from the location of the xy coordinates.
     * @see Map.Entry#getKey()
     * @see Map.Entry#getValue()
     */
    public static List<Map.Entry<Tank, Double>> withinRange(double x, double y, double radius, boolean isTileCoords)
    {
        if (isTileCoords)
        {
            x *= 50;
            y *= 50;
            radius *= 50;
        }

        return withinRange(x, y, radius, SortOrder.ascending);
    }

    /**
     * Tests if any {@link Tank} is within the radius of an area (in pixels).
     * Sorted by distance, and sorting order is determined by the <code>sortOrder</code> parameter.
     *
     * @return A list of map entries. The key returns the <code>Tank</code>,
     * and the value returns the distance from the location of the xy coordinates.
     * @see Map.Entry#getKey()
     * @see Map.Entry#getValue()
     */
    public static List<Map.Entry<Tank, Double>> withinRange(double x, double y, double radius, SortOrder sortOrder)
    {
        LinkedHashMap<Tank, Double> tanks = new LinkedHashMap<>();

        for (Movable m : Game.movables)
        {
            if (m instanceof Tank t)
            {
                double distance = (t.posX - x) * (t.posX - x) + (t.posY - y) * (t.posY - y);

                if (distance <= radius * radius)
                    tanks.put(t, distance);
            }
        }

        if (sortOrder != SortOrder.none)
        {
            Comparator<Map.Entry<Tank, Double>> comparator = Map.Entry.comparingByValue();
            if (sortOrder == SortOrder.descending)
                comparator = comparator.reversed();

            return tanks.entrySet().stream().sorted(comparator).collect(Collectors.toList());
        }
        return new ArrayList<>(tanks.entrySet());
    }

    /**
     * Load a level from a level string.
     *
     * @see #getInternalLevelString(String) ModAPI.getInternalLevelString(filePath)
     */
    public static Level loadLevel(String levelString)
    {
        Level l = new Level(levelString);
        loadLevel(l, false);

        if (Game.currentGame != null)
            Game.currentGame.level = l;

        return l;
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
        return "" + (int) number;
    }

    public static String capitalize(String s)
    {
        if (s.isEmpty())
            throw new RuntimeException("Capitalizing string without letters in it or of size 0");

        if (!(Game.lessThan('A', s.charAt(1), 'Z') || Game.lessThan('a', s.charAt(1), 'z')))
            return s.charAt(0) + capitalize(s.substring(1));

        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // Drawing functions added in this modding api

    /**
     * Abbreviations of renderers to draw fixed stuff
     */
    public static BaseShapeRenderer fixedShapes;
    public static BaseFontRenderer fixedText;

    /**
     * Places an obstacle on a tile.
     */
    public static void setObstacle(int x, int y, String registryName)
    {
        setObstacle(x, y, registryName, 1, 0);
    }

    /**
     * Places an obstacle on a tile.
     */
    public static void setObstacle(int x, int y, String registryName, double stackHeight)
    {
        setObstacle(x, y, registryName, stackHeight, 0);
    }

    /**
     * Places an obstacle on a tile.
     */
    public static void setObstacle(int x, int y, String registryName, double stackHeight, double startHeight)
    {
        EventSetObstacle e = new EventSetObstacle(x, y, registryName, stackHeight, startHeight);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    /**
     * Fills a rectangular area with obstacles.
     */
    public static void fillObstacle(int startX, int startY, int endX, int endY, String registryName)
    {
        fillObstacle(startX, startY, endX, endY, registryName, 1, 0);
    }

    /**
     * Fills a rectangular area with obstacles.
     */
    public static void fillObstacle(int startX, int startY, int endX, int endY, String registryName, double stackHeight)
    {
        fillObstacle(startX, startY, endX, endY, registryName, stackHeight, 0);
    }

    /**
     * Fills a rectangular area with obstacles.
     */
    public static void fillObstacle(int startX, int startY, int endX, int endY, String registryName, double stackHeight, double startHeight)
    {
        EventFillObstacle e = new EventFillObstacle(startX, startY, endX, endY, registryName, stackHeight, startHeight);
        e.execute();

        if (sendEvents)
            Game.eventsOut.add(e);
    }

    /**
     * Places a tree, centered on a specified tile.
     *
     * @param height     Height of the tree, including leaves
     * @param sizeFactor How big the tree will be.
     */
    public static void placeTree(int x, int y, int height, double sizeFactor)
    {
        if (sizeFactor > 3)
        {
            System.err.println("Warning (ModAPI.placeTree): Size Factor greater than 3, canceling");
            return;
        }

        ModAPI.fillObstacle((int) Math.round(x - sizeFactor / 4), (int) Math.round(y - sizeFactor / 4), (int) Math.round(x + sizeFactor / 4), (int) Math.round(y + sizeFactor / 4), "normal", height * sizeFactor);

        sizeFactor *= 4;
        x -= (int) (sizeFactor / 2);
        y -= (int) (sizeFactor / 2);

        for (int i = 0; i < sizeFactor / 2; i++)
        {
            for (int j = 0; j < sizeFactor / 4; j++)
                ModAPI.fillObstacle(x + i + j, y + i + j, (int) (x + sizeFactor - i - j), (int) (y + sizeFactor - i - j), "shrub", sizeFactor, height + i + j * (sizeFactor / 4));
        }
    }

    /**
     * Places an {@link tanks.obstacle.ObstacleText ObstacleText} at the specified location.
     */
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

    public enum SortOrder
    {ascending, descending, none}

    /**
     * Change the color of a level background.
     */
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
