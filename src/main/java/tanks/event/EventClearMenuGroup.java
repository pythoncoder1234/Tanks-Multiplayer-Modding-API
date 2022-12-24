package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
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
        ModAPI.menuGroup.clear();
    }
}
