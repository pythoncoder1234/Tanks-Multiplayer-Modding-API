package basewindow;

import java.util.HashSet;

/**
 * A shader group allows for shaders to share the same uniforms and attributes
 * across both normal and shadow map render mode shaders.
 */
public class ShaderGroup
{
    public basewindow.ShaderBase shaderBase;
    public basewindow.ShaderShadowMap shaderShadowMap;
    public HashSet<Attribute> attributes = new HashSet<>();
    public basewindow.BaseWindow window;

    public Uniform1b texture;

    public String name;
    protected int random = (int) (Math.random() * 100);

    public ShaderGroup(basewindow.BaseWindow w, String name)
    {
        this.window = w;
        this.shaderBase = new ShaderBase(w);
        this.shaderBase.group = this;
        this.shaderShadowMap = new ShaderShadowMap(w);
        this.shaderShadowMap.group = this;
        this.name = name;
    }

    public void initialize() throws Exception
    {
        this.shaderShadowMap.setUp
                ("/shaders/shadow_map.vert", new String[]{"/shaders/main_default.vert"},
                "/shaders/shadow_map.frag", null);
        this.shaderBase.setUp
                ("/shaders/main.vert", new String[]{"/shaders/main_default.vert"},
                "/shaders/main.frag", new String[]{"/shaders/main_default.frag"});
    }

    public void setVertexBuffer(int id)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setVertexBuffer(id);
        else
            this.shaderBase.util.setVertexBuffer(id);
    }

    public void setColorBuffer(int id)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setColorBuffer(id);
        else
            this.shaderBase.util.setColorBuffer(id);
    }

    public void setTexCoordBuffer(int id)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setTexCoordBuffer(id);
        else
            this.shaderBase.util.setTexCoordBuffer(id);
    }

    public void setNormalBuffer(int id)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setNormalBuffer(id);
        else
            this.shaderBase.util.setNormalBuffer(id);
    }

    public void setCustomBuffer(Attribute attribute, int bufferID, int size)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setCustomBuffer(attribute.shadowMapAttribute, bufferID, size);
        else
            this.shaderBase.util.setCustomBuffer(attribute.normalAttribute, bufferID, size);
    }

    public void drawVBO(int numberIndices)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.drawVBO(numberIndices);
        else
            this.shaderBase.util.drawVBO(numberIndices);
    }

    public void set()
    {
        if (this.window.drawingShadow)
            this.window.setShader(this.shaderShadowMap);
        else
            this.window.setShader(this.shaderBase);
    }

    public abstract static class Attribute
    {
        public int count;

        public basewindow.ShaderProgram.Attribute normalAttribute;
        public basewindow.ShaderProgram.Attribute shadowMapAttribute;

        public Attribute(int count)
        {
            this.count = count;
        }
    }

    public static class Attribute1f extends Attribute
    {
        public Attribute1f()
        {
            super(1);
        }
    }

    public static class Attribute2f extends Attribute
    {
        public Attribute2f()
        {
            super(2);
        }
    }

    public static class Attribute3f extends Attribute
    {
        public Attribute3f()
        {
            super(3);
        }
    }

    public static class Attribute4f extends Attribute
    {
        public Attribute4f()
        {
            super(4);
        }
    }

    public interface IGroupUniform
    {
        void setWindow(basewindow.BaseWindow window);

        void bind(boolean shadow);
    }

    public static abstract class GroupPrimitiveUniform<T, U extends basewindow.ShaderProgram.IPrimitiveUniform<T>> implements IGroupUniform
    {
        protected U baseUniform;
        protected U shadowMapUniform;

        protected basewindow.BaseWindow window;

        public void set(T t)
        {
            if (!window.drawingShadow)
                baseUniform.set(t);
            else
                shadowMapUniform.set(t);
        }

        public void setWindow(basewindow.BaseWindow w)
        {
            this.window = w;
        }

        public void bind(boolean shadow)
        {
            if (shadow)
                shadowMapUniform.bind();
            else
                baseUniform.bind();
        }
    }

    public static abstract class GroupMatrixUniform<T extends basewindow.ShaderProgram.IMatrixUniform>
    {
        protected T baseUniform;
        protected T shadowMapUniform;
        protected basewindow.BaseWindow window;

        public void set(float[] f, boolean transpose)
        {
            if (!window.drawingShadow)
                baseUniform.set(f, transpose);
            else
                shadowMapUniform.set(f, transpose);
        }

        public void setWindow(BaseWindow w)
        {
            this.window = w;
        }

        public void bind(boolean shadow)
        {
            if (shadow)
                shadowMapUniform.bind();
            else
                baseUniform.bind();
        }
    }

    public static class Uniform1b extends GroupPrimitiveUniform<Boolean, basewindow.ShaderProgram.Uniform1b> { }

    public static class Uniform1i extends GroupPrimitiveUniform<Integer, basewindow.ShaderProgram.Uniform1i> { }

    public static class Uniform2i extends GroupPrimitiveUniform<int[], basewindow.ShaderProgram.Uniform2i> { }

    public static class Uniform3i extends GroupPrimitiveUniform<int[], basewindow.ShaderProgram.Uniform3i> { }

    public static class Uniform4i extends GroupPrimitiveUniform<int[], basewindow.ShaderProgram.Uniform4i> { }

    public static class Uniform1f extends GroupPrimitiveUniform<Float, basewindow.ShaderProgram.Uniform1f> { }

    public static class Uniform2f extends GroupPrimitiveUniform<float[], basewindow.ShaderProgram.Uniform2f> { }

    public static class Uniform3f extends GroupPrimitiveUniform<float[], basewindow.ShaderProgram.Uniform3f> { }

    public static class Uniform4f extends GroupPrimitiveUniform<float[], basewindow.ShaderProgram.Uniform4f> { }

    public static class UniformMatrix2 extends GroupMatrixUniform<basewindow.ShaderProgram.UniformMatrix2> { }

    public static class UniformMatrix3 extends GroupMatrixUniform<basewindow.ShaderProgram.UniformMatrix3> { }

    public static class UniformMatrix4 extends GroupMatrixUniform<ShaderProgram.UniformMatrix4> { }

    @Override
    public String toString()
    {
        return this.name + this.random;
    }
}
