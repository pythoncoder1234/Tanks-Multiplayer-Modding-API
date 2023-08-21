package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.StatusEffect;
import tanks.tank.IAvoidObject;
import tanks.tank.Tank;

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

    public void onObjectEntry(Movable m)
    {
        if (m instanceof Tank && this.enableStacking)
        {
            Tank t = (Tank) m;

            int vx = (int) (1 / sinkSpeed * t.vX);
            int vy = (int) (1 / sinkSpeed * t.vY);

            int[] dx = {vx, 0};
            int[] dy = {0, vy};
            boolean floatUp = true;

            for (int i = 0; i < dx.length; i++)
            {
                int x = (int) (t.posX / 50 + dx[i]);
                int y = (int) (t.posY / 50 + dy[i]);

                if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
                    continue;

                floatUp = shouldFloat(x, y, t) && shouldFloat(x + dy[i], y + dx[i], t);

                if (floatUp) break;
            }

            if (t.posZ < 1)
                t.addStatusEffect(floatUp ? StatusEffect.water_float : StatusEffect.water_sink, 0, 0, 4);

            t.addStatusEffect(StatusEffect.snow_velocity, 0, 20, 30);
            t.addStatusEffect(StatusEffect.snow_friction, 0, 5, 10);
        }

        this.onObjectEntryLocal(m);
    }

    public boolean shouldFloat(int x, int y, Tank t)
    {
        if (t.getSpeed() < 0.1)
            return false;

        x = Math.min(Math.max(x, 0), Game.currentSizeX - 1);
        y = Math.min(Math.max(y, 0), Game.currentSizeY - 1);
        Obstacle o = Game.obstacleGrid[x][y];

        return !(o instanceof ObstacleLiquid) || t.posZ < o.getTileHeight();
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
            return 0;

        return -this.stackHeight * Game.tile_size;
    }

    public double getGroundHeight()
    {
        return getTileHeight() + baseGroundHeight;
    }
}
