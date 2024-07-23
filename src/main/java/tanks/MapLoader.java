package tanks;

import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MapLoader
{
    public HashMap<Integer, Chunk> chunkCache = new HashMap<>();
    public HashMap<Integer, Level> levelCache = new HashMap<>();
    public LevelSupplier supplier;
    public HashMap<Player, SpawnLocation> spawnLocations;
    public ArrayList<Player> participatingPlayers;

    public int levelSX, levelSY, seed = 0;

    public MapLoader(LevelSupplier levelSupplier, HashMap<Player, SpawnLocation> spawnLocations, int levelSizeX, int levelSizeY)
    {
        this.supplier = levelSupplier;
        this.spawnLocations = spawnLocations;
        this.participatingPlayers = new ArrayList<>(spawnLocations.keySet());
        this.levelSX = levelSizeX;
        this.levelSY = levelSizeY;
    }

    public void load()
    {
        Chunk.chunkList.clear();
        Player p = Game.player;
        if (p.tank == null)
            p = participatingPlayers.get((int) (Math.random() * participatingPlayers.size()));

        SpawnLocation loc = spawnLocations.get(p);
        getNearbyChunks(loc.chunkX, loc.chunkY, 6);
        for (Level l : getNearbyLevels(loc.chunkX, loc.chunkY, 6))
            l.loadLevel();
        Game.screen = new ScreenGame();
    }

    public void update()
    {
        for (Player p : Game.players)
        {
            Tank t = p.tank;
            if (t == null)
                continue;

        }
    }

    public ArrayList<Level> getNearbyLevels(int centerX, int centerY, int radius)
    {
        ArrayList<Level> levels = new ArrayList<>();
        for (int x = 0; x < radius; x++)
            for (int y = 0; y < radius; y++)
                levels.add(getLevel(x + centerX, y + centerY));
        return levels;
    }

    public Chunk getChunk(int chunkX, int chunkY)
    {
        int encodedPos = Chunk.encodeChunkCoords(chunkX, chunkY);
        Chunk c = chunkCache.get(encodedPos);
        if (c != null)
            return c;

        c = new Chunk(getLevel(chunkX, chunkY), new Random(seed + encodedPos), chunkX, chunkY);
        chunkCache.put(encodedPos, c);
        return c;
    }

    public Level getLevel(int chunkX, int chunkY)
    {
        int x = chunkX * Chunk.chunkSize / levelSX, y = chunkY * Chunk.chunkSize / levelSY;
        int encodedPos = Chunk.encodeChunkCoords(x, y);
        Level l = levelCache.get(encodedPos);
        if (l != null)
            return l;
        l = supplier.getLevel(x, y);
        l.startX = x * levelSX;
        l.startY = y * levelSY;
        l.mapLoad = true;
        levelCache.put(encodedPos, l);
        return l;
    }

    public ArrayList<Chunk> getNearbyChunks(int centerX, int centerY, int radius)
    {
        ArrayList<Chunk> chunks = new ArrayList<>();
        for (int x = 0; x < radius; x++)
            for (int y = 0; y < radius; y++)
                chunks.add(getChunk(x + centerX, y + centerY));
        return chunks;
    }

    public static HashMap<Player, SpawnLocation> singleSpawn(Player p, SpawnLocation location)
    {
        HashMap<Player, SpawnLocation> locations = new HashMap<>();
        locations.put(p, location);
        return locations;
    }

    public static HashMap<Player, SpawnLocation> distributePlayers(Random r, List<Player> players, List<SpawnLocation> spawnLocations)
    {
        HashMap<Player, SpawnLocation> locs = new HashMap<>();
        ArrayList<Player> copy = new ArrayList<>(players);

        for (int i = 0; i < players.size(); i++)
            locs.put(copy.remove(r.nextInt(copy.size())), spawnLocations.get(i % spawnLocations.size()));

        return locs;
    }

    public record SpawnLocation(int chunkX, int chunkY)
    {
        public Chunk getChunk(MapLoader l)
        {
            return l.getChunk(chunkX, chunkY);
        }
    }

    @FunctionalInterface
    public interface LevelSupplier
    {
        Level getLevel(int x, int y);
    }
}
