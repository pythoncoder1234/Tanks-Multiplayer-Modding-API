package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.editor.selector.ChoiceSelector;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;

import java.util.ArrayList;

public class OverlaySelectChoice extends ScreenLevelEditorOverlay
{
    public ChoiceSelector<?, ?> selector;
    public ArrayList<Button> choiceButtons = new ArrayList<>();

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", this::escape
    );

    public <V> OverlaySelectChoice(Screen previous, ScreenLevelEditor screenLevelEditor, ChoiceSelector<?, V> selector)
    {
        super(previous, screenLevelEditor);

        this.selector = selector;

        int i = 0;
        for (V b : selector.choices)
        {
            final int j = i;
            choiceButtons.add(new Button(0, 0, 350, 40, selector.choiceToString(b), () -> selector.setChoice(j)));
            i++;
        }

        if (selector.addNoneChoice)
            choiceButtons.add(new Button(0, 0, 350, 40, "\u00A7127000000255none", () -> selector.setChoice(-1)));

        selector.buttonList = new ButtonList(choiceButtons, 0, 0, -30);
    }

    public void update()
    {
        for (Button b : choiceButtons)
            b.enabled = true;

        if (selector.selectedChoice != null)
            choiceButtons.get(selector.selectedIndex).enabled = false;
        else
            choiceButtons.get(choiceButtons.size() - 1).enabled = false;

        selector.buttonList.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, selector.title);

        selector.buttonList.draw();

        back.draw();
    }
}
