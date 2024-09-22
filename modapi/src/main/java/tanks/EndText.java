package tanks;

public class EndText
{
    public static final EndText normal = new EndText().setTranslate(true);
    public static final EndText crusade = new EndText("Battle cleared!", "Battle failed!").setTranslate(true);

    public String winTitle = "Victory!";
    public String loseTitle = "You were destroyed!";
    public String winSubtitle = "", loseSubtitle = "", winTopText = "", loseTopText = "";
    public boolean translate = false;

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

    public EndText(String ws, String ls, String wsub, String lsub, String wtop, String ltop)
    {
        this(ws, ls, wsub, lsub);
        this.winTopText = wtop;
        this.loseTopText = ltop;
    }

    public EndText setTranslate(boolean translate)
    {
        this.translate = translate;
        return this;
    }
}
