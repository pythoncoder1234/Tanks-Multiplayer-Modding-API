package tanks;

import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenInterlevel;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.EventMinigameStart;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

/**
 * The base class you extend to create modded minigames.<br><br>
 * <p>
 * To create your own minigame, create a class that extends this <code>Minigame</code>
 * class and register it in {@link ModAPI#registerGames()}. You can test it by going to
 * Singleplayer -> Minigames -> the class name of your minigame.<br><br>
 * <p>
 * The default <code>Minigame</code> class does nothing, so implement functions such as {@link #start()}.
 * <br><br>
 * <p>
 * You can look at {@link tanks.minigames.TeamDeathmatch TeamDeathmatch} for reference.
 *
 * @see tanks.minigames.TeamDeathmatch TeamDeathmatch
 * @see ModAPI#loadLevel(String) ModAPI.loadLevel(levelString)
 */
public abstract class Minigame
{
    public Level level;

    public String name = this.getClass().getSimpleName();
    public String description = "";

    public EndCondition endCondition = EndCondition.defaultEndCondition;

    /**
     * The amount of coins one gets from killing players.
     */
    public int playerKillCoins = 0;

    /**
     * Forcibly disable the minimap. Useful for games like hide and seek
     */
    public boolean forceDisableMinimap = false;

    /**
     * If enabled, will ask the client side to load up the minigame, not just the server.
     * If the client does not have the minigame installed, nothing will happen.
     */
    public boolean enableRemote = false;

    public boolean enableKillMessages = false;
    public boolean hideSpeedrunTimer = false;

    public boolean enableShooting = true;
    public boolean enableLayingMines = true;

    /**
     * Unfinished feature by Aehmttw
     */
    public boolean flashBackground = false;

    /** Whether the minigame is remote. You can also use <code>ScreenPartyLobby.isClient</code>. */
    public boolean remote = false;

    public Minigame()
    {
        for (String s: Game.registryMinigame.minigames.keySet())
        {
            if (Game.registryMinigame.minigames.get(s).equals(this.getClass()))
                this.name = s;
        }
    }

    /** Initialize the minigame here. Make sure to call <code>super.start()</code>! */
    public void start()
    {
        ModAPI.fixedMenus.clear();
        ScreenInterlevel.fromMinigames = true;

        if (!ScreenPartyLobby.isClient && enableRemote)
            Game.eventsOut.add(new EventMinigameStart(this.name));

        Game.screen.splitTiles = flashBackground;

        if (this.flashBackground)
        {
            Game.screen.tiles = new Screen.FlashingTile[Game.currentSizeX][Game.currentSizeY];

            for (int i = 0; i < Game.screen.tiles.length; i++)
            {
                for (int j = 0; j < Game.screen.tiles[i].length; j++)
                {
                    Game.screen.tiles[i][j] = new Screen.FlashingTile(i, j);
                }
            }
        }
    }

    /**
     * Do any per-frame updating here
     */
    public void update()
    {

    }

    /**
     * Draw any HUD things here (server-side only, use Mod API drawing methods for client side)
     */
    public void draw()
    {

    }

    /**
     * Override this method to do something when the level ends
     */
    public void onLevelEnd(boolean levelWon)
    {

    }

    public void loadInterlevelScreen()
    {
        Game.screen = new ScreenInterlevel();
    }

    /**
     * Override to do something when a tank destroys another tank
     */
    public void onKill(Tank attacker, Tank target)
    {

    }

    public String generateKillMessage(Tank target, Tank attacker, boolean isBullet)
    {
        StringBuilder message = new StringBuilder(getName(target));

        message.append("\u00a7000000000255 was ").append(isBullet ? "shot" : "blown up").append(" by ");

        if (attacker == target)
            message.append("themselves :/");
        else
            message.append(getName(attacker));

        return message.toString();
    }

    public String generateDrownMessage(Tank killed)
    {
        return getName(killed) + "\u00a7000000000255 drowned (nice)";
    }

    public StringBuilder getName(Tank t)
    {
        StringBuilder message = new StringBuilder();

        String killedR;
        String killedG;
        String killedB;

        if (t.team != null && t.team.enableColor)
        {
            killedR = String.format("%03d", (int) t.team.teamColorR);
            killedG = String.format("%03d", (int) t.team.teamColorG);
            killedB = String.format("%03d", (int) t.team.teamColorB);
        }
        else
        {
            killedR = String.format("%03d", (int) t.colorR);
            killedG = String.format("%03d", (int) t.colorG);
            killedB = String.format("%03d", (int) t.colorB);
        }

        message.append("\u00a7").append(killedR).append(killedG).append(killedB).append("255");

        if (t instanceof TankPlayer)
            message.append(((TankPlayer) t).player.username);

        else if (t instanceof TankPlayerRemote)
            message.append(((TankPlayerRemote) t).player.username);

        else
        {
            String name = t.getClass().getSimpleName();
            StringBuilder outputName = new StringBuilder();
            int prevBeginIndex = 0;

            for (int i = 1; i < name.length(); i++)
            {
                if (65 <= name.charAt(i) && name.charAt(i) <= 90)
                {
                    if (prevBeginIndex > 0)
                        outputName.append(name, prevBeginIndex, i).append(" ");
                    prevBeginIndex = i;
                }
            }
            outputName.append(name.substring(prevBeginIndex)).append(" Tank");
            message.append(outputName);
        }

        return message;
    }
}