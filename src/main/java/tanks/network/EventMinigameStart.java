package tanks.network;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Minigame;
import tanks.Panel;
import tanks.gui.ScreenElement.Notification;
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
                Panel.currentNotification = new Notification("Warning: minigame '" + this.name + "' was not found in the registry.\nMinigame loading will be skipped.", 500);
                return;
            }

            Game.currentGame = cls.getConstructor().newInstance();
            Game.currentGame.startBase();
            Game.currentGame.remote = true;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
