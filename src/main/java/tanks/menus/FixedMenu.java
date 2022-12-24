package tanks.menus;

import tanks.Animations.Animation;
import tanks.IDrawable;
import tanks.ModAPI;
import tanks.Panel;

import java.util.ArrayList;

public abstract class FixedMenu implements IDrawable
{
    public double id = Math.random() * Double.MAX_VALUE;

    public double posX = 0;
    public double posY = 0;
    public double posZ = 0;
    public double sizeX = 300;
    public double sizeY = 300;
    public double sizeZ = 1;
    public double duration = 0;
    public boolean afterGameStarted = false;
    public int drawLevel = 3;

    public double colorR = 0;
    public double colorG = 0;
    public double colorB = 0;
    public double colorA = 255;
    public double glow = 0;

    public double age = 0;

    public ArrayList<Animation> animations = new ArrayList<>();

    public FixedMenu()
    {
        ModAPI.ids.put(this.id, this);
    }

    public void draw()
    {

    }

    public void updateAnimations()
    {
        for (int i = 0; i < animations.size(); i++)
        {
            Animation a = animations.get(i);

            if (!a.initialized)
                a.init(this);

            if (a.age > a.duration)
            {
                if (a.onEnd != null)
                    a.onEnd.run();

                animations.remove(a);
                i--;
                continue;
            }

            Animation.Properties properties = a.update();

            this.posX = properties.posX;
            this.posY = properties.posY;
            this.sizeX = properties.sizeX;
            this.sizeY = properties.sizeY;

            this.colorR = properties.colorR;
            this.colorG = properties.colorG;
            this.colorB = properties.colorB;
            this.colorA = properties.colorA;
        }
    }

    public void update()
    {
        this.age += Panel.frameFrequency;
        this.updateAnimations();
    }
}
