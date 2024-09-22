package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

public class ScreenOptionsMisc extends ScreenOptionsOverlay
{
    public static final String autostartText = "Autostart: ";
    public static final String fullStatsText = "Stats animations: ";
    public static final String pauseText = "Pause on defocus: ";

    Button speedrunOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Speedrunning options", () -> Game.screen = new ScreenOptionsSpeedrun());

    Button extensionOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Extension options", () -> Game.screen = new ScreenOptionsExtensions());

    @Override
    public void update()
    {
        back.update();
        dos.update();
        speedrunOptions.update();
        extensionOptions.update();
        pauseOnDefocus.update();
        fullStats.update();
        autostart.update();

        super.update();
    }

    Button dos = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.disableObstacleShaders = !Game.disableObstacleShaders;
            dos.setText("Disable obstacle shaders: ", Game.disableObstacleShaders ? ScreenOptions.onText : ScreenOptions.offText);
        }
    }, "Enable if obstacles like rails---are crashing your game");

    Button autostart = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.autostart = !Game.autostart;

            if (Game.autostart)
                autostart.setText(autostartText, ScreenOptions.onText);
            else
                autostart.setText(autostartText, ScreenOptions.offText);
        }
    },
            "When enabled, levels will---start playing automatically---4 seconds after they are---loaded (if the play button---isn't clicked earlier)");

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        dos.draw();
        speedrunOptions.draw();
        extensionOptions.draw();
        pauseOnDefocus.draw();
        fullStats.draw();
        autostart.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Miscellaneous options");
    }    Button fullStats = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.fullStats = !Game.fullStats;

            if (Game.fullStats)
                fullStats.setText(fullStatsText, ScreenOptions.onText);
            else
                fullStats.setText(fullStatsText, ScreenOptions.offText);
        }
    },
            "When off, skips directly to the summary tab---of the crusade end stats screen");

    Button pauseOnDefocus = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Panel.pauseOnDefocus = !Panel.pauseOnDefocus;

            if (Panel.pauseOnDefocus)
                pauseOnDefocus.setText(pauseText, ScreenOptions.onText);
            else
                pauseOnDefocus.setText(pauseText, ScreenOptions.offText);
        }
    });

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions());

    public ScreenOptionsMisc()
    {
        if (Game.autostart)
            autostart.setText(autostartText, ScreenOptions.onText);
        else
            autostart.setText(autostartText, ScreenOptions.offText);

        if (Game.fullStats)
            fullStats.setText(fullStatsText, ScreenOptions.onText);
        else
            fullStats.setText(fullStatsText, ScreenOptions.offText);

        if (Panel.pauseOnDefocus)
            pauseOnDefocus.setText(pauseText, ScreenOptions.onText);
        else
            pauseOnDefocus.setText(pauseText, ScreenOptions.offText);

        dos.setText("Disable obstacle shaders: ", Game.disableObstacleShaders ? ScreenOptions.onText : ScreenOptions.offText);
    }
}
