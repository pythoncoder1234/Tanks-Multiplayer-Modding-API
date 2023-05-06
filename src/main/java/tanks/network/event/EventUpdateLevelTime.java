package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.gui.screen.ScreenGame;

public class EventUpdateLevelTime extends PersonalEvent
{
    public EventUpdateLevelTime() {}

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(ScreenGame.lastTimePassed);

        if (Crusade.currentCrusade != null)
            b.writeDouble(Crusade.currentCrusade.timePassed);
    }

    @Override
    public void read(ByteBuf b)
    {
        ScreenGame.lastTimePassed = b.readDouble();

        if (Crusade.currentCrusade != null)
            Crusade.currentCrusade.timePassed = b.readDouble();
    }

    @Override
    public void execute()
    {

    }
}
