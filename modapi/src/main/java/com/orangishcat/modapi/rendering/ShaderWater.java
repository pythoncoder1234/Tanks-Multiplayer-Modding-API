package com.orangishcat.modapi.rendering;

import basewindow.BaseWindow;
import tanks.rendering.IObstacleTimeShader;
import tanks.rendering.RendererDrawLayer;
import tanks.rendering.RendererShader;

@RendererDrawLayer(7)
public class ShaderWater extends RendererShader implements IObstacleTimeShader
{
    public Uniform1i time;

    public ShaderWater(BaseWindow w)
    {
        super(w, "water");
        this.depthMask = false;
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_water.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_water.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setTime(int time)
    {
        this.time.set(time);
    }
}
