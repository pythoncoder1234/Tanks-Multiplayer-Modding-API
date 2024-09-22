package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.screen.ScreenGame;

public class EventDisableMinimap extends PersonalEvent
{
    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }

    @Override
    public void execute()
    {
        ScreenGame g = ScreenGame.getInstance();
        if (g != null)
            g.minimap.forceDisabled = true;
    }
}
