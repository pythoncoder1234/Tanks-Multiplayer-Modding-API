package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;

public class ObstacleHill extends Obstacle
{
    public ObstacleHill(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.destructible = false;
        this.description = "A tile that is the same color as the level";

        this.colorR = Level.currentColorR + 20;
        this.colorG = Level.currentColorG + 20;
        this.colorB = Level.currentColorB + 20;
    }

    @Override
    public void draw()
    {
        int x = (int) (this.posX / 50);
        int y = (int) (this.posY / 50);

        if (!Game.enable3d && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
        {
            Drawing.drawing.setColor(Game.tilesR[x][y], Game.tilesG[x][y], Game.tilesB[x][y]);
            Drawing.drawing.fillRect(this, this.posX, this.posY, Game.tile_size, Game.tile_size);
        }
    }

    @Override
    public void drawTile(double r, double g, double b, double d, double extra)
    {
        Drawing.drawing.setColor(r + 10, g + 10, b + 10);
        Drawing.drawing.fillBox(this, this.posX, this.posY, -extra + this.startHeight * 50,
                Game.tile_size, Game.tile_size, (this.startHeight + this.stackHeight) * 50 + d);
    }
}
