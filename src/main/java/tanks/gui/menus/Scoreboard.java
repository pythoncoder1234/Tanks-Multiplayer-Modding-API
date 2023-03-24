package tanks.gui.menus;

import tanks.*;
import tanks.gui.TextWithStyling;
import tanks.network.event.EventCreateScoreboard;
import tanks.network.event.EventScoreboardUpdateScore;

import java.util.*;
import java.util.stream.Collectors;

public class Scoreboard extends FixedMenu
{
    public enum SortOrder {ascending, descending}
    public enum SortBy {name, score}

    public TextWithStyling title = new TextWithStyling("Scoreboard", 255, 255, 0, 255, 24);
    public TextWithStyling namesStyle = new TextWithStyling("", 255, 255, 255, 255, 20);
    public TextWithStyling scoreStyle = new TextWithStyling("", 255, 64, 64, 255, 20);

    public HashMap<Player, Double> playerPoints = new HashMap<>();
    public HashMap<Team, Double> teamPoints = new HashMap<>();
    public ArrayList<Entry> pointsDisplay = new ArrayList<>();

    public SortOrder sortOrder = SortOrder.ascending;
    public SortBy sortBy = SortBy.score;
    protected Comparator<Map.Entry<Player, Double>> playerComparator;
    protected Comparator<Map.Entry<Team, Double>> teamComparator;

    public enum objectiveTypes
    {custom, kills, deaths, items_used, shots_fired, mines_placed, shots_fired_no_multiple_fire}

    public objectiveTypes objectiveType;

    public Scoreboard(String objectiveName, objectiveTypes objectiveType)
    {
        this.title.text = objectiveName;
        this.objectiveType = objectiveType;
        setSorting(SortBy.score, SortOrder.ascending);
    }

    public Scoreboard(String objectiveName, objectiveTypes objectiveType, ArrayList<Player> players, boolean isPlayer)
    {
        this.title.text = objectiveName;
        this.objectiveType = objectiveType;

        for (Player p : players)
            this.playerPoints.put(p, 0.0);

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
        this.playerComparator = sortBy == SortBy.name ? Map.Entry.comparingByKey(Comparator.comparing(player -> player.username)) : Map.Entry.comparingByValue();

        if (sortBy == SortBy.score)
        {
            this.playerComparator = this.playerComparator.reversed();
            this.teamComparator = this.teamComparator.reversed();
        }

        if (sortOrder == SortOrder.descending)
        {
            this.playerComparator = this.playerComparator.reversed();
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

    public void addPlayer(Player player)
    {
        addPlayerScore(player, 0);
    }

    public void addPlayerScore(Player p, double value)
    {
        playerPoints.putIfAbsent(p, 0D);
        playerPoints.put(p, value + teamPoints.get(p));

        sortAndSendEvent(p.username);
    }

    public boolean addPlayerScore(String playerName, double value)
    {
        for (Player p : this.playerPoints.keySet())
        {
            if (p.username.equals(playerName))
            {
                playerPoints.putIfAbsent(p, 0D);
                playerPoints.put(p, value + playerPoints.get(p));

                sortAndSendEvent(p.username);
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
                    .sorted(teamComparator).collect(Collectors.toList());

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
            List<Map.Entry<Player, Double>> sorted = playerPoints.entrySet().stream().limit(8)
                    .sorted(playerComparator).collect(Collectors.toList());

            for (int i = 0; i < sorted.size(); i++)
            {
                Map.Entry<Player, Double> e = sorted.get(i);

                if (e.getKey().username.equals(changed))
                {
                    end = i;
                    value = e.getValue();
                }

                pointsDisplay.add(new Entry(e.getKey().username, ModAPI.convertToString(e.getValue())));
            }
        }

        if (changed != null)
            Game.eventsOut.add(new EventScoreboardUpdateScore(this.id, start, end, changed, value));
    }

    @Override
    public void draw()
    {
        double[] sizes = drawScoreboard(title, namesStyle, scoreStyle, pointsDisplay, sizeX, sizeY);
        sizeX = sizes[0];
        sizeY = sizes[1];
    }

    public static double[] drawScoreboard(TextWithStyling title, TextWithStyling namesStyle, TextWithStyling scoreStyle, ArrayList<Entry> pointsDisplay, double sizeX, double sizeY)
    {
        Drawing.drawing.setColor(0, 0, 0, 128);
        ModAPI.fixedShapes.fillRect(Panel.windowWidth - sizeX, Panel.windowHeight / 2 - sizeY, sizeX, sizeY);

        double titleSize = ModAPI.fixedText.getStringSizeX(title.fontSize / 40, title.text);
        Drawing.drawing.setColor(title.colorR, title.colorG, title.colorB);
        ModAPI.fixedText.drawString(
                Panel.windowWidth - sizeX + 20,
                Panel.windowHeight / 2 - sizeY * 0.9,
                title.fontSize / 40, title.fontSize / 40,
                title.text);

        int index = 0;
        double maxSX = 0;
        double maxSY = 0;

        for (Entry entry: pointsDisplay)
        {
            double textSX = ModAPI.fixedText.getStringSizeX(namesStyle.fontSize / 40, entry.name);

            Drawing.drawing.setColor(namesStyle.colorR, namesStyle.colorG, namesStyle.colorB);
            ModAPI.fixedText.drawString(
                    Panel.windowWidth - sizeX + 20,
                    Panel.windowHeight / 2 - sizeY / 2 + index * (namesStyle.fontSize + 10),
                    namesStyle.fontSize / 40, namesStyle.fontSize / 40,
                    entry.name
            );

            double valueSX = ModAPI.fixedText.getStringSizeX(namesStyle.fontSize / 40, entry.value);

            Drawing.drawing.setColor(scoreStyle.colorR, scoreStyle.colorG, scoreStyle.colorB);
            ModAPI.fixedText.drawString(
                    Panel.windowWidth - 15 - valueSX,
                    Panel.windowHeight / 2 - sizeY / 2 + index * (namesStyle.fontSize + 10),
                    namesStyle.fontSize / 40, namesStyle.fontSize / 40,
                    entry.value
            );

            index++;

            maxSX = Math.max(textSX + valueSX + 100, maxSX);
            maxSY = Math.max(index * 30 + title.fontSize + namesStyle.fontSize + 30, maxSY);
        }

        return new double[] {Math.max(titleSize * 1.25, maxSX), maxSY};
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
