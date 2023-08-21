package tanks.gui.menus;

import basewindow.InputCodes;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class Minimap extends FixedMenu
{
    public boolean draggable = false;
    public Level level = Game.currentLevel;
    public int posX = (int) (Panel.windowWidth - sizeX);
    public int posY = (int) (Panel.windowHeight - sizeY - Drawing.drawing.statsHeight);
    public static boolean enabled;
    public boolean centered = true;
    public boolean forceDisabled;

    public static float scale = 1f;
    public static float panSpeed = 50;
    public static int posOffsetX = 0;
    public static int posOffsetY = 0;
    public static double panOffsetX = 0;
    public static double panOffsetY = 0;
    public static int sizeX = 200;
    public static int sizeY = 230;
    public static boolean darkMode = true;
    public static boolean colorfulObstacles = true;

    String closeControl = Game.game.input.minimapToggle.input1.getInputName() + " to close";

    public Minimap()
    {
        forceDisabled = Game.currentGame != null && Game.currentGame.forceDisableMinimap;
    }

    @Override
    public void draw()
    {
        if (!enabled)
            return;

        double colA = 255 * (Obstacle.draw_size / Game.tile_size);
        int brightness = darkMode ? 0 : 255;
        Drawing.drawing.setColor(brightness, brightness, brightness, colA * 150/255);
        ModAPI.fixedShapes.fillRect(posX, posY, sizeX, sizeY);

        brightness = darkMode ? 255 : 0;
        Drawing.drawing.setColor(brightness, brightness, brightness, colA);
        ModAPI.fixedText.drawString(posX + 40, posY + 10, 0.5, 0.5, "Minimap (x" + scale + ")");
        ModAPI.fixedText.drawString(posX + 30, posY + 210, 0.5, 0.5, "Mode: " + (colorfulObstacles ? "Obstacles" : "Tanks"));

        if (forceDisabled)
        {
            Drawing.drawing.setColor(255, 0, 0);
            ModAPI.fixedText.drawString(posX + sizeX / 2.0 - 90, posY + sizeY / 2.0 - 50, 0.5, 0.5, "Disabled by level");
            ModAPI.fixedText.drawString(posX + sizeX / 2.0 - 40, posY + sizeY / 2.0 - 30, 0.5, 0.5, "settings");

            Drawing.drawing.setColor(brightness, brightness, brightness, colA);
            ModAPI.fixedText.drawString(posX + sizeX / 2.0 - 50, posY + sizeY / 2.0 + 10, 0.5, 0.5, closeControl);

            return;
        }

        double drawSize = 4 * scale * (Obstacle.draw_size / Game.tile_size);

        for (int gridX = 0; gridX < Game.currentSizeX; gridX++)
        {
            for (int gridY = 0; gridY < Game.currentSizeY; gridY++)
            {
                Obstacle o = Game.obstacleGrid[gridX][gridY];
                if (o == null || o.startHeight >= 1)
                    continue;

                double x;
                double y;

                if (centered)
                {
                    x = (posX + 95) + o.posX / 13 * scale - Game.playerTank.posX / 13 * scale;
                    y = (posY + 110) + o.posY / 13 * scale - Game.playerTank.posY / 13 * scale;
                }
                else
                {
                    x = (posX + 95 - panOffsetX) + o.posX / 13 * scale;
                    y = (posY + 110 - panOffsetY) + o.posY / 13 * scale;
                }

                if ((posX < x && x < posX + sizeX) && (posY + 30 < y && y < posY + (sizeY - 30)))
                {
                    if (colorfulObstacles)
                    {
                        Drawing.drawing.setColor(o.colorR, o.colorG, o.colorB, colA);
                        ModAPI.fixedShapes.fillRect(x, y, drawSize, drawSize);
                    }
                    else if (o.tankCollision)
                    {
                        if (o.destructible)
                            Drawing.drawing.setColor(101, 60, 22, colA);
                        else
                            Drawing.drawing.setColor(100, 100, 100, colA);

                        ModAPI.fixedShapes.fillRect(x, y, drawSize, drawSize);
                    }
                }
            }
        }

        for (Movable m : Game.movables)
        {
            double x;
            double y;

            if (centered)
            {
                x = (posX + 95) + m.posX / 13 * scale - Game.playerTank.posX / 13 * scale;
                y = (posY + 110) + m.posY / 13 * scale - Game.playerTank.posY / 13 * scale;
            }
            else
            {
                x = (posX + 95 - panOffsetX) + m.posX / 13 * scale;
                y = (posY + 110 - panOffsetY) + m.posY / 13 * scale;
            }

            if ((posX < x && x < posX + sizeX) && (posY + 30 < y && y < posY + (sizeY - 30)))
            {
                if (m instanceof Tank && !m.destroy)
                {
                    Tank t = (Tank) m;

                    if (m.team != null && m.team.enableColor)
                        Drawing.drawing.setColor(m.team.teamColorR, m.team.teamColorG, m.team.teamColorB, colA);
                    else
                        Drawing.drawing.setColor(t.colorR, t.colorG, t.colorB, colA);

                    if (m.equals(Game.playerTank))
                    {
                        if (!centered)
                        {
                            if (!Game.playerTank.destroy || ScreenGame.finished || ScreenGame.finishedQuick)
                                Drawing.drawing.setColor(0, 255, 0, colA);
                            else
                                Drawing.drawing.setColor(255, 0, 0, colA);

                            ModAPI.fixedShapes.drawImage(x, y, 12, 10, "/images/icons/vertical_arrow_white.png", Game.playerTank.angle - ModAPI.up, false);
                        }
                    }
                    else
                    {
                        ModAPI.fixedShapes.fillOval(x, y, 6 * (t.size / 50) * scale, 6 * (t.size / 50) * scale);

                        if (t.emblem != null)
                        {
                            double size = t.size / 650;
                            Drawing.drawing.setColor(t.emblemR, t.emblemG, t.emblemB, colA);
                            ModAPI.fixedShapes.drawImage(x + size * 20, y + size * 20, size * scale, size * scale, "/images/" + t.emblem, true);
                        }
                    }
                }
                else if (!colorfulObstacles)
                {
                    if (m instanceof Mine)
                    {
                        Drawing.drawing.setColor(((Mine) m).outlineColorR, ((Mine) m).outlineColorG, ((Mine) m).outlineColorB, colA);
                        ModAPI.fixedShapes.fillOval(x, y, 5, 5);
                    }

                    else if (m instanceof Bullet && !m.destroy)
                    {
                        Drawing.drawing.setColor(((Bullet) m).baseColorR, ((Bullet) m).baseColorG, ((Bullet) m).baseColorB, colA);
                        ModAPI.fixedShapes.fillOval(x, y, 5, 5);
                        Drawing.drawing.setColor(((Bullet) m).outlineColorR, ((Bullet) m).outlineColorG, ((Bullet) m).outlineColorB, colA);
                        ModAPI.fixedShapes.drawOval(x, y, 5, 5);
                    }
                }
            }
        }

        Drawing.drawing.setColor(0, 255, 0, colA);
        if (centered && !Game.playerTank.destroy)
            ModAPI.fixedShapes.drawImage(posX + sizeX / 2.0, posY + sizeY / 2.0, 12, 10, "/images/icons/vertical_arrow_white.png", Game.playerTank.angle - ModAPI.up, false);
    }

    @Override
    public void update()
    {
        if (Game.game.input.minimapToggle.isValid())
        {
            Game.game.input.minimapToggle.invalidate();
            enabled = !enabled;
        }

        if (!enabled)
            return;

        posX = (int) (Panel.windowWidth - sizeX - posOffsetX);
        posY = (int) (Panel.windowHeight - sizeY - Drawing.drawing.statsHeight - posOffsetY);

        if (posX < 0)
            posOffsetX = (int) (Panel.windowWidth - sizeX);
        else if (posY < 0)
            posOffsetY = (int) (Panel.windowHeight - sizeY);

        if (draggable && Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1) && !forceDisabled)
        {
            if ((posX < Game.game.window.absoluteMouseX && Game.game.window.absoluteMouseX < posX + 200) && (posY < Game.game.window.absoluteMouseY && Game.game.window.absoluteMouseY < posY + 200))
            {
                posOffsetX = (int) (Panel.windowWidth - Game.game.window.absoluteMouseX - sizeX / 2);
                posOffsetY = (int) (Panel.windowHeight - Game.game.window.absoluteMouseY - sizeY / 2);

                if (posOffsetX < 0)
                    posOffsetX = 0;
                else if (posOffsetX > Panel.windowWidth - sizeX)
                    posOffsetX = (int) (Panel.windowWidth - sizeX);

                if (posOffsetY < 0)
                    posOffsetY = 0;
                else if (posOffsetY > Panel.windowHeight - sizeY - Drawing.drawing.statsHeight)
                    posOffsetY = (int) (Panel.windowHeight - sizeY - Drawing.drawing.statsHeight);
            }
        }

        if (Game.game.input.minimapChangeTheme.isValid())
        {
            Game.game.input.minimapChangeTheme.invalidate();
            darkMode = !darkMode;
        }

        if (Game.game.input.minimapChangeType.isValid())
        {
            Game.game.input.minimapChangeType.invalidate();
            colorfulObstacles = !colorfulObstacles;
        }

        if (Game.game.input.minimapIncreaseScale.isValid() && scale < 3)
        {
            Game.game.input.minimapIncreaseScale.invalidate();
            scale += 0.25;
        }

        if (Game.game.input.minimapDecreaseScale.isValid() && scale > 0.25)
        {
            Game.game.input.minimapDecreaseScale.invalidate();
            scale -= 0.25;
        }

        if (Game.game.input.minimapPanUp.isValid())
        {
            Game.game.input.minimapPanUp.invalidate();

            if (centered)
            {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetY -= panSpeed * scale;
        }

        if (Game.game.input.minimapPanDown.isValid())
        {
            Game.game.input.minimapPanDown.invalidate();

            if (centered)
            {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetY += panSpeed * scale;
        }

        if (Game.game.input.minimapPanRight.isValid())
        {
            Game.game.input.minimapPanRight.invalidate();

            if (centered)
            {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetX += panSpeed * scale;
        }

        if (Game.game.input.minimapPanLeft.isValid())
        {
            Game.game.input.minimapPanLeft.invalidate();

            if (centered)
            {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetX -= panSpeed * scale;
        }

        if (Game.game.input.minimapRecenter.isPressed())
        {
            panOffsetX = 0;
            panOffsetY = 0;
            centered = true;
        }
    }
}
