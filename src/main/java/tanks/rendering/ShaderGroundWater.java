package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderGroundWater extends RendererShader implements IObstacleSizeShader
{
    public Uniform1f obstacleSizeFrac;

    public ShaderGroundWater(BaseWindow w)
    {
        super(w, "ground_water");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_ground_water.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_ground_water.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }
}
