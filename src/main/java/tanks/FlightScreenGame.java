package tanks;

import basewindow.transformation.RotationAboutPoint;
import basewindow.transformation.ScaleAboutPoint;
import basewindow.transformation.Translation;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;

// 
// Decompiled by Procyon v0.5.36
// 

public class FlightScreenGame extends ScreenGame
{
    public double fcAngle;

    public void updateFollowingCam()
    {
        Panel.autoZoom = false;
        this.fcAngle += (Drawing.drawing.getInterfaceMouseX() - this.prevCursorX) / 130.0 * (Game.firstPerson ? (1.0 - this.fcZoom) : 1.0);
        Game.game.window.setCursorLocked(true);
        if (Game.game.input.tilt.isPressed())
        {
            this.fcPitch += (Drawing.drawing.getInterfaceMouseY() - this.prevCursorY) / 500.0;
        }
        this.fcPitch = Math.max(0.0, Math.min(0.5, this.fcPitch));
        this.prevCursorX = Drawing.drawing.getInterfaceMouseX();
        this.prevCursorY = Drawing.drawing.getInterfaceMouseY();
        FlightScreenGame.fcZoomPressed = Game.game.input.fcZoom.isPressed();
        if (FlightScreenGame.fcZoomPressed)
        {
            if (Game.game.input.fcZoom.isValid())
            {
                if (System.currentTimeMillis() - this.fcZoomLastTap < 500.0)
                {
                    this.fcTargetZoom = 0.0;
                }
                this.fcZoomLastTap = (double) System.currentTimeMillis();
                Game.game.input.fcZoom.invalidate();
            }
            if (Game.game.window.validScrollUp && this.fcTargetZoom < 0.9)
            {
                this.fcTargetZoom += 0.05;
                Game.game.window.validScrollUp = false;
            }
            if (Game.game.window.validScrollDown && this.fcTargetZoom > 0.0)
            {
                this.fcTargetZoom -= 0.05;
                Game.game.window.validScrollDown = false;
            }
        }
        if (Math.abs(this.fcTargetZoom - this.fcZoom) < 0.05)
        {
            this.fcZoom = this.fcTargetZoom;
        }
        else
        {
            this.fcZoom += (this.fcTargetZoom - this.fcZoom) / 10.0;
        }
    }

    public void setPerspective()
    {
        if (Game.angledView && Game.framework == Game.Framework.lwjgl)
        {
            if (!Game.game.window.drawingShadow)
            {
                if (this.playing && (!this.paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && !ScreenGame.finished)
                {
                    this.slant = Math.min(1.0, this.slant + 0.01 * Panel.frameFrequency);
                }
                else if (ScreenGame.finished)
                {
                    this.slant = Math.max(0.0, this.slant - 0.01 * Panel.frameFrequency);
                }
            }
            this.slantRotation.pitch = this.slant * -3.141592653589793 / 16.0;
            this.slantTranslation.y = -this.slant * 0.05;
            if (!Game.followingCam)
            {
                Game.game.window.transformations.add(this.slantTranslation);
                Game.game.window.transformations.add(this.slantRotation);
            }
            Game.game.window.loadPerspective();
        }
        if (Game.followingCam && Game.framework == Game.Framework.lwjgl && !Game.game.window.drawingShadow)
        {
            final double frac = Panel.panel.zoomTimer;
            if (!Game.firstPerson)
            {
                Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0.0, 0.0, frac * ((this.fcAngle + 4.71238898038469) % 6.283185307179586 - 3.141592653589793), 0.0, -Drawing.drawing.statsHeight / Game.game.window.absoluteHeight / 2.0, 0.0));
                Game.game.window.transformations.add(new Translation(Game.game.window, 0.0, 0.1 * frac, 0.0));
                Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0.0, -1.0995574287564276 * frac + this.fcPitch, 0.0, this.fcPitch * 3.0, this.fcPitch * 3.0, -1.0));
                Game.game.window.transformations.add(new Translation(Game.game.window, 0.0, 0.0, 0.5 * frac));
                if (this.fcZoom > 0.0)
                {
                    Game.game.window.transformations.add(new ScaleAboutPoint(Game.game.window, 1.0, 1.0, this.fcZoom + 1.0, 0.0, 0.0, 0.0));
                }
            }
            else
            {
                Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0.0, 0.0, frac * ((this.fcAngle + 4.71238898038469) % 6.283185307179586 - 3.141592653589793), 0.0, -Drawing.drawing.statsHeight / Game.game.window.absoluteHeight / 2.0, 0.0));
                Game.game.window.transformations.add(new Translation(Game.game.window, 0.0, 0.1 * frac, 0.0));
                Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0.0, -1.5707963267948966 * frac, 0.0, 0.0, 0.0, -1.0));
                Game.game.window.transformations.add(new Translation(Game.game.window, 0.0, 0.0575 * frac, 0.9 * frac));
                if (this.fcZoom > 0.0)
                {
                    Game.game.window.transformations.add(new ScaleAboutPoint(Game.game.window, 1.0, 1.0, 1.0 - this.fcZoom, 0.0, 0.0, 0.0));
                }
            }
            Game.game.window.loadPerspective();
        }
    }
}
