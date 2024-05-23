package tanks;

import basewindow.IBatchRenderableObject;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;

import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("UnusedReturnValue")
public class Chunk
{
    public static final Level defaultLevel = new Level("{28,18|,|,}");
    public static final Tile emptyTile = new Tile();
    public static boolean debug = false;

    public static HashMap<Integer, Chunk> chunks = new HashMap<>();
    public static ArrayList<Chunk> chunkList = new ArrayList<>();
    public static int chunkSize = 8;

    public final int chunkX, chunkY;
    public Face[] borderFaces = new Face[4];
    public final HashSet<Obstacle> obstacles = new HashSet<>();
    public final HashSet<Movable> movables = new HashSet<>();
    public final FaceList faces = new FaceList();
    public final FaceList staticFaces = new FaceList();
    public final FaceList[] faceLists = {faces, staticFaces};
    public final Tile[][] tileGrid = new Tile[chunkSize][chunkSize];

    public Chunk(Level l, Random r, int x, int y)
    {
        this.chunkX = x;
        this.chunkY = y;

        for (int i = 0; i < chunkSize; i++)
        {
            for (int j = 0; j < chunkSize; j++)
            {
                if (tileGrid[i][j] == null)
                    tileGrid[i][j] = setTileColor(l, r, new Tile());
            }
        }

        for (int i = 1; i <= 4; i++)
            this.borderFaces[i-1] = getBorderFace(this, i, l.sizeX, l.sizeY);
    }

    int[] x1 = {0, 1, 0, 0}, x2 = {1, 1, 1, 0};
    int[] y1 = {0, 0, 1, 0}, y2 = {0, 1, 1, 1};

    /**
     * @param side Integer from 1-4, indicating top, right, bottom, or left face
     */
    public Face getBorderFace(Chunk c, int side, int sizeX, int sizeY)
    {
        side--;
        Face f = new Face(null,
                Math.min(sizeX, (c.chunkX + x1[side]) * Chunk.chunkSize) * Game.tile_size,
                Math.min(sizeY, (c.chunkY + y1[side]) * Chunk.chunkSize) * Game.tile_size,
                Math.min(sizeX, (c.chunkX + x2[side]) * Chunk.chunkSize) * Game.tile_size,
                Math.min(sizeY, (c.chunkY + y2[side]) * Chunk.chunkSize) * Game.tile_size,
                side % 2 == 0, side <= 2, true, true);
        c.borderFaces[side] = f;
        return f;
    }

    public void addMovable(Movable m)
    {
        if (m == null)
            return;

        movables.add(m);
        faces.addFaces(m);
    }

    public void removeMovable(Movable m)
    {
        if (m == null)
            return;

        movables.remove(m);
        faces.removeFaces(m);
    }

    public void addObstacle(Obstacle o)
    {
        if (o == null)
            return;

        obstacles.add(o);
        staticFaces.addFaces(o);
    }

    public void removeObstacle(Obstacle o)
    {
        if (o == null)
            return;

        obstacles.remove(o);
        staticFaces.removeFaces(o);
    }

    public static Stream<Chunk> getChunksInRange(double x1, double y1, double x2, double y2)
    {
        return getChunksInRange((int) (x1 / Game.tile_size), (int) (y1 / Game.tile_size),
                (int) (x2 / Game.tile_size), (int) (y2 / Game.tile_size));
    }

    public static Stream<Chunk> getChunksInRange(int tx1, int ty1, int tx2, int ty2)
    {
        int x1 = tx1 / chunkSize, y1 = ty1 / chunkSize, x2 = tx2 / chunkSize, y2 = ty2 / chunkSize;
        return chunkList.stream().filter(chunk -> Game.lessThan(true, x1, chunk.chunkX, x2)
                && Game.lessThan(true, y1, chunk.chunkY, y2));
    }

