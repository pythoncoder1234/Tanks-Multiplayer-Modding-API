package tanks.rendering;

import basewindow.BaseWindow;

@RendererDrawLayer(7)
public class ShaderWater extends RendererShader implements IObstacleSizeShader, IGroundHeightShader
{
    public Uniform1f obstacleSizeFrac;
    public Attribute1f groundHeight;

    public ShaderWater(BaseWindow w)
    {
        super(w, "water");
        this.depthMask = false;
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_water.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_ice.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public Attribute1f getGroundHeight()
    {
        return groundHeight;
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }
}
