package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenTestDebug extends ScreenOptionsOverlay
{
    Button keyboardTest = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Test keyboard", () -> Game.screen = new ScreenTestKeyboard());

    Button textboxTest = new Button(this.centerX - this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Test text boxes", () -> Game.screen = new ScreenTestTextbox());

    Button modelTest = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Test models", () -> Game.screen = new ScreenTestModel(Drawing.drawing.createModel("/models/tankcamoflauge/base/")));

    Button fontTest = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Test fonts", () -> Game.screen = new ScreenTestFonts());

    Button shapeTest = new Button(this.centerX + this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Test shapes", () -> Game.screen = new ScreenTestShapes());

    Button rainbowTest = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Test rainbow", () -> Game.screen = new ScreenTestRainbow());

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenDebug());

    @Override
    public void update()
    {
        super.update();

        keyboardTest.update();
        textboxTest.update();
        modelTest.update();
        fontTest.update();
        shapeTest.update();
        rainbowTest.update();

        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(brightness, brightness, brightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 210, "Test stuff");

        modelTest.draw();
        keyboardTest.draw();
        textboxTest.draw();
        fontTest.draw();
        shapeTest.draw();
        rainbowTest.draw();

        back.draw();
    }
}
