package tanks.obstacle;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;

public class ObstaclePath extends Obstacle
{
    int randomR = (int) (Math.random() * 20);
    int randomG = (int) (Math.random() * 20);
    int randomB = (int) (Math.random() * 20);

    public ObstaclePath(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.drawLevel = 0;

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;
        this.enableStacking = false;

        updateColor();

        this.description = "A dusty, worn out brown path";
    }


    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (Game.effectsEnabled && !ScreenGame.finished && m instanceof Tank)
        {
            double speed = Math.sqrt((Math.pow(m.vX, 2) + Math.pow(m.vY, 2)));

            double amt = speed * Panel.frameFrequency * Game.effectMultiplier * 1.4 * Math.random();

            for (int i = 1; i < amt; i++)
            {
                double angle = m.getPolarDirection() + Math.PI / 2;

                Effect e = Effect.createNewEffect(m.posX, m.posY, m.posZ, Effect.EffectType.snow);
                e.colR = this.colorR;
                e.colG = this.colorG;
                e.colB = this.colorB;
                e.glowR = e.colR;
                e.glowG = e.colG;
                e.glowB = e.colB;
                e.vX = -m.vX / 2 * (Math.random() * 5 + 0.7);
                e.vY = -m.vY / 2 * (Math.random() * 4);
                e.vZ = Math.sqrt(m.vX * m.vX + m.vY * m.vY) / 2;
                e.addPolarMotion(angle, (Math.random() - 0.5) * e.vZ);
                e.vX += m.vX;
                e.vY += m.vY;
                e.enableGlow = false;
                Game.effects.add(e);
            }
        }
    }

    @Override
    public void draw()
    {
        if (!redrawn)
            updateColor();

        if (!Game.enable3d)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
            Drawing.drawing.fillRect(this, this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        }
    }

    @Override
    public void drawTile(double r, double g, double b, double d, double extra)
    {
        double frac = Obstacle.draw_size / Game.tile_size;

        if (frac < 1 || extra != 0)
        {
            Drawing.drawing.setColor(this.colorR * frac + r * (1 - frac), this.colorG * frac + g * (1 - frac), this.colorB * frac + b * (1 - frac));
            Drawing.drawing.fillBox(this, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, d * frac + extra);
        }
        else
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
            Drawing.drawing.fillBox(this, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, d + extra);
        }
    }

    public void updateColor()
    {
        double colorFactor = 1;

        int x = (int) (this.posX / 50);
        int y = (int) (this.posY / 50);
        if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
            colorFactor = (Game.tilesR[x][y] + Game.tilesG[x][y] + Game.tilesB[x][y]) / 255;

        this.colorR = 170 * colorFactor + randomR;
        this.colorG = 105 * colorFactor + randomG;
        this.colorB = 50 * colorFactor + randomB;
    }

    public double getTileHeight()
    {
        return Game.tilesDepth[(int) (this.posX / 50)][(int) (this.posY / 50)];
    }

    public double getGroundHeight()
    {
        return Game.tilesDepth[(int) (this.posX / 50)][(int) (this.posY / 50)];
    }
}
