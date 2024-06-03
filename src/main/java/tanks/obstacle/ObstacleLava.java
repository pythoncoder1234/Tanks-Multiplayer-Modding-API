package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.rendering.ShaderLava;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class ObstacleLava extends ObstacleLiquid
{
    public double particles = 0;
    public long lastParticleTime;

    public double offset = Math.random() * 1000;

    public ObstacleLava(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.enableStacking = true;
        this.isSurfaceTile = true;

        this.colorR = 255;
        this.colorG = 84;
        this.colorB = 0;
        this.glow = 1;

        this.update = true;
        this.renderer = ShaderLava.class;

        this.description = "A pool of hot lava that severely damages tanks";
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        super.onObjectEntry(m);

        if (m instanceof Mine)
            ((Mine) m).timer = Math.min(((Mine) m).timer, 50);

        StatusEffect.damage.attributeModifiers[0].value = 0.5;
        if (m instanceof Tank && ((Tank) m).flashAnimation < 1 && !ScreenGame.finished)
            m.addStatusEffect(StatusEffect.damage, 0, 100, 100);
    }

    public void addEffect()
    {
        lastParticleTime = System.currentTimeMillis();

        if (Game.effectsEnabled && !ScreenGame.finished)
        {
            if (Math.random() * Panel.frameFrequency < 0.008)
            {
                Effect e;
                if (Game.enable3d)
                {
                    e = Effect.createNewEffect(this.posX + (Math.random() - 0.5) * Game.tile_size, this.posY + (Math.random() - 0.5) * Game.tile_size, 0, Effect.EffectType.piece);
                    e.setSize(Math.random() * 5);
                    e.colR = 255;
                    e.colG = Math.random() * 128 + 64;
                    e.colB = 0;
                    e.setPolarMotion(Math.random() * Math.PI * 2, 0.5);
                    e.vZ = Math.random() + 1;
                }
                else
                {
                    e = Effect.createNewEffect(this.posX + (Math.random() - 0.5) * Game.tile_size, this.posY + (Math.random() - 0.5) * Game.tile_size, Effect.EffectType.piece);
                    e.setSize(Math.random() * 5);
                    e.colR = 255;
                    e.colG = Math.random() * 128 + 64;
                    e.colB = 0;
                }
                Game.effects.add(e);
            }
        }
    }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (m instanceof Tank && !ScreenGame.finishedQuick)
        {
            this.particles += Panel.frameFrequency * 10;

            if (Game.playerTank != null)
            {
                double distsq = Math.pow(m.posX - Game.playerTank.posX, 2) + Math.pow(m.posY - Game.playerTank.posY, 2);

                double radius = 250000;
                if (distsq <= radius && !Game.playerTank.destroy)
                    Drawing.drawing.playSound("hiss.ogg", (float) (Math.random() * 0.2 + 1), (float) (0.2 * (radius - distsq) / radius));
            }
        }
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
        Drawing.drawing.fillBox(this, this.posX, this.posY, 0, Game.tile_size, Game.tile_size, 0, (byte) 61);
    }

    @Override
    public void update()
    {
        this.particles += Math.random() * Panel.frameFrequency;

        while (this.particles > 0)
        {
            this.particles--;
            this.addEffect();
        }
    }

    @Override
    public void drawTile(IBatchRenderableObject tile, double r, double g, double b, double d, double extra)
    {

    }
}
