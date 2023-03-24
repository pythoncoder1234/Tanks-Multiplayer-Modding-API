package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.InputSelector;

public class ScreenControlsCamera extends Screen
{
    InputSelector toggleZoom = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 180, 700, 40, "Toggle zoom", Game.game.input.zoom);
    InputSelector zoomIn = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 90, 700, 40, "Zoom in", Game.game.input.zoomIn);
    InputSelector zoomOut = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 0, 700, 40, "Zoom out", Game.game.input.zoomOut);
    InputSelector zoomAuto = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 90, 700, 40, "Toggle automatic zoom", Game.game.input.zoomAuto);
    InputSelector togglePerspective = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 180, 700, 40, "Toggle Perspective", Game.game.input.perspective);
    InputSelector cameraPitch = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 270, 700, 40, "Following Cam View Angle", Game.game.input.followingCamPitch);

    public ScreenControlsCamera()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        toggleZoom.update();
        zoomIn.update();
        zoomOut.update();
        zoomAuto.update();
        togglePerspective.update();
        cameraPitch.update();

        ScreenOverlayControls.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        toggleZoom.draw();
        zoomOut.draw();
        zoomIn.draw();
        zoomAuto.draw();
        togglePerspective.draw();
        cameraPitch.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Camera controls");

        ScreenOverlayControls.overlay.draw();
    }

}
