package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsSpeedrun extends ScreenOptionsOverlay
{
    public static final String deterministicText = "Deterministic: ";
    public static final String timerText = "Timer: ";

    public static final String deterministic30 = "\u00A700010020025530 FPS";
    public static final String deterministic60 = "\u00A700020000025560 FPS";

    Button timer = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showSpeedrunTimer = !Game.showSpeedrunTimer;

            if (Game.showSpeedrunTimer)
                timer.setText(timerText, ScreenOptions.onText);
            else
                timer.setText(timerText, ScreenOptions.offText);
        }
    },
            "When enabled, time spent---in the current level attempt---and crusade will be displayed");

    Button deterministic = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            if (Game.deterministic30Fps)
            {
                Game.deterministicMode = false;
                Game.deterministic30Fps = false;
            }
            else if (Game.deterministicMode)
                Game.deterministic30Fps = true;
            else
                Game.deterministicMode = true;

            if (Game.deterministicMode && Game.deterministic30Fps)
                deterministic.setText(deterministicText, deterministic30);
            else if (Game.deterministicMode)
                deterministic.setText(deterministicText, deterministic60);
            else
                deterministic.setText(deterministicText, ScreenOptions.offText);
        }
    },
            "Deterministic mode changes the random number---generation to be fixed based on a seed, and---the game speed to be locked and independent---of framerate." +
                    "------This is useful for fair speedruns but may---provide for a less smooth experience.");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptionsMisc()
    );

    public ScreenOptionsSpeedrun()
    {

        if (Game.showSpeedrunTimer)
            timer.setText(timerText, ScreenOptions.onText);
        else
            timer.setText(timerText, ScreenOptions.offText);

        if (Game.deterministicMode && Game.deterministic30Fps)
            deterministic.setText(deterministicText, deterministic30);
        else if (Game.deterministicMode)
            deterministic.setText(deterministicText, deterministic60);
        else
            deterministic.setText(deterministicText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        back.update();
        timer.update();
        deterministic.update();

        super.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        deterministic.draw();
        timer.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Speedrunning options");
    }
}
