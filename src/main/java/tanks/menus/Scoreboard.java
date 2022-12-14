package tanks.menus;

import tanks.*;
import tanks.ModAPI;
import tanks.event.EventAddScoreboard;
import tanks.event.EventChangeScoreboardAttribute;
import tanks.event.EventScoreboardUpdateScore;

import java.util.ArrayList;
import java.util.HashMap;

public class Scoreboard extends FixedMenu
{
    public HashMap<Player, Double> playerPoints = new HashMap<>();
    public HashMap<Team, Double> teamPoints = new HashMap<>();
    public String name;

    public double titleColR = 255;
    public double titleColG = 255;
    public double titleColB = 0;
    public double titleFontSize = 24;
    public double namesFontSize = 20;

    public Team[] teamNames;
    public Player[] playerNames;

    public enum objectiveTypes
    {custom, kills, deaths, items_used, shots_fired, mines_placed, shots_fired_no_multiple_fire}

    public objectiveTypes objectiveType;

    private final RemoteScoreboard remoteScoreboard;

    public Scoreboard(String objectiveName, objectiveTypes objectiveType)
    {
        this.name = objectiveName;
        this.objectiveType = objectiveType;

        this.remoteScoreboard = new RemoteScoreboard(objectiveName, objectiveType.toString(), new ArrayList<>()
        {
        });
        this.remoteScoreboard.titleColR = this.titleColR;
        this.remoteScoreboard.titleColG = this.titleColG;
        this.remoteScoreboard.titleColB = this.titleColB;
        this.remoteScoreboard.namesFontSize = this.namesFontSize;
        this.remoteScoreboard.titleFontSize = this.titleFontSize;

        Game.eventsOut.add(new EventAddScoreboard(this.remoteScoreboard));
    }

    public Scoreboard(String objectiveName, objectiveTypes objectiveType, ArrayList<Player> players, boolean isPlayer)
    {
        this.name = objectiveName;
        this.objectiveType = objectiveType;

        ArrayList<String> names = new ArrayList<>();

        for (Player p : players)
        {
            this.playerPoints.put(p, 0.0);
            names.add(p.username);
        }

        this.playerNames = this.playerPoints.keySet().toArray(new Player[0]);

        this.remoteScoreboard = new RemoteScoreboard(objectiveName, objectiveType.toString(), names);
        this.remoteScoreboard.titleColR = this.titleColR;
        this.remoteScoreboard.titleColG = this.titleColG;
        this.remoteScoreboard.titleColB = this.titleColB;
        this.remoteScoreboard.namesFontSize = this.namesFontSize;
        this.remoteScoreboard.titleFontSize = this.titleFontSize;

        Game.eventsOut.add(new EventAddScoreboard(this.remoteScoreboard));
    }

    public Scoreboard(String objectiveName, objectiveTypes objectiveType, ArrayList<Team> teams)
    {
        this.name = objectiveName;
        this.objectiveType = objectiveType;

        ArrayList<String> names = new ArrayList<>();

        for (Team t : teams)
        {
            this.teamPoints.put(t, 0.0);
            names.add(t.name);
        }

        teamNames = this.teamPoints.keySet().toArray(new Team[0]);

        this.remoteScoreboard = new RemoteScoreboard(objectiveName, objectiveType.toString(), names);
        this.remoteScoreboard.titleColR = this.titleColR;
        this.remoteScoreboard.titleColG = this.titleColG;
        this.remoteScoreboard.titleColB = this.titleColB;
        this.remoteScoreboard.namesFontSize = this.namesFontSize;
        this.remoteScoreboard.titleFontSize = this.titleFontSize;

        Game.eventsOut.add(new EventAddScoreboard(this.remoteScoreboard));
    }

    public void addPlayer(Player player)
    {
        addPlayerScore(player, 0);
    }

    public void addPlayerScore(Player p, double value)
    {
        if (playerPoints.containsKey(p))
            playerPoints.replace(p, playerPoints.get(p) + value);
        else
            playerPoints.put(p, value);

        Game.eventsOut.add(new EventScoreboardUpdateScore(this.remoteScoreboard.id, p.username, playerPoints.get(p)));
    }

