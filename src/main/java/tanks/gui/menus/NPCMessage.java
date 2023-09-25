package tanks.gui.menus;

import tanks.Drawing;
import tanks.Game;
import tanks.tank.TankNPC;

import java.util.ArrayList;

public class NPCMessage extends FixedMenu
{
    public TankNPC tank;
    public String[] text;
    public int textNum = 0;

    public String currentLine = "";

    public NPCMessage(TankNPC t)
    {
        this.tank = t;
        this.text = this.tank.messages.raw;
    }

    @Override
    public void draw()
    {
        if (!tank.draw || this.text == null || this.text[textNum] == null || this.text[textNum].equals(TankNPC.shopCommand))
            return;

        Drawing.drawing.setColor(120, 66, 18, 200);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY * 0.2, Math.max(1300, Game.game.window.absoluteWidth * 0.8), Drawing.drawing.interfaceSizeY / 3);
        Drawing.drawing.setColor(175, 96, 26, 200);
        Drawing.drawing.drawInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY * 0.2, Math.max(1300, Game.game.window.absoluteWidth * 0.8) + 20, Drawing.drawing.interfaceSizeY / 3 + 20, 10);

        double prevAngle = tank.angle;
        tank.angle = 0;
        tank.orientation = 0;
        tank.drawForInterface(Math.min(150, (Drawing.drawing.interfaceSizeX - Game.game.window.absoluteWidth) / 2 + 300), Drawing.drawing.interfaceSizeY * 0.125, 1.75);
        tank.angle = prevAngle;
        tank.orientation = prevAngle;

        Drawing.drawing.setFontSize(24);
        Drawing.drawing.setColor(255, 255, 255);

        ArrayList<String> lines = Drawing.drawing.wrapText(this.currentLine, Math.max(1300, Game.game.window.absoluteWidth * 0.8) - 800, 24);
        for (int i = 0; i < lines.size(); i++)
            Drawing.drawing.drawUncenteredInterfaceText(250, Drawing.drawing.interfaceSizeY * 0.1 + 40 * (i - 1.5) + 50, lines.get(i));
    }

    @Override
    public void update()
    {
        this.text = this.tank.messages.raw;
        this.textNum = this.tank.messageNum;
        this.currentLine = this.tank.currentLine;
    }
}
