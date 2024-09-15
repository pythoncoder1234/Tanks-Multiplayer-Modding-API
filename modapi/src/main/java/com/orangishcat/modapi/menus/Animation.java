package com.orangishcat.modapi.menus;

import com.orangishcat.modapi.NetworkFieldUtils;
import io.netty.buffer.ByteBuf;
import tanks.Panel;
import tanks.network.NetworkUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Animation
{
    public static Animation[] arcadeText;
    public static Animation[] textChangeAnimation;

    public static void registerAnimations()
    {
        arcadeText = new Animation[] {new ZoomFrom(25, 70)};
        textChangeAnimation = new Animation[] {new OpacityChange(25, 255).withReverse(true), new ZoomTo(25, 125).withReverse(true)};

        registerAnimation(FadeIn.class);
        registerAnimation(FadeOut.class);
        registerAnimation(ZoomFrom.class, "finalSX", "finalSY", "startPercentage");
        registerAnimation(ZoomTo.class, "finalSX", "finalSY", "startPercentage");
        registerAnimation(OpacityChange.class, "changeTo");
    }


    public static ArrayList<AnimationEntry> arr = new ArrayList<>();
    public static HashMap<Class<? extends Animation>, Integer> map = new HashMap<>();
    protected static int currentID = -1;

    public FixedMenu menu = null;
    public Properties currentProperties = null;

    /** in centiseconds */
    public double duration;
    public double age = 0;
    public boolean withReverse = false;
    public boolean reverse = false;

    public Runnable onEnd = null;

    public Animation(double duration)
    {
        this.duration = duration;
    }

    protected Animation()
    {
    }

    public void init(FixedMenu menu)
    {
        this.menu = menu;
        this.currentProperties = new Properties(menu);
        this.age = 0;
    }

    public Properties update()
    {
        this.age += Panel.frameFrequency;

        double age1 = reverse ? duration - age : age;
        age1 = Math.min(duration, age1);

        this.currentProperties.initProperties(this.menu);
        this.currentProperties = apply(this.currentProperties, age1);
        if (this.withReverse && this.age > this.duration / 2)
        {
            this.withReverse = false;
            this.reverse = !this.reverse;
        }

        return this.currentProperties;
    }

    public Animation withReverse(boolean b)
    {
        this.withReverse = b;
        return this;
    }

    public void writeTo(ByteBuf b)
    {
        int index = map.get(this.getClass());
        b.writeInt(index);
        b.writeDouble(this.duration);

        NetworkFieldUtils.writeFields(b, this, arr.get(index).properties);
    }

    public static Animation readFrom(ByteBuf b)
    {
        try
        {
            int index = b.readInt();
            AnimationEntry entry = arr.get(index);
            Animation a = entry.cls.getConstructor().newInstance();
            a.duration = b.readDouble();

            NetworkFieldUtils.readFields(b, a, arr.get(index).properties);

            return a;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public abstract Properties apply(Properties prev, double age);


    public static void registerAnimation(Class<? extends Animation> cls, String... fieldNames)
    {
        arr.add(new AnimationEntry(cls, fieldNames));
        map.put(cls, currentID++);
    }

    public static class AnimationEntry
    {
        public Class<? extends Animation> cls;
        public Field[] properties;

        public AnimationEntry(Class<? extends Animation> cls, String[] properties)
        {
            try
            {
                this.cls = cls;
                this.properties = new Field[properties.length];

                for (int i = 0; i < properties.length; i++)
                    this.properties[i] = cls.getField(properties[i]);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * The below properties will be set to the menu's corresponding properties.<br><br>
     * Set or modify these properties as you wish.<br><br>
     * Changes will be updated the next frame during the FixedMenu's <code>updateAnimations()</code> call.<br><br>
     */
    public static class Properties
    {
        public double posX;
        public double posY;
        public double sizeX;
        public double sizeY;

        public double colorR;
        public double colorG;
        public double colorB;
        public double colorA;

        public Properties(FixedMenu menu)
        {
            initProperties(menu);
        }

        public void initProperties(FixedMenu menu)
        {
            this.posX = menu.posX;
            this.posY = menu.posY;
            this.sizeX = menu.sizeX;
            this.sizeY = menu.sizeY;

            this.colorR = menu.styling.colorR;
            this.colorG = menu.styling.colorG;
            this.colorB = menu.styling.colorB;
            this.colorA = menu.styling.colorA;
        }
    }


    public static class FadeIn extends Animation
    {
        public double changeTo = 255;

        public FadeIn(double duration)
        {
            super(duration);
        }

        public void init(FixedMenu menu)
        {
            super.init(menu);
            this.changeTo = menu.styling.colorA;
        }

        public Properties apply(Properties p, double age1)
        {
            p.colorA = (age1 / duration) * changeTo;

            return p;
        }
    }

    public static class FadeOut extends FadeIn
    {
        public FadeOut(double duration)
        {
            super(duration);
            this.reverse = true;
        }
    }

    public static class ColorTransition extends Animation
    {
        public double fromR, fromG, fromB;
        public double origR, origG, origB;

        public ColorTransition(double fromR, double fromG, double fromB)
        {
            this.fromR = fromR;
            this.fromG = fromG;
            this.fromB = fromB;
        }

        @Override
        public void init(FixedMenu menu)
        {
            super.init(menu);
            origR = menu.styling.colorR;
            origG = menu.styling.colorG;
            origB = menu.styling.colorB;
        }

        @Override
        public Properties apply(Properties prev, double age)
        {
            double frac = duration / age;
            prev.colorR = (fromR - origR) * frac;
            prev.colorG = (fromG - origG) * frac;
            prev.colorB = (fromB - origB) * frac;
            return prev;
        }
    }

    public static class OpacityChange extends FadeOut
    {
        public OpacityChange(double duration, double changeTo)
        {
            super(duration);
            this.changeTo = changeTo;
        }
    }

    public static class ZoomFrom extends Animation
    {
        public double startPercentage;
        public double finalSX;
        public double finalSY;

        public ZoomFrom(double duration, double startPercentage)
        {
            super(duration);
            this.startPercentage = startPercentage;

            if (this.startPercentage > 5)
                this.startPercentage /= 100;
        }

        public void init(FixedMenu menu)
        {
            super.init(menu);
            this.finalSX = menu.sizeX;
            this.finalSY = menu.sizeY;
        }

        public Properties apply(Properties p, double age)
        {
            double frac = age / duration * (1 - this.startPercentage) + this.startPercentage;

            p.sizeX = this.finalSX * frac;
            p.sizeY = this.finalSY * frac;
            return p;
        }
    }

    public static class ZoomTo extends ZoomFrom
    {
        public ZoomTo(double duration, double endPercentage)
        {
            super(duration, endPercentage);
            this.reverse = true;
        }
    }

    public static class SlideBy extends Animation
    {
        public double moveX;
        public double moveY;
        public double endX;
        public double endY;

        public SlideBy(double duration, double moveX, double moveY)
        {
            super(duration);
            this.moveX = moveX;
            this.moveY = moveY;
        }

        public void init(FixedMenu menu)
        {
            super.init(menu);
            this.endX = menu.posX;
            this.endY = menu.posY;
        }

        public Properties apply(Properties p, double age)
        {
            p.posX = age / duration * moveX + endX;
            p.posY = age / duration * moveY + endY;

            return p;
        }
    }
}