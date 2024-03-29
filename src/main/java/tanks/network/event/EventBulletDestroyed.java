package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.bullet.Bullet;
import tanks.bullet.BulletInstant;

public class EventBulletDestroyed extends PersonalEvent
{
    public Bullet bullet;
    public int bulletID;
    public double posX;
    public double posY;

    public EventBulletDestroyed()
    {

    }

    public EventBulletDestroyed(Bullet b)
    {
        this.bullet = b;
        this.posX = b.posX;
        this.posY = b.posY;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        this.bullet = Bullet.idMap.get(bulletID);

        if (bullet == null)
            return;

        if (bullet instanceof BulletInstant i)
        {
            i.remoteShoot();
        }
        else
        {
            bullet.posX = posX;
            bullet.posY = posY;
        }

        bullet.destroy = true;

        if (!Bullet.freeIDs.contains(bullet.networkID))
        {
            Bullet.freeIDs.add(bullet.networkID);
            Bullet.idMap.remove(bullet.networkID);
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.bullet.networkID);
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.bulletID = b.readInt();
        this.posX = b.readDouble();
        this.posY = b.readDouble();
    }
}
