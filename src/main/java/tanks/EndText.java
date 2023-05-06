package tanks;

public class EndText
{
    public static final EndText normal = new EndText();
    public static final EndText crusade = new EndText("Battle cleared!", "Battle failed!");

    public String winTitle = "Victory!";
    public String loseTitle = "You were destroyed!";
    public String winSubtitle = "";
    public String loseSubtitle = "";

    public EndText() {}

    public EndText(String ws, String ls)
    {
        this.winTitle = ws;
        this.loseTitle = ls;
    }

    public EndText(String ws, String ls, String wsub, String lsub)
    {
        this.winTitle = ws;
        this.loseTitle = ls;
        this.winSubtitle = wsub;
        this.loseSubtitle = lsub;
    }
}
