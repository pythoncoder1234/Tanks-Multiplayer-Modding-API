package com.orangishcat.modapi;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;

public class DrawingUtils
{
    public static void fillInterfaceRect(double x, double y, double sizeX, double sizeY, double borderRadius)
    {
        Drawing d = Drawing.drawing;
        double drawX = (d.interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - d.interfaceSizeX * d.interfaceScale) / 2);
        double drawY = (d.interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - d.statsHeight - d.interfaceSizeY * d.interfaceScale) / 2);
        double drawSizeX = (sizeX * d.interfaceScale);
        double drawSizeY = (sizeY * d.interfaceScale);

        ((IShapeRendererUtils) Game.game.window.shapeRenderer).fillRect(drawX, drawY, drawSizeX, drawSizeY, borderRadius);
    }

    public static void drawPopup(double x, double y, double sX, double sY, double borderWidth, double borderRadius)
    {
        fillInterfaceRect(x, y, sX, sY, borderRadius);
        drawInterfaceRect(x + borderWidth, y + borderWidth, sX, sY, borderWidth, borderRadius);
        Drawing.drawing.setColor(255, 255, 255);
    }

    public static void drawInterfaceRect(double x, double y, double sizeX, double sizeY, double lineWidth)
    {
        drawInterfaceRect(x, y, sizeX, sizeY, lineWidth, 0);
    }

    public static void drawInterfaceRect(double x, double y, double sizeX, double sizeY, double lineWidth, double borderRadius)
    {
        Drawing d = Drawing.drawing;
        double drawX = Math.round(d.interfaceScale * (x - sizeX / 2 - lineWidth) + Math.max(0, Panel.windowWidth - d.interfaceSizeX * d.interfaceScale) / 2);
        double drawY = Math.round(d.interfaceScale * (y - sizeY / 2 - lineWidth) + Math.max(0, Panel.windowHeight - d.statsHeight - d.interfaceSizeY * d.interfaceScale) / 2);
        double drawSizeX = Math.round(sizeX * d.interfaceScale);
        double drawSizeY = Math.round(sizeY * d.interfaceScale);

        ((IShapeRendererUtils) Game.game.window.shapeRenderer).drawRect(drawX, drawY, drawSizeX, drawSizeY, lineWidth, borderRadius);
    }
}
