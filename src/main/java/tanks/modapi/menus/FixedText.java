package tanks.modapi.menus;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;
import tanks.modapi.ModAPI;

public class FixedText extends FixedMenu
{
    public enum types
    {title, subtitle, actionbar}

    public types location;
    public double posX;
    public double posY;
    public String text;
    public boolean afterGameStarted = false;
    public boolean hasItems = false;

    public double fontSize = 24;
    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA = 0;

    private double age = 0;

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
    }

    public FixedText(types location, String text)
    {
        this(location, text, 255, 255, 255, 24);
    }

    public FixedText(types location, String text, double r, double g, double b, double fontSize)
    {
        this.location = location;
        this.text = text;
        this.fontSize = fontSize;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
        ModAPI.fixedText.drawString(this.posX - ModAPI.fixedText.getStringSizeX(this.fontSize / 40, this.text) / 2,
                this.posY - ModAPI.fixedText.getStringSizeY(this.fontSize / 40, this.text) / 2,
                this.fontSize / 40, this.fontSize / 40,
                this.text);

        if (duration > 0 && age > duration)
        {
            if (this.colorA <= 0)
                ModAPI.removeMenus.add(this);

            this.colorA -= Panel.frameFrequency * 1.25;
        }
        else
            this.colorA = Math.min(255, this.colorA + Panel.frameFrequency * 3);
    }

    @Override
    public void update()
    {
        if (afterGameStarted)
        {
            if (Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).playing)
                return;
        }

        this.age += Panel.frameFrequency;

        if (this.location != null)
        {
            switch (this.location)
            {
                case title:
                    this.posX = Panel.windowWidth / 2;
                    this.posY = Panel.windowHeight / 2 - 50;
                    this.fontSize = 60;
                    break;
                case subtitle:
                    this.posX = Panel.windowWidth / 2;
                    this.posY = Panel.windowHeight / 2 + 10;
                    this.fontSize = 40;
                    break;
                case actionbar:
                    this.posX = Panel.windowWidth / 2;
                    this.posY = Panel.windowHeight - (this.hasItems ? 190 - Game.player.hotbar.percentHidden * 0.9 : 100);
                    this.fontSize = 20;
                    break;
            }
        }
    }
}
