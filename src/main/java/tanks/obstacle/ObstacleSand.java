package tanks.obstacle;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;

public class ObstacleSand extends Obstacle
{
    public static StatusEffect sand_effect = new StatusEffect("sand", new AttributeModifier("sand_velocity", AttributeModifier.velocity, AttributeModifier.Operation.multiply, -0.25),
            new AttributeModifier("sand_friction", AttributeModifier.friction, AttributeModifier.Operation.multiply, -0.25));

    public boolean firstFrame = true;

    public ObstacleSand(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.destructible = true;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.isSurfaceTile = true;
        this.checkForObjects = true;
        this.destroyEffect = Effect.EffectType.snow;
        this.destroyEffectAmount = 1.5;

        if (Game.fancyTerrain)
        {
            this.colorR = 233 - Math.random() * 20;
            this.colorG = 215 - Math.random() * 20;
            this.colorB = 188 - Math.random() * 20;
        }
        else
        {
            this.colorR = 233;
            this.colorG = 215;
            this.colorB = 188;
        }

        for (int i = 0; i < default_max_height; i++)
        {
            this.stackColorR[i] = this.colorR;
            this.stackColorB[i] = this.colorB;
            this.stackColorG[i] = this.colorG;
        }

        this.description = "A thick patch of sand that slows tanks down";
    }

    @Override
    public void draw()
    {
        double z = 0;
        if (Game.getObstacle(posX, posY) instanceof ObstacleWater w)
            z = w.getGroundHeight() + w.baseGroundHeight;

        double maxHeight = Game.sampleDefaultGroundHeight(posX, posY);

        for (int i = 0; i < 4; i++)
        {
            double x = posX + Game.dirX[i] * Game.tile_size;
            double y = posY + Game.dirY[i] * Game.tile_size;

            maxHeight = Math.max(maxHeight, Game.sampleDefaultGroundHeight(x, y) - 2);
        }

        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
        Drawing.drawing.fillBox(this, this.posX, this.posY, z, Game.tile_size, Game.tile_size, maxHeight);
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (!ScreenPartyLobby.isClient && m instanceof Tank)
        {
            sand_effect.attributeModifiers[0].value = -0.5 * this.stackHeight;
            sand_effect.attributeModifiers[1].value = -0.5 * this.stackHeight;
            m.addStatusEffect(sand_effect, 20, 20, 20);

            ((Tank) m).hidden = m.vX == 0 && m.vY == 0;
        }

        this.onObjectEntryLocal(m);
    }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (Game.effectsEnabled && m instanceof Tank t && !ScreenGame.finished && Math.random() * Panel.frameFrequency <= 0.3 * Game.effectMultiplier)
        {
            double a = m.getPolarDirection();
            Effect e1 = Effect.createNewEffect(m.posX, m.posY, Effect.EffectType.piece);
            Effect e2 = Effect.createNewEffect(m.posX, m.posY, Effect.EffectType.piece);
            e1.posZ = m.posZ;
            e2.posZ = m.posZ;
            e1.enableGlow = false;
            e2.enableGlow = false;
            e1.drawLayer = 1;
            e2.drawLayer = 1;
            e1.setPolarMotion(a - Math.PI / 2, t.size * 0.25);
            e2.setPolarMotion(a + Math.PI / 2, t.size * 0.25);
            e1.size = t.size / 8;
            e2.size = t.size / 8;
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
            e1.addPolarMotion(angle, (Math.random() - 0.5) * 2 * e1.vZ);

            e2.vX = -t.vX / 2 * (Math.random() * 0.6 + 0.7);
            e2.vY = -t.vY / 2 * (Math.random() * 0.6 + 0.7);
            e2.vZ = e1.vZ;
            e2.addPolarMotion(angle, (Math.random() - 0.5) * 2 * e2.vZ);

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

    public int unfavorability(TankAIControlled t)
    {
        return 3;
    }

    public double getTileHeight()
    {
        return this.stackHeight < 1 ? 0.2 : this.stackHeight;
    }
}
