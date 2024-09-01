package tanks;

import basewindow.IBatchRenderableObject;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;

public class Effect extends Movable implements IDrawableWithGlow, IDrawableLightSource, IBatchRenderableObject
{
    public enum State {live, removed, recycle}

    public EffectType type;
    public double age = 0;
    public double colR;
    public double colG;
    public double colB;

    public boolean force = false;
    public boolean enableGlow = true;
    public double glowR;
    public double glowG;
    public double glowB;

    public double maxAge = 100;
    public double size;
    public double radius;
    public double angle;
    public double distance;

    public int prevGridX;
    public int prevGridY;

    public int initialGridX;
    public int initialGridY;

    public double[] lightInfo = new double[7];

    //Effects that have this set to true are removed faster when the level has ended
    public boolean fastRemoveOnExit = false;

    public int drawLayer = 7;

    public State state = State.live;

    public static Effect createNewEffect(double x, double y, double z, EffectType type)
    {
        while (!Game.recycleEffects.isEmpty())
        {
            Effect e = Game.recycleEffects.remove();

            if (e.state == State.recycle)
            {
                e.refurbish();
                e.initialize(x, y, z, type);
                return e;
            }
        }

        Effect e = new Effect();
        e.initialize(x, y, z, type);
        return e;
    }

    protected void initialize(double x, double y, double z, EffectType type)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.type = type;

        this.prevGridX = (int) (this.posX / Game.tile_size);
        this.prevGridY = (int) (this.posY / Game.tile_size);

        this.initialGridX = this.prevGridX;
        this.initialGridY = this.prevGridY;

