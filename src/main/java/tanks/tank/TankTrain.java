package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleTrainTrack;

public class TankTrain extends TankAIControlled
{
    public static final String[] descriptions = new String[] {"Chuga chuga chuga chuga choo choo!", "Choo Choo!", "*queue thomas the tank engine theme song*"};
    public static final double searchDistSqd = Math.pow(50, 4);

    @TankProperty(category = TankProperty.Category.movementGeneral, id = "damage_on_collide", name = "Damage on Collide", desc = "Damage taken is calculated using the train velocity subtracted by the tank velocity")
    public double damageOnCollide = 1.5;

    public boolean moving = true;
    public boolean forwards;

    public int collided = 0;

    public TankTrain(String name, double x, double y, double angle)
    {
        super(name, x, y, 50, 185, 119, 14, angle, ShootAI.none);

        this.forwards = Math.toDegrees(angle) <= 315 && Math.toDegrees(angle) >= 45;
        this.maxSpeed = 3.5;
        this.mandatoryKill = false;
        this.mine.destroysObstacles = false;
        this.explodeOnDestroy = true;
        this.enableBulletAvoidance = false;
        this.enableMineAvoidance = false;

        this.description = descriptions[(int) (Math.random() * descriptions.length)];
    }

    @Override
    public void draw()
    {
        if (this.age <= 0)
        {
            ObstacleTrainTrack o = getNearest();
            if (o == null)
                return;

            this.posX = o.posX;
            this.posY = o.posY;
            this.orientation = Math.toRadians(o.horizontal ? 180 : 90);
        }

        super.draw();
    }

    @Override
    public void updateMotionAI()
    {
        ObstacleTrainTrack current = getCurrent();

        if (current != null && moving)
        {
            int a = forwards ? 1 : 0;
            int b = forwards ? 2 : 3;

            if (current.connectedTo[a] != null)
                this.setAccelerationInDirection(current.connectedTo[a].posX, current.connectedTo[a].posY, this.acceleration);
            else if (current.connectedTo[b] != null)
                this.setAccelerationInDirection(current.connectedTo[b].posX, current.connectedTo[b].posY, this.acceleration);
        }

        if (current == null)
            return;

        if (current.horizontal)
            this.posY = current.posY;
        else
            this.posX = current.posX;

        collided = Math.max(0, collided - 1);
    }

    @Override
    public void onCollidedWith(Tank t, double distSq)
    {
        if (collided == 0)
        {
            double relative = Math.sqrt(Math.pow((t.vX-this.vX), 2) + Math.pow((t.vY-this.vY), 2));

            if (!t.damage(relative / this.getSpeed() * damageOnCollide, this))
                Drawing.drawing.playGlobalSound("damage.ogg", 1f);
        }

        collided = 5;

        super.onCollidedWith(t, distSq);
    }

    public ObstacleTrainTrack getCurrent()
    {
        int x = (int) (this.posX / Game.tile_size);
        int y = (int) (this.posY / Game.tile_size);

        if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
            return null;

        Obstacle o = Game.obstacleMap[x][y];
        if (!(o instanceof ObstacleTrainTrack))
            return null;

        return ((ObstacleTrainTrack) o);
    }

    public ObstacleTrainTrack getNearest()
    {
        double nearest = 69420;
        ObstacleTrainTrack nearestObs = null;

        for (Obstacle o : Game.obstacles)
        {
            if (o instanceof ObstacleTrainTrack)
            {
                double dist = Movable.distanceBetween(o, this);
                if (dist < nearest && dist < searchDistSqd)
                {
                    nearest = dist;
                    nearestObs = (ObstacleTrainTrack) o;
                }
            }
        }

        return nearestObs;
    }
}
