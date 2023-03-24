package tanks;

public class EndText
{
    public static final EndText normal = new EndText();
    public static final EndText crusade = new EndText("Battle cleared!", "Battle failed!");

    public String winString = "Victory!";
    public String loseString = "You were destroyed!";
    public String winSubtitle = "";
    public String loseSubtitle = "";

    public EndText() {}

    public EndText(String ws, String ls)
    {
        this.winString = ws;
        this.loseString = ls;
    }

    public EndText(String ws, String ls, String wsub, String lsub)
    {
        this.winString = ws;
        this.loseString = ls;
        this.winSubtitle = wsub;
        this.loseSubtitle = lsub;
    }
}
