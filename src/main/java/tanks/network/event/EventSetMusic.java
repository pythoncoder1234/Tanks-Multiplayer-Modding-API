package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.MusicState;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;
import tanks.network.NetworkUtils;

public class EventSetMusic extends PersonalEvent
{
    public boolean forGame = Game.screen instanceof ScreenGame;
    public String music;
    public String musicID = "battle";
    public String introMusic = "";
    public boolean endMusic = true;

    public EventSetMusic() {}

    public EventSetMusic(String music)
    {
        if (forGame && ((ScreenGame) Game.screen).playCounter == -1)
            this.introMusic = music;
        else
            this.music = music;
    }

    public EventSetMusic(MusicState state)
    {
        this.music = state.music;
        this.musicID = state.musicID;
        this.introMusic = state.intro;
        this.endMusic = state.endMusic;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, music);

        b.writeBoolean(forGame);
        if (forGame)
        {
            NetworkUtils.writeString(b, introMusic);
            b.writeBoolean(endMusic);
        }
        else
            NetworkUtils.writeString(b, musicID);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.music = NetworkUtils.readString(b);

        if (b.readBoolean())
        {
            this.introMusic = NetworkUtils.readString(b);
            this.endMusic = b.readBoolean();
        }
        else
            this.musicID = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        if (Game.screen instanceof ScreenGame)
        {
            ScreenGame s = (ScreenGame) Game.screen;
            s.introMusic = introMusic;
            s.mainMusic = music;
            s.endMusic = endMusic;
        }
        else
        {
            if (!introMusic.equals(""))
                System.err.println("WARNING (MusicState.applyMusic): Game.screen was not an instance of ScreenGame when called!");

            Game.screen.music = music;
            Game.screen.musicID = musicID;
        }

        Panel.forceRefreshMusic = true;
    }
}
