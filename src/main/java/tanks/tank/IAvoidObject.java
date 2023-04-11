package tanks.tank;

import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.obstacle.Obstacle;

import java.util.HashSet;

public interface IAvoidObject
{
    HashSet<IAvoidObject> avoidances = new HashSet<>();

    double getRadius();

    double getSeverity(double posX, double posY);

    default void updateAvoidance()
    {
        boolean destroy = false;

        if (this instanceof Mine)
            destroy = ((Mine) this).timer <= Panel.frameFrequency * 3 || ((Mine) this).destroy;
        else if (this instanceof Movable)
            destroy = ((Movable) this).destroy;
        else if (this instanceof Obstacle)
            destroy = exists((Obstacle) this);

        if (destroy)
            avoidances.remove(this);
        else
            avoidances.add(this);
    }

    static boolean exists(Obstacle o)
    {
        if (o == null)
            return false;

        int x = (int) (o.posX / Game.tile_size);
        int y = (int) (o.posY / Game.tile_size);

        if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
            return false;

        return Game.obstacleMap[x][y] == o;
    }
}
