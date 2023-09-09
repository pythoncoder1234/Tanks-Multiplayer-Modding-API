package tanks.editorselector;

import tanks.Game;
import tanks.GameObject;
import tanks.Team;
import tanks.tank.Tank;

import java.util.ArrayList;
import java.util.Arrays;

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
        this.objectProperty = "team";

        this.keybind = Game.game.input.editorTeam;
        this.image = "team.png";
        this.addNoneChoice = true;

        updateDefaultChoices();

        if (Game.currentLevel.enableTeams)
            setChoice(-1, false);
        else
            setChoice(Math.min(this.choices.size() - 1, defaultTeamIndex), false);

        this.team = this.selectedIndex >= 0 ? this.choices.get(this.selectedIndex) : null;
    }

    @Override
    public void load()
    {
        updateDefaultChoices();

        this.team = this.selectedChoice;

        if (this.team != null)
            this.button.setText("Team: ", this.team.name);
        else
            this.button.setText("No team");
    }

    @Override
    public String choiceToString(Team choice)
    {
        if (choice == null)
            return "null";

        return choice.name;
    }

    public void updateDefaultChoices()
    {
        if (editor != null)
            this.choices = editor.teams;
        else if (Game.currentLevel.enableTeams)
            this.choices = Game.currentLevel.teamsList;
        else if (!Game.currentLevel.disableFriendlyFire)
            this.choices = new ArrayList<>(Arrays.asList(Game.playerTeam, Game.enemyTeam));
        else
            this.choices = new ArrayList<>(Arrays.asList(Game.playerTeamNoFF, Game.enemyTeamNoFF));
    }
}
