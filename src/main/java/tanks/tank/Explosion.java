package tanks.tank;

import tanks.*;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.network.event.EventExplosion;
import tanks.network.event.EventObstacleDestroy;
import tanks.obstacle.Obstacle;

public class Explosion extends Movable
{
    public double damage;
    public boolean destroysObstacles;
    public boolean destroysBullets = true;

    public double radius;
    public Tank tank;
    public Item item;

    public double knockbackRadius;
    public double bulletKnockback;
    public double tankKnockback;

    public Explosion(double x, double y, double radius, double damage, boolean destroysObstacles, Tank tank, Item item)
    {
        super(x, y);

        this.tank = tank;
        this.item = item;
        this.radius = radius;
        this.damage = damage;
        this.destroysObstacles = destroysObstacles;
        this.team = tank.team;
        this.isRemote = tank.isRemote;
    }

    public Explosion(double x, double y, double radius, double damage, boolean destroysObstacles, Tank tank)
    {
        this(x, y, radius, damage, destroysObstacles, tank, null);
    }

    public Explosion(Mine m)
    {
        this(m.posX, m.posY, m.radius, m.damage, m.destroysObstacles, m.tank, m.item);
        this.knockbackRadius = m.knockbackRadius;
        this.bulletKnockback = m.bulletKnockback;
        this.tankKnockback = m.tankKnockback;
        this.destroysBullets = m.destroysBullets;
    }

    public void explode()
    {
        Drawing.drawing.playGameSound("explosion.ogg", this, Game.tile_size * 25, (float) (Mine.mine_radius / this.radius));

        if (Game.effectsEnabled)
        {
            for (int j = 0; j < Math.min(800, 200 * this.radius / 125) * Game.effectMultiplier; j++)
            {
                double random = Math.random();
                Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
                e.maxAge /= 2;
                e.colR = 255;
                e.colG = (1 - random) * 155 + Math.random() * 100;
                e.colB = 0;

                if (Game.enable3d)
                    e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.asin(Math.random()), random * (this.radius - Game.tile_size / 2) / Game.tile_size * 2);
                else
                    e.setPolarMotion(Math.random() * 2 * Math.PI, random * (this.radius - Game.tile_size / 2) / Game.tile_size * 2);
                Game.effects.add(e);
            }
        }

        this.destroy = true;

        if (!ScreenPartyLobby.isClient)
        {
            Game.eventsOut.add(new EventExplosion(this));

            for (Movable m: Game.movables)
            {
                if (!(m instanceof IExplodable m1))
                    continue;

                double distSq = Movable.sqDistBetw(this, m);
                double kr2 = knockbackRadius * knockbackRadius;

                if (distSq < kr2)
                {
                    m1.applyExplosionKnockback(
                            this.getAngleInDirection(m.posX, m.posY),
                            1 - distSq / kr2,
                            this
                    );
                }

                if (withinExplosionRange(m))
                    m1.onExploded(this);
            }
        }

        if (this.destroysObstacles && !ScreenPartyLobby.isClient)
        {
            for (Obstacle o : Game.getInRadius(posX, posY, radius + Game.tile_size / 2, c -> c.obstacles))
            {
                if (!o.destructible) continue;
                o.onDestroy(this);
                o.playDestroyAnimation(this.posX, this.posY, this.radius);
                Game.eventsOut.add(new EventObstacleDestroy(o.posX, o.posY, o.name, this.posX, this.posY, this.radius));
            }
        }

        Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.explosion);
        e.radius = Math.max(this.radius - Game.tile_size * 0.5, 0);
        Game.effects.add(e);
    }

    public boolean withinExplosionRange(Movable m)
    {
        return withinExplosionRange(m, posX, posY, radius);
    }

    public static double getAdjustedRadius(Movable m, double radius)
    {
        double adjustedRadius = m instanceof Tank t ? radius - Game.tile_size * 0.95 + m.size * t.hitboxSize : radius;
        if (Game.vanillaMode)
            adjustedRadius = radius + m.size;
        return adjustedRadius;
    }

    public static boolean withinExplosionRange(Movable m, double mineX, double mineY, double radius)
    {
        double r = getAdjustedRadius(m, radius);
        return Movable.sqDistBetw(m.posX, m.posY, mineX, mineY) < r * r;
    }

    @Override
    public void draw()
    {

    }
}
