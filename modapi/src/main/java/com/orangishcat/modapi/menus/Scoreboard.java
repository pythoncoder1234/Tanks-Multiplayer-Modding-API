package com.orangishcat.modapi.menus;

import com.orangishcat.modapi.Minigame;
import com.orangishcat.modapi.ModAPI;
import com.orangishcat.modapi.TextWithStyling;
import com.orangishcat.modapi.network.event.EventCreateScoreboard;
import tanks.Game;
import tanks.Team;
import tanks.tank.Tank;

import java.util.*;

/**
 * A scoreboard that displays a list of players and their scores, as well as the name of the objective.<br>
 * Highly customizable, and most variable names are self-explanatory.
 */
@SuppressWarnings("unused")
public class Scoreboard extends FixedMenu
{
    public enum SortOrder {ascending, descending}

    public enum SortBy {name, score}

    public TextWithStyling title = new TextWithStyling("Scoreboard", 255, 255, 0, 255, 24);
    public TextWithStyling subtitle = new TextWithStyling("", 255, 255, 255, 255, 20);
    public TextWithStyling namesStyle = new TextWithStyling("", 255, 255, 255, 255, 20);
    public TextWithStyling scoreStyle = new TextWithStyling("", 255, 64, 64, 255, 20);

    public HashMap<Tank, Double> tankPoints = new HashMap<>();
    public HashMap<Team, Double> teamPoints = new HashMap<>();
    public ArrayList<Entry> pointsDisplay = new ArrayList<>();

    public SortOrder sortOrder = SortOrder.ascending;
    public SortBy sortBy = SortBy.score;
    protected Comparator<Map.Entry<Tank, Double>> tankComparator;
    protected Comparator<Map.Entry<Team, Double>> teamComparator;

    public enum objectiveTypes
    {custom, kills, deaths, items_used, shots_fired, mines_placed}

    public objectiveTypes objectiveType;

    public Scoreboard(String objectiveName, objectiveTypes objectiveType)
    {
        this.title.text = objectiveName;
        this.objectiveType = objectiveType;
        setSorting(SortBy.score, SortOrder.ascending);
    }

    /** The <code>isPlayer</code> variable is used so the constructor does not clash with the team constructor, and does nothing. */
    public Scoreboard(String objectiveName, objectiveTypes objectiveType, ArrayList<Tank> tanks, boolean isTank)
    {
        this.title.text = objectiveName;
        this.objectiveType = objectiveType;

        for (Tank p : tanks)
            this.tankPoints.put(p, 0.0);

        setSorting(SortBy.score, SortOrder.ascending);
        refreshOrder();
    }

    public Scoreboard(String objectiveName, objectiveTypes objectiveType, ArrayList<Team> teams)
    {
        this.title.text = objectiveName;
        this.objectiveType = objectiveType;

        for (Team t : teams)
            this.teamPoints.put(t, 0.0);

        setSorting(SortBy.score, SortOrder.ascending);
        refreshOrder();
    }

    public void setSorting(SortBy sortBy, SortOrder sortOrder)
    {
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.teamComparator = sortBy == SortBy.name ? Map.Entry.comparingByKey(Comparator.comparing(team -> team.name)) : Map.Entry.comparingByValue();
        this.tankComparator = sortBy == SortBy.name ? Map.Entry.comparingByKey(Comparator.comparing(this::getName)) : Map.Entry.comparingByValue();

        if (sortBy == SortBy.score)
        {
            this.tankComparator = this.tankComparator.reversed();
            this.teamComparator = this.teamComparator.reversed();
        }

        if (sortOrder == SortOrder.descending)
        {
            this.tankComparator = this.tankComparator.reversed();
            this.teamComparator = this.teamComparator.reversed();
        }
    }

    public Scoreboard setSync(boolean enabled)
    {
        syncEnabled = enabled;
        return this;
    }

    public Scoreboard add()
    {
        ModAPI.fixedMenus.add(this);
        Game.eventsOut.add(new EventCreateScoreboard(this));
        return this;
    }

    public void addTank(Tank player)
    {
        addTankScore(player, 0);
    }

    public void addTankScore(Tank t, double value)
    {
        tankPoints.putIfAbsent(t, 0D);
        tankPoints.put(t, value + tankPoints.get(t));

        sortAndSendEvent(getName(t));
    }

    public boolean addTankScore(String playerName, double value)
    {
        for (Tank p : this.tankPoints.keySet())
        {
            if (getName(p).equals(playerName))
            {
                tankPoints.putIfAbsent(p, 0D);
                tankPoints.put(p, value + tankPoints.get(p));

                sortAndSendEvent(getName(p));
                return true;
            }
        }

        return false;
    }

    public boolean addTeamScore(String teamName, double value)
    {
        for (Team t : this.teamPoints.keySet())
        {
            if (t.name.equals(teamName))
            {
                teamPoints.putIfAbsent(t, 0D);
                teamPoints.put(t, value + teamPoints.get(t));

                sortAndSendEvent(t.name);
                return true;
            }
        }

        return false;
    }

    public void addTeam(Team t)
    {
        addTeamScore(t, 0);
    }

