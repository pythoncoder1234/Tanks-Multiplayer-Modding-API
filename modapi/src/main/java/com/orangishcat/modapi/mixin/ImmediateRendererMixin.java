package com.orangishcat.modapi.mixin;

import basewindow.BaseShapeRenderer;
import com.orangishcat.modapi.IShapeRendererUtils;
import lwjglwindow.ImmediateModeShapeRenderer;
import org.spongepowered.asm.mixin.Mixin;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glEnd;

@Mixin(ImmediateModeShapeRenderer.class)
public abstract class ImmediateRendererMixin extends BaseShapeRenderer implements IShapeRendererUtils
{
    public void fillRect(double x, double y, double sX, double sY, double radius)
    {
        if (radius <= 0.2)
        {
            fillRect(x, y, sX, sY);
            return;
        }

        glBegin(GL_TRIANGLE_FAN);

        int sides = Math.max(4, (int) (radius / 4) + 5) / 2;

        radius = Math.min(radius, Math.min(sX, sY) / 2);

        final double[] xs = {x + radius, x + sX - radius, x + sX - radius, x + radius};
        final double[] ys = {y + radius, y + radius, y + sY - radius, y + sY - radius};
        int[] order = {2, 3, 4, 1};

        for (int i = 0; i < 4; i++)
        {
            for (double j = Math.PI * 2 * (order[i] / 4.); j < Math.PI * 2 * (order[i] + 1) / 4; j += Math.PI / 2 / sides)
                glVertex2d(xs[i] + Math.cos(j) * radius, ys[i] + Math.sin(j) * radius);
        }

        glEnd();
    }

    public void drawRect(double x, double y, double sX, double sY, double width)
    {
        width = Math.min(Math.min(width, sX), sY);

        if (width <= 1)
        {
            drawRect(x, y, sX, sY);
            return;
        }

        if (width > Math.min(sX, sY) / 2)
        {
            fillRect(x, y, sX, sY);
            return;
        }

        glBegin(GL_QUADS);
        glVertex2d(x, y);
        glVertex2d(x + sX - width, y);
        glVertex2d(x + sX - width, y + width);
        glVertex2d(x, y + width);

        glVertex2d(x, y + width);
        glVertex2d(x, y + sY - width);
        glVertex2d(x + width, y + sY - width);
        glVertex2d(x + width, y + width);

        glVertex2d(x, y + sY - width);
        glVertex2d(x + sX, y + sY - width);
        glVertex2d(x + sX, y + sY);
        glVertex2d( x, y + sY);

        glVertex2d(x + sX - width, y);
        glVertex2d(x + sX - width, y + sY - width);
        glVertex2d(x + sX, y + sY - width);
        glVertex2d(x + sX, y);
        glEnd();
    }

    public void drawRect(double x, double y, double sX, double sY, double width, double radius)
    {
        if (radius < Math.min(width / 6, Math.min(sX, sY) / 18))
        {
            drawRect(x, y, sX, sY, width);
            return;
        }

        if (width >= Math.min(sX, sY) * 0.9 || radius >= Math.min(sX, sY) * 0.9)
        {
            fillRect(x, y, sX, sY, radius);
            return;
        }

        radius = Math.min(radius, Math.min(width, (Math.min(sX, sY) - width) / 2));

        width /= 2;
        double innerRadius = radius / 2;
        int sides = Math.max(4, (int) (radius / 4) + 5);
        double change = Math.PI / 2 / sides;

        // Where the outer arc begins
        final double[] xs = {x + radius, x + sX - radius, x + sX - radius, x + radius};
        final double[] ys = {y + radius, y + radius, y + sY - radius, y + sY - radius};
        int[] order = {2, 3, 4, 1};

        final double[] xRadius = {0, radius, 0, -radius};
        final double[] yRadius = {-radius, 0, radius, 0};
        final double[] xWidth = {width, -width, -width, width};
        final double[] yWidth = {width, width, -width, -width};

        for (int i = 0; i < 4; i++)
        {
            glBegin(GL_TRIANGLE_FAN);
            double maxJ = Math.PI * 2 * (order[i] + 1) / 4;

            for (double j = Math.PI * 2 * (order[i] / 4.); j <= maxJ + change / 2; j += change)
                glVertex2d(xs[i] + Math.cos(j) * radius, ys[i] + Math.sin(j) * radius);

            int nextI = (i + 1) % 4;
            glVertex2d(xs[nextI] + xRadius[i], ys[nextI] + yRadius[i]);
            glVertex2d(xs[nextI] + xWidth[nextI] + innerRadius * Math.cos(maxJ), ys[nextI] + yWidth[nextI] + innerRadius * Math.sin(maxJ));

            if (innerRadius > 1)
            {
                for (double j = maxJ; j >= Math.PI * 2 * (order[i] / 4.) - change / 2; j -= change)
                    glVertex2d(xs[i] + xWidth[i] + Math.cos(j) * innerRadius, ys[i] + yWidth[i] + Math.sin(j) * innerRadius);
            }

            glEnd();
        }
    }
}
