package tanks.tank;

import basewindow.Model;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.obstacle.Obstacle;

public class TankShoe extends TankAIControlled implements IAvoidObject
{
    public static final Model shoeModel = Drawing.drawing.createModel("/models/tankshoe/shoe/");
    public static final Model sunglassesModel = Drawing.drawing.createModel("/models/tankshoe/sunglasses/");
    public static final double gravity = 0.08;
    public State currentState = State.touchingGround;
    public State prevState = State.touchingGround;
    public boolean bouncing = false;
    public double startBounceAge = 0;
    public double jumpCounter = 0;

    public TankShoe(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 235, 145, 103, angle, ShootAI.straight);

        this.maxSpeed = 2.5;
        this.enableLookingAtTargetEnemy = false;
        this.enableMineLaying = false;
        this.enablePathfinding = true;
        this.seekChance = 1;
        this.health = 2;
        this.enableBulletAvoidance = false;
        this.enableDefensiveFiring = false;
        this.size = 75;

        this.description = "Best idea ever, thanks brodis";
    }

    @Override
    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawModel(shoeModel, this.posX, this.posY, Math.max(10, this.posZ), size, size, size, this.getSpeed() > 0 ? this.getPolarDirection() : this.orientation);
        Drawing.drawing.drawModel(sunglassesModel, this.posX, this.posY, Math.max(10, this.posZ), size, size, size, this.angle);
    }

    @Override
    public void update()
    {
        if (!this.bouncing && jumpCounter <= 0 && this.maxSpeed > 0)
        {
            jumpCounter = this.maxSpeed * 60;
            this.bouncing = true;
            this.startBounceAge = this.age;
            this.vZ = this.maxSpeed * 1.75;
        }

        jumpCounter -= Panel.frameFrequency;

        this.hitboxSize = currentState.equals(State.inAir) ? 1e-6 : 0.95;
        this.treadAnimation = 0;

        this.posZ += this.vZ;

        currentState = getState();

        if (currentState.equals(State.inAir))
            this.vZ -= gravity * Panel.frameFrequency;

        this.setPolarAcceleration(this.angle, this.maxSpeed / 10);

        if (currentState.equals(State.touchingGround) || currentState.equals(State.onObstacle))
        {
            if (prevState.equals(State.inAir))
            {
                Drawing.drawing.playGlobalSound("stomp.ogg", 1, 1.5f);

                double dist, radius = getRadius();

                for (Movable m : Game.movables)
                {
                    if (m == this || (dist = Movable.distanceBetween(this, m)) > radius || Math.abs(m.posZ - this.posZ) > 10)
                        continue;

                    if (m instanceof Tank)
                    {
                        ((Tank) m).damage((radius - dist) / radius * (this.size / 25) * (dist < this.size * 1.5 ? 3 : 1), this);
                    }
                    else if (m instanceof Bullet || m instanceof Mine)
                        m.destroy = true;
                }
            }

            this.vZ = 0;
            this.bouncing = false;
        }

        prevState = currentState;

        super.update();

        if (!this.bouncing || currentState.equals(State.collideWithObstacle))
        {
            this.setPolarMotion(0, 0);
            this.setPolarAcceleration(0, 0);
        }
    }

    @Override
    public void updateMotionAI()
    {
        super.updateMotionAI();
    }

    @Override
    public void preUpdate()
    {
        if (this.targetEnemy != null && Movable.distanceBetween(this.targetEnemy, this) < this.size * 5)
        {
            if (Math.signum(this.vZ) != Math.signum(this.lastVZ))
            {
                this.vZ *= 1.25;
                this.jumpCounter *= 1.3;
            }
        }

        super.preUpdate();
    }

    public State getState()
    {
        if (posZ < 5)
            return State.touchingGround;

        double maxHeight = 0;
        double bound = this.size / 2 + Game.tile_size / 2;

        for (Obstacle o : Game.obstacles)
        {
            if (Math.abs(o.posX - this.posX) < bound && Math.abs(o.posY - this.posY) < bound)
                maxHeight = Math.max(maxHeight, (o.startHeight + o.stackHeight) * Game.tile_size);
        }

        if (posZ < maxHeight && vZ > 0)
            return State.collideWithObstacle;

        if (posZ < maxHeight && vZ < 0)
            return State.onObstacle;

        return State.inAir;
    }

    @Override
    public void setPathfindingTileProperties(Tile t, Obstacle o)
    {
        if (o != null && ((o.enableStacking ? o.stackHeight : 0) + o.startHeight) * Game.tile_size > this.size * 1.75)
            t.type = Tile.Type.solid;
        else
            t.type = Tile.Type.empty;
    }

    @Override
    public void shoot()
    {

    }

    @Override
    public boolean damage(double amount, GameObject source)
    {
        return super.damage(amount * 0.25, source);
    }

    @Override
    public void drawTank(boolean forInterface, boolean interface3d)
    {
        this.posZ += size / 2;
        super.drawTank(forInterface, interface3d);
        this.posZ -= size / 2;
    }

    @Override
    public double getRadius()
    {
        return this.size * 5;
    }

    @Override
    public double getSeverity(double posX, double posY)
    {
        return 20;
    }

    public enum State
    {touchingGround, onObstacle, collideWithObstacle, inAir}
}
