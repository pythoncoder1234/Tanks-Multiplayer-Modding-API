package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.obstacle.Obstacle;

public class EventSetObstacle extends PersonalEvent
{
    public int posX;
    public int posY;
    public String registryName;
    public double stackHeight;
    public double startHeight;

    public EventSetObstacle()
    {
    }

    public EventSetObstacle(int posX, int posY, String registryName, double stackHeight, double startHeight)
    {
        this.posX = posX;
        this.posY = posY;
        this.registryName = registryName;
        this.stackHeight = stackHeight;
        this.startHeight = startHeight;
    }

    @Override
    public void execute()
    {
        try
        {
            Obstacle o = Game.registryObstacle.getEntry(registryName).obstacle
                    .getConstructor(String.class, double.class, double.class)
                    .newInstance(registryName, posX, posY);

            o.stackHeight = stackHeight;
            o.startHeight = startHeight;
            Game.addObstacle(o);
        }
        catch (Exception e)
        {
            System.err.println("Warning: Bad obstacle registry name provided to setObstacle");
            e.printStackTrace();
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.posX);
        b.writeInt(this.posY);

        NetworkUtils.writeString(b, this.registryName);
        b.writeDouble(this.stackHeight);
        b.writeDouble(this.startHeight);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readInt();
        this.posY = b.readInt();

        this.registryName = NetworkUtils.readString(b);
        this.stackHeight = b.readDouble();
        this.startHeight = b.readDouble();
    }
}
