package com.orangishcat.modapi.network.event;

import io.netty.buffer.ByteBuf;
import tanks.EndCondition;
import tanks.gui.screen.ScreenGame;
import tanks.network.event.PersonalEvent;

public class EventCustomLevelEndCondition extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        ScreenGame g = ModAPI.getGameInstance();
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
