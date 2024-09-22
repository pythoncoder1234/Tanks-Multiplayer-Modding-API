package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.screen.ScreenGame;

public class EventSkipCountdown extends PersonalEvent
{
    @Override
    public void execute()
    {
        ScreenGame g = ScreenGame.getInstance();
        if (g != null)
            g.playing = true;
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
