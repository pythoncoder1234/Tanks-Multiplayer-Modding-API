package tanks.modapi;

import tanks.Game;
import tanks.Level;
import tanks.modapi.events.EventCustomLevelEndCondition;
import tanks.modapi.events.EventDisableMinimap;
import tanks.tank.Tank;

public abstract class ModGame
{
    public boolean customLevelEndCondition = false;
    public boolean customRestart = false;
    public boolean enableShooting = true;
    public boolean enableLayingMines = true;
    public boolean forceDisableMinimap = false;
    public boolean enableKillMessages = false;
    public double playerKillCoins = 0;

    public String name;
    public String description = null;

    /** Set this variable to <code>true</code> to end the level if <code>customLevelEndCondition</code> is set to <code>true</code>. */
    protected boolean levelEnded = false;

    public ModGame()
    {
        this.name = this.getClass().getSimpleName().replace("_", " ");
    }

    /**
     * Override for initialization code when the <code>ModGame</code> is started.
     *  <code>super.start()</code> call is required.
     *  */
    public void start()
    {
        if (this.forceDisableMinimap)
            Game.eventsOut.add(new EventDisableMinimap());

        if (this.customLevelEndCondition)
            Game.eventsOut.add(new EventCustomLevelEndCondition());
    }

    /**
     * Called when the <code>Restart this Level</code> button in the pause menu is clicked
     */
    public void onLevelRestart()
    {
        Game.cleanUp();
        this.start();
    }

    public void onKill(Tank killer, Tank killed)
    {

    }

    public String levelEndString(boolean levelWon)
    {
        if (levelWon)
            return "Victory!";
        else
            return "You were destroyed!";
    }

    public String levelEndSubtitle(boolean levelWon)
    {
        return null;
    }

    public boolean levelEndCondition()
    {
        return levelEnded;
    }

    public void onLevelEnd(boolean levelWon)
    {

    }

    public void update()
    {

    }

    public void draw()
    {

    }

    public String generateKillMessage(Tank killed, Tank killer, boolean isBullet)
    {
        return Level.genKillMessage(killed, killer, isBullet);
    }

    public String generateDrownMessage(Tank killed)
    {
        return Level.genDrownMessage(killed);
    }
}