    public static Tile setTileColor(Level l, Random r, Tile t)
    {
        t.colR = l.colorR + (Game.fancyTerrain ? r.nextDouble() * l.colorVarR : 0);
        t.colG = l.colorG + (Game.fancyTerrain ? r.nextDouble() * l.colorVarG : 0);
        t.colB = l.colorB + (Game.fancyTerrain ? r.nextDouble() * l.colorVarB : 0);
        t.depth = Game.fancyTerrain ? r.nextDouble() * 10 : 0;
        return t;
    }

    public static void reset()
    {
        Random r = new Random(0);
        for (Chunk c : chunkList)
            for (int x = 0; x < chunkSize; x++)
                for (int y = 0; y < chunkSize; y++)
                    c.tileGrid[x][y] = setTileColor(defaultLevel, r, new Tile());
    }

    public static void fillHeightGrid()
    {
        for (Chunk c : chunkList)
            for (int x = 0; x < chunkSize; x++)
                for (int y = 0; y < chunkSize; y++)
                    c.tileGrid[x][y].height = c.tileGrid[x][y].groundHeight = -1000;
    }

    public void removeSurfaceIfEquals(Obstacle o)
    {
        int x = toChunkTileCoords(o.posX);
        int y = toChunkTileCoords(o.posY);

        if (Objects.equals(tileGrid[x][y].surfaceObstacle, o))
        {
            tileGrid[x][y].surfaceObstacle = null;
            removeObstacle(o);
        }
    }

    public void removeObstacleIfEquals(Obstacle o)
    {
        int x = toChunkTileCoords(o.posX);
        int y = toChunkTileCoords(o.posY);

        if (Objects.equals(tileGrid[x][y].obstacle, o))
        {
            tileGrid[x][y].obstacle = null;
            removeObstacle(o);
        }
    }

    /** Automatically converts to chunk coordinates. */
    public Tile getChunkTile(int posX, int posY)
    {
        if (posX < 0 || posY < 0)
            return null;

        return tileGrid[posX % chunkSize][posY % chunkSize];
    }

    /** Automatically converts to tile coordinates and then chunk coordinates. */
    public Tile getChunkTile(double posX, double posY)
    {
        if (posX < 0 || posX >= Game.currentSizeX * Game.tile_size || posY < 0 || posY >= Game.currentSizeY * Game.tile_size)
            return null;

        return tileGrid[toChunkTileCoords(posX)][toChunkTileCoords(posY)];
    }

    public void setObstacle(int x, int y, Obstacle o)
    {
        if (!o.isSurfaceTile || tileGrid[x][y] == null)
        {
            if (tileGrid[x][y].obstacle != null && tileGrid[x][y].obstacle.isSurfaceTile)
                tileGrid[x][y].surfaceObstacle = tileGrid[x][y].obstacle;

            tileGrid[x][y].obstacle = o;
        }
        else
            tileGrid[x][y].surfaceObstacle = o;
    }

    public static Tile getTile(int tileX, int tileY)
    {
        return getChunk(tileX / chunkSize, tileY / chunkSize).getChunkTile(tileX, tileY);
    }

    /** Equivalent to <code>Chunk.getChunk(posX, posY).getChunkTile(posX, posY)</code> */
    public static Tile getTile(double posX, double posY)
    {
        return getChunk(posX, posY).getChunkTile(posX, posY);
    }

    public static Tile tileCoordsGet(double posX, double posY)
    {
        posX = (posX + 0.5) * Game.tile_size;
        posY = (posY + 0.5) * Game.tile_size;
        return getTile(posX, posY);
    }

    public static int toChunkTileCoords(double a)
    {
        return  (int) (a / Game.tile_size) % chunkSize;
    }

    public static double addCoords(double chunk, double tile)
    {
        return chunk * chunkSize + tile;
    }

    public static void drawDebugStuff()
    {
        if (!debug)
            return;

        Drawing.drawing.setColor(255, 0, 0, 128);

        for (Chunk c : chunkList)
            Drawing.drawing.drawRect(addCoords(c.chunkX, chunkSize / 2.) * Game.tile_size, addCoords(c.chunkY, chunkSize / 2.) * Game.tile_size,
                    chunkSize * Game.tile_size, chunkSize * Game.tile_size, 2);
    }

