package tanks.tank;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleTrainTrack;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;

public class TankTrain extends Tank implements IAvoidObject
{
    public static final String[] descriptions = new String[] {
            "chuga chuga chuga chuga choo choo", "choo choo", "*queue thomas the tank engine theme song*",
            "To the nearest rail it goes!", "SUBWAY SURFERS", "I added trains lmao", "TRAIN"};
    public static final HashSet<TankProperty.Category> propertiesToCopy = new HashSet<>();

    static
    {
        propertiesToCopy.addAll(Arrays.asList(
                TankProperty.Category.appearanceGeneral,
                TankProperty.Category.appearanceEmblem,
                TankProperty.Category.appearanceTurretBase,
                TankProperty.Category.appearanceTurretBarrel,
                TankProperty.Category.appearanceBody,
                TankProperty.Category.appearanceTreads,
                TankProperty.Category.appearanceGlow,
                TankProperty.Category.appearanceTracks,
                TankProperty.Category.movementGeneral
        ));
    }

    public static final double searchDist = 200;

    public float damageOnCollide = 1.5f;
    public boolean moving = true;
    public double collided = 0;
    protected boolean firstFrame = true;

    public boolean turned = false;
    public boolean rightTurn = false;
    public int prevTurn = 0;
    public double resultAngle = 0;

    public TankTrain(String name, double x, double y, double angle)
    {
        super(name, x, y, 50, 185, 120, 14);

        this.angle = angle;
        this.maxSpeed = 3.5;
        this.mandatoryKill = false;
        this.enableTracks = false;
        this.mine.destroysObstacles = false;
        this.explodeOnDestroy = true;

        this.description = descriptions[(int) (Math.random() * descriptions.length)];

        if (Math.random() < 0.05)
            this.description = "Breadloaf";
    }

    @Override
    public void draw()
    {
        super.draw();

        if (firstFrame)
        {
            firstFrame = false;

            copyCustomTank();

            ObstacleTrainTrack o = getNearest();
            if (o == null)
                return;

            this.posX = o.posX;
            this.posY = o.posY;
            this.orientation = Math.toRadians(o.horizontal ? 180 : 90);
        }

        if (Game.prevScreen != Game.screen && ScreenGame.getInstance() != null)
            copyCustomTank();
    }

    @Override
    public void update()
    {
        super.update();

        ObstacleTrainTrack current = getCurrentTrack();
        moving = current != null;

        if (current == null || current.turn == 0 || current.turn != prevTurn)
            turned = false;

        if (current == null)
        {
            this.setPolarMotion(this.angle, Math.max(0, this.getSpeed() - (this.friction * this.frictionModifier) * Panel.frameFrequency));
            return;
        }

        prevTurn = current.turn;

        if (turned)
        {
            double turn = (this.maxSpeed * Panel.frameFrequency) / 25;

            if (rightTurn)
                this.angle = Math.min(resultAngle, this.angle + turn);
            else
                this.angle = Math.max(resultAngle, this.angle - turn);
        }
        else
            this.angle = Math.round(this.angle / (Math.PI / 2)) * (Math.PI / 2);

        if (current.turn > 0 && !turned)
        {
            turned = true;
            rightTurn = getTurnDir(current);
            resultAngle = this.angle + (rightTurn ? 1 : -1) * (Math.PI / 2);
        }

        this.orientation = this.angle;
        this.setPolarMotion(this.angle, Math.min(this.maxSpeed * this.maxSpeedModifier, this.getSpeed() + (this.acceleration * this.accelerationModifier) * Panel.frameFrequency));

        if (current.turn == 0)
        {
            if (current.horizontal)
                this.posY = current.posY;
            else
                this.posX = current.posX;
        }

        collided = Math.max(0, collided - Panel.frameFrequency);
    }

    public void copyCustomTank()
    {
        Level l;
        if (Game.screen instanceof ScreenLevelEditor)
            l = ((ScreenLevelEditor) Game.screen).level;
        else if (Game.currentLevel != null)
            l = Game.currentLevel;
        else
            return;

        TankAIControlled t = null;
        for (TankAIControlled t1 : l.customTanks)
        {
            if (t1.description.startsWith("train"))
            {
                try
                {
                    String[] parts = t1.description.split("-");
                    if (parts.length > 1 && !parts[1].equals("all") && (team == null || !parts[1].equals(team.name)))
                        continue;

                    if (parts.length > 2 && !parts[2].isEmpty())
                    {
                        this.showName = true;
                        this.nameTag.name.text = parts[2].replaceAll("\\+\\+", "-");
                    }

                    t = t1;
                    break;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    t1.description = "Invalid format!";
                }
            }
        }

        if (t == null)
            return;

        try
        {
            for (Field f : this.getClass().getFields())
            {
                TankProperty p = f.getAnnotation(TankProperty.class);
                if (p == null)
                    continue;

                if (!f.getName().equals("name") && propertiesToCopy.contains(p.category()))
                {
                    f.set(this, f.get(t));
                    System.out.println(p.id());
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public float isDiagonal(ObstacleTrainTrack current)
    {
        int angle = (int) (this.angle / (Math.PI / 4) - 1);
        int opposite = ObstacleTrainTrack.AnglePair.oppositeTurn(this, current);

        return 0;
    }


    @Override
    public void onCollidedWith(Tank t, double distSq)
    {
        if (this.destroy)
            return;

        if (collided == 0)
        {
            double relative = Math.sqrt(Math.pow((t.vX-this.vX), 2) + Math.pow((t.vY-this.vY), 2));
            t.damage(relative / this.getSpeed() * damageOnCollide, this);
        }

        collided = 5;

        super.onCollidedWith(t, distSq);
    }

    public ObstacleTrainTrack getCurrentTrack()
    {
        int x = (int) Math.round(this.posX / Game.tile_size - 0.5);
        int y = (int) Math.round(this.posY / Game.tile_size - 0.5);

        if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
            return null;

        Obstacle o = Game.getObstacle(x, y);
        if (!(o instanceof ObstacleTrainTrack))
            return null;

        return ((ObstacleTrainTrack) o);
    }

    public ObstacleTrainTrack getNearest()
    {
        double nearest = 69420;
        ObstacleTrainTrack nearestObs = null;

        for (Obstacle o : Game.getInRadius(posX, posY, searchDist, c -> c.obstacles))
        {
            if (o instanceof ObstacleTrainTrack)
            {
                double dist = Movable.distanceBetween(o, this);
                if (dist < nearest && dist < searchDist)
                {
                    nearest = dist;
                    nearestObs = (ObstacleTrainTrack) o;
                }
            }
        }

        return nearestObs;
    }

    public boolean getTurnDir(ObstacleTrainTrack o)
    {
        if (o.turn == 4 || o.turn == 2)
            return Math.abs(this.vX) > Math.abs(this.vY);
        return Math.abs(this.vX) < Math.abs(this.vY);
    }

    @Override
    public double getRadius()
    {
        return this.size * this.getSpeed();
    }

    @Override
    public double getSeverity(double posX, double posY)
    {
        return 0;
    }
}
