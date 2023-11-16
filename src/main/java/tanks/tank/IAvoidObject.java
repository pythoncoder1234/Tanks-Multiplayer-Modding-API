package tanks.tank;

import tanks.Game;
import tanks.obstacle.Obstacle;

import java.util.HashSet;

public interface IAvoidObject
{
    HashSet<IAvoidObject> avoidances = new HashSet<>();

    double getRadius();

    double getSeverity(double posX, double posY);

    static boolean exists(Obstacle o)
    {
        if (o == null)
            return false;

        int x = (int) (o.posX / Game.tile_size);
        int y = (int) (o.posY / Game.tile_size);

        if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
            return false;

        return Game.obstacleGrid[x][y] == o;
    }
}
