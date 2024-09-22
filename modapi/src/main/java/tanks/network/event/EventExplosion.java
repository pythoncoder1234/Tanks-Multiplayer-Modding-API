package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Explosion;
import tanks.tank.Tank;

import java.util.Objects;

public class EventExplosion extends PersonalEvent
{
    public double posX;
    public double posY;
    public double radius;
    public boolean destroysObstacles;
    public int source = -1;

    public Tank sourceTank;

    public EventExplosion()
    {

    }

    public EventExplosion(Explosion e)
    {
        this.posX = e.posX;
        this.posY = e.posY;
        this.radius = e.radius;
        this.destroysObstacles = e.destroysObstacles;

        if (e.tank != null)
        {
            this.source = e.tank.networkID;
            this.sourceTank = e.tank;
        }
    }

    @Override
    public void execute()
    {
        if (clientID == null)
        {
            sourceTank = Objects.requireNonNullElse(Tank.idMap.get(source), Game.dummyTank);
            Explosion e = new Explosion(this.posX, this.posY, this.radius, 0, destroysObstacles, sourceTank);
            e.explode();
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.radius);
        b.writeBoolean(this.destroysObstacles);

        if (!Game.vanillaMode)
            b.writeInt(this.source);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.radius = b.readDouble();
        this.destroysObstacles = b.readBoolean();

        if (!Game.vanillaMode)
            this.source = b.readInt();
    }
}
