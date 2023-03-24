package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.ModAPI;

public class EventClearMenuGroup extends PersonalEvent
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
        ModAPI.fixedMenus.clear();
    }
}