    public boolean addPlayerScore(String playerName, double value)
    {
        for (Player p : this.playerPoints.keySet())
        {
            if (p.username.equals(playerName))
            {
                playerPoints.replace(p, playerPoints.get(p) + value);
                Game.eventsOut.add(new EventScoreboardUpdateScore(this.remoteScoreboard.id, p.username, playerPoints.get(p)));
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
                teamPoints.replace(t, teamPoints.get(t) + value);
                Game.eventsOut.add(new EventScoreboardUpdateScore(this.remoteScoreboard.id, t.name, teamPoints.get(t)));
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

        if (teamPoints.containsKey(t))
            teamPoints.replace(t, teamPoints.get(t) + value);
        else
            teamPoints.put(t, value);

        Game.eventsOut.add(new EventScoreboardUpdateScore(this.remoteScoreboard.id, t.name, teamPoints.get(t)));
    }

    /**
     * Use this function to change an attribute of a scoreboard.<br>
     * You could change the variables directly, but side effects may occur if you do so.<br>
     *
     * @param attributeName Can be "titleColR", "titleColG", "titleColB", "titleFontSize", "namesFontSize", which each edit their own variables.
     * @param value         Can be any double.
     * @return itself
     */
    public Scoreboard changeAttribute(String attributeName, double value)
    {
        switch (attributeName)
        {
            case "titleColR":
                this.titleColR = value;
                break;
            case "titleColG":
                this.titleColG = value;
                break;
            case "titleColB":
                this.titleColB = value;
                break;
            case "titleFontSize":
                this.titleFontSize = value;
                break;
            case "namesFontSize":
                this.namesFontSize = value;
                break;
            default:
                System.err.println("Invalid attribute name: " + attributeName);
                break;
        }

        Game.eventsOut.add(new EventChangeScoreboardAttribute(this.remoteScoreboard.id, attributeName, value));
        return this;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 0, 128);
        ModAPI.fixedShapes.fillRect(Panel.windowWidth - sizeX, Panel.windowHeight / 2 - sizeY, sizeX, sizeY);

        Drawing.drawing.setColor(titleColR, titleColG, titleColB);
        ModAPI.fixedText.drawString(
                Panel.windowWidth - sizeX / 2 - ModAPI.fixedText.getStringSizeX(titleFontSize / 40, this.name) / 2,
                Panel.windowHeight / 2 - sizeY * 0.9,
                titleFontSize / 40, titleFontSize / 40,
                this.name);

        Drawing.drawing.setColor(255, 255, 255);

        double longestSX = 0;
        if (this.teamPoints.isEmpty())
        {
            for (int i = 0; i < playerNames.length; i++)
            {
                double textSizeX = ModAPI.fixedText.getStringSizeX(namesFontSize / 40, playerNames[i].username);

                Drawing.drawing.setColor(255, 255, 255);
                ModAPI.fixedText.drawString(
                        Panel.windowWidth - sizeX / 4 - 90 - textSizeX / 2,
                        Panel.windowHeight / 2 - sizeY / 2 + 200 + i * 30,
                        namesFontSize / 40, namesFontSize / 40,
                        playerNames[i].username
                );

                if (textSizeX > longestSX)
                    longestSX = textSizeX;

                Drawing.drawing.setColor(255, 64, 64);
                ModAPI.fixedText.drawString(
                        Panel.windowWidth - 15 - ModAPI.fixedText.getStringSizeX(namesFontSize / 40, ModAPI.convertToString(playerPoints.get(playerNames[i]))),
                        Panel.windowHeight / 2 - sizeY / 2 + i * 30,
                        namesFontSize / 40, namesFontSize / 40,
                        ModAPI.convertToString(playerPoints.get(playerNames[i]))
                );
            }
        }
        else
        {
            for (int i = 0; i < teamNames.length; i++)
            {
                double textSizeX = ModAPI.fixedText.getStringSizeX(namesFontSize / 40, teamNames[i].name);

                Drawing.drawing.setColor(255, 255, 255);
                ModAPI.fixedText.drawString(
                        Panel.windowWidth - sizeX / 4 - 90 - textSizeX / 2,
                        Panel.windowHeight / 2 - sizeY / 2 + i * 30,
                        namesFontSize / 40, namesFontSize / 40,
                        teamNames[i].name
                );

                if (longestSX > textSizeX)
                    longestSX = textSizeX;

                Drawing.drawing.setColor(255, 64, 64);
                ModAPI.fixedText.drawString(
                        Panel.windowWidth - 15 - ModAPI.fixedText.getStringSizeX(namesFontSize / 40, ModAPI.convertToString(teamPoints.get(teamNames[i]))),
                        Panel.windowHeight / 2 - sizeY / 2 + i * 30,
                        namesFontSize / 40, namesFontSize / 40,
                        ModAPI.convertToString(teamPoints.get(teamNames[i]))
                );
            }
        }

        sizeX = longestSX + 200;
        if (playerPoints.isEmpty())
            sizeY = this.teamNames.length * 30 + 70;
        else
            sizeY = this.playerNames.length * 30 + 70;
    }

    @Override
    public void update()
    {

    }
}
