package tanks.obstacle;

import tanks.*;
import tanks.editor.selector.GroupIdSelector;
import tanks.editor.selector.LevelEditorSelector;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Mine;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;

public class ObstacleConveyor extends Obstacle
{
    public static float speedMult = 0.5f;
    public static float speedAdj = -0.5f;
    public static final StatusEffect conveyor_effect = new StatusEffect("conveyor_speed",
            new AttributeModifier(AttributeModifier.max_speed, AttributeModifier.Operation.add, 0));

    public double age = 0;
    public boolean connectedFront = false;
    public boolean connectedBack = false;

    public int multX, multY;
    public double speed = 1;
    public double finishedSpeed = 1;

    public ObstacleConveyor(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.enableStacking = false;
        this.enableGroupID = true;
        this.enableRotation = true;
        this.drawLevel = 1;
        this.batchDraw = false;
        this.update = true;
        this.checkForObjects = true;

        this.colorR = 0;
        this.colorG = 0;
        this.colorB = 0;

        this.description = "A conveyor belt that moves tanks and mines forward";
    }

    protected static boolean valid(int x, int y, double rotation)
    {
        if (!(Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing))
            return false;

        return x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY
                && Game.getObstacle(x, y) instanceof ObstacleConveyor && Game.getObstacle(x, y).rotation == rotation;
    }

    @Override
    public void draw()
    {
        double offset = (age * finishedSpeed * this.speed * speedMult + (finishedSpeed >= 1 ? speedAdj : 1)) % 50 - 25;
        double percent = (offset + 25) / 50;

        if (Game.enable3d)
        {
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.fillBox(this, this.posX, this.posY, 0, draw_size, draw_size, 15);
        }
        else
        {
            Drawing.drawing.setColor(30, 30, 30);
            Drawing.drawing.fillRect(this, this.posX, this.posY, (35 * multX + 15) * (draw_size / Game.tile_size), (35 * multY + 15) * (draw_size / Game.tile_size));
        }

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

        if (!(Game.screen instanceof ScreenGame))
            s *= 2;

        if (connectedBack || connectedFront || age % 100 < 50)
            Drawing.drawing.fill3dPolygon(this.posX + offset * multX, this.posY + offset * multY, 15, 3, this.rotation, this, 0, 0, s / 2, s / 2, 0, s, 0, 0);
    }

    @Override
    public void drawOutline(double r, double g, double b, double a)
    {
        super.drawOutline(0, 0, 0, a / 4);
        double s = draw_size * 0.25;

        Drawing.drawing.setColor(0, 0, 0, a / 4);
        Drawing.drawing.fillRect(this.posX, this.posY, draw_size, draw_size);
        Drawing.drawing.setColor(r, g, b);
        Drawing.drawing.fillPolygon(posX, posY, this.rotation, 0, 0, s / 2, s / 2, 0, s, 0, 0);
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
        Drawing.drawing.fillPolygon(x, y, 0, 0, 0, s / 2, s / 2, 0, s, 0, 0);
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
    public void postInitSelectors()
    {
        super.postInitSelectors();

        GroupIdSelector selector = (GroupIdSelector) this.selectors.get(0);

        selector.id = "speed";
        selector.title = "Speed";
        selector.min = 1;
        selector.max = 10;
        selector.image = "icons/speed.png";
        selector.allowDecimals = true;
        selector.objectProperty = "speed";
        selector.buttonText = "Speed: %.1f";

        if (!selector.modified)
            selector.number = 2;
    }

    @Override
    public int unfavorability(TankAIControlled t)
    {
        return this.speed >= 3 ? 100 : 3;
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

        connectedFront = valid(x + multX, y + multY, rotation);
        connectedBack = valid(x - multX, y - multY, rotation);

        if (ScreenGame.finishedQuick)
            finishedSpeed = Math.max(0, finishedSpeed - Panel.frameFrequency / 700);
        else
            finishedSpeed = 1;
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        double friction = 0.05;
        if (m instanceof Tank)
            friction = ((Tank) m).friction * ((Tank) m).frictionModifier;

        double moveX = (speed * Panel.frameFrequency * speedMult + speedAdj) * multX;
        double moveY = (speed * Panel.frameFrequency * speedMult + speedAdj) * multY;

        double sp = speed * speedMult + speedAdj;
        conveyor_effect.attributeModifiers[0].value = sp;
        m.addStatusEffect(conveyor_effect, 0, sp / friction / 2, sp / friction / 2);

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
