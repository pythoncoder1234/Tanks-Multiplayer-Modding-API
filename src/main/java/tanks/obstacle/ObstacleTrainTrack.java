package tanks.obstacle;

import basewindow.Model;
import tanks.Drawing;
import tanks.Game;

import static tanks.Game.dirX;
import static tanks.Game.dirY;

public class ObstacleTrainTrack extends Obstacle
{
    public static final Model turnTrackWood = Drawing.drawing.createModel("/models/obstacletrack/wood/");
    public static final Model turnTrackRail = Drawing.drawing.createModel("/models/obstacletrack/rail/");

    /**
     * Connected Track Positions: Top, Right, Bottom, Left (clockwise)
     */
    public ObstacleTrainTrack[] connectedTo = new ObstacleTrainTrack[4];
    public boolean horizontal = true;
    public boolean colorChanged = false;
    public int turn = 0;
    protected boolean firstFrame = true;

    public ObstacleTrainTrack(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.tankCollision = false;
        this.bulletCollision = false;
        this.enableStacking = false;
        this.isSurfaceTile = true;

        for (int i = 0; i < 5; i++)
        {
            this.stackColorR[i] = 176 - Math.random() * 5;
            this.stackColorG[i] = 111 - Math.random() * 5;
            this.stackColorB[i] = 14 - Math.random() * 5;
        }

        this.description = "A train track, put trains on it!";
    }

    @Override
    public void draw()
    {
        if (firstFrame)
            setOrientation();

        batchDraw = turn == 0;
        if (turn > 0)
        {
            Drawing.drawing.setColor(176, 111, 14);
            Drawing.drawing.drawModel(turnTrackWood, this.posX, this.posY, 5, 50, 50, 40, Math.PI / 2 * (turn - 1));

            Drawing.drawing.setColor(192, 192, 192);
            Drawing.drawing.drawModel(turnTrackRail, this.posX, this.posY, 10, 50, 50, 50, Math.PI / 2 * (turn - 1));
        }
        else if (!Game.enable3d)
        {
            double offX = this.horizontal ? 0 : 15;
            double offY = this.horizontal ? 15 : 0;

            for (int i = -1; i <= 1; i++)
            {
                Drawing.drawing.setColor(this.stackColorR[i + 1], this.stackColorG[i + 1], this.stackColorB[i + 1]);
                Drawing.drawing.fillInterfaceRect(this.posX + offY * 1.1 * i, this.posY + offX * 1.1 * i, offX * 2.86 + 7, offY * 2.86 + 7);
            }

            Drawing.drawing.setColor(192, 192, 192);
            Drawing.drawing.fillInterfaceRect(this.posX + offX, this.posY + offY, offY * 3 + 5, offX * 3 + 5);
            Drawing.drawing.fillInterfaceRect(this.posX - offX, this.posY - offY, offY * 3 + 5, offX * 3 + 5);
        }
    }

    @Override
    public void drawForInterface(double x, double y)
    {
        for (int i = -2; i <= 2; i++)
        {
            Drawing.drawing.setColor(this.stackColorR[i + 2], this.stackColorG[i + 2], this.stackColorB[i + 2]);
            Drawing.drawing.fillInterfaceRect(x + 15 * i, y, 7, 50);
        }

        Drawing.drawing.setColor(192, 192, 192);
        Drawing.drawing.fillInterfaceRect(x, y + 15, 80, 7);
        Drawing.drawing.fillInterfaceRect(x, y - 15, 80, 7);
    }

    @Override
    public void drawTile(double r, double g, double b, double d, double extra)
    {
        if (firstFrame)
            setOrientation();

        d = Game.fancyTerrain ? 5 : 0;

        Drawing.drawing.setColor(r, g, b);
        Drawing.drawing.fillBox(this, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, extra + d);

        if (turn > 0)
            return;

        double offX = this.horizontal ? 0 : 15;
        double offY = this.horizontal ? 15 : 0;

        Drawing.drawing.setColor(192, 192, 192);
        Drawing.drawing.fillBox(this, this.posX + offX, this.posY + offY, d, offY * 3 + 5, offX * 3 + 5, 10);
        Drawing.drawing.fillBox(this, this.posX - offX, this.posY - offY, d, offY * 3 + 5, offX * 3 + 5, 10);

        for (int i = -1; i <= 1; i++)
        {
            Drawing.drawing.setColor(this.stackColorR[i + 1], this.stackColorG[i + 1], this.stackColorB[i + 1]);
            Drawing.drawing.fillBox(this, this.posX + offY * 1.1 * i, this.posY + offX * 1.1 * i, d, offX * 2.86 + 7, offY * 2.86 + 7, 6);
        }
    }
    public void setOrientation()
    {
        firstFrame = false;
        this.colorChanged = turn > 0;

        for (int i = 0; i < 4; i++)
        {
            int x = (int) (posX / 50) + dirX[i];
            int y = (int) (posY / 50) + dirY[i];

            if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY || !(Game.obstacleMap[x][y] instanceof ObstacleTrainTrack))
                continue;

            ObstacleTrainTrack o = ((ObstacleTrainTrack) Game.obstacleMap[x][y]);
            setObstacleOrientation(o, dirX[i], dirY[i]);
        }
    }

    public void setObstacleOrientation(ObstacleTrainTrack o, int dxi, int dyi)
    {
        if (dxi != 0)
        {
            if (o.connectedY() > 1)
                return;

            this.colorChanged = !this.horizontal;
            this.horizontal = true;
            o.colorChanged = o.colorChanged || !o.horizontal;
            o.horizontal = true;

            this.connectedTo[2 - dxi] = o;
            o.connectedTo[2 + dxi] = this;

            o.setTurn(dxi, true);
            this.setTurn(-dxi, true);
        }
        else
        {
            if (o.connectedX() > 1)
                return;

            this.colorChanged = this.horizontal;
            this.horizontal = false;
            o.colorChanged = o.colorChanged || o.horizontal;
            o.horizontal = false;

            this.connectedTo[1 + dyi] = o;
            o.connectedTo[1 - dyi] = this;

            o.setTurn(dyi, false);
            this.setTurn(-dyi, false);
        }
    }

    public void setTurn(int direction, boolean x)
    {
        int num;

        if (x)
        {
            if (this.connectedY() != 1)
                return;

            if (this.connectedTo[0] != null)
            {
                if (direction == -1)
                    num = 4;
                else
                    num = 3;
            }
            else
            {
                if (direction == -1)
                    num = 1;
                else
                    num = 2;
            }
        }
        else
        {
            if (this.connectedX() != 1)
                return;

            if (this.connectedTo[1] != null)
            {
                if (direction == -1)
                    num = 4;
                else
                    num = 1;
            }
            else
            {
                if (direction == -1)
                    num = 2;
                else
                    num = 3;
            }
        }

        if (this.connectedY() == 1)
            this.turn = num;
        else
            this.turn = 0;
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

    public static boolean exists(Obstacle o)
    {
        if (o == null)
            return false;

        int x = (int) (o.posX / Game.tile_size);
        int y = (int) (o.posY / Game.tile_size);

        if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
            return false;

        return Game.obstacleMap[x][y] == o;
    }

    @Override
    public boolean colorChanged()
    {
        boolean r = colorChanged;
        colorChanged = false;

        return super.colorChanged() || r;
    }
}