package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.Drawing;
import tanks.Game;
import tanks.Team;
import tanks.gui.screen.ScreenGame;
import tanks.tank.TankAIControlled;

import java.util.ArrayList;

public class ObstaclePathfinding extends Obstacle
{
    public ArrayList<Team> teams = new ArrayList<>();

    public boolean selected = false;
    public boolean prevSelected = false;
    public int[] randomness = new int[3];

    public ObstaclePathfinding(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.update = true;
        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.enableStacking = false;
        this.enableGroupID = true;
        this.enableTeams = true;

        for (int i = 0; i < 3; i++)
            randomness[i] = (int) (randomness[i] + (Math.random() - 0.5) * 10);

        this.description = "An obstacle that controls tanks' pathfinding";
    }

    @Override
    public void draw()
    {
        if (Game.screen instanceof ScreenGame)
            return;

        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 150);
        Drawing.drawing.fillBox(this, this.posX, this.posY, 10, Game.tile_size * 0.5, Game.tile_size * 0.5, Game.tile_size * 0.5);
    }

    @Override
    public void drawTile(IBatchRenderableObject tile, double r, double g, double b, double d, double extra)
    {
        Drawing.drawing.setColor(r, g, b);
        Drawing.drawing.fillBox(tile, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, extra + d * (1 - Obstacle.draw_size / Game.tile_size));
    }

    @Override
    public int unfavorability(TankAIControlled t)
    {
        if (teams.contains(t.team))
            return 0;
        return 100;
    }
}
