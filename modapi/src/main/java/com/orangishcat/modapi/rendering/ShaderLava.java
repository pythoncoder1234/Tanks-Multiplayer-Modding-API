package com.orangishcat.modapi.rendering;

import basewindow.BaseWindow;
import tanks.rendering.IObstacleTimeShader;
import tanks.rendering.RendererShader;

public class ShaderLava extends RendererShader implements IObstacleTimeShader
{
    public Uniform1i time;

    public ShaderLava(BaseWindow w)
    {
        super(w, "lava");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_lava.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_lava.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setTime(int time)
    {
        this.time.set(time);
    }
}
