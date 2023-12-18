package tanks.gui.menus;

import tanks.Drawing;
import tanks.Game;
import tanks.ModAPI;
import tanks.Panel;
import tanks.gui.TextWithStyling;
import tanks.gui.screen.ScreenGame;

import java.util.Collections;

public class FixedText extends FixedMenu
{
    public static final double fadeDuration = 100;
    public enum types {title, subtitle, actionbar, topLeft, topRight}

    public types location;
    public boolean afterGameStarted = false;
    public boolean hasItems = false;
    public boolean fadeInEffect = true;
    public boolean fadeOutEffect = true;
    public boolean shadowEffect = true;
    public Animation[] textChangeAnimation = null;

    public boolean end = false;

    protected boolean firstFrame = true;
    protected String prevText;

    public FixedText(double x, double y, String text)
    {
        this(x, y, text, 255, 255, 255, 24);
    }

    public FixedText(double x, double y, String text, double r, double g, double b, double fontSize, Animation... animations)
    {
        this(x, y, new TextWithStyling(text, r, g, b, 255, fontSize), animations);
    }

    public FixedText(double x, double y, TextWithStyling styling, Animation... animations)
    {
        this.posX = x;
        this.posY = y;
        this.styling = styling;

        if (animations.length > 0)
        {
            this.fadeInEffect = false;
            this.fadeOutEffect = false;
            Collections.addAll(this.animations, animations);
        }

        this.sizeX = this.styling.fontSize / 36;
        this.sizeY = this.styling.fontSize / 36;
    }

    public FixedText(types location, String text)
    {
        this(location, text, 0, 255, 255, 255);
    }

    public FixedText(types location, String text, double duration)
    {
        this(location, text, duration, 255, 255, 255);
    }

    public FixedText(types location, String text, double duration, double r, double g, double b, Animation... animations)
    {
        this(location, text, duration, r, g, b, 24, animations);

        if (location == types.title)
            this.styling.fontSize = 50;
        else if (location == types.subtitle)
            this.styling.fontSize = 30;
        else if (location == types.actionbar)
            this.styling.fontSize = 20;

        if (location == types.actionbar)
            this.shadowEffect = false;
    }

    public FixedText(types location, String text, double duration, double r, double g, double b, double fontSize, Animation... animations)
    {
        this(location, new TextWithStyling(text, r, g, b, 255, fontSize), duration, animations);
    }

    public FixedText(types location, TextWithStyling styling, double duration, Animation... animations)
    {
        this.location = location;
        this.styling = styling;
        this.duration = duration;

        if (animations.length > 0)
        {
            this.fadeInEffect = false;
            this.fadeOutEffect = false;
            Collections.addAll(this.animations, animations);
        }

        this.sizeX = this.styling.fontSize / 36;
        this.sizeY = this.styling.fontSize / 36;

        if (location == types.actionbar)
            this.shadowEffect = false;
    }

    @Override
    public void draw()
    {
        if (firstFrame)
        {
            firstFrame = false;
            this.prevText = this.styling.text;
            this.fadeOutEffect = !this.fadeOutEffect;
            this.hasItems = Game.player.hotbar.enabledItemBar;

            if (this.fadeInEffect)
                this.animations.add(new Animation.FadeIn(fadeDuration));
        }

        setLocation();
        this.updateAnimations();

        double x = ModAPI.fixedText.getStringSizeX(this.sizeX, this.styling.text) / 2;
        double y = ModAPI.fixedText.getStringSizeY(this.sizeY, this.styling.text) / 2;

        if (this.location != null)
        {
            if (this.location == types.topLeft)
                x = 0;
            else if (this.location == types.topRight)
                x *= 2;
        }

        if (this.shadowEffect && (this.sizeX * 3 >= 1 || this.sizeY * 3 >= 1))
        {
            if (this.shadowColor == null)
                this.shadowColor = this.styling.shadowColor();

            this.shadowColor.colorA = this.styling.colorA;
            this.shadowColor.setColor();
            Drawing.drawing.currentGlow = this.glow;

            if (this.posZ >= 0)
                ModAPI.fixedText.drawString(this.posX - x + this.sizeX * 3, this.posY - y + this.sizeY * 3, this.sizeX, this.sizeY, this.styling.text);
            else
                ModAPI.fixedText.drawString(this.posX - x + this.sizeX * 3, this.posY - y + this.sizeY * 3, this.posZ, this.sizeX, this.sizeY, this.styling.text);
        }

        this.styling.setColor();
        Drawing.drawing.currentGlow = glow;

        if (this.posZ >= 0)
            ModAPI.fixedText.drawString(this.posX - x, this.posY - y, this.sizeX, this.sizeY, this.styling.text);
        else
            ModAPI.fixedText.drawString(this.posX - x, this.posY - y, this.posZ, this.sizeX, this.sizeY, this.styling.text);
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

        if (this.textChangeAnimation != null && !this.styling.text.equals(this.prevText))
            Collections.addAll(this.animations, this.textChangeAnimation);

        if (end || (duration > 0 && age >= duration - fadeDuration))
        {
            if (!fadeOutEffect)
            {
                fadeOutEffect = true;
                this.animations.add(new Animation.FadeOut(Math.min(duration - fadeDuration, fadeDuration)));
            }

            if (age >= duration)
                ModAPI.removeMenus.add(this);
        }
    }

    public void setLocation()
    {
        if (this.location == null)
            return;

        double[] pos;

        switch (this.location)
        {
            case title:
                this.posX = Panel.windowWidth / 2;
                this.posY = Panel.windowHeight / 2 - Drawing.drawing.statsHeight;
                break;
            case subtitle:
                this.posX = Panel.windowWidth / 2;
                this.posY = Panel.windowHeight / 2 - Drawing.drawing.statsHeight + this.styling.fontSize + 20;

                for (FixedMenu m : ModAPI.fixedMenus)
                {
                    if (m instanceof FixedText && ((FixedText) m).location == types.title)
                    {
                        m.posY -= this.styling.fontSize / 4;
                        break;
                    }
                }
                break;
            case actionbar:
                this.posX = Panel.windowWidth / 2;
                this.posY = Panel.windowHeight - (this.hasItems ? 190 - Game.player.hotbar.percentHidden * 0.9 : 100);
                break;
            case topLeft:
                pos = ModAPI.topCoords(true);
                this.posX = pos[0];
                this.posY = pos[1];

                if (Game.showSpeedrunTimer && !(Game.screen instanceof ScreenGame && Game.screen.hideSpeedrunTimer))
                    this.posY += this.styling.fontSize;
                break;
            case topRight:
                pos = ModAPI.topCoords(false);
                this.posX = pos[0];
                this.posY = pos[1];
                break;
        }

        this.sizeX = this.styling.fontSize / 36;
        this.sizeY = this.styling.fontSize / 36;
    }

    public FixedText remove()
    {
        end = true;
        return this;
    }

    public FixedText remove(boolean instant)
    {
        if (instant)
            ModAPI.removeMenus.add(this);
        else
            end = true;

        return this;
    }

    public FixedText display()
    {
        ModAPI.displayText(this);
        return this;
    }
}
