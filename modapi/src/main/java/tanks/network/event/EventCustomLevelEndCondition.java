package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.EndCondition;
import tanks.gui.screen.ScreenGame;

public class EventCustomLevelEndCondition extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        ScreenGame g = ScreenGame.getInstance();
        if (g != null)
            g.endCondition = EndCondition.neverEnd;
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
