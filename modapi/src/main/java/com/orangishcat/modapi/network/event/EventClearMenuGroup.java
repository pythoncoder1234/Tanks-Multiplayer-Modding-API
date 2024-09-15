package com.orangishcat.modapi.network.event;

import io.netty.buffer.ByteBuf;
import com.orangishcat.modapi.ModAPI;
import tanks.network.event.PersonalEvent;

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
