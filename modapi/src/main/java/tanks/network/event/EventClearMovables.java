package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;

public class EventClearMovables extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            for (Movable m : Game.movables)
                m.destroy = true;
        }
    }

    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }
}
