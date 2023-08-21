package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.editorselector.NumberSelector;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

@SuppressWarnings({"unchecked", "rawtypes"})
public class OverlayNumberSelector extends ScreenLevelEditorOverlay
{
    public TextBox textBox;
    public NumberSelector selector;

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", this::escape
    );

    public Button increase = new Button(this.centerX + 250, this.centerY, 60, 60, "+", new Runnable()
    {
        @Override
        public void run()
        {
            selector.number += selector.step;
            textBox.inputText = String.format(selector.format, selector.number);
            selector.syncProperties(selector.gameObject);
        }
    }
    );

    public Button decrease = new Button(this.centerX - 250, this.centerY, 60, 60, "-", new Runnable()
    {
        @Override
        public void run()
        {
            selector.number -= selector.step;
            textBox.inputText = String.format(selector.format, selector.number);
            selector.syncProperties(selector.gameObject);
        }
    }
    );

    public OverlayNumberSelector(Screen previous, ScreenLevelEditor screenLevelEditor, NumberSelector selector)
    {
        super(previous, screenLevelEditor);

        this.selector = selector;

        textBox = new TextBox(this.centerX, this.centerY + 15, 350, 40, this.selector.title, () ->
        {
            if (textBox.inputText.length() == 0 || textBox.inputText.equals(textBox.previousInputText))
            {
                textBox.inputText = textBox.previousInputText;
                return;
            }

            this.selector.modified = true;

            if (selector.forceStep)
                textBox.inputText = String.format(selector.format, Math.round(Double.parseDouble(textBox.inputText) * selector.step) / selector.step);

            this.selector.setMetadata(textBox.inputText);
            this.selector.syncProperties(this.selector.gameObject);
        }
                , this.selector.numberString());

        textBox.allowLetters = false;
        textBox.allowSpaces = false;
        textBox.allowDoubles = this.selector.allowDecimals;
        textBox.maxChars = 9;
        textBox.minValue = 0;
        textBox.checkMaxValue = true;
        textBox.checkMinValue = true;
    }

    public void update()
    {
        this.increase.enabled = selector.number < selector.max;
        this.decrease.enabled = selector.number > selector.min;

        this.increase.update();
        this.decrease.update();
        this.textBox.update();

        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, selector.title);

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 600, 100);

        this.increase.draw();
        this.decrease.draw();
        this.textBox.draw();

        this.back.draw();
    }
}
