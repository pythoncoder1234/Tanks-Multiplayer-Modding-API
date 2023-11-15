package tanks.tank;

import tanks.*;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.ItemMine;
import tanks.network.event.EventMineChangeTimer;
import tanks.network.event.EventMineRemove;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.HashMap;

public class Mine extends Movable implements IAvoidObject, ISolidObject
{
    public static double mine_size = 30;
    public static double mine_radius = Game.tile_size * 2.5;
    public static double mine_friction = 0.02;

    public double timer;
    public double outlineColorR;
    public double outlineColorG;
    public double outlineColorB;
    public double height = 0;

    public double triggeredTimer = 50;
    public double damage = 2;
    public boolean destroysObstacles = true;

    public boolean enableCollision = true;
    public double friction = mine_friction;
    public double frictionModifier = 1;

    public double radius = mine_radius;
    public Tank tank;
    public ItemMine item;
    public double cooldown = 0;
    public int lastBeep = Integer.MAX_VALUE;

    public double knockbackRadius = this.radius * 2;
    public double bulletKnockback = 10;
    public double tankKnockback = 10;

    public int networkID = -1;

    public static int currentID = 0;
    public static ArrayList<Integer> freeIDs = new ArrayList<>();
    public static HashMap<Integer, Mine> idMap = new HashMap<>();

    public Face[] horizontalFaces;
    public Face[] verticalFaces;

    public Mine(double x, double y, double timer, Tank t, ItemMine item)
    {
        super(x, y);

        this.size = mine_size;
        this.timer = timer;
        this.drawLevel = 2;
        this.bounciness = 0.2;
        this.tank = t;

        this.item = item;

        if (!ScreenPartyLobby.isClient)
            this.item.liveMines++;

        double[] outlineCol = Team.getObjectColor(t.colorR, t.colorG, t.colorB, t);
        this.outlineColorR = outlineCol[0];
        this.outlineColorG = outlineCol[1];
        this.outlineColorB = outlineCol[2];

        this.posZ = t.posZ;
        this.team = t.team;

        if (!ScreenPartyLobby.isClient)
        {
            if (!freeIDs.isEmpty())
                this.networkID = freeIDs.remove(0);
            else
            {
                this.networkID = currentID;
                currentID++;
            }

            idMap.put(this.networkID, this);
        }
    }

    public Mine(double x, double y, Tank t, ItemMine im)
    {
        this(x, y, 1000, t, im);
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, 255, 0.5);

