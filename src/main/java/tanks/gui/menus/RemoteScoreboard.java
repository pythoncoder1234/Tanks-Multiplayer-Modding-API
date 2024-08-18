package tanks.gui.menus;

import tanks.ModAPI;
import tanks.gui.TextWithStyling;

import java.util.ArrayList;

public class RemoteScoreboard extends FixedMenu
{
    public ArrayList<Scoreboard.Entry> names;

    public Scoreboard.SortOrder sortOrder;
    public Scoreboard.SortBy sortBy;

    public TextWithStyling title = new TextWithStyling("Scoreboard", 255, 255, 0, 255, 24);
    public TextWithStyling subtitle = new TextWithStyling("", 255, 255, 255, 255, 20);
    public TextWithStyling namesStyle = new TextWithStyling("", 255, 255, 255, 255, 20);
    public TextWithStyling scoreStyle = new TextWithStyling("", 255, 64, 64, 255, 20);

    public RemoteScoreboard(double id, ArrayList<Scoreboard.Entry> names, Scoreboard.SortOrder sortOrder, Scoreboard.SortBy sortBy)
    {
        this.id = id;
        this.names = names;
        this.sortOrder = sortOrder;
        this.sortBy = sortBy;
    }

    @Override
    public void draw()
    {
        double[] sizes = Scoreboard.drawScoreboard(title, subtitle, namesStyle, scoreStyle, names, sizeX, sizeY);
        sizeX = sizes[0];
        sizeY = sizes[1];
    }

    public void updateScore(int start, int end, String name, double value)
    {
        if (end < 0)
            return;

        end = Math.min(names.size(), end);
        if (start != end)
        {
            if (start >= 0 && start < end)
                end--;

            if (start > -1)
                names.remove(start);

            names.add(end, new Scoreboard.Entry(name, ModAPI.convertToString(value)));
        }
        else
            names.get(end).value = ModAPI.convertToString(value);
    }
}