        this.type.initialize(this);
    }

    @Override
    public void draw()
    {
        if (this.maxAge > 0 && this.maxAge < this.age) return;

        if (this.type == EffectType.ray)
        {
            this.state = State.removed;
            Game.removeEffects.add(this);
        }

        if (!this.force && Game.sampleObstacleHeight(this.posX, this.posY) > this.posZ) return;
        if (this.age < 0) this.age = 0;

        this.type.draw(this);
    }

    public static Effect createNewEffect(double x, double y, EffectType type, double age)
    {
        return Effect.createNewEffect(x, y, 0, type, age);
    }

    public static Effect createNewEffect(double x, double y, double z, EffectType type, double age)
    {
        Effect e = Effect.createNewEffect(x, y, z, type);
        e.age = age;
        return e;
    }

    public static Effect createNewEffect(double x, double y, EffectType type)
    {
        return Effect.createNewEffect(x, y, 0, type);
    }

    /**
     * Use Effect.createNewEffect(double x, double y, EffectType type) instead of this because it can refurbish and reuse old effects
     */
    protected Effect()
    {
        super(0, 0);
    }

    protected void refurbish()
    {
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
        this.vX = 0;
        this.vY = 0;
        this.vZ = 0;
        this.type = null;
        this.age = 0;
        this.colR = 0;
        this.colG = 0;
        this.colB = 0;
        this.glowR = 0;
        this.glowG = 0;
        this.glowB = 0;
        this.maxAge = Math.random() * 100 + 50;
        this.size = 0;
        this.angle = 0;
        this.distance = 0;
        this.radius = 0;
        this.enableGlow = true;
        this.drawLayer = 7;
        this.state = State.live;
        this.force = false;
        this.fastRemoveOnExit = false;
    }

    public Effect setColor(double r, double g, double b)
    {
        this.colR = r;
        this.colG = g;
        this.colB = b;
        return this;
    }

    public Effect setColor(double r, double g, double b, double noise)
    {
        this.colR = r + (Math.random() - 0.5) * noise;
        this.colG = g + (Math.random() - 0.5) * noise;
        this.colB = b + (Math.random() - 0.5) * noise;
        return this;
    }

    public Effect setGlowColor(double r, double g, double b)
    {
        this.glowR = r;
        this.glowG = g;
        this.glowB = b;
        return this;
    }

    public Effect setRadius(double radius)
    {
        this.radius = radius;
        return this;
    }

    public Effect setSize(double size)
    {
        this.size = size;
        return this;
    }

    public void drawGlow()
    {
        if (this.maxAge > 0 && this.maxAge < this.age)
            return;

        if (!this.force && Game.sampleObstacleHeight(this.posX, this.posY) > this.posZ)
            return;

        if (this.age < 0)
            this.age = 0;

        Drawing drawing = Drawing.drawing;

        if (this.type == EffectType.piece)
        {
            double size = 1 + Bullet.bullet_size * (1 - this.age / this.maxAge);

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 127, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size * 8, size * 8);
            else
                drawing.fillGlow(this.posX, this.posY, size * 8, size * 8);
        }
        if (this.type == EffectType.interfacePiece)
        {
            double size = 1 + Bullet.bullet_size * (1 - this.age / this.maxAge);

            if (this.size > 0)
                size *= this.size;

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 127, 1);

            drawing.fillInterfaceGlow(this.posX, this.posY, size * 8, size * 8);
        }
        else if (this.type == EffectType.interfacePieceSparkle)
        {
            double size = 1 + Bullet.bullet_size * (1 - this.age / this.maxAge);

            if (this.size > 0)
                size *= this.size;

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 127, 1);

            drawing.fillInterfaceGlow(this.posX, this.posY, size * 8, size * 8);
            drawing.fillInterfaceGlowSparkle(this.posX, this.posY, 0, size * 4, this.age / 100.0 * this.radius);
        }
        else if (this.type == EffectType.charge)
        {
            double size = 1 + Bullet.bullet_size * (this.age / this.maxAge);

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 127, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size * 8, size * 8);
            else
                drawing.fillGlow(this.posX, this.posY, size * 8, size * 8);
        }
        else if (this.type == EffectType.stun)
        {
            double size = 1 + this.size * Math.min(Math.min(1, (this.maxAge - this.age) * 3 / this.maxAge), Math.min(1, this.age * 3 / this.maxAge));
            double angle = this.angle + this.age / 20;
            double distance = 1 + this.distance * Math.min(Math.min(1, (this.maxAge - this.age) * 3 / this.maxAge), Math.min(1, this.age * 3 / this.maxAge));

            double[] o = Movable.getLocationInDirection(angle, distance);

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 255, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX + o[0], this.posY + o[1], this.posZ, size * 8, size * 8);
            else
                drawing.fillGlow(this.posX + o[0], this.posY + o[1], size * 8, size * 8);
        }
        else if (this.type == EffectType.glow)
        {
            double size = 1 + 40 * (1 - this.age / this.maxAge);

            drawing.setColor(255, 255, 255, 40, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size * 8, size * 8, false, true);
            else
                drawing.fillGlow(this.posX, this.posY, size * 8, size * 8);
        }
        else if (this.type == EffectType.teleporterPiece)
        {
            double size = 1 + Bullet.bullet_size * (1 - this.age / this.maxAge);

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 127, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size * 8, size * 8, false, true);
            else
                drawing.fillGlow(this.posX, this.posY, size * 8, size * 8);
        }
        else if (this.type == EffectType.snow)
        {
            double size = this.size * (1 + this.age / this.maxAge);
            drawing.setColor(this.colR, this.colG, this.colB, (1 - this.age / this.maxAge) * 255);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size, size, true);
            else
                drawing.fillGlow(this.posX, this.posY, size, size, true);

            /*if (Game.enable3d)
                drawing.drawImage("glow.png", this.posX, this.posY, this.posZ, size, size);
            else
                drawing.drawImage("glow.png", this.posX, this.posY, size, size);*/
        }
        else if (this.type == EffectType.ray)
        {
            drawing.setColor(255, 255, 255, Math.min(0.5, 1 - Level.currentLightIntensity) * 150, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, 24, 24, false);
            else
                drawing.fillGlow(this.posX, this.posY, 24, 24, false);
        }
    }

    @Override
    public boolean isGlowEnabled()
    {
        return this.enableGlow;
    }

    @Override
    public void update()
    {
        this.posX += this.vX * Panel.frameFrequency;
        this.posY += this.vY * Panel.frameFrequency;
        this.posZ += this.vZ * Panel.frameFrequency;

        if (this.maxAge >= 0)
            this.age += Panel.frameFrequency;

        if (this.fastRemoveOnExit && ScreenGame.finishedQuick)
            this.age += Panel.frameFrequency * 4;

        if (this.maxAge > 0 && this.age > this.maxAge && this.state == State.live)
        {
            this.state = State.removed;

            if (Game.effects.contains(this) && !Game.removeEffects.contains(this))
            {
                Game.removeEffects.add(this);
            }
            else if (Game.tracks.contains(this) && !Game.removeTracks.contains(this))
            {
                Drawing.drawing.trackRenderer.remove(this);
                Game.removeTracks.add(this);
            }
        }

        if (this.type == EffectType.obstaclePiece3d)
        {
            int x = (int) Math.floor(this.posX / Game.tile_size);
            int y = (int) Math.floor(this.posY / Game.tile_size);

            boolean collidedX = false;
            boolean collidedY = false;
            boolean collided;

            if (x < 0 || x >= Game.currentSizeX)
                collidedX = true;

            if (y < 0 || y >= Game.currentSizeY)
                collidedY = true;

            if (this.posZ <= 5)
            {
                this.vZ = -0.6 * this.vZ;
                this.vX *= 0.8;
                this.vY *= 0.8;
                this.posZ = 10 - this.posZ;
            }

            if (!(collidedX || collidedY))
            {
                collided = this.posZ <= Chunk.getTile(x, y).lastHeight;

                if (collided && prevGridX >= 0 && prevGridX < Game.currentSizeX && prevGridY >= 0 && prevGridY < Game.currentSizeY && Chunk.getTile(x, y).lastHeight != Chunk.getTile(prevGridX, prevGridY).lastHeight)
                {
                    collidedX = this.prevGridX != x;
                    collidedY = this.prevGridY != y;
                }
            }
            else
                collided = true;

            this.vZ -= 0.1 * Panel.frameFrequency;

            if (collided)
            {
                this.vX *= 0.8;
                this.vY *= 0.8;

                if (collidedX)
                {
                    double barrierX = this.prevGridX * Game.tile_size;

                    if (this.vX > 0)
                        barrierX += Game.tile_size;

                    double dist = this.posX - barrierX;

                    this.vX = -this.vX;
                    this.posX = this.posX - dist;
                }

                if (collidedY)
                {
                    double barrierY = this.prevGridY * Game.tile_size;

                    if (this.vY > 0)
                        barrierY += Game.tile_size;

                    double dist = this.posY - barrierY;

                    this.vY = -this.vY;
                    this.posY = this.posY - dist;
                }

                if (!collidedX && !collidedY && (x != this.initialGridX || y != initialGridY) && Math.abs(this.posZ - Chunk.getTile(x, y).lastHeight) < Game.tile_size / 2)
                {
                    this.vZ = -0.6 * this.vZ;
                    this.posZ = 2 * Chunk.getTile(x, y).lastHeight - this.posZ;
                }
            }

            this.prevGridX = (int) (this.posX / Game.tile_size);
            this.prevGridY = (int) (this.posY / Game.tile_size);
        }
    }

    public void firstDraw()
    {
        Drawing.drawing.setColor(0, 0, 0, 64);
        Drawing.drawing.trackRenderer.addRect(this, this.posX, this.posY, this.posZ, size * Obstacle.draw_size / Game.tile_size, size * Obstacle.draw_size / Game.tile_size, angle);
    }

    @Override
    public boolean lit()
    {
        return (Game.fancyLights && type == EffectType.explosion);
    }

    @Override
    public double[] getLightInfo()
    {
        this.lightInfo[3] = 4 * (1 - this.age / this.maxAge);
        this.lightInfo[4] = 255;
        this.lightInfo[5] = 50;
        this.lightInfo[6] = 40;
        return this.lightInfo;
    }
}
