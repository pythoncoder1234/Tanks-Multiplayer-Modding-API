package tanks;

import tanks.gui.screen.ILevelPreviewScreen;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;
import tanks.hotbar.item.Item;
import tanks.network.event.EventEnterLevel;
import tanks.network.event.EventLoadLevel;
import tanks.network.event.EventTankRemove;
import tanks.network.event.INetworkEvent;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.*;

public class Level 
{
	public String levelString;

	public String[] preset, screen, obstaclesPos, tanks, teams;
	public TreeMap<TankAIControlled, ArrayList<TankAIControlled>> references = new TreeMap<>(Comparator.comparing(t -> t.name));
	public ArrayList<TankAIControlled> valueTanks = new ArrayList<>();

	public Team[] tankTeams;
	public boolean enableTeams = false;

	public static double currentColorR = 235;
	public static double currentColorG = 207;
    public static double currentColorB = 166;

    public static double currentColorVarR = 235;
    public static double currentColorVarG = 207;
    public static double currentColorVarB = 166;

    public static double currentLightIntensity = 1;
    public static double currentShadowIntensity = 0.5;

    public static int currentCloudCount = 0;

    public static Random random = new Random();

    public boolean editable = true;
    public boolean remote = false;
    public boolean preview = false;

    public boolean timed = false;
    public double timer;

	public int startX, startY;
	public int sizeX, sizeY;

    public int colorR = 235;
    public int colorG = 207;
    public int colorB = 166;

    public int colorVarR = 20;
    public int colorVarG = 20;
    public int colorVarB = 20;

	public int tilesRandomSeed = 0;

	public double light = 1.0;
	public double shadow = 0.5;

	public HashMap<String, Team> teamsMap = new HashMap<>();

	public ArrayList<Team> teamsList = new ArrayList<>();

	public ArrayList<Integer> availablePlayerSpawns = new ArrayList<>();

	public ArrayList<Double> playerSpawnsX = new ArrayList<>();
	public ArrayList<Double> playerSpawnsY = new ArrayList<>();
	public ArrayList<Double> playerSpawnsAngle = new ArrayList<>();
	public ArrayList<Team> playerSpawnsTeam = new ArrayList<>();

	public ArrayList<Player> includedPlayers = new ArrayList<>();
	public HashSet<String> properties = new HashSet<>();

	public int startingCoins;
	public ArrayList<Item> shop = new ArrayList<>();
	public ArrayList<Item> startingItems = new ArrayList<>();
	public ArrayList<TankAIControlled> customTanks = new ArrayList<>();

	public double startTime = 400;
	public boolean disableFriendlyFire = false;
	public boolean updateModify = true;
	public boolean mapLoad = false;

