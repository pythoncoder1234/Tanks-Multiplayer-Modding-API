package tanks.obstacle;

import tanks.Chunk;
import tanks.Game;
import tanks.Level;

public class ObstacleHill extends Obstacle
{
    public ObstacleHill(String name, double posX, double posY)
    {
        super(name, posX, posY);

        if (Game.currentLevel != null)
        {
            Level l = Game.currentLevel;
            this.colorR = l.colorR;
            this.colorG = l.colorG;
            this.colorB = l.colorB;
        }

        this.destructible = false;
        this.description = "A tile that is the same color as the level";
    }

    @Override
    public void draw()
    {
        int x = (int) Math.max(0, Math.min(Game.currentSizeX, this.posX / Game.tile_size));
        int y = (int) Math.max(0, Math.min(Game.currentSizeY, this.posY / Game.tile_size));
        Chunk.Tile t = Chunk.getTile(x, y);

        for (int i = 0; i < default_max_height; i++)
        {
            this.stackColorR[i] = t.colR;
            this.stackColorG[i] = t.colG;
            this.stackColorB[i] = t.colB;
        }

        super.draw();
    }
}
