package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Level;
import tanks.MapLoader;
import tanks.network.NetworkUtils;

public class EventLoadMapLevel extends PersonalEvent
{
    public String levelString;
    public int offsetX;
    public int offsetY;

    public EventLoadMapLevel() {}

    public EventLoadMapLevel(Level l, int offsetX, int offsetY)
    {
        this.levelString = l.levelString;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void execute()
    {
        Level l = new Level(levelString);

        MapLoader.Section.loadLevelWithOffset(l, this.offsetX, this.offsetY);
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.levelString);
        b.writeInt(this.offsetX);
        b.writeInt(this.offsetY);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.levelString = NetworkUtils.readString(b);
        this.offsetX = b.readInt();
        this.offsetY = b.readInt();
    }
}
