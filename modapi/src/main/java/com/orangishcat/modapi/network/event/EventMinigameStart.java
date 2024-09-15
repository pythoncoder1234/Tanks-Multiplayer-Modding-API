package com.orangishcat.modapi.network.event;

import com.orangishcat.modapi.Minigame;
import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.network.NetworkUtils;
import tanks.network.event.PersonalEvent;

public class EventMinigameStart extends PersonalEvent
{
    public String name;

    public EventMinigameStart() {}

    public EventMinigameStart(String name)
    {
        this.name = name;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.name);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.name = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        try
        {
            Class<? extends Minigame> cls = Game.registryMinigame.getEntry(this.name);
            if (cls == null)
            {
                Panel.notifs.add(new Notification("Warning: minigame '" + this.name + "' was not found in the registry.\nMinigame loading will be skipped.", 500));
                return;
            }

            ModAPI.currentGame = cls.getConstructor().newInstance();
            ModAPI.currentGame.remote = true;
            ModAPI.currentGame.startBase();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
