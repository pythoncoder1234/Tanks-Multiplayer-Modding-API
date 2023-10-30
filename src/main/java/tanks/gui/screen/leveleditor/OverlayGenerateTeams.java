package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;

public class OverlayGenerateTeams extends ScreenLevelEditorOverlay
{
    public Team generator = new Team("Team 1");
    public int changedChar = -1;
    public int copies = 1;
    public int start = 1;
    public int step = 1;

    public boolean ff = true;
    public boolean enableColors = false;
    Button generate = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Generate", () ->
    {
        if (Panel.selectedTextBox != null)
            Panel.selectedTextBox.submit();

        OverlayLevelOptionsTeams o = null;
        if (previous instanceof OverlayLevelOptionsTeams)
            o = (OverlayLevelOptionsTeams) previous;

        for (int i = 0; i < copies; i++)
        {
            String name = generator.name;
            if (changedChar >= 0)
                name = name.substring(0, changedChar) + (start + i * step) + name.substring(changedChar + 1);

            Team t = new Team(name, ff);
            if (enableColors)
            {
                double[] col = Game.getRainbowColor(0.7 / copies * i);
                t.enableColor = true;
                t.teamColorR = col[0];
                t.teamColorG = col[1];
                t.teamColorB = col[2];
            }

            if (o != null)
                o.lastGeneratedTeams.add(t);

            editor.teams.add(t);
        }

        this.escape();
    });    TextBox name = new TextBox(this.centerX, this.centerY - this.objYSpace * 3, this.objWidth, this.objHeight, "Team name", new Runnable()
    {
        @Override
        public void run()
        {
            if (name.inputText.length() > 0)
                generator.name = name.inputText;
            else
                generator.name = name.previousInputText;

            if (changedChar > generator.name.length() - 1)
                changedChar = generator.name.length() - 1;
        }
    }, generator.name);
    Button changeChar = new Button(this.centerX, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Change letter: None", () -> Game.screen = new OverlayChangeCharacter(this, this.editor));    TextBox amount = new TextBox(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Copies", new Runnable()
    {
        @Override
        public void run()
        {
            if (amount.inputText.length() > 0)
                copies = Integer.parseInt(amount.inputText);
            else
                amount.inputText = amount.previousInputText;
        }
    }, "1");
    Button back = new Button(this.centerX, this.centerY + 240, this.objWidth, this.objHeight, "Back", this::escape);

    public OverlayGenerateTeams(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        amount.allowLetters = false;
        amount.allowSpaces = false;
        amount.maxChars = 2;

        name.lowerCase = true;
    }

    @Override
    public void load()
    {
        changeChar.setText("Change character: ", (changedChar > 0 ? changedChar + 1 + "" : "None"));
    }    Button friendlyFire = new Button(this.centerX, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "Friendly fire: " + ScreenOptions.onText, new Runnable()
    {
        @Override
        public void run()
        {
            ff = !ff;
            friendlyFire.setText("Friendly fire: ", (ff ? ScreenOptions.onText : ScreenOptions.offText));
        }
    });

    @Override
    public void update()
    {
        name.update();
        generate.update();
        changeChar.update();
        friendlyFire.update();
        teamColors.update();
        amount.update();

        back.update();
        super.update();
    }

    Button teamColors = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Team colors: " + ScreenOptions.offText, new Runnable()
    {
        @Override
        public void run()
        {
            enableColors = !enableColors;
            teamColors.setText("Team colors: ", (enableColors ? ScreenOptions.onText : ScreenOptions.offText));
        }
    });

    @Override
    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(this.editor.fontBrightness, this.editor.fontBrightness, this.editor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 300, "Generate teams");

        name.draw();
        generate.draw();
        teamColors.draw();
        friendlyFire.draw();
        changeChar.draw();
        amount.draw();

        back.draw();
    }








}