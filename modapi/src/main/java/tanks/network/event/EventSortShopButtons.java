package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.screen.ScreenGame;

public class EventSortShopButtons extends PersonalEvent
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
        ScreenGame s = ScreenGame.getInstance();
        if (s != null && this.clientID == null)
        {
            s.initializeShopList();
        }
    }
}
