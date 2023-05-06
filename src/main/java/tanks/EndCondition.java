package tanks;

import java.util.HashSet;

public abstract class EndCondition
{
    public HashSet<Team> aliveTeams;
    public HashSet<Team> fullyAliveTeams;
    public String winSound = "win.ogg";
    public String loseSound = "lose.ogg";

    public boolean finishedQuick() { return false; }
    public boolean finished() { return false; }
    public boolean teamWon(Team t) { return true; }
    public boolean playerWon(Player p) { return true; }

    public static EndCondition defaultEndCondition = new EndCondition()
    {
        public boolean finishedQuick() { return aliveTeams != null && aliveTeams.size() <= 1; }
        public boolean finished() { return fullyAliveTeams != null && fullyAliveTeams.size() <= 1; }
        public boolean teamWon(Team t) {
            return aliveTeams != null && aliveTeams.contains(t);
        }
        public boolean playerWon(Player p)
        {
            if (aliveTeams == null || aliveTeams.isEmpty())
                return false;

            return aliveTeams.iterator().next().name.equals(p.clientID.toString());
        }
    };

    public static EndCondition neverEnd = new EndCondition() {};
    public static EndCondition alwaysWin = new EndCondition()
    {
        public boolean finishedQuick() { return aliveTeams != null && aliveTeams.size() <= 1; }
        public boolean finished() { return fullyAliveTeams != null && fullyAliveTeams.size() <= 1; }

        public boolean teamWon(Team t) { return true; }
        public boolean playerWon(Player p) { return true; }
    };

    public static EndCondition instantWin = new EndCondition()
    {
        public boolean finishedQuick() { return true; }
        public boolean finished() { return true; }

        public boolean teamWon(Team t) { return true; }
        public boolean playerWon(Player p) { return true;}
    };
}
