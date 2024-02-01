package tanks.gui.screen;

import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.TextBoxSlider;

public class ScreenInputColor extends Screen
{
    protected static int colorR, colorG, colorB;
    public TextBoxSlider colorRed, colorGreen, colorBlue;

    public double px, py;

    public Screen prevScreen = Game.screen;
    public TextBox textBox;
    public Button add = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Add", () -> textBox.inputText += String.format("§%03.0f%03.0f%03.0f255", colorRed.value, colorGreen.value, colorBlue.value));

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 4.5, this.objWidth, this.objHeight, "Back", () ->
    {
        textBox.setPosition(px, py);
        Game.screen = prevScreen;
    });

    public ScreenInputColor(TextBox b)
    {
        this.music = prevScreen.music;
        this.musicID = prevScreen.musicID;

        this.textBox = b;
        px = textBox.posX;
        py = textBox.posY;
        this.textBox.setPosition(this.centerX, this.centerY - this.objYSpace * 3);

        colorRed = new TextBoxSlider(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Red", () ->
        {
            if (colorRed.inputText.isEmpty())
                colorRed.inputText = colorRed.previousInputText;

            colorR = Integer.parseInt(colorRed.inputText);
        }
                , colorR, 0, 255, 1);

        colorRed.allowLetters = false;
        colorRed.allowSpaces = false;
        colorRed.maxChars = 3;
        colorRed.maxValue = 255;
        colorRed.checkMaxValue = true;
        colorRed.integer = true;

        colorGreen = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Green", () ->
        {
            if (colorGreen.inputText.isEmpty())
                colorGreen.inputText = colorGreen.previousInputText;

            colorG = Integer.parseInt(colorGreen.inputText);
        }
                , colorG, 0, 255, 1);

        colorGreen.allowLetters = false;
        colorGreen.allowSpaces = false;
        colorGreen.maxChars = 3;
        colorGreen.maxValue = 255;
        colorGreen.checkMaxValue = true;
        colorGreen.integer = true;

        colorBlue = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Blue", () ->
        {
            if (colorBlue.inputText.isEmpty())
                colorBlue.inputText = colorBlue.previousInputText;

            colorB = Integer.parseInt(colorBlue.inputText);
        }
                , colorB, 0, 255, 1);

        colorBlue.allowLetters = false;
        colorBlue.allowSpaces = false;
        colorBlue.maxChars = 3;
        colorBlue.maxValue = 255;
        colorBlue.checkMaxValue = true;
        colorBlue.integer = true;
    }

    @Override
    public void update()
    {
        colorRed.update();
        colorGreen.update();
        colorBlue.update();

        add.update();
        back.update();

        textBox.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        colorRed.r1 = 0;
        colorRed.r2 = 255;
        colorRed.g1 = colorGreen.value;
        colorRed.g2 = colorGreen.value;
        colorRed.b1 = colorBlue.value;
        colorRed.b2 = colorBlue.value;

        colorGreen.r1 = colorRed.value;
        colorGreen.r2 = colorRed.value;
        colorGreen.g1 = 0;
        colorGreen.g2 = 255;
        colorGreen.b1 = colorBlue.value;
        colorGreen.b2 = colorBlue.value;

        colorBlue.r1 = colorRed.value;
        colorBlue.r2 = colorRed.value;
        colorBlue.g1 = colorGreen.value;
        colorBlue.g2 = colorGreen.value;
        colorBlue.b1 = 0;
        colorBlue.b2 = 255;

        colorRed.draw();
        colorGreen.draw();
        colorBlue.draw();

        textBox.draw();

        add.draw();
        back.draw();
    }
}
