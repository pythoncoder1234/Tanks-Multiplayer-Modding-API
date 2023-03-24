package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;

public class EventDisableSpeedrunTimer extends PersonalEvent
{
    @Override
    public void execute()
    {
        Game.screen.hideSpeedrunTimer = true;
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
