package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.UUIDTextBox;

public class ScreenTestTextbox extends ScreenOptionsOverlay
{
    public ScreenTestTextbox()
    {
        box.allowAll = true;
        box.enableCaps = true;
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "Back", () -> Game.screen = prevScreen
    );

    TextBox box = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 700, 40, "Text box", () ->
    {}, "");

    UUIDTextBox uuidBox = new UUIDTextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 700, 40, "UUID box", () ->
    {}, "");


    @Override
    public void update()
    {
        back.update();
        box.update();
        uuidBox.update();

        super.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Text box test");

        box.draw();
        uuidBox.draw();

        back.draw();
    }
}
