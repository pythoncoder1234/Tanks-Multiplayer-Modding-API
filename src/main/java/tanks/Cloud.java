package tanks;

import basewindow.IBatchRenderableObject;
import tanks.gui.screen.ScreenGame;

public class Cloud implements IDrawable, IBatchRenderableObject
{
    public static int cloudRange = 100;

    public double posX, posY, posZ;
    public double vX, vY, vZ;
    public int drawLevel = 8;

    public double[] relativeX = new double[5];
    public double[] relativeY = new double[5];
    public double[] relativeZ = new double[5];
    public double[] sizeX = new double[5];
    public double[] sizeY = new double[5];
    public double[] sizeZ = new double[5];

    public Cloud()
    {
        if (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing)
            this.posX = -1000;
        else
            this.posX = (Math.random() - 0.5) * (Game.currentSizeX + cloudRange) * Game.tile_size;

        this.posY = (Math.random() - 0.5) * (Game.currentSizeY + cloudRange) * Game.tile_size;
        this.posZ = Math.random() * 150 + 800;

        this.vX = 0.5;

        for (int i = 0; i < this.relativeY.length; i++)
        {
            this.sizeX[i] = Math.random() * 500 + 200;
            this.sizeY[i] = Math.random() * 500 + 200;
            this.sizeZ[i] = Math.random() * 50 + 10;

            this.relativeX[i] = Math.random() * sizeX[i] * 0.75;
            this.relativeY[i] = Math.random() * sizeY[i] * 0.75;
            this.relativeZ[i] = Math.random() * sizeZ[i] / 2;
        }
    }

    public void draw()
    {
        if (!Game.followingCam || !Game.enable3d || !Drawing.drawing.movingCamera)
            return;

        double fade = Math.min(1 - (Math.max(0, (this.posX - Game.currentSizeX * Game.tile_size) / 2000)), Math.min(0, this.posX) / 1000 + 1);

        for (int i = 0; i < relativeY.length; i++)
        {
            int brightness = (int) (255 * (1.25 - this.relativeZ[i] / 70) * Panel.skylight);
            Drawing.drawing.setColor(brightness, brightness, brightness, 128 * fade);
            Drawing.drawing.fillBox(posX + relativeX[i], posY + relativeY[i], posZ + relativeZ[i], sizeX[i], sizeY[i], sizeZ[i], (byte) 0);
        }
    }

    public void update()
    {
        posX += vX * Panel.frameFrequency;
        posY += vY * Panel.frameFrequency;

        if (relativeX[0] < -Game.tile_size * 5 || relativeX[0] > (Game.currentSizeX + 5) * Game.tile_size)
            Game.removeClouds.add(this);
    }
}