	/**
	 * A level string is structured like this:
	 * (parentheses signify required parameters, and square brackets signify optional parameters.
	 * Asterisks indicate that the parameter can be repeated, separated by commas
	 * Do not include these in the level string.)
	 * {(SizeX),(SizeY),[(Red),(Green),(Blue)],[(RedNoise),(GreenNoise),(BlueNoise)]|[(ObstacleX)-(ObstacleY)-[ObstacleMetadata]]*|[(TankX)-(TankY)-(TankType)-[TankAngle]-[TeamName]]*|[(TeamName)-[FriendlyFire]-[(Red)-(Green)-(Blue)]]*}
	 */
	public Level(String level)
	{
		if (ScreenPartyHost.isServer)
			this.startTime = Game.partyStartTime;

		this.levelString = level.replaceAll("\u0000", "");

		int parsing = 0;

		String[] lines = this.levelString.split("\n");

		for (String s: lines)
		{
            switch (s.toLowerCase())
            {
                case "level" -> parsing = 0;
                case "items" -> parsing = 1;
                case "shop" -> parsing = 2;
                case "coins" -> parsing = 3;
                case "tanks" -> parsing = 4;
                case "properties" -> parsing = 5;
                default ->
                {
                    if (parsing == 0)
                    {
                        preset = s.substring(s.indexOf('{') + 1, s.indexOf('}')).split("\\|");
                        screen = preset[0].split(",");
                        obstaclesPos = preset[1].split(",");
                        tanks = preset[2].split(",");

                        if (preset.length >= 4)
                        {
                            teams = preset[3].split(",");
                            enableTeams = true;
                        }

                        if (screen[0].startsWith("*"))
                        {
                            editable = false;
                            screen[0] = screen[0].substring(1);
                        }
                    }
                    else if (parsing == 4)
                    {
                        TankAIControlled t = TankAIControlled.fromString(s, this);
                        this.customTanks.add(t);
                    }
                    else if (parsing == 5)
                    {
                        properties.add(s);
                    }
                    else if (!ScreenPartyLobby.isClient)
                    {
                        if (parsing == 1)
                            this.startingItems.add(Item.parseItem(null, s));
                        else if (parsing == 2)
                            this.shop.add(Item.parseItem(null, s));
                        else
                            this.startingCoins = Integer.parseInt(s);
                    }
                }
            }
		}

//		TankReferenceSolver.solveReferences(customTanks);

		if (ScreenPartyHost.isServer && Game.disablePartyFriendlyFire)
			this.disableFriendlyFire = true;

		sizeX = Integer.parseInt(screen[0]);
		sizeY = Integer.parseInt(screen[1]);

		if (screen.length >= 5)
		{
			colorR = Integer.parseInt(screen[2]);
			colorG = Integer.parseInt(screen[3]);
			colorB = Integer.parseInt(screen[4]);

			if (screen.length >= 8)
			{
				colorVarR = Math.min(255 - colorR, Integer.parseInt(screen[5]));
				colorVarG = Math.min(255 - colorG, Integer.parseInt(screen[6]));
				colorVarB = Math.min(255 - colorB, Integer.parseInt(screen[7]));
			}
		}
	}

	public void loadLevel()
	{
		loadLevel(null);
	}

	public void loadLevel(boolean remote)
	{
		loadLevel(null, remote);
	}

	public void loadLevel(ILevelPreviewScreen s)
	{
		loadLevel(s, false);
	}

