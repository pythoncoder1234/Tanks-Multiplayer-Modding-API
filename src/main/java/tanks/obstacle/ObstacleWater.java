package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.rendering.ShaderGroundWater;
import tanks.rendering.ShaderWater;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;

public class ObstacleWater extends ObstacleLiquid
{
    public static final int drownTime = 10000;

    public ObstacleWater(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;

        this.isSurfaceTile = false;

        if (Game.enable3d)
            this.drawLevel = 6;
        else
            this.drawLevel = 1;

        this.colorR = 50;
        this.colorG = 120;
        this.colorB = 255;
        this.colorA = 64;

        for (int i = 0; i < default_max_height; i++)
        {
            this.stackColorR[i] = this.colorR;
            this.stackColorB[i] = this.colorB;
            this.stackColorG[i] = this.colorG;
        }

        this.renderer = ShaderWater.class;
        this.tileRenderer = ShaderGroundWater.class;

        this.description = "A pool of water that can slow and drown tanks";
    }


    @Override
    public void onObjectEntry(Movable m)
    {
        if (m instanceof Tank t)
        {
            if (t.posZ < -1)
                t.inWater = true;

            CustomPropertiesMap p = t.customProperties;

            if (t.posZ < -Game.tile_size - 15)
            {
                p.putIfAbsent("drown", 0D);
                double drown = p.getDouble("drown") + Panel.frameFrequency;
                p.put("drown", drown);
                p.put("last_water_enter", System.currentTimeMillis());

                StatusEffect.damage.attributeModifiers[0].value = 0.1;
                if (drown > drownTime)
                    t.addStatusEffect(StatusEffect.damage, 0, 0, 10);
            }
            else
            {
                Long lastEnter = p.getLong("last_water_enter");
                if (lastEnter != null && System.currentTimeMillis() - lastEnter > 1000)
                    p.put("drown", 0D);
            }
        }

        super.onObjectEntry(m);
    }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (Game.effectsEnabled && m instanceof Tank t && !ScreenGame.finished && Math.random() * Panel.frameFrequency <= 0.1 * Game.effectMultiplier)
        {
            double a = m.getPolarDirection();
            Effect e1 = Effect.createNewEffect(m.posX, m.posY, Effect.EffectType.piece);
            Effect e2 = Effect.createNewEffect(m.posX, m.posY, Effect.EffectType.piece);
            e1.enableGlow = false;
            e1.posZ = m.posZ;
            e2.posZ = m.posZ;
            e1.drawLayer = 1;
            e2.drawLayer = 1;
            e1.setPolarMotion(a - Math.PI / 2, t.size * 0.25);
            e2.setPolarMotion(a + Math.PI / 2, t.size * 0.25);
            e1.size = t.size / 5;
            e2.size = t.size / 5;
            e1.posX += e1.vX;
            e1.posY += e1.vY;
            e2.posX += e2.vX;
            e2.posY += e2.vY;
            e1.angle = a;
            e2.angle = a;
            e1.setPolarMotion(0, 0);
            e2.setPolarMotion(0, 0);

            double var = 20;
            e1.colR = Math.min(255, Math.max(0, this.colorR - 20 + Math.random() * var - var / 2));
            e1.colG = Math.min(255, Math.max(0, this.colorG - 20 + Math.random() * var - var / 2));
            e1.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

            e2.colR = Math.min(255, Math.max(0, this.colorR - 20 + Math.random() * var - var / 2));
            e2.colG = Math.min(255, Math.max(0, this.colorG - 20 + Math.random() * var - var / 2));
            e2.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

            double angle = t.getPolarDirection() + Math.PI / 2;

            e1.vX = -t.vX / 2 * (Math.random() * 0.6 + 0.7);
            e1.vY = -t.vY / 2 * (Math.random() * 0.6 + 0.7);
            e1.vZ = Math.sqrt(t.vX * t.vX + t.vY * t.vY) / 2;
            e1.addPolarMotion(angle, (Math.random() - 0.5) * e1.vZ);

            e2.vX = -t.vX / 2 * (Math.random() * 0.6 + 0.7);
            e2.vY = -t.vY / 2 * (Math.random() * 0.6 + 0.7);
            e2.vZ = e1.vZ;
            e2.addPolarMotion(angle, (Math.random() - 0.5) * e2.vZ);

            e1.vZ *= (Math.random() * 0.6 + 0.4);
            e2.vZ *= (Math.random() * 0.6 + 0.4);

            e1.maxAge = 50 + Math.random() * 20;
            e2.maxAge = 50 + Math.random() * 20;

            e1.size /= 2;
            e2.size /= 2;

            Game.effects.add(e1);
            Game.effects.add(e2);
        }
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
        Drawing.drawing.fillBox(this, this.posX, this.posY, 0, Game.tile_size, Game.tile_size, 0, (byte) 61);
    }

    @Override
    public void drawTile(IBatchRenderableObject tile, double r, double g, double b, double d, double extra)
    {
        if (Game.getSurfaceObstacle(posX, posY) instanceof ObstacleSand s)
            Drawing.drawing.setColor(s.colorR, s.colorG, s.colorB);
        else
            Drawing.drawing.setColor(r, g, b);

        Drawing.drawing.fillBox(tile, this.posX, this.posY, -Game.tile_size * stackHeight - extra, Game.tile_size, Game.tile_size, extra + d);
    }

    @Override
    public boolean isTransparent()
    {
        return true;
    }

    @Override
    public int unfavorability(TankAIControlled t)
    {
        return 5;
    }
}
