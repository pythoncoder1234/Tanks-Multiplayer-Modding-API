package tanks;

import basewindow.IBatchRenderableObject;

public class Cloud implements IDrawable, IBatchRenderableObject
{
    // todo: fix bug
    public double posX, posY, posZ;
    public double vX, vY, vZ;
    public int drawLevel = 8;

    public double[] relativeX = new double[5];
    public double[] relativeY = new double[5];
    public double[] relativeZ = new double[5];

    public double size = Math.random() * 300 + 100;
    public double sizeZ = Math.random() * 100 + 500;
    private boolean redrawn;

    public Cloud(double x, double y)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = Math.random() * 100 + 500;

        this.vX = Game.dirX[Level.windDirection];
        this.vY = Game.dirY[Level.windDirection];
        this.vZ = 0;

        for (int i = 0; i < 5; i++)
        {
            this.relativeX[i] = Math.random() * size * 0.75;
            this.relativeY[i] = Math.random() * size * 0.75;
            this.relativeZ[i] = Math.random() * size / 2;
        }
    }

    public void draw()
    {
        if (!Game.followingCam || !Game.enable3d || !Drawing.drawing.movingCamera || Game.game.window.drawingShadow)
            return;

        for (int i = 0; i < this.relativeY.length; i++)
        {
            Drawing.drawing.setColor(255 * Level.currentLightIntensity, 255 * Level.currentLightIntensity, 255 * Level.currentLightIntensity, 255);
            Drawing.drawing.fillBox(this, this.relativeX[i], this.relativeY[i], this.posZ + this.relativeZ[i], size, size, sizeZ, (byte) 0);
        }
    }

    public void update()
    {
        for (int i = 0; i < this.relativeX.length; i++)
        {
            this.posX += Game.dirX[Level.windDirection] * Panel.frameFrequency;
            this.posY += Game.dirY[Level.windDirection] * Panel.frameFrequency;
        }

        if (this.relativeX[0] < -Game.tile_size * 5 || this.relativeX[0] > Game.currentSizeX * Game.tile_size * 5)
            Game.removeClouds.add(this);
    }

    @Override
    public boolean positionChanged()
    {
        return true;
    }

    @Override
    public boolean colorChanged()
    {
        return false;
    }

    @Override
    public boolean wasRedrawn()
    {
        return this.redrawn;
    }

    @Override
    public void setRedrawn(boolean b)
    {
        this.redrawn = b;
    }
}
