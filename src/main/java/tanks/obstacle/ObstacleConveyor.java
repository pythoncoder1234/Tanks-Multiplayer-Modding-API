package tanks.obstacle;

import tanks.*;
import tanks.editorselector.GroupIdSelector;
import tanks.editorselector.LevelEditorSelector;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class ObstacleConveyor extends Obstacle
{
    public static final StatusEffect conveyor_effect = new StatusEffect("conveyor_speed",
            new AttributeModifier(AttributeModifier.max_speed, AttributeModifier.Operation.add, 0));

    public double age = 0;
    public boolean connectedFront = false;
    public boolean connectedBack = false;

    public int multX, multY;
    public int speed = 1;

    public ObstacleConveyor(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.enableStacking = false;
        this.enableRotation = true;
        this.update = true;
        this.checkForObjects = true;

        this.colorR = 0;
        this.colorG = 0;
        this.colorB = 0;

        this.description = "A conveyor belt that moves tanks and mines forward";
    }

    protected static boolean valid(int x, int y)
    {
        return x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY && Game.obstacleGrid[x][y] instanceof ObstacleConveyor;
    }

    @Override
    public void draw()
    {
        double offset = (age * this.speed) % 50 - 25;
        double percent = (offset + 25) / 50;

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.fillBox(this, this.posX, this.posY, 0, draw_size, draw_size, 15);

        Drawing.drawing.setColor(30, 30, 30);
        Drawing.drawing.fillRect(this, this.posX, this.posY, (35 * multX + 15) * (draw_size / Game.tile_size), (35 * multY + 15) * (draw_size / Game.tile_size));

        double s = draw_size * 0.25;

        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 0.5);

        if (connectedBack || connectedFront)
        {
            if (!connectedBack)
                s *= percent * 0.8;

            if (!connectedFront)
                s *= 1 - (percent * 0.8);
        }
        else
        {
            if (percent < 0.5)
                s *= percent / 2 + 0.5;
            else
                s *= 1.25 - percent / 2;
        }

        if (connectedBack || connectedFront || age % 100 < 50)
            Drawing.drawing.fill3dPolygon(this.posX + offset * multX, this.posY + offset * multY, 15, 3, this.rotation, this, 0, 0, s / 2, s / 2, 0, s, 0, 0);
    }

    @Override
    public void drawOutline(double r, double g, double b, double a)
    {
        super.drawOutline(0, 0, 0, 255);
    }

    @Override
    public void draw3dOutline(double r, double g, double b, double a)
    {
        double s = draw_size * 0.25;
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
        Drawing.drawing.fillPolygon(this.posX, this.posY, this.rotation, true, 0, 0, s / 2, s / 2, 0, s, 0, 0);
    }

    @Override
    public void drawForInterface(double x, double y)
    {
        double s = draw_size * 0.25;

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.fillInterfaceRect(x, y, draw_size, draw_size);
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
        Drawing.drawing.fillPolygon(x, y, 0, true, 0, 0, s / 2, s / 2, 0, s, 0, 0);
    }

    @Override
    public void onPropertySet(LevelEditorSelector<?> s)
    {
        double[] col = Game.getRainbowColor(Math.max(0, 0.6 - (this.speed - 1) / 15.));
        this.colorR = col[0];
        this.colorG = col[1];
        this.colorB = col[2];
    }

    @Override
    public void registerSelectors()
    {
        GroupIdSelector selector = new GroupIdSelector();
        this.registerSelector(selector);

        selector.id = "speed_selector";
        selector.title = "Speed";
        selector.min = 1;
        selector.max = 10;
        selector.image = "icons/speed.png";
        selector.buttonText = "Speed: %.0f";

        super.registerSelectors();
    }

    @Override
    public void update()
    {
        if (age == 0)
        {
            multX = (int) Math.cos(this.rotation);
            multY = (int) Math.sin(this.rotation);
        }

        age += Panel.frameFrequency;

        int x = (int) (posX / Game.tile_size);
        int y = (int) (posY / Game.tile_size);

        if (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing)
        {
            connectedFront = Game.obstacleGrid[x + multX][y + multY] instanceof ObstacleConveyor;
            connectedBack = Game.obstacleGrid[x - multX][y - multY] instanceof ObstacleConveyor;
        }
        else
        {
            connectedFront = true;
            connectedBack = true;
        }
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        double friction = 0.05;
        if (m instanceof Tank)
            friction = ((Tank) m).friction * ((Tank) m).frictionModifier;

        double moveX = speed * Panel.frameFrequency * multX;
        double moveY = speed * Panel.frameFrequency * multY;

        conveyor_effect.attributeModifiers[0].value = speed;
        m.addStatusEffect(conveyor_effect, 0, speed / friction / 2, speed / friction / 2);

        if (!(m instanceof Mine))
        {
            m.posX += moveX;
            m.posY += moveY;
            m.lastPosX += moveX;
            m.lastPosY += moveY;
        }
        else
        {
            m.vX += moveX / 50 * Panel.frameFrequency;
            m.vY += moveY / 50 * Panel.frameFrequency;
        }
    }

    @Override
    public boolean colorChanged()
    {
        return age > 0;
    }

    @Override
    public double getTileHeight()
    {
        return 15;
    }

    @Override
    public double getGroundHeight()
    {
        return 15;
    }
}
