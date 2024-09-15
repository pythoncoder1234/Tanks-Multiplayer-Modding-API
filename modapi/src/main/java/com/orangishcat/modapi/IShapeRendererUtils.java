package com.orangishcat.modapi;

public interface IShapeRendererUtils
{
    void fillRect(double x, double y, double sX, double sY, double radius);

    void drawRect(double x, double y, double sX, double sY, double borderWidth);

    void drawRect(double x, double y, double sX, double sY, double borderWidth, double borderRadius);
}
