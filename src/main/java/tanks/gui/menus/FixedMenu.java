package tanks.gui.menus;

import tanks.IDrawable;
import tanks.ModAPI;
import tanks.Panel;
import tanks.gui.TextWithStyling;
import tanks.network.ISyncable;
import tanks.network.SyncedFieldMap;

import java.util.ArrayList;

public abstract class FixedMenu implements ISyncable, IDrawable
{
    public double id = Math.random() * Double.MAX_VALUE;

    public ArrayList<Animation> animations = new ArrayList<>();
    public SyncedFieldMap map = new SyncedFieldMap();
    public boolean syncEnabled;

    public double posX = 0;
    public double posY = 0;
    public double posZ = 0;
    public double sizeX = 300;
    public double sizeY = 300;
    public double sizeZ = 1;
    public double duration = 0;
    public boolean afterGameStarted = false;
    public int drawLevel = 3;

    public TextWithStyling styling = new TextWithStyling();
    public double glow = 0;

    public double age = 0;


    public FixedMenu()
    {
        ModAPI.ids.put(this.id, this);
        this.syncEnabled = false;
    }

    public void draw()
    {

    }

    public void updateAnimations()
    {
        Animation.Properties prev = new Animation.Properties(this);

        this.styling.changedByAnimation = !animations.isEmpty();

        for (int i = 0; i < animations.size(); i++)
        {
            Animation a = animations.get(i);

            if (a.menu != this)
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

            if (properties.posX / properties.posY != prev.posX / prev.posY)
            {
                this.posX = properties.posX;
                this.posY = properties.posY;
            }

            if (properties.sizeX / properties.sizeY != prev.sizeX / prev.sizeY)
            {
                this.sizeX = properties.sizeX;
                this.sizeY = properties.sizeY;
            }

            if (properties.colorR / properties.colorG / properties.colorB != prev.colorR / prev.colorG / prev.colorB)
            {
                this.styling.colorR = properties.colorR;
                this.styling.colorG = properties.colorG;
                this.styling.colorB = properties.colorB;
            }

            if (properties.colorA != prev.colorA)
                this.styling.colorA = properties.colorA;
        }
    }

    public void update()
    {
        if (age <= 0 && syncEnabled)
            initSync();

        this.age += Panel.frameFrequency;
        this.updateAnimations();
    }
}
