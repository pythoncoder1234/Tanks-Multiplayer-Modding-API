package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

public class ScreenOverlayControls extends ScreenOptionsOverlay
{
    public static Screen lastControlsScreen = new ScreenControlsGame();

    public double objWidth = 350;
    public double objHeight = 40;
    public double objXSpace = 380;
    public double objYSpace = 60;

    public Button game = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 330, this.objWidth, this.objHeight, "Game", () -> Game.screen = new ScreenControlsGame());

    public Button camera = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 270, this.objWidth, this.objHeight, "Camera", () -> Game.screen = new ScreenControlsCamera());

    public Button tank = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 210, this.objWidth, this.objHeight, "Tank", () -> Game.screen = new ScreenControlsTank());

    public Button hotbar = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 150, this.objWidth, this.objHeight, "Hotbar", () -> Game.screen = new ScreenControlsHotbar());

    public Button editor = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 90, this.objWidth, this.objHeight, "Editor", () -> Game.screen = new ScreenControlsEditor());

    public Button minimap = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 30, this.objWidth, this.objHeight, "Minimap", () -> Game.screen = new ScreenControlsMinimap());

    Button reset = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 45, this.objWidth, this.objHeight, "Reset controls", () -> Game.screen = new ScreenResetControls()
    );

    public static final String mouseTargetText = "Mouse target: ";
    public static final String mouseTargetHeightText = "Mouse spotlight: ";
    public static final String constrainMouseText = "Constrain mouse: ";

    public static ScreenOverlayControls overlay = new ScreenOverlayControls();

    public ScreenOverlayControls()
    {
        if (Panel.showMouseTarget)
            mouseTarget.setText(mouseTargetText, ScreenOptions.onText);
        else
            mouseTarget.setText(mouseTargetText, ScreenOptions.offText);

        if (Panel.showMouseTargetHeight && Game.enable3d)
            mouseTargetHeight.setText(mouseTargetHeightText, ScreenOptions.onText);
        else
            mouseTargetHeight.setText(mouseTargetHeightText, ScreenOptions.offText);

        if (!Game.enable3d)
            mouseTargetHeight.enabled = false;

        if (Game.constrainMouse)
            constrainMouse.setText(constrainMouseText, ScreenOptions.onText);
        else
            constrainMouse.setText(constrainMouseText, ScreenOptions.offText);
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 360, this.objWidth, this.objHeight, "Back", () ->
    {
        Game.screen = new ScreenOptions();
        Game.game.input.save();
    });

    Button mouseTarget = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 135, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Panel.showMouseTarget = !Panel.showMouseTarget;

            if (Panel.showMouseTarget)
                mouseTarget.setText(mouseTargetText, ScreenOptions.onText);
            else
                mouseTarget.setText(mouseTargetText, ScreenOptions.offText);

            Game.game.window.setShowCursor(!Panel.showMouseTarget);
        }
    },
            "When enabled, your mouse pointer---will be replaced by a target");

    Button mouseTargetHeight = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 195, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Panel.showMouseTargetHeight = !Panel.showMouseTargetHeight;

            if (Panel.showMouseTargetHeight)
                mouseTargetHeight.setText(mouseTargetHeightText, ScreenOptions.onText);
            else
                mouseTargetHeight.setText(mouseTargetHeightText, ScreenOptions.offText);

            Game.game.window.setShowCursor(!Panel.showMouseTarget);
        }
    },
            "When enabled, while ingame or in the editor,---a spotlight will appear on your mouse---to help you judge the height of objects.");

    Button constrainMouse = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 255, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.constrainMouse = !Game.constrainMouse;

            if (Game.constrainMouse)
                constrainMouse.setText(constrainMouseText, ScreenOptions.onText);
            else
                constrainMouse.setText(constrainMouseText, ScreenOptions.offText);
        }
    },
            "Disallows your mouse pointer from---leaving the window while playing");

    public void update()
    {
        Screen s = Game.screen;
        if (s instanceof ScreenBindInput)
            s = ((ScreenBindInput) s).previous;

        game.enabled = !(s instanceof ScreenControlsGame);
        camera.enabled = !(s instanceof ScreenControlsCamera);
        tank.enabled = !(s instanceof ScreenControlsTank);
        hotbar.enabled = !(s instanceof ScreenControlsHotbar);
        editor.enabled = !(s instanceof ScreenControlsEditor);
        minimap.enabled = !(s instanceof ScreenControlsMinimap);

        lastControlsScreen = Game.screen;

        game.update();
        camera.update();
        tank.update();
        hotbar.update();
        editor.update();
        minimap.update();
        reset.update();
        back.update();

        mouseTarget.update();
        mouseTargetHeight.update();
        constrainMouse.update();
    }

    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 0, 127);

        double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
        double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

        Drawing.drawing.fillInterfaceRect(-extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.interfaceSizeX / 3, height);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(Game.screen.titleSize);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 380, "Controls");

        game.draw();
        camera.draw();
        tank.draw();
        hotbar.draw();
        editor.draw();
        minimap.draw();
        reset.draw();
        back.draw();

        constrainMouse.draw();
        mouseTargetHeight.draw();
        mouseTarget.draw();
        back.draw();
    }
}
