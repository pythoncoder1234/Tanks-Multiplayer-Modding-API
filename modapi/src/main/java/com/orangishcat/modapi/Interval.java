package com.orangishcat.modapi;

import tanks.Panel;

import java.util.HashMap;

public class Interval
{
    protected static HashMap<String, Interval> gameIntervals = new HashMap<>();
    protected static HashMap<String, Interval> levelIntervals = new HashMap<>();

    public String name;

    public Runnable runnable;
    public double baseTimer;
    public boolean isTimeout;

    public double timer;

    protected Interval(String name, Runnable r, double timer, boolean isTimeout)
    {
        this.baseTimer = timer;
        this.timer = timer;
        this.runnable = r;
        this.isTimeout = isTimeout;
        this.name = name;
    }

    /**
     * Sets a function to be run every <code>timer</code> centiseconds.
     * The first function call is not executed instantly.
     *
     * @param timer     The time in centiseconds in between every function call,
     *                  set to 0 to run the function once per frame.
     * @param levelOnly If true, the interval will be cleared when the current level ends,
     *                  otherwise it will be cleared when the minigame ends.
     */
    public static void setInterval(String name, Runnable r, double timer, boolean levelOnly)
    {
        if ((levelOnly ? levelIntervals : gameIntervals).containsKey(name))
            return;

        Interval i = new Interval(name, r, timer, false);

        if (!levelOnly)
            gameIntervals.put(name, i);
        else
            levelIntervals.put(name, i);
    }

    public static Interval getInterval(String name)
    {
        return gameIntervals.getOrDefault(name, levelIntervals.get(name));
    }

    public static Interval removeInterval(String name)
    {
        Interval removed = levelIntervals.remove(name);

        if (removed == null)
            removed = gameIntervals.remove(name);

        return removed;
    }

    public boolean run()
    {
        timer -= Panel.frameFrequency;

        if (timer <= 0)
        {
            this.runnable.run();
            timer = baseTimer;
            return this.isTimeout;
        }

        return false;
    }
}
