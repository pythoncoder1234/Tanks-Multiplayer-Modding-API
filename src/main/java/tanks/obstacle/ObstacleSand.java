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
        if (firstFrame && this.stackHeight < 1)
        {
            firstFrame = false;
            if (Math.random() > 0.1)
                this.stackHeight = 0.3;
            else
                this.stackHeight = 0.45;
        }

        super.draw();
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
        if (Game.effectsEnabled && !ScreenGame.finished && m instanceof Tank)
        {
            double speed = Math.sqrt((Math.pow(m.vX, 2) + Math.pow(m.vY, 2)));

            double mul = 0.0625 / 4;

            double amt = speed * mul * Panel.frameFrequency * Game.effectMultiplier;

            if (amt < 1 && Math.random() < amt % 1)
                amt += 1;

            for (int i = 0; i < amt; i++)
            {
                Effect e = Effect.createNewEffect(m.posX, m.posY, m.posZ, Effect.EffectType.snow);
                e.colR = this.colorR;
                e.colG = this.colorG;
                e.colB = this.colorB;
                e.glowR = e.colR;
                e.glowG = e.colG;
                e.glowB = e.colB;
                e.set3dPolarMotion(Math.random() * Math.PI, Math.random() * Math.PI, Math.random() * speed);
                e.vX += m.vX;
                e.vY += m.vY;
                e.enableGlow = false;
                Game.effects.add(e);
            }
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
