package tanks.obstacle;

import tanks.Chunk;
import tanks.Game;

public class ObstacleHill extends Obstacle
{
    public ObstacleHill(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.destructible = false;
        this.description = "A tile that is the same color as the level";
    }

    @Override
    public void draw()
    {
        int x = (int) Math.max(0, Math.min(Game.currentSizeX, this.posX / Game.tile_size));
        int y = (int) Math.max(0, Math.min(Game.currentSizeY, this.posY / Game.tile_size));

        for (int i = 0; i < default_max_height; i++)
        {
            Chunk.Tile t = Chunk.getTile(x, y);
            this.stackColorR[i] = t.colR;
            this.stackColorG[i] = t.colG;
            this.stackColorB[i] = t.colB;
        }

        super.draw();
    }
}
