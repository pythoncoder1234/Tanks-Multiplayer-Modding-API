package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.Level;
import tanks.event.PersonalEvent;
import tanks.modapi.MapLoader;
import tanks.network.NetworkUtils;

public class EventLoadMapLevel extends PersonalEvent
{
    public String levelString;
    public int offsetX;
    public int offsetY;
    public int moveX;
    public int moveY;

    public EventLoadMapLevel() {}

    public EventLoadMapLevel(Level l, int offsetX, int offsetY, int moveX, int moveY)
    {
        this.levelString = l.levelString;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.moveX = moveX;
        this.moveY = moveY;
    }

    @Override
    public void execute()
    {
        Level l = new Level(levelString);

        MapLoader.Section.moveObjects(this.moveX, this.moveY);
        MapLoader.Section.loadLevelWithOffset(l, this.offsetX, this.offsetY);
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.levelString);
        b.writeInt(this.offsetX);
        b.writeInt(this.offsetY);
        b.writeInt(this.moveX);
        b.writeInt(this.moveY);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.levelString = NetworkUtils.readString(b);
        this.offsetX = b.readInt();
        this.offsetY = b.readInt();
        this.moveX = b.readInt();
        this.moveY = b.readInt();
    }
}
