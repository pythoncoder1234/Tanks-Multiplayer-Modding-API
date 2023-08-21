package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayChangeCharacter extends ScreenLevelEditorOverlay
{
    public String name;
    Button back = new Button(this.centerX, this.centerY + 220, this.objWidth, this.objHeight, "Back", this::escape);    TextBox start = new TextBox(this.centerX, this.centerY - 60, this.objWidth, this.objHeight, "Start", new Runnable()
    {
        @Override
        public void run()
        {
            if (start.inputText.length() > 0)
                ((OverlayGenerateTeams) previous).start = Integer.parseInt(start.inputText);
            else
                start.setText(start.previousInputText);
        }
    }, ((OverlayGenerateTeams) previous).start + "");

    public OverlayChangeCharacter(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        this.name = ((OverlayGenerateTeams) previous).generator.name;

        start.allowLetters = false;
        start.allowSpaces = false;
        step.allowLetters = false;
        step.allowSpaces = false;

        charNum.allowLetters = false;
        charNum.allowSpaces = false;
        charNum.maxChars = 2;
        charNum.minValue = 0;
        charNum.maxValue = this.name.length();
        charNum.checkMinValue = true;
        charNum.checkMaxValue = true;
    }    TextBox step = new TextBox(this.centerX, this.centerY + 30, this.objWidth, this.objHeight, "Step", new Runnable()
    {
        @Override
        public void run()
        {
            if (step.inputText.length() > 0)
                ((OverlayGenerateTeams) previous).step = Integer.parseInt(step.inputText);
            else
                step.setText(step.previousInputText);
        }
    }, ((OverlayGenerateTeams) previous).step + "");

    @Override
    public void load()
    {
        this.name = ((OverlayGenerateTeams) previous).generator.name;
        charNum.maxValue = this.name.length();
    }    TextBox charNum = new TextBox(this.centerX, this.centerY + 130, this.objWidth, this.objHeight, "Character number", new Runnable()
    {
        @Override
        public void run()
        {
            if (charNum.inputText.length() > 0)
                ((OverlayGenerateTeams) previous).changedChar = Integer.parseInt(charNum.inputText) - 1;
            else
                charNum.setText(charNum.previousInputText);
        }
    }, (((OverlayGenerateTeams) previous).changedChar + 1) + "",
            "Set to 0 to remove---changing a character");

    @Override
    public void update()
    {
        start.update();
        step.update();
        charNum.update();

        back.update();
    }

    @Override
    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(this.editor.fontBrightness, this.editor.fontBrightness, this.editor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 300, "Change character");

        String brightness = editor.fontBrightness == 255 ? "255255255" : "000000000";

        int i = ((OverlayGenerateTeams) previous).changedChar;
        if (i > -1)
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 200, this.name.substring(0, i) + "\u00a7255000000255" + this.name.charAt(i) + "\u00a7" + brightness + "255" + this.name.substring(i + 1));
        else
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 200, this.name);

        start.draw();
        step.draw();
        charNum.draw();

        back.draw();
    }






}