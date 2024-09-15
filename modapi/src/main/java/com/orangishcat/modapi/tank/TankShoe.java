package com.orangishcat.modapi.tank;

import basewindow.Model;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.ArrayList;

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
    public double slamCounter = 0, slamCooldown = 300;
    private double jumpAngle;
    private boolean slam;

    public TankShoe(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 235, 145, 103, angle, ShootAI.straight);

        this.maxSpeed = 2.5;
        this.enableLookingAtTargetEnemy = false;
        this.baseModel = TankModels.checkerboard.base;
        this.colorModel = TankModels.checkerboard.color;
        this.enableMineLaying = false;
        this.enableMovement = false;
        this.customPosZBehavior = true;
        this.enableDefensiveFiring = false;
        this.size = 75;

        this.description = Math.random() > 0.25 ? "Best idea ever, thanks brodis" : "It's beatable because of its weakness.";
    }

    @Override
    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawModel(shoeModel, this.posX, this.posY, this.posZ, size, size, size, this.getSpeed() > 0 ? this.getPolarDirection() : this.orientation);
        Drawing.drawing.drawModel(sunglassesModel, this.posX, this.posY, this.posZ + size / 2, size, size, size, this.angle);
    }

    @Override
    public void update()
    {
        if (ScreenGame.finishedQuick)
        {
            this.angle += Panel.frameFrequency * 0.1;
            super.update();
            return;
        }

        if (!this.bouncing && jumpCounter <= 0)
        {
            this.jumpCounter = 50;
            this.jumpAngle = angle;
            this.bouncing = true;
            this.slam = false;
            this.startBounceAge = this.age;
            this.vZ = 5;
        }

        jumpCounter -= Panel.frameFrequency;
        slamCooldown -= Panel.frameFrequency;

        this.hitboxSize = currentState.equals(State.inAir) ? 1e-6 : 0.95;
        this.treadAnimation = 0;

        this.posZ += this.vZ;

        currentState = getState();
        double radius = getRadius();
        ArrayList<Movable> nearbyMovables = Game.getInRadius(posX, posY, radius * 0.8, c -> c.movables);

        if (currentState.equals(State.inAir) || currentState.equals(State.collideWithObstacle))
        {
            if (slamCounter <= 0)
            {
                this.vZ -= gravity * Panel.frameFrequency;
                if (!slam && targetEnemy != null && !currentState.equals(State.collideWithObstacle)
                    && (!Movable.withinRange(this, targetEnemy, Game.tile_size * 2) || !getNearbyObstacles().isEmpty()))
                    setPolarMotion(jumpAngle, 2.5);
            }
            else
                setPolarMotion(0, 0);

            if (posZ > Game.tile_size * 5 && targetEnemy != null && Movable.withinRange(this, targetEnemy, Game.tile_size * 3) &&
                getNearbyObstacles().isEmpty() && nearbyMovables.size() >= 4 && vZ < 0 && !slam && slamCooldown <= 0)
            {
                jumpCounter = 0;
                vZ = 0.4;
                slamCounter = 100;
                slam = true;
            }

            if (slamCounter < 0)
            {
                vZ = -12;
                slamCounter = 0;
            }
            else if (slamCounter > 0)
            {
                if (Panel.panel.ageFrames % 3 == 0)
                {
                    for (int i = 0; i < 8; i++)
                        Game.effects.add(Effect.createNewEffect(posX, posY, posZ + i * 30, EffectType.stun)
                                .setColor(colorR, colorG, colorB));

                    Drawing.drawing.playGameSound("laser.ogg", this, Game.tile_size * 20, (float) (1 - slamCounter / 100) * 0.5f, 0.2f);
                }

                slamCounter -= Panel.frameFrequency;
            }
        }
        else
            setPolarMotion(0, 0);

        if (currentState.equals(State.touchingGround) || currentState.equals(State.collideWithObstacle))
        {
            if (prevState.equals(State.inAir))
            {
                Drawing.drawing.playGameSound("stomp.ogg", this, Game.tile_size * 40, 1f);

                double dist;
                if (slam)
                {
                    slamCooldown = 1200;
                    radius *= 1.5;
                    Drawing.drawing.playGameSound("freeze.ogg", this, Game.tile_size * 80, 1f);
                    Game.effects.add(Effect.createNewEffect(posX, posY, posZ, EffectType.explosion).setRadius(radius * 0.8));
                }

                for (Movable m : nearbyMovables)
                {
                    if (m == this || (dist = Movable.sqDistBetw(this, m)) > radius * radius || Math.abs(m.posZ - this.posZ) > Game.tile_size)
                        continue;

                    if (m instanceof Tank t && !(Team.isAllied(this, t) && !this.team.friendlyFire))
                        ((Tank) m).damage((1 - dist / (radius * radius)) * Math.max(0, -vZ) * this.size * 0.003, this);
                    else if (m instanceof Bullet || m instanceof Mine)
                        m.destroy = true;
                }
            }

            if (!currentState.equals(State.collideWithObstacle))
                this.vZ = 0;

            this.bouncing = false;
            this.slam = false;
        }

        prevState = currentState;
        currentState = getState();

        super.update();
    }

    public ArrayList<Obstacle> getNearbyObstacles()
    {
        return Game.getInRadius(posX, posY, size / 2 + Game.tile_size, c -> c.obstacles);
    }

    @Override
    public void checkObstacleCollision()
    {

    }

    @Override
    public void preUpdate()
    {
        if (this.targetEnemy != null && Movable.distanceBetween(this.targetEnemy, this) < this.size * 5)
        {
            if (Math.signum(this.vZ) != Math.signum(this.lastVZ))
                this.vZ *= 1.25;
        }

        super.preUpdate();
    }

    public State getState()
    {
        double maxHeight = -1000;
        double bound = this.size / 2 + Game.tile_size / 2;

        for (Obstacle o : getNearbyObstacles())
        {
            if (o.tankCollision && Math.abs(o.posX - this.posX) < bound && Math.abs(o.posY - this.posY) < bound)
                maxHeight = Math.max(maxHeight, o.getTileHeight() + o.startHeight * Game.tile_size);
        }

        if (maxHeight <= -1000)
            maxHeight = 0;

        if (posZ <= maxHeight - Game.tile_size)
        {
            posZ = Math.max(posZ, maxHeight - Game.tile_size);
            return State.collideWithObstacle;
        }

        if (posZ <= maxHeight)
        {
            posZ = maxHeight;
            return State.touchingGround;
        }

        return State.inAir;
    }

    @Override
    public void shoot()
    {

    }

    @Override
    public boolean damage(double amount, GameObject source, boolean playDamageSound)
    {
        return super.damage(Math.min(0.2, amount * 0.2), source, playDamageSound);
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
        return Game.tile_size * 5 * (slam ? 1.5 : 1);
    }

    @Override
    public double getSeverity(double posX, double posY)
    {
        return 20;
    }

    public enum State
    {touchingGround, collideWithObstacle, inAir}
}
