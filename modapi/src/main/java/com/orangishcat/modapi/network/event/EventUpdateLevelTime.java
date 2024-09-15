package com.orangishcat.modapi.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.gui.screen.ScreenGame;
import tanks.network.event.PersonalEvent;

public class EventUpdateLevelTime extends PersonalEvent
{
    public EventUpdateLevelTime() {}

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(ScreenGame.lastTimePassed);

        if (Crusade.crusadeMode)
            b.writeDouble(Crusade.currentCrusade.timePassed);
    }

    @Override
    public void read(ByteBuf b)
    {
        ScreenGame.lastTimePassed = b.readDouble();

        if (Crusade.crusadeMode)
            Crusade.currentCrusade.timePassed = b.readDouble();
    }

    @Override
    public void execute()
    {

    }
}
