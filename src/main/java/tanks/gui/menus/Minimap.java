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
    public Tank focusedTank;

    public boolean draggable = false;
    public Level level = Game.currentLevel;
    public int posX = (int) (Panel.windowWidth - size);
    public int posY = (int) (Panel.windowHeight - size - Drawing.drawing.statsHeight);
    public static boolean enabled;
    public boolean centered = true;
    public boolean forceDisabled;

    public static double scale = 1f;
    public static float panSpeed = 1;
    public static int posOffsetX = 0;
    public static int posOffsetY = 0;
    public static double panOffsetX = 0;
    public static double panOffsetY = 0;
    public static int size = 200;
    public static boolean darkMode = true;
    public static boolean obstaclesMode = true;

    String closeControl = Game.game.input.minimapToggle.input1.getInputName() + " to close";

    public Minimap()
    {
        forceDisabled = Game.currentGame != null && Game.currentGame.forceDisableMinimap;
    }

    @Override
    public void draw()
    {
        if (!enabled || Game.obstacleGrid == null)
            return;

        focusedTank = ScreenGame.focusedTank();

        double colA = 255 * (Obstacle.draw_size / Game.tile_size);

        double drawSize = 4 * scale;
        int cX = (int) ((centered ? focusedTank.posX : panOffsetX * 13 / scale) / Game.tile_size);
        int cY = (int) ((centered ? focusedTank.posY : panOffsetY * 13 / scale) / Game.tile_size);
        int tX = (int) (50 / scale);
        int tY = tX * size;
        int add = (Game.currentSizeX + Game.currentSizeY) / 40;

        for (int gridX = Math.max(-add, cX - tX); gridX < Math.min(Game.currentSizeX + add, cX + tX); gridX++)
        {
            for (int gridY = Math.max(-add, cY - tY); gridY < Math.min(Game.currentSizeY + add, cY + tY); gridY++)
            {
                double x;
                double y;

                if (centered)
                {
                    x = (posX + 95) + gridX * 50. / 13 * scale - focusedTank.posX / 13 * scale;
                    y = (posY + 110) + gridY * 50. / 13 * scale - focusedTank.posY / 13 * scale;
                }
                else
                {
                    x = (posX + 95 - panOffsetX) + gridX * 50. / 13 * scale;
                    y = (posY + 110 - panOffsetY) + gridY * 50. / 13 * scale;
                }

                Obstacle o = null;
                boolean inside = gridX >= 0 && gridX < Game.currentSizeX && gridY >= 0 && gridY < Game.currentSizeY;
                if (inside)
                    o = Game.obstacleGrid[gridX][gridY];

                double cx = posX + size / 2.;
                double cy = posY + size / 2.;

                if ((x-cx)*(x-cx)+(y-cy)*(y-cy) < (size*size) / 4.)
                {
                    if (o == null)
                    {
                        if (obstaclesMode)
                        {
                            if (inside)
                                Drawing.drawing.setColor(Game.tilesR[gridX][gridY], Game.tilesG[gridX][gridY], Game.tilesB[gridX][gridY], colA);
                            else
                                Drawing.drawing.setColor(174, 92, 16, colA);

                            ModAPI.fixedShapes.fillRect(x, y, drawSize, drawSize);
                        }

                        continue;
                    }

                    if (obstaclesMode)
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
                x = (posX + 95) + m.posX / 13 * scale - focusedTank.posX / 13 * scale;
                y = (posY + 110) + m.posY / 13 * scale - focusedTank.posY / 13 * scale;
            }
            else
            {
                x = (posX + 95 - panOffsetX) + m.posX / 13 * scale;
                y = (posY + 110 - panOffsetY) + m.posY / 13 * scale;
            }

            if ((posX < x && x < posX + size) && (posY + 30 < y && y < posY + (size - 30)))
            {
                if (m instanceof Tank && !m.destroy)
                {
                    Tank t = (Tank) m;
                    if (m.team != null && m.team.enableColor)
                        Drawing.drawing.setColor(m.team.teamColorR, m.team.teamColorG, m.team.teamColorB, colA);
                    else
                        Drawing.drawing.setColor(t.colorR, t.colorG, t.colorB, colA);

                    if (m.equals(focusedTank))
                    {
                        if (!centered)
                        {
                            if (!focusedTank.destroy || ScreenGame.finished || ScreenGame.finishedQuick)
                                Drawing.drawing.setColor(0, 255, 0, colA);
                            else
                                Drawing.drawing.setColor(255, 0, 0, colA);

                            ModAPI.fixedShapes.drawImage(x, y, 12, 10, "/images/icons/vertical_arrow.png", focusedTank.angle - ModAPI.up, false);
                        }
                    }
                    else
                    {
                        double size = 6 * (t.size / 50) * scale;
                        ModAPI.fixedShapes.fillOval(x - size / 2, y - size / 2, size, size);

                        if (t.emblem != null)
                        {
                            double emblemSize = t.size / 650;
                            double emblemOffset = -size / 2 + emblemSize * 7 * scale;
                            Drawing.drawing.setColor(t.emblemR, t.emblemG, t.emblemB, colA);
                            ModAPI.fixedShapes.drawImage(x + emblemOffset, y + emblemOffset, emblemSize * scale, emblemSize * scale, "/images/" + t.emblem, true);
                        }
                    }
                }
                else if (!obstaclesMode)
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
        if (centered && !focusedTank.destroy)
            ModAPI.fixedShapes.drawImage(posX + size / 2.0, posY + size / 2.0, 12, 10, "/images/icons/vertical_arrow.png", focusedTank.angle - ModAPI.up, false);

        if (darkMode)
            Drawing.drawing.setColor(0, 0, 0, colA);
        else
            Drawing.drawing.setColor(255, 255, 255, colA);

        for (double width = 0; width < 20; width++)
            ModAPI.fixedShapes.drawOval(posX - width / 2, posY - width / 2 + 5, size + width, size + width);
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

        posX = (int) (Panel.windowWidth - size - posOffsetX - 40);
        posY = (int) (Panel.windowHeight - size - Drawing.drawing.statsHeight - posOffsetY - 20);

        if (posX < 0)
            posOffsetX = (int) (Panel.windowWidth - size);
        else if (posY < 0)
            posOffsetY = (int) (Panel.windowHeight - size);

        if (draggable && Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1) && !forceDisabled)
        {
            if ((posX < Game.game.window.absoluteMouseX && Game.game.window.absoluteMouseX < posX + 200) && (posY < Game.game.window.absoluteMouseY && Game.game.window.absoluteMouseY < posY + 200))
            {
                posOffsetX = (int) (Panel.windowWidth - Game.game.window.absoluteMouseX - size / 2);
                posOffsetY = (int) (Panel.windowHeight - Game.game.window.absoluteMouseY - size / 2);

                if (posOffsetX < 0)
                    posOffsetX = 0;
                else if (posOffsetX > Panel.windowWidth - size)
                    posOffsetX = (int) (Panel.windowWidth - size);

                if (posOffsetY < 0)
                    posOffsetY = 0;
                else if (posOffsetY > Panel.windowHeight - size - Drawing.drawing.statsHeight)
                    posOffsetY = (int) (Panel.windowHeight - size - Drawing.drawing.statsHeight);
            }
        }

        if (Game.game.input.minimapTheme.isValid())
        {
            Game.game.input.minimapTheme.invalidate();
            darkMode = !darkMode;
        }

        if (Game.game.input.minimapType.isValid())
        {
            Game.game.input.minimapType.invalidate();
            obstaclesMode = !obstaclesMode;
        }

        if (Game.game.input.minimapZoomIn.isValid() && scale < 3)
        {
            scale += 0.01 * Panel.frameFrequency;
            panOffsetX -= .13;
            panOffsetY -= .13;
        }

        if (Game.game.input.minimapZoomOut.isValid() && scale > 0.25)
        {
            scale -= 0.01 * Panel.frameFrequency;
            panOffsetX += .13;
            panOffsetY += .13;
        }

        if (Game.game.input.minimapPanUp.isValid() && panOffsetY > -Game.tile_size / 13)
        {
            if (centered)
            {
                centered = false;
                panOffsetX = focusedTank.posX / 13 * scale;
                panOffsetY = focusedTank.posY / 13 * scale;
            }
            panOffsetY -= panSpeed * scale * Panel.frameFrequency;
        }

        if (Game.game.input.minimapPanDown.isValid() && panOffsetY < Game.currentSizeY * Game.tile_size / 13 * scale)
        {
            if (centered)
            {
                centered = false;
                panOffsetX = focusedTank.posX / 13 * scale;
                panOffsetY = focusedTank.posY / 13 * scale;
            }
            panOffsetY += panSpeed * scale * Panel.frameFrequency;
         }

        if (Game.game.input.minimapPanRight.isValid() && panOffsetX < Game.currentSizeX * Game.tile_size / 13 * scale)
        {
            if (centered)
            {
                centered = false;
                panOffsetX = focusedTank.posX / 13 * scale;
                panOffsetY = focusedTank.posY / 13 * scale;
            }
            panOffsetX += panSpeed * scale * Panel.frameFrequency;
        }

        if (Game.game.input.minimapPanLeft.isValid() && panOffsetX > -Game.tile_size / 13)
        {
            if (centered)
            {
                centered = false;
                panOffsetX = focusedTank.posX / 13 * scale;
                panOffsetY = focusedTank.posY / 13 * scale;
            }
            panOffsetX -= panSpeed * scale * Panel.frameFrequency;
        }

        if (Game.game.input.minimapRecenter.isPressed())
        {
            panOffsetX = 0;
            panOffsetY = 0;
            centered = true;
        }
    }
}
