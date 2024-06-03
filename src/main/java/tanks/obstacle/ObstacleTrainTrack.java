
package tanks.obstacle;

import basewindow.Model;
import tanks.Drawing;
import tanks.Game;
import tanks.rendering.ShaderTrainTrack;
import tanks.tank.IAvoidObject;
import tanks.tank.TankTrain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static tanks.Game.dirX;
import static tanks.Game.dirY;

public class ObstacleTrainTrack extends Obstacle
{
    public static final String[] descriptions = new String[] {"Train track!!!", "An essential for all trains.", "One stick and six iron."};

    public static final Model turnTrackWood = Drawing.drawing.createModel("/models/obstacletrack/wood/");
    public static final Model turnTrackRail = Drawing.drawing.createModel("/models/obstacletrack/rail/");

    /**
     * Connected Track Positions: Top, Right, Bottom, Left (clockwise)
     */
    public ObstacleTrainTrack[] connectedTo = new ObstacleTrainTrack[4];
    public boolean horizontal = true;
    public int turn = 0;
    protected boolean firstFrame = true;

    public boolean redraw = false;

    public ObstacleTrainTrack(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.tankCollision = false;
        this.bulletCollision = false;
        this.destructible = false;
        this.enableStacking = false;

        this.colorR = 176;
        this.colorG = 111;
        this.colorB = 14;

        this.renderer = ShaderTrainTrack.class;

        this.description = descriptions[(int) (Math.random() * descriptions.length)];
    }

    @Override
    public void draw()
    {
        batchDraw = turn == 0;

        if (turn > 0)
        {
            double z = -3 * (1 - Obstacle.draw_size / Game.tile_size);

            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
            Drawing.drawing.drawModel(turnTrackWood, this.posX, this.posY, 5 + startHeight * 50 + z, 50, 50, 40, Math.PI / 2 * (turn - 1));

            Drawing.drawing.setColor(192, 192, 192);
            Drawing.drawing.drawModel(turnTrackRail, this.posX, this.posY, 10 + startHeight * 50 + z, 50, 50, 50, Math.PI / 2 * (turn - 1));
        }
        else if (!Game.enable3d)
        {
            double offX = this.horizontal ? 0 : 15;
            double offY = this.horizontal ? 15 : 0;

            for (int i = -1; i <= 1; i++)
            {
                Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
                Drawing.drawing.fillRect(this, this.posX + offY * 1.1 * i, this.posY + offX * 1.1 * i, offX * 2.86 + 7, offY * 2.86 + 7);
            }

            Drawing.drawing.setColor(192, 192, 192);
            Drawing.drawing.fillRect(this, this.posX + offX, this.posY + offY, offY * 3 + 5, offX * 3 + 5);
            Drawing.drawing.fillRect(this, this.posX - offX, this.posY - offY, offY * 3 + 5, offX * 3 + 5);
        }
        else
        {
            double d = Game.fancyTerrain ? 5 : 0;

            double offX = this.horizontal ? 0 : 15;
            double offY = this.horizontal ? 15 : 0;

            Drawing.drawing.setColor(192, 192, 192);
            Drawing.drawing.fillBox(this, this.posX + offX, this.posY + offY, d + startHeight * 50, offY * 3 + 5, offX * 3 + 5, 10);
            Drawing.drawing.fillBox(this, this.posX - offX, this.posY - offY, d + startHeight * 50, offY * 3 + 5, offX * 3 + 5, 10);

            for (int i = -1; i <= 1; i++)
            {
                Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
                Drawing.drawing.fillBox(this, this.posX + offY * 1.1 * i, this.posY + offX * 1.1 * i, d + startHeight * 50, offX * 2.86 + 7, offY * 2.86 + 7, 6);
            }
        }
    }

    @Override
    public void afterAdd()
    {
        setOrientation();
    }

    @Override
    public void onNeighborUpdate()
    {
        super.onNeighborUpdate();

        updateTurn();
        if (turn != 0 && batchDraw)
            Drawing.drawing.terrainRenderer.remove(this);

        batchDraw = turn == 0;
        if (redraw && batchDraw)
            Game.redrawObstacles.add(this);
    }

    public static boolean exists(Obstacle o)
    {
        return IAvoidObject.exists(o);
    }

