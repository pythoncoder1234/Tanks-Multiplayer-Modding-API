package tanks.gui.menus;

import tanks.ModAPI;
import tanks.gui.TextWithStyling;

import java.util.ArrayList;

public class RemoteScoreboard extends FixedMenu
{
    public ArrayList<Scoreboard.Entry> names;

    public Scoreboard.SortOrder sortOrder;
    public Scoreboard.SortBy sortBy;

    public TextWithStyling title = new TextWithStyling();
    public TextWithStyling subtitle = new TextWithStyling();
    public TextWithStyling namesStyle = new TextWithStyling();
    public TextWithStyling scoreStyle = new TextWithStyling();

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
        if (start != end)
        {
            if (start < end)
                end--;

            if (start > -1)
                names.remove(start);

            names.add(end, new Scoreboard.Entry(name, ModAPI.convertToString(value)));
        }
        else
            names.get(end).value = ModAPI.convertToString(value);
    }
}
