package tanks;

import tanks.menus.FixedMenu;

public class Animations
{
    public abstract static class Animation
    {
        public FixedMenu menu = null;
        public boolean initialized = false;
        public Properties currentProperties = null;

        /** in centiseconds */
        public double duration;
        public double age = 0;
        public boolean reverse = false;

        public Runnable onEnd = null;

        public Animation(double duration)
        {
            this.duration = duration;
        }

        public void init(FixedMenu menu)
        {
            this.initialized = true;
            this.menu = menu;
            this.currentProperties = new Properties(menu);
        }

        public Properties update()
        {
            this.age += Panel.frameFrequency;
            this.currentProperties.initProperties(this.menu);
            this.currentProperties = apply(this.currentProperties);
            return this.currentProperties;
        }

        public abstract Properties apply(Properties prev);

        // todo
        public String toString()
        {
            return this.getClass().getSimpleName();
        }

        public static Animation fromString(String s)
        {
            return null;
        }

        /** The below properties will be set to the menu's corresponding properties.<br><br>
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

                this.colorR = menu.colorR;
                this.colorG = menu.colorG;
                this.colorB = menu.colorB;
                this.colorA = menu.colorA;
            }
        }
    }

    public static class FadeIn extends Animation
    {
        public double initialColA;

        public FadeIn(double duration)
        {
            super(duration);
        }

        public void init(FixedMenu menu)
        {
            super.init(menu);
            this.initialColA = menu.colorA;
        }

        public Properties apply(Properties p)
        {
            double age1 = reverse ? duration - age : age;
            age1 = Math.min(duration, age1);

            p.colorA = (age1 / duration) * initialColA;
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

    public static class ZoomFrom extends Animation
    {
        public double startPercentage;
        public double finalSX;
        public double finalSY;

        public ZoomFrom(double duration, double startPercentage)
        {
            super(duration);
            this.startPercentage = startPercentage;

            if (this.startPercentage > 3)
                this.startPercentage /= 100;
        }

        public void init(FixedMenu menu)
        {
            super.init(menu);
            this.finalSX = menu.sizeX;
            this.finalSY = menu.sizeY;
        }

        public Properties apply(Properties p)
        {
            double age1 = reverse ? duration - age : age;
            age1 = Math.min(duration, age1);

            double frac = age1 / duration * (1 - this.startPercentage) + this.startPercentage;

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

        public Properties apply(Properties p)
        {
            double age1 = reverse ? duration - age : age;
            age1 = Math.min(duration, age1);

            p.posX = age1 / duration * moveX + endX;
            p.posY = age1 / duration * moveY + endY;

            return p;
        }
    }
}