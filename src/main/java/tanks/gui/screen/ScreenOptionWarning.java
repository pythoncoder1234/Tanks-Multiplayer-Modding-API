package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionWarning extends ScreenOptionsOverlay
{
    public String text;
    public String[] objects;

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Ok", () -> Game.screen = prevScreen);

    public ScreenOptionWarning(Screen back, String text, String... objects)
    {
        this.prevScreen = back;
        this.text = text;
        this.objects = objects;
    }

    @Override
    public void update()
    {
        back.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		back.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Notice!");

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace / 2, text, objects);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "the next time you start the game.");
	}
}