        if (Game.enable3d && Game.enable3dBg && Game.fancyTerrain)
        {
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX - this.size / 2, this.posY - this.size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX + this.size / 2, this.posY - this.size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX - this.size / 2, this.posY + this.size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX + this.size / 2, this.posY + this.size / 2));
        }

        if (Game.enable3d)
        {
            int x = (int) (this.posX / Game.tile_size);
            int y = (int) (this.posY / Game.tile_size);

            for (double i = height; i < height + 6; i++)
            {
                double frac = ((i - height + 1) / 6 + 1) / 2;
                boolean shouldXRay = i == height + 5 && Game.xrayBullets && Game.obstacleGrid[x][y] == null;

                Drawing.drawing.setColor(this.outlineColorR * frac, this.outlineColorG  * frac, this.outlineColorB * frac, 255, 0.5);
                Drawing.drawing.fillOval(this.posX, this.posY, this.posZ + i + 1.5, this.size, this.size, !shouldXRay, false);
            }

            Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, 255, 1);

            if (Game.glowEnabled)
                Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ + height + 1, this.size * 4, this.size * 4, true, false);
        }
        else
        {
            Drawing.drawing.fillOval(this.posX, this.posY, this.size, this.size);

            if (Game.glowEnabled)
                Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 4, this.size * 4);
        }

        Drawing.drawing.setColor(255, Math.min(255, this.timer / 1000 * 255), 0, 255, 0.5);

        if (timer < 150 && timer % 20 >= 10)
            Drawing.drawing.setColor(255, 255, 0, 255, 0.5);

        if (Game.enable3d)
            Drawing.drawing.fillOval(this.posX, this.posY, this.posZ + height + 7.5, this.size * 0.8, this.size * 0.8, true, false);
        else
            Drawing.drawing.fillOval(this.posX, this.posY, this.size * 0.8, this.size * 0.8);
    }

    @Override
    public void update()
    {
        this.timer -= Panel.frameFrequency;

        this.frictionModifier = this.getAttributeValue(AttributeModifier.friction, 1);

        if (this.timer < 0)
            this.timer = 0;

        if ((this.timer <= 0 || destroy) && !ScreenPartyLobby.isClient)
            this.explode();

        int beepTime = ((int) this.timer / 10);
        if (this.timer <= 150 && beepTime % 2 == 1 && this.lastBeep != beepTime && this.tank == Game.playerTank)
        {
            Drawing.drawing.playSound("beep.ogg", 1f, 0.25f);
            this.lastBeep = beepTime;
        }

        super.update();

        this.vX *= Math.pow(1 - friction, frictionModifier * Panel.frameFrequency);
        this.vY *= Math.pow(1 - friction, frictionModifier * Panel.frameFrequency);

        if (this.enableCollision)
            checkCollision();

        boolean enemyNear = false;
        boolean allyNear = false;
        for (Movable m : Game.movables)
        {
            if (Math.pow(Math.abs(m.posX - this.posX), 2) + Math.pow(Math.abs(m.posY - this.posY), 2) < Math.pow(radius, 2))
            {
                if (m instanceof Tank && !m.destroy && ((Tank) m).targetable)
                {
                    if (Team.isAllied(m, this.tank))
                        allyNear = true;
                    else
                        enemyNear = true;
                }
            }
        }

        if (enemyNear && !allyNear && this.timer > this.triggeredTimer && !this.isRemote)
        {
            this.timer = this.triggeredTimer;
            Game.eventsOut.add(new EventMineChangeTimer(this));
        }
    }

    @Override
    public boolean drawBeforeObstacles()
    {
        return true;
    }

    public void checkCollision()
    {
        if (Tank.checkBorderCollision(this))
            onCollidedWith(null);

        double t = Game.tile_size;

        int x1 = (int) Math.min(Math.max(0, this.posX / t - this.size / t / 2 - 1), Game.currentSizeX);
        int y1 = (int) Math.min(Math.max(0, this.posY / t - this.size / t / 2 - 1), Game.currentSizeY);
        int x2 = (int) Math.min(Math.max(0, this.posX / t + this.size / t / 2 + 1), Game.currentSizeX);
        int y2 = (int) Math.min(Math.max(0, this.posY / t + this.size / t / 2 + 1), Game.currentSizeY);

        for (int x = x1; x < x2; x++)
        {
            for (int y = y1; y < y2; y++)
            {
                checkCollisionWith(Game.obstacleGrid[x][y]);
                checkCollisionWith(Game.surfaceTileGrid[x][y]);
            }
        }
    }

    public void checkCollisionWith(Obstacle o)
    {
        if (Tank.checkCollideWith(this, o))
            onCollidedWith(o);
    }

    public void onCollidedWith(Obstacle o)
    {

    }

    public void explode()
    {
        Game.eventsOut.add(new EventMineRemove(this));
        Game.removeMovables.add(this);

        if (!ScreenPartyLobby.isClient)
        {
            freeIDs.add(this.networkID);
            idMap.remove(this.networkID);

            Explosion e = new Explosion(this);
            e.explode();

            this.item.liveMines--;
        }
    }

    @Override
    public double getRadius()
    {
        return Math.min(Mine.mine_radius * 1.5, this.radius);
    }

    @Override
    public double getSeverity(double posX, double posY)
    {
        return this.timer;
    }

    public static void drawRange(double posX, double posY, double size)
    {
        int faces = (int) (size + 5);
        double r = Drawing.drawing.currentColorR;
        double g = Drawing.drawing.currentColorG;
        double b = Drawing.drawing.currentColorB;
        double a = Drawing.drawing.currentColorA;

        Game.game.window.shapeRenderer.setBatchMode(true, true, true, false, false);
        for (int f = 0; f < faces; f++)
        {
            for (int i = 0; i <= 10; i++)
            {
                double hfrac = i / 10.0;
                double hfrac2 = (i + 1) / 10.0;
                double height = Math.sin(hfrac);
                double pitchMul = Math.cos(hfrac);
                double height2 = Math.sin(hfrac2);
                double pitchMul2 = Math.cos(hfrac2);

                double angle = f * Math.PI * 2 / faces;
                double angle2 = (f + 1) * Math.PI * 2 / faces;
                Drawing.drawing.setColor(r, g, b, (1 - hfrac) * a, 1);
                Drawing.drawing.addVertex(posX + Math.cos(angle) * size * pitchMul, posY + Math.sin(angle) * size * pitchMul, height * size);
                Drawing.drawing.addVertex(posX + Math.cos(angle2) * size * pitchMul, posY + Math.sin(angle2) * size * pitchMul, height * size);
                Drawing.drawing.setColor(r, g, b, (1 - hfrac2) * a, 1);
                Drawing.drawing.addVertex(posX + Math.cos(angle2) * size * pitchMul2, posY + Math.sin(angle2) * size * pitchMul2, height2 * size);
                Drawing.drawing.addVertex(posX + Math.cos(angle) * size * pitchMul2, posY + Math.sin(angle) * size * pitchMul2, height2 * size);
            }
        }
        Game.game.window.shapeRenderer.setBatchMode(false, true, false);
    }

    public static void drawRange2D(double posX, double posY, double size)
    {
        int faces = (int) (size + 5);
        double r = Drawing.drawing.currentColorR;
        double g = Drawing.drawing.currentColorG;
        double b = Drawing.drawing.currentColorB;
        double a = Drawing.drawing.currentColorA;

        Game.game.window.shapeRenderer.setBatchMode(true, true, false);
        for (int f = 0; f < faces; f++)
        {
            double angle = f * Math.PI * 2 / faces;
            double angle2 = (f + 1) * Math.PI * 2 / faces;
            double inner = 0.8;
            Drawing.drawing.setColor(r, g, b, a, 1);
            Drawing.drawing.addVertex(posX + Math.cos(angle) * size, posY + Math.sin(angle) * size, 0);
            Drawing.drawing.addVertex(posX + Math.cos(angle2) * size, posY + Math.sin(angle2) * size, 0);
            Drawing.drawing.setColor(r, g, b, 0, 1);
            Drawing.drawing.addVertex(posX + Math.cos(angle2) * size * inner, posY + Math.sin(angle2) * size * inner, 0);
            Drawing.drawing.addVertex(posX + Math.cos(angle) * size * inner, posY + Math.sin(angle) * size * inner, 0);
        }
        Game.game.window.shapeRenderer.setBatchMode(false, true, false);
    }

    @Override
    public Face[] getHorizontalFaces()
    {
        double s = this.size / 2;

        if (this.horizontalFaces == null)
        {
            this.horizontalFaces = new Face[2];
            this.horizontalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX + s, this.posY - s, true, true, true, true);
            this.horizontalFaces[1] = new Face(this, this.posX - s, this.posY + s, this.posX + s, this.posY + s, true, false,true, true);
        }
        else
        {
            this.horizontalFaces[0].update(this.posX - s, this.posY - s, this.posX + s, this.posY - s);
            this.horizontalFaces[1].update(this.posX - s, this.posY + s, this.posX + s, this.posY + s);
        }

        return this.horizontalFaces;
    }

    @Override
    public Face[] getVerticalFaces()
    {
        double s = this.size / 2;

        if (this.verticalFaces == null)
        {
            this.verticalFaces = new Face[2];
            this.verticalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX - s, this.posY + s, false, true, true, true);
            this.verticalFaces[1] = new Face(this, this.posX + s, this.posY - s, this.posX + s, this.posY + s, false, false, true, true);
        }
        else
        {
            this.verticalFaces[0].update(this.posX - s, this.posY - s, this.posX - s, this.posY + s);
            this.verticalFaces[1].update(this.posX + s, this.posY - s, this.posX + s, this.posY + s);
        }

        return this.verticalFaces;
    }
}
