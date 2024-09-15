package com.orangishcat.modapi.network.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.screen.ScreenGame;
import tanks.network.event.PersonalEvent;

public class EventSkipCountdown extends PersonalEvent
{
    @Override
    public void execute()
    {
        ScreenGame g = ModAPI.getGameInstance();
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
