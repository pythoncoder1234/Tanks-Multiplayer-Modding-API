package tanks.editorselector;

import tanks.Game;
import tanks.GameObject;
import tanks.Team;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;

public class TeamSelector<T extends GameObject> extends ChoiceSelector<T, Team>
{
    public Team team;

    /**
     * The default selected team, by default the index of the enemy team.<br>
     * It is changed when placing player spawns, in which it defaults to the index of the ally team.
     * <br><br>
     * If <code>choices.size()</code> <= <code>defaultTeamIndex</code>,
     * the chosen team is the last one in the choice list.
     */
    public int defaultTeamIndex = 1;

    @Override
    public void init()
    {
        this.id = "team";
        this.title = "Select " + (gameObject instanceof Tank ? "tank" : "obstacle") + " team";
        this.keybind = Game.game.input.editorTeam;
        this.image = "team.png";
        this.addNoneChoice = true;

        if (editor != null)
            this.choices = editor.teams;
        else
            this.choices = Game.currentLevel.teamsList;

        setChoice(Math.min(this.choices.size() - 1, defaultTeamIndex), false);

        this.team = this.selectedIndex >= 0 ? this.choices.get(this.selectedIndex) : null;
    }

    @Override
    public void setProperty(T o)
    {
        this.team = selectedChoice;

        if (o instanceof Obstacle)
            ((Obstacle) o).team = this.team;
        else if (o instanceof Tank)
            ((Tank) o).team = this.team;
    }

    @Override
    public void load()
    {
        if (editor != null)
            this.choices = editor.teams;
        else
            this.choices = Game.currentLevel.teamsList;

        this.team = this.choices.get(this.selectedIndex);

        if (this.team != null)
            this.button.setText("Team: ", this.team.name);
        else
            this.button.setText("No team");
    }

    @Override
    public String choiceToString(Team choice)
    {
        return choice.name;
    }
}
