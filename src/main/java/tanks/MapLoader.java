package tanks;

import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.event.EventLoadMapLevel;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class MapLoader
{
    /**
     * Distance (in tiles) before loading a new level onto the map
     */
    public static final int baseTilesBeforeLoad = 40;
    public static boolean debug = true;
    public static int tilesBeforeLoad = baseTilesBeforeLoad;

    /** Level to use when any part of the grid is <code>null</code> */
    public static Level fillerLevel = new Level("{28,18|||ally-true}");
    public int loadDistance;

    public Section[][] grid;
    public Section current;
    public boolean loaded = false;
    public boolean isRemote = false;
    public int spawnX;
    public int spawnY;
    public int currentX;
    public int currentY;

    public static final int[] dx = {1, -1, 0, 0};
    public static final int[] dy = {0, 0, 1, -1};
    protected Tank targetTank;
    protected Player targetPlayer;
    protected static double redrawCounter = 0;
    protected HashSet<Section> visited = new HashSet<>();

    public MapLoader(String[][] levelStrings, int spawnX, int spawnY)
    {
        this.grid = new Section[levelStrings.length][levelStrings[0].length];
        Section.loadedRows = new boolean[levelStrings.length];
        Section.loadedColumns = new boolean[levelStrings[0].length];

        for (int x = 0; x < levelStrings.length; x++)
        {
            for (int y = 0; y < levelStrings[0].length; y++)
            {
                int totalX = x == 0 ? 0 : this.grid[x - 1][y].totalX;
                int totalY = y == 0 ? 0 : this.grid[x][y - 1].totalY;

                this.grid[x][y] = new Section(x, y, levelStrings[x][y] != null ? new Level(levelStrings[x][y]) : fillerLevel, totalX, totalY);
            }
        }

        setVars(spawnX, spawnY);
    }

    public MapLoader(Level[][] grid, int spawnX, int spawnY)
    {
        this.grid = new Section[grid.length][grid[0].length];
        Section.loadedRows = new boolean[grid.length];
        Section.loadedColumns = new boolean[grid[0].length];

        for (int x = 0; x < grid.length; x++)
        {
            for (int y = 0; y < grid[0].length; y++)
            {
                int totalX = x == 0 ? 0 : this.grid[x - 1][y].totalY;
                int totalY = y == 0 ? 0 : this.grid[x][y - 1].totalX;

                this.grid[y][x] = new Section(x, y, grid[x][y] != null ? grid[x][y] : fillerLevel, totalX, totalY);
            }
        }

        setVars(spawnX, spawnY);
    }

    public MapLoader(int sizeX, int sizeY, int spawnX, int spawnY)
    {
        this.isRemote = true;
        this.grid = new Section[sizeX][sizeY];
        this.currentX = spawnX;
        this.currentY = spawnY;
    }

    public void load()
    {
        if (loaded)
            return;

        loaded = true;

        this.loadDistance = tilesBeforeLoad;
        tilesBeforeLoad = baseTilesBeforeLoad;

        if (!this.isRemote)
        {
            this.current = this.grid[spawnX][spawnY];
            this.current.level.loadLevel();
            this.current.loaded = true;
            Section.loadedColumns[spawnX] = true;
            Section.loadedRows[spawnY] = true;

            for (Obstacle o : Game.obstacles)
            {
                o.posX += spawnX * 50;
                o.posY += spawnY * 50;
            }

            for (Movable m : Game.movables)
            {
                m.posX += spawnX * 50;
                m.posY += spawnY * 50;
            }

            if (spawnX > 0 || spawnY > 0)
            {
                Game.currentSizeX = this.current.sizeX;
                Game.currentSizeY = this.current.sizeY;

                Section.loadTiles(this.current.level, 0, 0, grid);
            }

            for (Player p : Game.players)
            {
                if (p.tank == null)
                    continue;

                targetTank = p.tank;
                handleCloseTiles(spawnX, spawnY);
            }
        }

        Game.screen = new ScreenGame();
        ((ScreenGame) Game.screen).mapLoader = this;
    }

    public void update()
    {
        if (!loaded)
            return;

        tilesBeforeLoad = Math.min(tilesBeforeLoad, 100);

        for (Player p : Game.players)
        {
            targetPlayer = p;
            targetTank = p.tank;

            setCurrentPos();

            visited.clear();
            handleCloseTiles(currentX, currentY);
        }

        if (redrawCounter > 0)
            redrawCounter -= Panel.frameFrequency;

        if (redrawCounter < 0)
        {
            redrawCounter = 0;
//            Drawing.drawing.forceRedrawTerrain();
        }
    }

    private void setVars(int spawnX, int spawnY)
    {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.currentX = spawnX;
        this.currentY = spawnY;
        fillerLevel = new Level("{28,18|||ally-true}");

        for (Section[] sections : this.grid)
        {
            for (Section s : sections)
                s.grid = this.grid;
        }
    }

    private void setCurrentPos()
    {
        for (currentX = 0; currentX < this.grid[0].length; currentX++)
        {
            this.current = this.grid[currentX][currentY];

            if (targetTank.posX <= this.current.totalX * Game.tile_size)
                break;
        }

        if (currentX == this.grid[0].length)
            currentX--;

        for (currentY = 0; currentY < this.grid.length; currentY++)
        {
            this.current = this.grid[currentX][currentY];

            if (targetTank.posY <= this.current.totalY * Game.tile_size)
                break;
        }

        if (currentY == this.grid.length)
            currentY--;

        Level.currentLightIntensity = this.current.level.light;
        Level.currentShadowIntensity = this.current.level.shadow;
    }

    private void handleCloseTiles(int x, int y)
    {
        for (int i = 0; i < 4; i++)
        {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (newX < 0 || newX >= this.grid.length || newY < 0 || newY >= this.grid[0].length)
                continue;

            Section tile = this.grid[newX][newY];

            int distX = (int) Math.abs(targetTank.posX - tile.startX * 50);
            int distY = (int) Math.abs(targetTank.posY - tile.startY * 50);

            if (distX <= loadDistance * 50 && distY <= loadDistance * 50 && !tile.loaded)
            {
                tile.load(tile.startX, tile.sizeY);
                handleCloseTiles(newX, newY);
            }

            if (distX >= loadDistance * 100 && distY >= loadDistance * 100 && tile.loaded)
            {
                tile.unload();
            }
            else if (tile.loaded && !visited.contains(tile))
            {
                visited.add(tile);
                handleCloseTiles(newX, newY);
            }
        }
    }


    public static class Section
    {
        static boolean[] loadedRows;
        static boolean[] loadedColumns;

        public Level level;
        public Section[][] grid;
        public boolean loaded = false;

        public ArrayList<Obstacle> obstacles = new ArrayList<>();
        public ArrayList<Movable> movables = new ArrayList<>();
        public boolean saved = false;

        public int posX;
        public int posY;
        public int sizeX;
        public int sizeY;

        public int startX;
        public int startY;
        public int totalX;
        public int totalY;

        public int loadedX = 0;
        public int loadedY = 0;

        protected LinkedHashMap<String, TankAIControlled> customTanksMap = new LinkedHashMap<>();

        public Section(int x, int y, Level l, int prevX, int prevY)
        {
            this.level = l;

            level.sizeX = Integer.parseInt(level.screen[0]);
            level.sizeY = Integer.parseInt(level.screen[1]);

            posX = x;
            posY = y;
            sizeX = level.sizeX;
            sizeY = level.sizeY;
            startX = prevX;
            startY = prevY;
            totalX = sizeX + prevX;
            totalY = sizeY + prevY;
        }

        public void load(int moveX, int moveY)
        {
            if (loaded)
                return;

            loaded = true;

            Section.loadedColumns[this.posX] = true;
            Section.loadedRows[this.posY] = true;

            if (saved)
            {
                Game.obstacles.addAll(obstacles);
                Game.movables.addAll(movables);
                loadTiles(level, loadedX, loadedY, grid);
            }
            else
            {
                Game.currentSizeX = Math.max(Game.currentSizeX, totalX);
                Game.currentSizeY = Math.max(Game.currentSizeY, totalY);

                Game.eventsOut.add(new EventLoadMapLevel(level, moveX, moveY));

                loadTiles(level, loadedX, loadedY, grid);
                loadLevelWithOffset(level, moveX, moveY);
                saved = true;
            }

            updateLoadedSizes();
        }

        public static void loadTiles(Level level, int startX, int startY, Section[][] grid)
        {
            double[][] tilesR = Game.tilesR;
            double[][] tilesG = Game.tilesG;
            double[][] tilesB = Game.tilesB;
            double[][] tilesDepth = Game.tilesDepth;
            Obstacle[][] map = Game.obstacleGrid;

            Game.tilesR = new double[Game.currentSizeX][Game.currentSizeY];
            Game.tilesG = new double[Game.currentSizeX][Game.currentSizeY];
            Game.tilesB = new double[Game.currentSizeX][Game.currentSizeY];
            Game.tilesDepth = new double[Game.currentSizeX][Game.currentSizeY];
            Game.obstacleGrid = new Obstacle[Game.currentSizeX][Game.currentSizeY];

            int tileX = -1;
            int tileY = -1;
            int nextX = -1;
            int nextY = -1;
            Section currentTile = null;

            for (int x = 0; x < Game.currentSizeX; x++)
            {
                for (int y = 0; y < Game.currentSizeY; y++)
                {
                    if (x > nextX)
                    {
                        tileX++;
                        nextX = grid[tileX][Math.max(0, tileY)].totalX;
                        currentTile = grid[tileX][Math.max(0, tileY)];
                    }

                    if (y > nextY)
                    {
                        tileY++;
                        nextY = grid[tileX][tileY].totalY;
                        currentTile = grid[tileX][tileY];
                    }

                    if (!currentTile.loaded)
                        continue;

                    if (x + startX < tilesR.length && y + startY < tilesR[0].length)
                    {
                        Game.tilesR[x][y] = tilesR[x + startX][y + startY];
                        Game.tilesG[x][y] = tilesG[x + startX][y + startY];
                        Game.tilesB[x][y] = tilesB[x + startX][y + startY];
                        Game.tilesDepth[x][y] = tilesDepth[x + startX][y + startY];
                        Game.obstacleGrid[x][y] = map[x + startX][y + startY];
                    }
                    else
                    {
                        Game.tilesR[x][y] = (level.colorR + Math.random() * level.colorVarR);
                        Game.tilesG[x][y] = (level.colorG + Math.random() * level.colorVarG);
                        Game.tilesB[x][y] = (level.colorB + Math.random() * level.colorVarB);
                        Game.tilesDepth[x][y] = Math.random() * 10;
                    }
                }
            }

            Game.game.heightGrid = new double[Game.currentSizeX][Game.currentSizeY];
            Game.game.groundHeightGrid = new double[Game.currentSizeX][Game.currentSizeY];

            Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
            Game.game.unbreakableGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

            for (Obstacle o : Game.obstacles)
            {
                int x = (int) (o.posX / Game.tile_size);
                int y = (int) (o.posY / Game.tile_size);

                o.postOverride();

                if (o.startHeight > 1)
                    continue;

                if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
                {
                    if (o.bulletCollision)
                        Game.game.solidGrid[x][y] = true;

                    if (!o.shouldShootThrough)
                        Game.game.unbreakableGrid[x][y] = true;
                }
            }

            Drawing.drawing.setScreenBounds(Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size);
        }

        private void updateLoadedSizes()
        {
            if (posX > 0)
            {
                for (int i = posX; i < grid.length; i++)
                {
                    Section current = grid[i][posY];
                    Section prev = grid[i - 1][posY];

                    current.loadedX = prev.loadedX;

                    if (prev.loaded)
                        current.loadedX += prev.sizeX;
                }
            }

            if (posY > 0)
            {
                for (int i = posY; i < grid.length; i++)
                {
                    Section current = grid[posX][i];
                    Section prev = grid[posX][i - 1];

                    current.loadedY = prev.loadedY;

                    if (prev.loaded)
                        current.loadedY += prev.sizeY;
                }
            }
        }

        public static void loadLevelWithOffset(Level level, int offsetX, int offsetY, Section section)
        {
            for (TankAIControlled t : level.customTanks)
                section.customTanksMap.put(t.name, t);

            // note to self: copy Level.loadLevel code, starting with team loading
            if (level.enableTeams)
            {
                level.tankTeams = new Team[level.teams.length];

                for (int i = 0; i < level.teams.length; i++)
                {
                    String[] t = level.teams[i].split("-");

                    if (t.length >= 5)
                        level.tankTeams[i] = new Team(t[0], Boolean.parseBoolean(t[1]), Double.parseDouble(t[2]), Double.parseDouble(t[3]), Double.parseDouble(t[4]));
                    else if (t.length >= 2)
                        level.tankTeams[i] = new Team(t[0], Boolean.parseBoolean(t[1]));
                    else
                        level.tankTeams[i] = new Team(t[0]);

                    if (level.disableFriendlyFire)
                        level.tankTeams[i].friendlyFire = false;

                    level.teamsMap.put(t[0], level.tankTeams[i]);

                    level.teamsList.add(level.tankTeams[i]);
                }
            }
            else
            {
                if (level.disableFriendlyFire)
                {
                    level.teamsMap.put("ally", Game.playerTeamNoFF);
                    level.teamsMap.put("enemy", Game.enemyTeamNoFF);
                }
                else
                {
                    level.teamsMap.put("ally", Game.playerTeam);
                    level.teamsMap.put("enemy", Game.enemyTeam);
                }
            }

            Level.currentCloudCount = (int) (Math.random() * (double) level.sizeX / 10.0D + Math.random() * (double) level.sizeY / 10.0D);

            if (level.screen.length >= 11)
            {
                level.light = Integer.parseInt(level.screen[9]) / 100.0;
                level.shadow = Integer.parseInt(level.screen[10]) / 100.0;
            }

            level.reloadTiles();

            if (!((level.obstaclesPos.length == 1 && level.obstaclesPos[0].equals("")) || level.obstaclesPos.length == 0))
            {
                for (int i = 0; i < level.obstaclesPos.length; i++)
                {
                    String[] obs = level.obstaclesPos[i].split("-");

                    String[] xPos = obs[0].split("\\.\\.\\.");

                    double startX;
                    double endX;

                    startX = Double.parseDouble(xPos[0]);
                    endX = startX;

                    if (xPos.length > 1)
                        endX = Double.parseDouble(xPos[1]);

                    String[] yPos = obs[1].split("\\.\\.\\.");

                    double startY;
                    double endY;

                    startY = Double.parseDouble(yPos[0]);
                    endY = startY;

                    if (yPos.length > 1)
                        endY = Double.parseDouble(yPos[1]);

                    String name = "normal";

                    if (obs.length >= 3)
                        name = obs[2];

                    String meta = null;

                    if (obs.length >= 4)
                        meta = obs[3];

                    if (obs.length >= 5)
                        meta += "-" + obs[4];

                    startX += offsetX;
                    startY += offsetY;

                    for (double x = startX; x <= endX; x++)
                    {
                        for (double y = startY; y <= endY; y++)
                        {
                            Obstacle o = Game.registryObstacle.getEntry(name).getObstacle(x, y);

                            if (meta != null)
                                o.setMetadata(meta);

                            Game.obstacles.add(o);
                        }
                    }
                }
            }

            Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
            Game.game.unbreakableGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
            boolean[][] solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

            for (Obstacle o: Game.obstacles)
            {
                int x = (int) (o.posX / Game.tile_size);
                int y = (int) (o.posY / Game.tile_size);

                o.postOverride();

                if (o.startHeight > 1)
                    continue;

                if (o.bulletCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
                {
                    Game.game.solidGrid[x][y] = true;

                    if (!o.shouldShootThrough)
                        Game.game.unbreakableGrid[x][y] = true;
                }

                if (o.tankCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
                    solidGrid[x][y] = true;
            }

            boolean[][] tankGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

            for (Movable m : Game.movables)
            {
                if (m instanceof Tank)
                {
                    int x = (int) (m.posX / Game.tile_size);
                    int y = (int) (m.posY / Game.tile_size);

                    if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
                        tankGrid[x][y] = true;
                }
            }

            if (!level.preset[2].equals(""))
            {
                for (int i = 0; i < level.tanks.length; i++)
                {
                    String[] tank = level.tanks[i].split("-");
                    double x = Game.tile_size * (0.5 + Double.parseDouble(tank[0]) + offsetX);
                    double y = Game.tile_size * (0.5 + Double.parseDouble(tank[1]) + offsetY);
                    String type = tank[2].toLowerCase();
                    double angle = 0;

                    if (tank.length >= 4)
                        angle = (Math.PI / 2 * Double.parseDouble(tank[3]));

                    Team team = Game.enemyTeam;

                    if (level.disableFriendlyFire)
                        team = Game.enemyTeamNoFF;

                    if (level.enableTeams)
                    {
                        if (tank.length >= 5)
                            team = level.teamsMap.get(tank[4]);
                        else
                            team = null;
                    }

                    Tank t;
                    if (type.equals("player"))
                    {
                        if (team == Game.enemyTeam)
                            team = Game.playerTeam;

                        if (team == Game.enemyTeamNoFF)
                            team = Game.playerTeamNoFF;

                        level.playerSpawnsX.add(x);
                        level.playerSpawnsY.add(y);
                        level.playerSpawnsAngle.add(angle);
                        level.playerSpawnsTeam.add(team);

                        int x1 = (int) Double.parseDouble(tank[0]);
                        int y1 = (int) Double.parseDouble(tank[1]);

                        if (x1 >= 0 && y1 >= 0 && x1 < tankGrid.length && y1 < tankGrid[0].length)
                            tankGrid[x1][y1] = true;

                        continue;
                    }
                    else
                    {
                        if (section.customTanksMap.get(type) != null)
                            t = section.customTanksMap.get(type).instantiate(type, x, y, angle);
                        else
                            t = Game.registryTank.getEntry(type).getTank(x, y, angle);
                    }

                    t.team = team;

                    // Don't do this in your code! We only want to dynamically generate tank IDs on level load!
                    t.networkID = Tank.nextFreeNetworkID();
                    Tank.idMap.put(t.networkID, t);

                    if (ScreenPartyLobby.isClient)
                    {
                        TankRemote t1 = new TankRemote(t);
                        Game.movables.add(t1);
                    }
                    else
                        Game.movables.add(t);
                }
            }
        }

        public static void loadLevelWithOffset(Level level, int offsetX, int offsetY)
        {
            loadLevelWithOffset(level, offsetX, offsetY, null);
        }

        public void unload()
        {
            if (loaded)
                loaded = false;
            else
                return;

            obstacles = new ArrayList<>();
            movables = new ArrayList<>();

            for (Obstacle o : Game.obstacles)
            {
                if (Game.lessThan(startX, o.posX / 50 - 0.5, totalX) && Game.lessThan(startY, o.posY / 50 - 0.5, totalY))
                    obstacles.add(o);
            }

            movables = new ArrayList<>();
            for (Movable m : Game.movables)
            {
                if (!(!(m instanceof Tank) || m instanceof TankPlayer || m instanceof TankPlayerRemote) &&
                        Game.lessThan(startX, m.posX / 50 - 0.5, totalX) && Game.lessThan(startY, m.posY / 50 - 0.5, totalY))
                    movables.add(m);
            }

            Game.movables.removeAll(movables);
            Game.obstacles.removeAll(obstacles);

            for (int tileX = startX; tileX < totalX; tileX++)
            {
                for (int tileY = startY; tileY < totalY; tileY++)
                    Game.obstacleGrid[tileX][tileY] = null;
            }
        }
    }
}
