package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.network.event.EventExplosion;
import tanks.network.event.EventMineChangeTimer;
import tanks.network.event.EventObstacleDestroy;
import tanks.network.event.EventUpdateCoins;
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
        Drawing.drawing.playSound("explosion.ogg", (float) (Mine.mine_radius / this.radius));

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
                double distSq = Math.pow(Math.abs(m.posX - this.posX), 2) + Math.pow(Math.abs(m.posY - this.posY), 2);
                if (distSq < knockbackRadius * knockbackRadius)
                {
                    double power = (1 - distSq / Math.pow(knockbackRadius, 2));
                    if (m instanceof Bullet)
                    {
                        Bullet b = (Bullet) m;
                        double angle = this.getAngleInDirection(m.posX, m.posY);
                        m.addPolarMotion(angle, power * this.bulletKnockback * Math.pow(Bullet.bullet_size, 2) / Math.max(1, Math.pow(b.size, 2)));
                        b.collisionX = m.posX;
                        b.collisionY = m.posY;
                        b.addTrail();
                    }
                    else if (m instanceof Tank)
                    {
                        Tank t = (Tank) m;
                        double angle = this.getAngleInDirection(m.posX, m.posY);
                        m.addPolarMotion(angle, power * this.tankKnockback * Math.pow(Game.tile_size, 2) / Math.max(1, Math.pow(m.size, 2)));
                        t.recoilSpeed = m.getSpeed();
                        if (t.recoilSpeed > t.maxSpeed)
                        {
                            t.inControlOfMotion = false;
                            t.tookRecoil = true;
                        }
                    }
                }

                if (withinExplosionRange(m))
                {
                    if (m instanceof Tank && !m.destroy && ((Tank) m).getDamageMultiplier(this) > 0)
                    {
                        if (!(Team.isAllied(this, m) && !this.team.friendlyFire) && !ScreenGame.finishedQuick)
                        {
                            Tank t = (Tank) m;
                            boolean kill = t.damage(this.damage, this);

                            if (kill)
                            {
                                if (this.tank.equals(Game.playerTank))
                                {
                                    if (Game.currentGame != null && (t instanceof TankPlayer || t instanceof TankPlayerRemote))
                                        Game.player.hotbar.coins += Game.currentGame.playerKillCoins;
                                    else
                                        Game.player.hotbar.coins += t.coinValue;
                                }
                                else if (this.tank instanceof TankPlayerRemote && (Crusade.crusadeMode || !Game.currentLevel.shop.isEmpty() || !Game.currentLevel.startingItems.isEmpty()))
                                {
                                    if (t instanceof TankPlayer || t instanceof TankPlayerRemote)
                                    {
                                        if (Game.currentGame != null && Game.currentGame.playerKillCoins > 0)
                                            ((TankPlayerRemote) this.tank).player.hotbar.coins += Game.currentGame.playerKillCoins;
                                        else
                                            ((TankPlayerRemote) this.tank).player.hotbar.coins += t.coinValue;
                                    }
                                    Game.eventsOut.add(new EventUpdateCoins(((TankPlayerRemote) this.tank).player));
                                }
                            }
                            else
                                Drawing.drawing.playGlobalSound("damage.ogg");
                        }
                    }
                    else if (m instanceof Mine && !m.destroy)
                    {
                        if (((Mine) m).timer > 10 && !this.isRemote)
                        {
                            ((Mine) m).timer = 10;
                            Game.eventsOut.add(new EventMineChangeTimer((Mine) m));
                        }
                    }
                    else if (m instanceof Bullet && !m.destroy)
                    {
                        m.destroy = true;
                    }
                }
            }
        }

        if (this.destroysObstacles && !ScreenPartyLobby.isClient)
        {
            for (Obstacle o: Game.obstacles)
            {
                if (o.destructible && !Game.removeObstacles.contains(o) && withinExplosionRange(o))
                {
                    o.onDestroy(this);
                    o.playDestroyAnimation(this.posX, this.posY, this.radius);
                    Game.eventsOut.add(new EventObstacleDestroy(o.posX, o.posY, o.name, this.posX, this.posY, this.radius));
                }
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

    public boolean withinExplosionRange(Obstacle o)
    {
        return Movable.distanceBetween(o, this) < radius + Game.tile_size / 2;
    }

    public static boolean withinExplosionRange(Movable m, double mineX, double mineY, double radius)
    {
        double adjustedRadius = m instanceof Tank ? radius - Game.tile_size * 0.95 + m.size * ((Tank) m).hitboxSize : radius;
        if (Game.vanillaMode && ScreenPartyLobby.isClient)
            adjustedRadius = radius + m.size;

        return (m.posX-mineX)*(m.posX-mineX) + (m.posY-mineY)*(m.posY-mineY) < adjustedRadius * adjustedRadius;
    }

    @Override
    public void draw()
    {

    }
}
