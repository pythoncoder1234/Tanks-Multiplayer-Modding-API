package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventDisableSpeedrunTimer extends PersonalEvent
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
        if (Game.screen instanceof ScreenGame)
            ((ScreenGame) Game.screen).noSpeedrunTimer = true;
        else
            ScreenGame.disableSpeedrunTimer = true;
    }
}