    public static class FaceList
    {
        /** dyn x, same y */
        public final TreeSet<Face> topFaces = new TreeSet<>();
        /** dym x, same y */
        public final TreeSet<Face> bottomFaces = new TreeSet<>();
        /** same x, dyn y */
        public final TreeSet<Face> leftFaces = new TreeSet<>();
        /** same x, dyn y */
        public final TreeSet<Face> rightFaces = new TreeSet<>();

        public void addFaces(ISolidObject s)
        {
            for (Face f : s.getHorizontalFaces())
            {
                if (f.positiveCollision)
                    topFaces.add(f);
                else
                    bottomFaces.add(f);
            }

            for (Face f : s.getVerticalFaces())
            {
                if (f.positiveCollision)
                    leftFaces.add(f);
                else
                    rightFaces.add(f);
            }
        }

        public void removeFaces(ISolidObject s)
        {
            for (Face f : s.getHorizontalFaces())
            {
                if (f.positiveCollision)
                    topFaces.remove(f);
                else
                    bottomFaces.remove(f);
            }

            for (Face f : s.getVerticalFaces())
            {
                if (f.positiveCollision)
                    leftFaces.remove(f);
                else
                    rightFaces.remove(f);
            }
        }

        public void clear()
        {
            topFaces.clear();
            bottomFaces.clear();
            leftFaces.clear();
            rightFaces.clear();
        }
    }

    public static class Tile implements IBatchRenderableObject
    {
        public Obstacle obstacle, surfaceObstacle;
        public double colR, colG, colB, depth, height, groundHeight, lastHeight;
        public boolean solid, unbreakable;

        public Tile updateHeight(double height)
        {
            this.height = Math.max(height, this.height);
            return this;
        }

        public Tile updateGroundHeight(double gh)
        {
            this.groundHeight = Math.max(gh, this.height);
            return this;
        }
    }

    public static void initialize()
    {
        populateChunks(defaultLevel);
    }

    public static void populateChunks(Level l)
    {
        chunks.clear();
        chunkList.clear();

        int sX = l.sizeX / chunkSize + 1, sY = l.sizeY / chunkSize + 1;
        Random r = new Random(0);

        for (int x = 0; x < sX; x++)
            for (int y = 0; y < sY; y++)
                addChunk(x, y, new Chunk(l, r, x, y));
    }

    public static Obstacle getObstacle(double posX, double posY)
    {
        return getObstacle(posX, posY, false);
    }

    public static Obstacle getObstacle(double posX, double posY, boolean isTileCoords)
    {
        int x = (int) posX;
        int y = (int) posY;

        if (!isTileCoords)
        {
            posX /= Game.tile_size;
            posY /= Game.tile_size;
        }

        return getChunk(posX, posY).tileGrid[x % chunkSize][y % chunkSize].obstacle;
    }

    public static Chunk getChunk(double posX, double posY)
    {
        return getChunk(posX, posY, false);
    }

    public static Chunk getChunk(double posX, double posY, boolean isTileCoords)
    {
        if (!isTileCoords)
        {
            posX = posX / Game.tile_size - 0.5;
            posY = posY / Game.tile_size - 0.5;
        }

        return getChunk((int) (posX / chunkSize), (int) (posY / chunkSize));
    }

    public static Chunk getChunk(int chunkX, int chunkY)
    {
        return chunks.get(encodeChunkCoords(chunkX, chunkY));
    }

    public static Chunk addChunk(int chunkX, int chunkY, Chunk c)
    {
        chunkList.add(c);
        return chunks.put(encodeChunkCoords(chunkX, chunkY), c);
    }

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

    public static int encodeChunkCoords(int chunkX, int chunkY)
    {
        return f(f(chunkX) + chunkY);
    }
}
