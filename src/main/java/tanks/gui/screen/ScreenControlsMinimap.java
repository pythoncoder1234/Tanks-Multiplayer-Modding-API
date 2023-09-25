package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.InputSelector;

public class ScreenControlsMinimap extends ScreenOptionsOverlay
{
    public static int page = 0;
    public static final int page_count = 2;

    InputSelector toggle = new InputSelector(this.centerX * 4 / 3, this.centerY - 225, 700, 40, "Toggle minimap", Game.game.input.minimapToggle);
    InputSelector zoomIn = new InputSelector(this.centerX * 4 / 3, this.centerY - 135, 700, 40, "Zoom in", Game.game.input.minimapZoomIn);
    InputSelector zoomOut = new InputSelector(this.centerX * 4 / 3, this.centerY - 45, 700, 40, "Zoom out", Game.game.input.minimapZoomOut);
    InputSelector changeType = new InputSelector(this.centerX * 4 / 3, this.centerY + 45, 700, 40, "Change type", Game.game.input.minimapType);
    InputSelector changeTheme = new InputSelector(this.centerX * 4 / 3, this.centerY + 135, 700, 40, "Change theme", Game.game.input.minimapTheme);

    InputSelector panUp = new InputSelector(this.centerX * 4 / 3, this.centerY - 225, 700, 40, "Pan up", Game.game.input.minimapPanUp);
    InputSelector panDown = new InputSelector(this.centerX * 4 / 3, this.centerY - 135, 700, 40, "Pan down", Game.game.input.minimapPanDown);
    InputSelector panLeft = new InputSelector(this.centerX * 4 / 3, this.centerY - 45, 700, 40, "Pan left", Game.game.input.minimapPanLeft);
    InputSelector panRight = new InputSelector(this.centerX * 4 / 3, this.centerY + 45, 700, 40, "Pan right", Game.game.input.minimapPanRight);
    InputSelector recenter = new InputSelector(this.centerX * 4 / 3, this.centerY + 135, 700, 40, "Recenter", Game.game.input.minimapRecenter);

    Button next = new Button(this.centerX * 4 / 3 + 190, this.centerY + 300, this.objWidth, this.objHeight, "Next page", () -> page++
    );

    Button previous = new Button(this.centerX * 4 / 3 - 190, this.centerY + 300, this.objWidth, this.objHeight, "Previous page", () -> page--
    );

    public ScreenControlsMinimap()
    {
        next.enabled = page < page_count - 1;
        previous.enabled = page > 0;

        this.next.image = "icons/forward.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "icons/back.png";
        this.previous.imageSizeX = 25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;
    }

    @Override
    public void update()
    {
        super.update();

        if (page == 0)
        {
            toggle.update();
            zoomIn.update();
            zoomOut.update();
            changeType.update();
            changeTheme.update();
        }
        else if (page == 1)
        {
            panUp.update();
            panDown.update();
            panLeft.update();
            panRight.update();
            recenter.update();
        }

        previous.enabled = page > 0;
        next.enabled = page < page_count - 1;

        previous.update();
        next.update();

        ScreenOverlayControls.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (page == 0)
        {
            toggle.draw();
            zoomOut.draw();
            zoomIn.draw();
            changeType.draw();
            changeTheme.draw();
        }
        else if (page == 1)
        {
            panUp.draw();
            panDown.draw();
            panLeft.draw();
            panRight.draw();
            recenter.draw();
        }

        previous.draw();
        next.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX * 4 / 3, this.centerY - 350, "Minimap controls");

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 250, "Page %d of %d", (page + 1), page_count);

        ScreenOverlayControls.overlay.draw();
    }
}