    @Override
    public void drawForInterface(double x, double y)
    {
        double sizeMult = draw_size / Game.tile_size;

        for (int i = -2; i <= 2; i++)
        {
            Drawing.drawing.setColor(colorR, colorG, colorB);
            Drawing.drawing.fillInterfaceRect(x + 15 * i * sizeMult, y, 7 * sizeMult, 50 * sizeMult);
        }

        Drawing.drawing.setColor(192, 192, 192);
        Drawing.drawing.fillInterfaceRect(x, y + 15 * sizeMult, 80 * sizeMult, 7 * sizeMult);
        Drawing.drawing.fillInterfaceRect(x, y - 15 * sizeMult, 80 * sizeMult, 7 * sizeMult);
    }

    public void setObstacleOrientation(ObstacleTrainTrack o, int dxi, int dyi)
    {
        if (o.turn > 0 && o.connectedX() == 1 && o.connectedY() == 1)
            return;

        if (dxi != 0)
        {
            if (o.connectedY() > 1)
                return;

            this.redraw = !this.horizontal;
            this.horizontal = true;
            o.redraw = o.redraw || !o.horizontal;
            o.horizontal = true;

            this.connectedTo[2 - dxi] = o;
            o.connectedTo[2 + dxi] = this;

            o.updateTurn();
        }
        else
        {
            if (o.connectedX() > 1)
                return;

            this.redraw = this.horizontal;
            this.horizontal = false;
            o.redraw = o.redraw || o.horizontal;
            o.horizontal = false;

            this.connectedTo[1 + dyi] = o;
            o.connectedTo[1 - dyi] = this;

            o.updateTurn();
        }
    }

    public void updateTurn()
    {
        this.turn = Objects.requireNonNullElse(AnglePair.get(this), 0);
    }

    public int connectedX()
    {
        int cnt = 0;

        if (exists(this.connectedTo[1]))
            cnt++;

        if (exists(this.connectedTo[3]))
            cnt++;

        return cnt;
    }

    public int connectedY()
    {
        int cnt = 0;

        if (exists(this.connectedTo[0]))
            cnt++;

        if (exists(this.connectedTo[2]))
            cnt++;

        return cnt;
    }

    public void setOrientation()
    {
        firstFrame = false;
        this.redraw = turn > 0;

        for (int i = 0; i < 4; i++)
        {
            int x = (int) (posX / 50) + dirX[i];
            int y = (int) (posY / 50) + dirY[i];

            if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY || !(Game.getObstacle(x, y) instanceof ObstacleTrainTrack))
                continue;

            setObstacleOrientation(((ObstacleTrainTrack) Game.getObstacle(x, y)), dirX[i], dirY[i]);
        }

        this.updateTurn();
    }

    @Override
    public double getTileHeight()
    {
        return 0;
    }

    @Override
    public double getGroundHeight()
    {
        return 12;
    }


    public static class AnglePair
    {
        private static final HashMap<AnglePair, Integer> pairs = new HashMap<>();
        public int a, b;

        static
        {
            pairs.put(new AnglePair(1, 2), 1);
            pairs.put(new AnglePair(2, 3), 2);
            pairs.put(new AnglePair(0, 3), 3);
            pairs.put(new AnglePair(0, 1), 4);
        }

        private AnglePair(int a, int b)
        {
            this.a = a;
            this.b = b;
        }

        public static int oppositeTurn(TankTrain t, ObstacleTrainTrack o)
        {
            int tankTurn = (int) Math.round(t.angle / (Math.PI / 2));

            for (Map.Entry<AnglePair, Integer> e : pairs.entrySet())
            {
                if (e.getValue() == o.turn)
                {
                    AnglePair p = e.getKey();
                    return tankTurn == p.a ? p.b : p.a;
                }
            }

            return tankTurn;
        }

        public static Integer get(ObstacleTrainTrack o)
        {
            int a = -1, b = -1;

            for (int i = 0; i < 4; i++)
            {
                if (exists(o.connectedTo[i]))
                {
                    if (a == -1)
                        a = i;
                    else if (b == -1)
                        b = i;
                    else
                        break;
                }
            }

            return pairs.get(new AnglePair(a, b));
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof AnglePair p)
            {
                return (a == p.a && b == p.b) || (a == p.b && b == p.a);
            }

            return super.equals(obj);
        }

        public int hashCode()
        {
            return a * b;
        }
    }
}