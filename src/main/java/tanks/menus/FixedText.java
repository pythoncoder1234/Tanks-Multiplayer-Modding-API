package tanks.menus;

import tanks.*;
import tanks.event.EventChangeText;
import tanks.gui.screen.ScreenGame;

public class FixedText extends FixedMenu
{
    public enum types {title, subtitle, actionbar, topLeft, topRight}

    public types location;
    public String text;
    public boolean afterGameStarted = false;
    public boolean hasItems = false;
    public boolean fadeInEffect = true;
    public boolean shadowEffect = true;

    public double fontSize = 24;

    private static final double fadeDuration = 50;

    public FixedText(double x, double y, String text)
    {
        this(x, y, text, 255, 255, 255, 24);
    }

    public FixedText(double x, double y, String text, double r, double g, double b, double fontSize)
    {
        this.posX = x;
        this.posY = y;
        this.text = text;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.fontSize = fontSize;

        this.sizeX = this.fontSize / 36;
        this.sizeY = this.fontSize / 36;
    }

    public FixedText(types location, String text)
    {
        this(location, text, 0, 255, 255, 255);
    }

    public FixedText(types location, String text, double duration, double r, double g, double b)
    {
        this.location = location;
        this.text = text;
        this.duration = duration;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;

        this.sizeX = this.fontSize / 36;
        this.sizeY = this.fontSize / 36;
    }

    @Override
    public void draw()
    {
        if (this.fadeInEffect && this.animations.size() == 0 && this.age == 0)
        {
            setLocation();
            this.animations.add(new Animations.FadeIn(fadeDuration));
            this.updateAnimations();
        }

        double x = ModAPI.fixedText.getStringSizeX(this.sizeX, this.text) / 2;
        double y = ModAPI.fixedText.getStringSizeY(this.sizeY, this.text) / 2;

        if (this.location != null)
        {
            if (this.location == types.topLeft)
                x = 0;
            else if (this.location == types.topRight)
                x *= 2;
        }

        if (this.shadowEffect && (this.sizeX * 3 >= 1 || this.sizeY * 3 >= 1))
        {
            double[] shadowColor = shadowColor();
            Drawing.drawing.setColor(shadowColor[0], shadowColor[1], shadowColor[2], this.colorA, this.glow);

            if (this.posZ >= 0)
                ModAPI.fixedText.drawString(this.posX - x + this.sizeX * 3, this.posY - y + this.sizeY * 3, this.sizeX, this.sizeY, this.text);
            else
                ModAPI.fixedText.drawString(this.posX - x + this.sizeX * 3, this.posY - y + this.sizeY * 3, this.posZ, this.sizeX, this.sizeY, this.text);
        }

        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA, this.glow);

        if (this.posZ >= 0)
            ModAPI.fixedText.drawString(this.posX - x, this.posY - y, this.sizeX, this.sizeY, this.text);
        else
            ModAPI.fixedText.drawString(this.posX - x, this.posY - y, this.posZ, this.sizeX, this.sizeY, this.text);
    }

    @Override
    public void update()
    {
        if (afterGameStarted)
        {
            if (Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).playing)
                return;
        }

        super.update();

        if (duration > 0 && age >= duration - fadeDuration)
        {
            if (!fadeInEffect)
            {
                fadeInEffect = true;
                this.animations.add(new Animations.FadeOut(fadeDuration));
            }

            if (age >= duration)
                ModAPI.removeMenus.add(this);
        }
    }

    public void setLocation()
    {
        if (this.location == null)
            return;

        switch (this.location)
        {
            case title:
                this.posX = Panel.windowWidth / 2;
                this.posY = Panel.windowHeight / 2 - 50;
                this.fontSize = 50;
                break;
            case subtitle:
                this.posX = Panel.windowWidth / 2;
                this.posY = Panel.windowHeight / 2;
                this.fontSize = 30;
                break;
            case actionbar:
                this.posX = Panel.windowWidth / 2;
                this.posY = Panel.windowHeight - (this.hasItems ? 190 - Game.player.hotbar.percentHidden * 0.9 : 100);
                this.fontSize = 20;
                break;
            case topLeft:
                // Totally not copied from SpeedrunTimer
                this.posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2 + Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50;
                this.posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;

                if (Game.showSpeedrunTimer && !(Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).noSpeedrunTimer))
                    this.posY += this.fontSize;
                break;
            case topRight:
                this.posX = Game.game.window.absoluteWidth - (-(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2 + Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50);
                this.posY = Game.game.window.absoluteHeight - (-((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50);
                break;
        }

        this.sizeX = this.fontSize / 36;
        this.sizeY = this.fontSize / 36;
    }

    public void setText(String text)
    {
        Game.eventsOut.add(new EventChangeText(this.id, text));
        this.text = text;
    }

    private double[] shadowColor()
    {
        double[] output = new double[] {this.colorR - 50, this.colorG - 50, this.colorB - 50};

        for (int i = 0; i < 3; i++)
        {
            if (output[i] < 0)
                output[i] += 100;
        }

        return output;
    }
}
