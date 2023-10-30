package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.ModAPI;
import tanks.gui.TextWithStyling;
import tanks.gui.menus.RemoteScoreboard;
import tanks.gui.menus.Scoreboard;
import tanks.network.NetworkUtils;

import java.util.ArrayList;
import java.util.Objects;

public class EventCreateScoreboard extends PersonalEvent
{
    public ArrayList<Scoreboard.Entry> names = new ArrayList<>();
    public boolean sortOrder;
    public boolean sortBy;
    public double id;
    public TextWithStyling[] texts = new TextWithStyling[4];

    public boolean syncEnabled = false;

    public EventCreateScoreboard()
    {

    }

    public EventCreateScoreboard(Scoreboard scoreboard)
    {
        this.id = scoreboard.id;

        this.names = scoreboard.pointsDisplay;
        this.sortOrder = scoreboard.sortOrder == Scoreboard.SortOrder.ascending;
        this.sortBy = scoreboard.sortBy == Scoreboard.SortBy.score;
        this.syncEnabled = scoreboard.syncEnabled;

        texts[0] = scoreboard.title;
        texts[1] = scoreboard.subtitle;
        texts[2] = scoreboard.namesStyle;
        texts[3] = scoreboard.scoreStyle;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.id);
        b.writeBoolean(this.sortOrder);
        b.writeBoolean(this.sortBy);
        b.writeBoolean(this.syncEnabled);

        for (TextWithStyling text : texts)
            text.writeTo(b);

        StringBuilder names = new StringBuilder();
        for (Scoreboard.Entry s : this.names)
            names.append(s.name).append("\n").append(s.value).append("\n\n");

        NetworkUtils.writeString(b, names.toString());
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readDouble();
        this.sortOrder = b.readBoolean();
        this.sortBy = b.readBoolean();
        this.syncEnabled = b.readBoolean();

        for (int i = 0; i < texts.length; i++)
            texts[i] = TextWithStyling.readFrom(b);

        String[] read = Objects.requireNonNull(NetworkUtils.readString(b)).split("\n\n");
        for (String s : read)
        {
            String[] pair = s.split("\n");
            names.add(new Scoreboard.Entry(pair[0], pair[1]));
        }
    }

    @Override
    public void execute()
    {
        RemoteScoreboard scoreboard = new RemoteScoreboard(
                id, names,
                sortOrder ? Scoreboard.SortOrder.ascending : Scoreboard.SortOrder.descending,
                sortBy ? Scoreboard.SortBy.score : Scoreboard.SortBy.name
        );
        scoreboard.title = texts[0];
        scoreboard.subtitle = texts[1];
        scoreboard.namesStyle = texts[1];
        scoreboard.scoreStyle = texts[2];
        scoreboard.syncEnabled = syncEnabled;

        ModAPI.fixedMenus.add(scoreboard);
    }
}
