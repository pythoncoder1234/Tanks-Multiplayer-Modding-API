package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;
import tanks.network.NetworkUtils;

public class EventSetMusic extends PersonalEvent
{
    public String music;

    public EventSetMusic() {}

    public EventSetMusic(String music)
    {
        this.music = music;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, music);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.music = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        Game.screen.music = music;

        String musicID = music.replace(".ogg", "");
        int index = music.indexOf("_");
        if (index > -1)
            musicID = musicID.substring(0, index);

        Game.screen.musicID = musicID;

        if (Game.screen instanceof ScreenGame)
            ((ScreenGame) Game.screen).tankMusic = false;

        Panel.forceRefreshMusic = true;
    }
}
