package com.orangishcat.modapi.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.obstacle.Obstacle;
import tanks.tank.IAvoidObject;

public abstract class ObstacleLiquid extends Obstacle implements IAvoidObject
{
    public float sinkSpeed = 0.5f;

    public ObstacleLiquid(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.drawLevel = 0;

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;
    }

    @Override
    public void draw3dOutline(double r, double g, double b, double a)
    {
        if (!Game.enable3d)
        {
            drawOutline(r, g, b, a);
            return;
        }

        Drawing.drawing.setColor(r, g, b, a, 0.5);
        Drawing.drawing.fillRect(this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
    }

    public double getRadius()
    {
        return Game.tile_size;
    }

    public double getSeverity(double x, double y)
    {
        return 0;
    }

    public double getTileHeight()
    {
        if (!this.enableStacking)
            return -25;

        return -this.stackHeight * Game.tile_size;
    }

    public double getGroundHeight()
    {
        return getTileHeight() + baseGroundHeight;
    }
}
