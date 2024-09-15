package com.orangishcat.modapi.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;
import tanks.network.event.PersonalEvent;

public class EventSetLevelTimer extends PersonalEvent
{
    public int seconds;

    public EventSetLevelTimer() {}

    public EventSetLevelTimer(int seconds)
    {
        this.seconds = seconds;
    }


    @Override
    public void execute()
    {
        if (Game.currentLevel != null)
        {
            Game.currentLevel.timed = seconds > 0;
            Game.currentLevel.timer = seconds * 100;
        }

        ScreenGame g = ScreenGame.getInstance();
        if (g != null)
            g.timeRemaining = seconds * 100;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.seconds);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.seconds = b.readInt();
    }
}
