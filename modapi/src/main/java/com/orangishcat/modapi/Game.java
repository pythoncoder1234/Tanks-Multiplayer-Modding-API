package com.orangishcat.modapi;

import tanks.Function;
import tanks.Movable;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static com.orangishcat.modapi.ModAPI.lessThan;

public class Game
{
    public static boolean disableObstacleShaders = false;

    public static final int[] dirX = {1, -1, 0, 0};
    public static final int[] dirY = {0, 0, 1, -1};

    public static Obstacle getObstacle(int posX, int posY)
    {
        return Objects.requireNonNullElse(Chunk.getTile(posX, posY), Chunk.emptyTile).obstacle;
    }

    public static Obstacle getSurfaceObstacle(int posX, int posY)
    {
        Chunk c = Chunk.getChunk(posX / Chunk.chunkSize, posY / Chunk.chunkSize);
        if (c != null)
            return Objects.requireNonNullElse(c.getChunkTile(posX, posY), Chunk.emptyTile).surfaceObstacle;
        return null;
    }

    public static Obstacle getObstacle(double posX, double posY)
    {
        return getObstacle((int) (posX / tanks.Game.tile_size), (int) (posY / tanks.Game.tile_size));
    }

    public static Obstacle getSurfaceObstacle(double posX, double posY)
    {
        return getSurfaceObstacle((int) (posX / tanks.Game.tile_size), (int) (posY / tanks.Game.tile_size));
    }

    /** Iterates through all chunks, applies {@code func} to the ones within the specified position range,
     * and filters through the collection it returns. Expects all pixel coordinates. */
    public static <T extends GameObject> ArrayList<T> getInRange(double x1, double y1, double x2, double y2, Function<Chunk, Collection<T>> func)
    {
        ArrayList<T> out = new ArrayList<>();
        Chunk.getChunksInRange(x1, y1, x2, y2).forEach(c ->
        {
            for (T o : func.apply(c))
            {
                if (lessThan(true, x1, o.posX, x2) && lessThan(true, x2, o.posY, y2))
                    out.add(o);
            }
        });
        return out;
    }

    /** Iterates through all chunks, applies {@code func} to the ones within {@code radius} of the position,
     * and filters through the collection it returns. Expects all pixel coordinates. */
    public static <T extends GameObject> ArrayList<T> getInRadius(double posX, double posY, double radius, Function<Chunk, Collection<T>> func)
    {
        ArrayList<T> out = new ArrayList<>();
        Chunk.getChunksInRadius(posX, posY, radius).forEach(c ->
        {
            for (T o : func.apply(c))
                if (Movable.sqDistBetw(o.posX, o.posY, posX, posY) < radius * radius)
                    out.add(o);
        });
        return out;
    }

    public static void removeObstacle(Obstacle o)
    {
        Chunk c = Chunk.getChunk(o.posX, o.posY);
        if (c != null)
            c.removeObstacleIfEquals(o);
    }

    public static boolean isSolid(int tileX, int tileY)
    {
        return Objects.requireNonNullElse(Chunk.getTile(tileX, tileY), Chunk.emptyTile).solid;
    }

    public static boolean isSolid(double posX, double posY)
    {
        return Objects.requireNonNullElse(Chunk.getTile(posX, posY), Chunk.emptyTile).solid;
    }

    public static boolean isUnbreakable(int tileX, int tileY)
    {
        return Objects.requireNonNullElse(Chunk.getTile(tileX, tileY), Chunk.emptyTile).unbreakable;
    }

    public static boolean isUnbreakable(double posX, double posY)
    {
        return Objects.requireNonNullElse(Chunk.getTile(posX, posY), Chunk.emptyTile).unbreakable;
    }

    public static double getTileHeight(double posX, double posY)
    {
        return Objects.requireNonNullElse(Chunk.getTile(posX, posY), Chunk.emptyTile).height;
    }

    public static void removeSurfaceObstacle(Obstacle o)
    {
        Chunk c = Chunk.getChunk(o.posX, o.posY);
        if (c != null)
            c.removeSurfaceIfEquals(o);
    }

    public static void setObstacle(double posX, double posY, Obstacle o)
    {
        Chunk c = Chunk.getChunk(posX, posY);
        if (c != null)
            c.setObstacle(Chunk.toChunkTileCoords(posX), Chunk.toChunkTileCoords(posY), o);
    }

    public static double sampleGroundHeight(double px, double py)
    {
        int x = (int) (px / tanks.Game.tile_size);
        int y = (int) (py / tanks.Game.tile_size);

        if (!tanks.Game.enable3dBg || !tanks.Game.enable3d || x < 0 || x >= tanks.Game.currentSizeX || y < 0 || y >= tanks.Game.currentSizeY)
            return 0;

        Chunk.Tile t = Chunk.getTile(x, y);
        return t.groundHeight + t.depth;
    }

    public static double sampleDefaultGroundHeight(double px, double py)
    {
        int x = (int) (px / tanks.Game.tile_size);
        int y = (int) (py / tanks.Game.tile_size);

        if (px < 0)
            x--;

        if (py < 0)
            y--;

        double r;
        if (!tanks.Game.fancyTerrain || !tanks.Game.enable3d || x < 0 || x >= tanks.Game.currentSizeX || y < 0 || y >= tanks.Game.currentSizeY)
            r = 0;
        else
            r = Chunk.getTile(x, y).depth;

        return r;
    }

    public static double sampleObstacleHeight(double px, double py)
    {
        int x = (int) (px / tanks.Game.tile_size);
        int y = (int) (py / tanks.Game.tile_size);

        if (px < 0)
            x--;

        if (py < 0)
            y--;

        double r;
        if (!tanks.Game.fancyTerrain || !tanks.Game.enable3d || x < 0 || x >= tanks.Game.currentSizeX || y < 0 || y >= tanks.Game.currentSizeY)
            r = 0;
        else
            r = Game.getTileHeight(px, py);

        return r;
    }
}
