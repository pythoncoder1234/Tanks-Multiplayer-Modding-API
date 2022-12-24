package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;

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

        if (Game.screen instanceof ScreenGame)
            ((ScreenGame) Game.screen).timeRemaining = seconds * 100;
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
