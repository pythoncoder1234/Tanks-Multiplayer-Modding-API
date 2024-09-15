package com.orangishcat.modapi.gui.screen;

import com.orangishcat.modapi.MusicState;

public interface IGame
{
    void setMusicState(MusicState state);

    MusicState getMusicState();
}
