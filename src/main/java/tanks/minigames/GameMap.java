package tanks.minigames;

import tanks.*;
import tanks.generator.LevelGeneratorRandom;

import java.util.List;
import java.util.Random;

public class GameMap extends Minigame
{
    public MapLoader mapLoader = new MapLoader((x, y) ->
    {
        LevelGeneratorRandom.largeLevels = false;
        return new Level(LevelGeneratorRandom.generateLevelString());
    },
            MapLoader.distributePlayers(new Random(), Game.players, List.of(new MapLoader.SpawnLocation(0, 0))),
            28, 18);

    /** Doesn't work yet! */
    public GameMap()
    {
        this.name = "Infinite World";
        this.description = "For testing! Try it---out if you want!";
    }

    @Override
    protected void start()
    {
        super.start();
        mapLoader.load();
    }
}