	public void loadLevel(ILevelPreviewScreen sc, boolean remote)
	{
		int currentCrusadeID = 0;

		if (Game.deterministicMode)
			random = new Random(Game.seed);
		else
			random = new Random(tilesRandomSeed);

		if (ScreenPartyHost.isServer)
			ScreenPartyHost.includedPlayers.clear();
		else if (ScreenPartyLobby.isClient)
			ScreenPartyLobby.includedPlayers.clear();

		if (sc == null)
			Obstacle.draw_size = 0;
		else
			Obstacle.draw_size = Game.tile_size;

		this.remote = remote;

		if (!remote && sc == null || (sc instanceof ScreenLevelEditor))
			Game.eventsOut.add(new EventLoadLevel(this));

		LinkedHashMap<String, TankAIControlled> customTanksMap = getTankMap();

		Tank.currentID = 0;
		Tank.freeIDs.clear();

		Game.currentLevel = this;
		Game.currentLevelString = this.levelString;

		ScreenGame.finishedQuick = false;

		ScreenGame.finished = false;
		ScreenGame.finishTimer = ScreenGame.finishTimerMax;

		TankReferenceSolver.solveAllReferences(this, customTanksMap);

		if (enableTeams)
		{
			tankTeams = new Team[teams.length];

			for (int i = 0; i < teams.length; i++)
			{
				String[] t = teams[i].split("-");

				if (t.length >= 5)
					tankTeams[i] = new Team(t[0], Boolean.parseBoolean(t[1]), Double.parseDouble(t[2]), Double.parseDouble(t[3]), Double.parseDouble(t[4]));
				else if (t.length >= 2)
					tankTeams[i] = new Team(t[0], Boolean.parseBoolean(t[1]));
				else
					tankTeams[i] = new Team(t[0]);

				if (disableFriendlyFire)
					tankTeams[i].friendlyFire = false;

				teamsMap.put(t[0], tankTeams[i]);

				teamsList.add(tankTeams[i]);
			}
		}
		else
		{
			if (disableFriendlyFire)
			{
				teamsMap.put("ally", Game.playerTeamNoFF);
                teamsMap.put("enemy", Game.enemyTeamNoFF);
            }
            else
            {
                teamsMap.put("ally", Game.playerTeam);
                teamsMap.put("enemy", Game.enemyTeam);
            }
        }

		currentCloudCount = (int) (Math.random() * this.sizeX / 10.2 + Math.random() * this.sizeY / 10.2);

        for (int i = 0; i < Level.currentCloudCount; i++)
            Game.clouds.add(new Cloud());

        if (screen.length >= 9)
        {
            int length = Integer.parseInt(screen[8]) * 100;

            if (length > 0)
            {
                this.timed = true;
                this.timer = length;
            }
		}

		if (screen.length >= 11)
		{
			light = Integer.parseInt(screen[9]) / 100.0;
			shadow = Integer.parseInt(screen[10]) / 100.0;
		}

		for (Item i: this.shop)
			i.importProperties();

		for (Item i: this.startingItems)
			i.importProperties();

		if (sc instanceof ScreenLevelEditor s)
		{
            s.level = this;

			s.selectedTiles = new boolean[sizeX][sizeY];
			Game.movables.remove(Game.playerTank);

			if (!enableTeams)
			{
				this.teamsList.add(Game.playerTeam);
				this.teamsList.add(Game.enemyTeam);
			}

			s.teams = this.teamsList;
		}

		if (!mapLoad)
			this.reloadTiles();
		else
			Chunk.populateChunks(this);

		boolean[][] solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

		if (!((obstaclesPos.length == 1 && obstaclesPos[0].isEmpty()) || obstaclesPos.length == 0))
		{
            for (String pos : obstaclesPos)
            {
                String[] obs = pos.split("-");

                String[] xPos = obs[0].split("\\.\\.\\.");

                int startX;
                double endX;

                startX = Integer.parseInt(xPos[0]);
                endX = startX;

				if (xPos.length > 1)
					endX = Double.parseDouble(xPos[1]);

				String[] yPos = obs[1].split("\\.\\.\\.");

				int startY;
				double endY;

				startY = Integer.parseInt(yPos[0]);
				endY = startY;

				if (yPos.length > 1)
					endY = Double.parseDouble(yPos[1]);

				String name = "normal";

				if (obs.length >= 3)
					name = obs[2];

				StringBuilder meta = null;

				if (obs.length >= 4)
                {
                    meta = new StringBuilder(obs[3]);

                    for (int j = 4; j < obs.length; j++)
                        meta.append("-").append(obs[j]);
                }

				for (int x = startX; x <= endX; x++)
				{
					for (int y = startY; y <= endY; y++)
					{
						Obstacle o = Game.registryObstacle.getEntry(name).getObstacle(x + this.startX, y + this.startY);
						o.initSelectors(sc instanceof ScreenLevelEditor ? (ScreenLevelEditor) sc : null);

						if (meta != null)
							o.setMetadata(meta.toString());

						Chunk.Tile t = Chunk.getTile(x, y);

						if (o.bulletCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY && o.startHeight < 1)
						{
							t.solid = true;
							if (!o.shouldShootThrough)
								t.unbreakable = true;
						}

						o.postOverride();
						Game.obstacles.add(o);

						if (o.tankCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
                            solidGrid[(int) (x / Game.tile_size)][(int) (y / Game.tile_size)] = true;
					}
				}
			}
		}

		for (Obstacle o: Game.obstacles)
        {
            if (o.startHeight > 1)
                continue;

			Chunk c = Chunk.getChunk(o.posX, o.posY);
			if (c != null)
				c.addObstacle(o);
			o.afterAdd();
        }

		boolean[][] tankGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

		for (Movable m: Game.movables)
		{
			if (m instanceof Tank)
			{
				int x = (int) (m.posX / Game.tile_size);
				int y = (int) (m.posY / Game.tile_size);

				if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
					tankGrid[x][y] = true;
			}
		}

		ArrayList<Tank> tanksToRemove = new ArrayList<>();

		if (!preset[2].isEmpty())
		{
            for (String s : tanks)
            {
                String[] tank = s.split("-");
                double x = Game.tile_size * (0.5 + Double.parseDouble(tank[0]) + startX);
                double y = Game.tile_size * (0.5 + Double.parseDouble(tank[1]) + startY);
                String type = tank[2].toLowerCase();
                double angle = 0;

				StringBuilder metadata = new StringBuilder();
				for (int i = 3; i < tank.length; i++)
				{
					metadata.append(tank[i]);
					if (i < tank.length - 1)
						metadata.append("-");
				}

                if (tank.length >= 4)
                    angle = (Math.PI / 2 * Double.parseDouble(tank[3]));

                Team team = Game.enemyTeam;

				if (this.disableFriendlyFire)
					team = Game.enemyTeamNoFF;

				if (enableTeams)
				{
					if (tank.length >= 5)
						team = teamsMap.get(tank[4]);
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

					this.playerSpawnsX.add(x);
					this.playerSpawnsY.add(y);
					this.playerSpawnsAngle.add(angle);
					this.playerSpawnsTeam.add(team);

					int x1 = (int) Double.parseDouble(tank[0]);
					int y1 = (int) Double.parseDouble(tank[1]);

					if (x1 >= 0 && y1 >= 0 && x1 < tankGrid.length && y1 < tankGrid[0].length)
						tankGrid[x1][y1] = true;

					continue;
				}
                if (customTanksMap.get(type) != null)
					t = customTanksMap.get(type).instantiate(type, x, y, angle);
                else
                    t = Game.registryTank.getEntry(type).getTank(x, y, angle);

                t.initSelectors(sc instanceof ScreenLevelEditor ? (ScreenLevelEditor) sc : null);

                t.crusadeID = currentCrusadeID;
                currentCrusadeID++;

                if (Crusade.crusadeMode && !Crusade.currentCrusade.respawnTanks && Crusade.currentCrusade.retry && !Crusade.currentCrusade.livingTankIDs.contains(t.crusadeID))
                    tanksToRemove.add(t);
                else if (!metadata.isEmpty())
                    t.setMetadata(metadata.toString());

                // Don't do this in your code! We only want to dynamically generate tank IDs on level load!
				t.networkID = Tank.nextFreeNetworkID();
				Tank.idMap.put(t.networkID, t);

				if (remote)
				{
					TankRemote t1 = new TankRemote(t);
					Game.movables.add(t1);
				}
				else
				{
					Game.movables.add(t);
				}
			}
		}

		this.availablePlayerSpawns.clear();

		int playerCount = 1;
		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null && sc == null)
			playerCount += ScreenPartyHost.server.connections.size();

		if (!this.includedPlayers.isEmpty())
			playerCount = this.includedPlayers.size();
		else
			this.includedPlayers.addAll(Game.players);

		int extraSpawns = 0;
		if (playerCount > playerSpawnsX.size() && !playerSpawnsX.isEmpty())
		{
			extraSpawns = playerCount / playerSpawnsX.size() - 1;

			if (playerCount % playerSpawnsX.size() != 0)
				extraSpawns++;
		}

		int spawns = playerSpawnsX.size();

		for (int i = 0; i < spawns; i++)
		{
			int spawnsLeft = extraSpawns;
			ArrayList<Integer> extraSpawnsX = new ArrayList<>();
			ArrayList<Integer> extraSpawnsY = new ArrayList<>();

			boolean[][] explored = new boolean[Game.currentSizeX][Game.currentSizeY];
			boolean[][] blacklist = new boolean[Game.currentSizeX][Game.currentSizeY];

			ArrayList<Tile> queue = new ArrayList<>();
			queue.add(new Tile((int) (playerSpawnsX.get(i) / Game.tile_size), (int) (playerSpawnsY.get(i) / Game.tile_size)));

			while (!queue.isEmpty() && spawnsLeft > 0)
			{
				boolean stop = false;

				Tile t = queue.remove(0);

				for (int j: t.sidesOrder)
				{
					Tile t1;

					if (j == 0)
						t1 = new Tile(t.posX - 1, t.posY);
					else if (j == 1)
						t1 = new Tile(t.posX + 1, t.posY);
					else if (j == 2)
						t1 = new Tile(t.posX, t.posY - 1);
					else
						t1 = new Tile(t.posX, t.posY + 1);

					if (t1.posX >= 0 && t1.posX < Game.currentSizeX && t1.posY >= 0 && t1.posY < Game.currentSizeY &&
							!solidGrid[t1.posX][t1.posY] && !tankGrid[t1.posX][t1.posY] && !explored[t1.posX][t1.posY])
					{
						explored[t1.posX][t1.posY] = true;

						t1.age = t.age + 1;

						extraSpawnsX.add(t1.posX);
						extraSpawnsY.add(t1.posY);

						if (!blacklist[t1.posX][t1.posY] && (t1.age == 3 && Math.random() < 0.333 || t1.age == 4 && Math.random() < 0.5 || t1.age >= 5))
						{
							spawnsLeft--;
							t1.age = 0;

							playerSpawnsX.add((t1.posX + 0.5) * Game.tile_size);
							playerSpawnsY.add((t1.posY + 0.5) * Game.tile_size);
							playerSpawnsTeam.add(playerSpawnsTeam.get(i));
							playerSpawnsAngle.add(playerSpawnsAngle.get(i));

							tankGrid[t1.posX][t1.posY] = true;

							for (int x = Math.max(t1.posX - 1, 0); x <= Math.min(t1.posX + 1, Game.currentSizeX - 1); x++)
							{
								for (int y = Math.max(t1.posY - 1, 0); y <= Math.min(t1.posY + 1, Game.currentSizeY - 1); y++)
								{
									blacklist[x][y] = true;
								}
							}

							if (spawnsLeft <= 0)
							{
								stop = true;
								break;
							}
						}

						queue.add(t1);
					}
				}

				if (stop)
					break;
			}

			while (spawnsLeft > 0)
			{
				if (extraSpawnsX.isEmpty())
					break;

				int in = (int) (Math.random() * extraSpawnsX.size());
				int x = extraSpawnsX.remove(in);
				int y = extraSpawnsY.remove(in);

				if (!tankGrid[x][y])
				{
					playerSpawnsX.add((x + 0.5) * Game.tile_size);
					playerSpawnsY.add((y + 0.5) * Game.tile_size);
					playerSpawnsTeam.add(playerSpawnsTeam.get(i));
					playerSpawnsAngle.add(playerSpawnsAngle.get(i));
					spawnsLeft--;
				}
			}
		}

		playerCount = Math.min(playerCount, this.includedPlayers.size());

		if (sc == null && !preview)
		{
			for (int i = 0; i < playerCount; i++)
			{
				if (this.availablePlayerSpawns.isEmpty())
				{
					for (int j = 0; j < this.playerSpawnsTeam.size(); j++)
					{
						this.availablePlayerSpawns.add(j);
					}
				}

				int spawn = this.availablePlayerSpawns.remove((int) (Math.random() * this.availablePlayerSpawns.size()));

				double x = this.playerSpawnsX.get(spawn);
				double y = this.playerSpawnsY.get(spawn);
				double angle = this.playerSpawnsAngle.get(spawn);
				Team team = this.playerSpawnsTeam.get(spawn);

				if (ScreenPartyHost.isServer)
				{
					Game.addPlayerTank(this.includedPlayers.get(i), x, y, angle, team);
				}
				else if (!remote)
				{
					TankPlayer tank = new TankPlayer(x, y, angle);
					Game.playerTank = tank;
					tank.team = team;
					tank.registerNetworkID();
					Game.movables.add(tank);
				}
			}
		}
		else
		{
			updateModify();

			for (int i = 0; i < playerSpawnsTeam.size(); i++)
			{
				TankSpawnMarker t = new TankSpawnMarker("player", this.playerSpawnsX.get(i), this.playerSpawnsY.get(i), this.playerSpawnsAngle.get(i));
				t.team = this.playerSpawnsTeam.get(i);
				t.registerSelectors();
				t.initSelectors(sc instanceof ScreenLevelEditor ? (ScreenLevelEditor) sc : null);
				t.refreshSelectorValue();
				Game.movables.add(t);

				if (sc != null)
					sc.getSpawns().add(t);
			}

			if (sc instanceof ScreenLevelEditor)
				((ScreenLevelEditor) sc).movePlayer = (sc.getSpawns().size() <= 1);
		}

		if (Crusade.crusadeMode && Crusade.currentCrusade.retry)
		{
			for (Tank t: tanksToRemove)
			{
				INetworkEvent e = new EventTankRemove(t, false);
				Game.removeMovables.add(t);
				Game.eventsOut.add(e);
			}
		}

		if (!mapLoad)
			addLevelBorders();

		if (!remote && sc == null || (sc instanceof ScreenLevelEditor))
			Game.eventsOut.add(new EventEnterLevel());
	}

