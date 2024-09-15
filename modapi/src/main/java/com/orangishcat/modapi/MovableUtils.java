package com.orangishcat.modapi;

public class MovableUtils
{
    public static double distanceBetween(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt(sqDistBetw(x1, y1, x2, y2));
    }

    public static double sqDistBetw(double x1, double y1, double x2, double y2)
    {
        return (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2);
    }

    public static double sqDistBetw(final GameObject a, final GameObject b)
    {
        return sqDistBetw(a.posX, a.posY, b.posX, b.posY);
    }

    public static boolean withinRange(final GameObject a, final GameObject b, double range)
    {
        return sqDistBetw(a, b) < range * range;
    }
}
