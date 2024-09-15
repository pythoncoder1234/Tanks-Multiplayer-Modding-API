package com.orangishcat.modapi.mixin;

import com.orangishcat.modapi.MusicState;
import com.orangishcat.modapi.gui.screen.IGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenGame;

@Mixin(ScreenGame.class)
public abstract class ScreenGameMixin extends Screen implements IGame
{
    @Unique public MusicState musicState;

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "isValid", ordinal = 0))
    public boolean redirectMusic(InputBindingGroup instance)
    {

        return false;
    }

    @Override
    public void setMusicState(MusicState state)
    {
        this.musicState = state;
    }

    @Override
    public MusicState getMusicState()
    {
        return this.musicState;
    }
}
