package com.orangishcat.modapi.rendering;

import basewindow.BaseWindow;
import basewindow.OnlyBaseUniform;
import basewindow.ShaderGroup;
import tanks.rendering.IObstacleSizeShader;

public class ShaderTrainTrack extends ShaderGroup implements IObstacleSizeShader
{
    @OnlyBaseUniform
    public Uniform1f obstacleSizeFrac;

    public ShaderTrainTrack(BaseWindow w)
    {
        super(w, "train_track");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_train_tracks.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_train_tracks.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }
}
