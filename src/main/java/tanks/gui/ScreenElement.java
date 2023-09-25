package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;

import java.util.ArrayList;

public abstract class ScreenElement
{
    public int duration;
    protected double age = 0;

    public abstract void draw();

    public static class Notification extends ScreenElement
    {
        public ArrayList<String> text;

        public Notification(String text, int duration)
        {
            this.text = Drawing.drawing.wrapText(text, 300, 16);
            this.duration = duration;
        }

        public void draw()
        {
            this.age += Panel.frameFrequency;

            if (this.age > this.duration)
                Panel.currentNotification = null;

            double linesHeight = Math.max(4, this.text.size()) * 20;
            double x = Drawing.drawing.interfaceSizeX - 320;
            double y = Drawing.drawing.interfaceSizeY - Drawing.drawing.statsHeight - linesHeight - 80;

            Drawing.drawing.setColor(0, 0, 0, 128);
            Drawing.drawing.drawPopup(x + 158, y + linesHeight / 2, 315, linesHeight + 10, 10, 5);
            Drawing.drawing.setInterfaceFontSize(14);

            for (int i = 0; i < this.text.size(); i++)
                Drawing.drawing.drawUncenteredInterfaceText(x + 50, y + i * 20 + 12, this.text.get(i));

            Drawing.drawing.setColor(0, 150, 255);
            Drawing.drawing.fillOval(x + 27, y + 25, 25, 25);

            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.setInterfaceFontSize(16);
            Drawing.drawing.drawInterfaceText(x + 27, y + 25, "!");
        }
    }

    public static class CenterMessage extends ScreenElement
    {
        public boolean previous = false;
        public TextWithStyling styling;
        public double baseColorA = -1;

        public CenterMessage(String message, Object... objects)
        {
            this(String.format(message, objects), 1000);
        }
        public CenterMessage(String message, int duration)
        {
            int brightness = (Game.screen instanceof ScreenGame && Level.isDark()) ? 255 : 0;
            this.styling = new TextWithStyling("A very very cool message", brightness, brightness, brightness, 80 - Math.max(8, message.length() * 2));
            this.styling.colorA = Game.screen instanceof ScreenGame ? 128 : 255;
            this.styling.text = message;
            this.duration = duration;
            this.previous = Panel.currentMessage != null;
        }

        @Override
        public void draw()
        {
            this.age += Panel.frameFrequency;
            this.styling.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 200);

            if (this.age < 50 && !previous)
            {
                if (this.baseColorA < 0)
                    this.baseColorA = this.styling.colorA;

                this.styling.colorA = this.baseColorA * Math.min(1, this.age / 50);
            }
            else if (this.age > this.duration - 50)
                this.styling.colorA = this.baseColorA * Math.max(0, (this.duration - this.age) / 50);
            else
                this.baseColorA = this.styling.colorA;

            if (this.age > this.duration)
                Panel.currentMessage = null;
        }
    }
}