    public void addTeamScore(Team t, double value)
    {
        if (t == null)
            return;

        teamPoints.putIfAbsent(t, 0D);
        teamPoints.put(t, value + teamPoints.get(t));

        sortAndSendEvent(t.name);
    }

    public void refreshOrder()
    {
        sortAndSendEvent(null);
    }

    public void sortAndSendEvent(String changed)
    {
        int start = -1;
        int end = -1;
        double value = -1;

        if (changed != null)
        {
            for (int i = 0; i < pointsDisplay.size(); i++)
            {
                if (pointsDisplay.get(i).name.equals(changed))
                {
                    start = i;
                    break;
                }
            }
        }

        pointsDisplay.clear();

        if (!teamPoints.isEmpty())
        {
            List<Map.Entry<Team, Double>> sorted = teamPoints.entrySet().stream().limit(8)
                    .sorted(teamComparator).toList();

            for (int i = 0; i < sorted.size(); i++)
            {
                Map.Entry<Team, Double> e = sorted.get(i);

                if (e.getKey().name.equals(changed))
                {
                    end = i;
                    value = e.getValue();
                }

                pointsDisplay.add(new Entry(e.getKey().name, ModAPI.convertToString(e.getValue())));
            }
        }
        else
        {
            List<Map.Entry<Tank, Double>> sorted = tankPoints.entrySet().stream().limit(8)
                    .sorted(tankComparator).toList();

            for (int i = 0; i < sorted.size(); i++)
            {
                Map.Entry<Tank, Double> e = sorted.get(i);

                if (getName(e.getKey()).equals(changed))
                {
                    end = i;
                    value = e.getValue();
                }

                pointsDisplay.add(new Entry(getName(e.getKey()), ModAPI.convertToString(e.getValue())));
            }
        }

        if (changed != null)
            Game.eventsOut.add(new EventUpdateScoreboard(this.id, start, end, changed, value));
    }

    @Override
    public void draw()
    {
        double[] sizes = drawScoreboard(title, subtitle, namesStyle, scoreStyle, pointsDisplay, sizeX, sizeY);
        sizeX = sizes[0];
        sizeY = sizes[1];
    }

    public static double[] drawScoreboard(TextWithStyling title, TextWithStyling subtitle, TextWithStyling namesStyle, TextWithStyling scoreStyle, ArrayList<Entry> pointsDisplay, double sizeX, double sizeY)
    {
        double subtitleSize = (subtitle.text.isEmpty() ? 0 : subtitle.fontSize + 8);

        Drawing.drawing.setColor(0, 0, 0, 128);
        ModAPI.fixedShapes.fillRect(Panel.windowWidth - sizeX, Panel.windowHeight / 2 - sizeY / 2, sizeX, sizeY);

        double titleSize = Game.game.window.fontRenderer.getStringSizeX(title.fontSize / 40, title.text);
        double subtitleSizeX = Game.game.window.fontRenderer.getStringSizeX(subtitle.fontSize / 40, subtitle.text);
        title.setColor();
        Game.game.window.fontRenderer.drawString(
                Panel.windowWidth - sizeX / 2 - titleSize / 2,
                Panel.windowHeight / 2 - sizeY / 2 + 15,
                title.fontSize / 40, title.fontSize / 40,
                title.text
        );

        subtitle.setColor();
        Game.game.window.fontRenderer.drawString(
                Panel.windowWidth - sizeX / 2 - subtitleSizeX / 2,
                Panel.windowHeight / 2 - sizeY / 2 + 15 + subtitleSize,
                subtitle.fontSize / 40, subtitle.fontSize / 40,
                subtitle.text
        );

        int index = 0;
        double startY = Panel.windowHeight / 2 - sizeY / 2 + title.fontSize + 35 + subtitleSize;
        double maxSX = 0;
        double maxSY = 0;

        for (Entry entry: pointsDisplay)
        {
            double textSX = Game.game.window.fontRenderer.getStringSizeX(namesStyle.fontSize / 40, entry.name);

            namesStyle.setColor();
            Game.game.window.fontRenderer.drawString(
                    Panel.windowWidth - sizeX + 20,
                    startY + index * (namesStyle.fontSize + 10),
                    namesStyle.fontSize / 40, namesStyle.fontSize / 40,
                    entry.name
            );

            double valueSX = Game.game.window.fontRenderer.getStringSizeX(namesStyle.fontSize / 40, entry.value);

            scoreStyle.setColor();
            Game.game.window.fontRenderer.drawString(
                    Panel.windowWidth - 15 - valueSX,
                    startY + index * (namesStyle.fontSize + 10),
                    namesStyle.fontSize / 40, namesStyle.fontSize / 40,
                    entry.value
            );

            index++;

            maxSX = Math.max(textSX + valueSX + 100, maxSX);
            maxSY = Math.max(index * 30 + title.fontSize + subtitleSize + namesStyle.fontSize + 30, maxSY);
        }

        return new double[] {Math.max(titleSize + 50, Math.max(subtitleSize + 50, maxSX)), maxSY};
    }

    public String getName(Tank t)
    {
        return Minigame.getName(t);
    }

    public static class Entry
    {
        public String name;
        public String value;

        public int index = -1;

        public Entry(String name, String value)
        {
            this.name = name;
            this.value = value;
        }
    }
}
