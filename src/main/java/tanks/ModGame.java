package tanks;

import tanks.bullet.Bullet;
import tanks.event.EventCustomLevelEndCondition;
import tanks.event.EventDisableMinimap;
import tanks.event.EventDisableSpeedrunTimer;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;

public abstract class ModGame
{
    public boolean customLevelEndCondition = false;
    public boolean enableShooting = true;
    public boolean enableLayingMines = true;
    public boolean forceDisableMinimap = false;
    public boolean disableSpeedrunTimer = false;
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

        if (this.disableSpeedrunTimer)
        {
            Game.eventsOut.add(new EventDisableSpeedrunTimer());

            if (Game.screen instanceof ScreenGame)
                ((ScreenGame) Game.screen).noSpeedrunTimer = true;
            else
                ScreenGame.disableSpeedrunTimer = true;
        }
    }

    /**
     * Called when the <code>Restart this Level</code> button in the pause menu is clicked
     */
    public void onLevelRestart()
    {
        Game.resetTiles();
        Game.silentCleanUp();
        this.start();
    }

    public void onKill(Tank attacker, Tank target)
    {

    }

    public void onBulletFire(Bullet b)
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

    public void onTimerEnd()
    {
        for (Movable m : Game.movables)
        {
            m.destroy = true;

            if (m instanceof Tank)
                ((Tank) m).health = 0;
        }
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

    public String generateKillMessage(Tank target, Tank attacker, boolean isBullet)
    {
        return Level.genKillMessage(target, attacker, isBullet);
    }

    public String generateDrownMessage(Tank target)
    {
        return Level.genDrownMessage(target);
    }
}
