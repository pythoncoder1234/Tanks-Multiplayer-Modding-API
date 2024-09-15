package com.orangishcat.modapi;

public class MusicState
{
    public String music;
    public String musicID;

    // for changing music of ScreenGame
    public String intro = "";
    public boolean endMusic = true;

    public MusicState(String music, String id)
    {
        this.music = music;
        this.musicID = id;
    }

    public MusicState(String intro, String main, boolean endMusic)
    {
        this.intro = intro;
        this.music = main;
        this.musicID = "battle";
        this.endMusic = endMusic;
    }

    public void applyMusic()
    {
        ModAPI.setScreenMusic(this);
    }
}
