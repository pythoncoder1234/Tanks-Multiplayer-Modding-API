package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.EndCondition;
import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventCustomLevelEndCondition extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        if (Game.screen instanceof ScreenGame)
            ((ScreenGame) Game.screen).endCondition = EndCondition.neverEnd;
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
