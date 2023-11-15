package tanks.obstacle;

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
            this.stackColorR[i] = Game.tilesR[x][y];
            this.stackColorG[i] = Game.tilesG[x][y];
            this.stackColorB[i] = Game.tilesB[x][y];
        }

        super.draw();
    }
}
