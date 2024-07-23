package tanks.bullet;

import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.Team;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Explosion;
import tanks.tank.Tank;

import java.util.HashMap;
import java.util.HashSet;

public class BulletWhistlingBird extends BulletHoming
{
    public static HashMap<Tank, HashSet<BulletWhistlingBird>> bullets = new HashMap<>();
    private boolean moveBack;
    private boolean hasTargets;

    public BulletWhistlingBird(double x, double y, int bounces, Tank t, ItemBullet item)
    {
        super(x, y, bounces, t, item);
    }

    @Override
    public void preUpdate()
    {
        if (age < 5)
            modify(s -> s.add(this));
        else if (this.destroy && destroyTimer < 5)
            modify(s -> s.remove(this));

        if (this.justBounced)
            this.vX = this.vY = 0;

        if (target != null)
        {
            this.addPolarMotion(this.getAngleInDirection(target.posX, target.posY), Panel.frameFrequency);
            this.setPolarMotion(this.getPolarDirection(), Math.min(speed, this.getSpeed()));
        }

        super.preUpdate();
    }

    @Override
    public void update()
    {
        if (age < 25 || target == null)
            this.setPolarMotion(this.getAngleInDirection(tank.posX, tank.posY) + Math.PI * getMotionMultiplier(), speed);
        super.update();

        if (Game.bulletTrails)
            createHomingEffect();
    }

    private double getMotionMultiplier()
    {
        return moveTowardsPlayer() && !Movable.withinRange(this, tank, tank.size + Game.tile_size * 0.25) ? 0.4 : 0.55;
    }

    public boolean moveTowardsPlayer()
    {
        return (moveBack || target == null);
    }

    @Override
    public void collidedWithBullet(Bullet b)
    {
        if (moveTowardsPlayer() && b instanceof BulletWhistlingBird && b.tank == tank)
            return;
        super.collidedWithBullet(b);
    }

    @Override
    public void collidedWithTank(Tank t)
    {
        Explosion e = new Explosion(this.posX, this.posY, 100, 0, true, this.tank, this.item);
        e.explode();
        super.collidedWithTank(t);
        moveBack = true;
    }

    @Override
    public void onDestroy()
    {
        Explosion e = new Explosion(this.posX, this.posY, 100, 0, true, this.tank, this.item);
        e.explode();
        super.onDestroy();
    }

    @Override
    public void onExploded(Explosion e)
    {

    }

    public Tank getNearest()
    {
        Tank nearest = null;
        double nearestDist = Double.MAX_VALUE;
        hasTargets = false;

        for (Movable m: Game.getInRadius(posX, posY, Game.tile_size * 20, c -> c.movables))
        {
            if (m instanceof Tank t && t.targetable && !t.hidden && t.getDamageMultiplier(this) > 0 && !Team.isAllied(this, m) && !m.destroy)
            {
                double d = Movable.distanceBetween(this, m);
                double angle = Movable.absoluteAngleBetween(getAngleInDirection(t.posX, t.posY), this.getPolarDirection());

                if (d < nearestDist && (angle < Math.toRadians(20) || d < Game.tile_size * 1.5))
                {
                    nearestDist = d;
                    hasTargets = true;

                    boolean taken = false;
                    for (BulletWhistlingBird b : getBullets())
                    {
                        if (b != this && b.target == t)
                        {
                            taken = true;
                            break;
                        }
                    }

                    if (!taken)
                        nearest = t;
                }
            }
        }

        return nearest;
    }

    public void modify(ModifyFunc func)
    {
        func.modify(getBullets());
    }

    public HashSet<BulletWhistlingBird> getBullets()
    {
        return bullets.computeIfAbsent(tank, k -> new HashSet<>());
    }

    @FunctionalInterface
    public interface ModifyFunc
    {
        void modify(HashSet<BulletWhistlingBird> bullets);
    }
}