	public LinkedHashMap<String, TankAIControlled> getTankMap()
	{
		LinkedHashMap<String, TankAIControlled> customTanksMap = new LinkedHashMap<>();
		for (TankAIControlled t: this.customTanks)
			customTanksMap.put(t.name, t);
		return customTanksMap;
	}

	public void addLevelBorders()
	{
		Chunk.getChunksInRange(0, 0, sizeX, 0).forEach(chunk -> chunk.addBorderFace(0, this));
		Chunk.getChunksInRange(sizeX, 0, sizeX, sizeY).forEach(chunk -> chunk.addBorderFace(1, this));
		Chunk.getChunksInRange(0, sizeY, sizeX, sizeY).forEach(chunk -> chunk.addBorderFace(2, this));
		Chunk.getChunksInRange(0, 0, 0, sizeY).forEach(chunk -> chunk.addBorderFace(3, this));
	}

	public void updateModify()
	{
		Team prev = playerSpawnsTeam.get(0);

		for (Team t : playerSpawnsTeam)
		{
			if (t != prev)
			{
				updateModify = false;
				break;
			}
		}
	}

	public void reloadTiles()
	{
		Game.currentSizeX = (int) (sizeX * Game.bgResMultiplier);
		Game.currentSizeY = (int) (sizeY * Game.bgResMultiplier);

		currentColorR = colorR;
		currentColorG = colorG;
		currentColorB = colorB;

		currentColorVarR = colorVarR;
		currentColorVarG = colorVarG;
        currentColorVarB = colorVarB;

        currentLightIntensity = light;
        currentShadowIntensity = shadow;

		Drawing.drawing.setScreenBounds(Game.tile_size * sizeX, Game.tile_size * sizeY);
		Chunk.populateChunks(Game.currentLevel);
		if (!mapLoad)
			addLevelBorders();

		for (Obstacle o: Game.obstacles)
        {
            int x = (int) (o.posX / Game.tile_size);
            int y = (int) (o.posY / Game.tile_size);

            o.postOverride();

            if (o.startHeight > 1)
                continue;

            if (o.bulletCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY && o.startHeight < 1)
            {
				Chunk.Tile t = Chunk.getTile(x, y);
				t.solid = true;
                if (o.shouldShootThrough)
                    t.unbreakable = true;
            }
        }

		ScreenLevelEditor s = null;
		
		if (Game.screen instanceof ScreenLevelEditor)
			s = (ScreenLevelEditor) Game.screen;
		else if (Game.screen instanceof ScreenLevelEditorOverlay)
            s = ((ScreenLevelEditorOverlay) Game.screen).editor;

		if (s != null)
			s.selectedTiles = new boolean[Game.currentSizeX][Game.currentSizeY];
	}

	public static class Tile
	{
		public int posX;
		public int posY;
		public int age = 0;
		public int[] sidesOrder = new int[4];

		public Tile(int x, int y)
		{
			this.posX = x;
			this.posY = y;

			ArrayList<Integer> sides = new ArrayList<>();
			sides.add(0);
			sides.add(1);
			sides.add(2);
			sides.add(3);

			int i = 0;

			while (!sides.isEmpty())
			{
				int s = sides.remove((int) (Math.random() * sides.size()));
				sidesOrder[i] = s;
				i++;
			}
		}
	}

	public static boolean isDark()
	{
		return isDark(false);
	}

	public static boolean isDark(boolean forText)
	{
		return Level.currentColorR * 0.2126 + Level.currentColorG * 0.7152 + Level.currentColorB * 0.0722 <= (forText ? 127 : 92) || currentLightIntensity <= 0.5;
	}
}
