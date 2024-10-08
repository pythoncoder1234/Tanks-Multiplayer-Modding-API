package tanks.gui.screen;

import basewindow.InputCodes;
import basewindow.InputPoint;
import basewindow.transformation.RotationAboutPoint;
import basewindow.transformation.ScaleAboutPoint;
import basewindow.transformation.Translation;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletArc;
import tanks.generator.LevelGeneratorVersus;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.SpeedrunTimer;
import tanks.gui.TextWithStyling;
import tanks.gui.menus.FixedMenu;
import tanks.gui.menus.FixedText;
import tanks.gui.menus.Minimap;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemBullet;
import tanks.hotbar.item.ItemMine;
import tanks.hotbar.item.ItemRemote;
import tanks.network.Client;
import tanks.network.ConnectedPlayer;
import tanks.network.event.*;
import tanks.obstacle.Face;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenGame extends Screen implements IHiddenChatboxScreen, IPartyGameScreen
{
    public static float sensitivity = 0.5f;

	public boolean playing = false;
	public boolean paused = false;
	public EndCondition endCondition = EndCondition.normal;
	public EndText endText = EndText.normal;
	public boolean savedRemainingTanks = false;

	public boolean shopScreen = false;
	public boolean npcShopScreen = false;
    public static double lastTimePassed = 0;
    public static boolean fcZoomPressed = false;

	public static boolean finishedQuick = false;
	public static boolean finished = false;
	public static double finishTimer = 100;
	public static double finishTimerMax = 100;
	public double finishQuickTimer = 0;

	public boolean cancelCountdown = false;
	public String name = null;

	public static boolean newItemsNotification = false;
	public static String lastShop = "";
	public ArrayList<Item> shop = new ArrayList<>();
	public boolean screenshotMode = false;

	public boolean ready = false;
	public double readyNameSpacing = 10;
	public double lastNewReadyName = readyNameSpacing;
	public int readyNamesCount = 0;
	public int prevReadyNames = 0;
	public ArrayList<ConnectedPlayer> readyPlayers = new ArrayList<>();

	public static boolean versus = false;
	public TextWithStyling title = new TextWithStyling("", 0, 0, 0, 100, 100);
	public TextWithStyling subtitle = new TextWithStyling("", 0, 0, 0, 100, 50);
	public FixedText titleText;
	public FixedText subtitleText;
	public boolean displayedTitle = false;
	public boolean displayedSubtitle = false;

	public long introMusicEnd;
	public long introBattleMusicEnd;

	public RotationAboutPoint slantRotation;
	public Translation slantTranslation;

	public Tank spectatingTank = null;

	public double readyPanelCounter = 0;
	public double playCounter = 0;

    public double slant = 0;
    public double randomTickCounter;
    public double timeRemaining;
    public double timePassed = 0;

	public double prevCursorX;
    public double prevCursorY;

	public double shrubberyScale = 0.25;
    public ScreenInfo overlay = null;
    public Minimap minimap = new Minimap();
    public HashSet<String> prevTankMusics = new HashSet<>();
    public HashSet<String> tankMusics = new HashSet<>();

	public boolean zoomPressed = false;
    public boolean zoomScrolled = false;

	public double fcZoomLastTap = 0;
    public double fcZoom = 0;
    public double fcTargetZoom = 0;
    public double fcPitch = 0;
    public boolean selectedArcBullet = false;
    public double fcArcAim = 0;
    public String introMusic = null;
    public String mainMusic = null;
    public boolean endMusic = true;
    public boolean playedIntro = false;
    @SuppressWarnings("unchecked")
    public ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[]) (new ArrayList[10]);
	@SuppressWarnings("unchecked")
    public ArrayList<IDrawable>[] drawBeforeTerrain = (ArrayList<IDrawable>[]) (new ArrayList[10]);

	public static boolean controlPlayer = false;
    public boolean freecam = false;
    public double x = 0;
    public double y = 0;
    public double z = 0;
    public double yaw = 0;
    public double pitch = 0;
    public double pitchAdd = 0;
    public double roll = 0;
    protected boolean musicStarted = false;

	private EndCondition prevEndCond;
	private boolean firstFrame = true;
	private boolean endFirstFrame = true;

    Button play = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Play", () ->
    {
        playing = true;
        Game.playerTank.setBufferCooldown(20);

        if (titleText != null)
        {
            titleText.remove();
            titleText = null;
		}

		if (subtitleText != null)
		{
			subtitleText.remove();
			subtitleText = null;
		}
	});

	Button readyButton = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Ready", () ->
	{
		if (ScreenPartyLobby.isClient)
			Game.eventsOut.add(new EventPlayerReady());
		else
		{
			ScreenPartyHost.readyPlayers.add(Game.player);
			Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));

			//synchronized(ScreenPartyHost.server.connections)
			{
				if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.includedPlayers.size())
				{
					Game.eventsOut.add(new EventBeginLevelCountdown());
					cancelCountdown = false;
				}
			}
		}
		ready = true;
	}
	);

	Button restart = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace / 2, this.objWidth, this.objHeight, "Restart this level", () ->
	{
		playing = false;
		paused = false;

		if (ScreenPartyHost.isServer)
		{
			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}

		Game.silentCleanUp();

		if (Game.currentGame == null)
		{
			Game.reset();

			Level level = new Level(Game.currentLevelString);
			level.loadLevel();
		}
		else
		{
			try
			{
				Game.currentGame = Game.currentGame.getClass().getConstructor().newInstance();
				Game.currentGame.startBase();
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}
		}

		ScreenGame s = new ScreenGame();
		s.name = name;
		Game.screen = s;
	}
	);

	Button startNow = new Button( 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Start now", () ->
	{
		if (ScreenPartyHost.isServer)
		{
			for (Player p: Game.players)
			{
				if (!ScreenPartyHost.readyPlayers.contains(p) && ScreenPartyHost.includedPlayers.contains(p.clientID))
					ScreenPartyHost.readyPlayers.add(p);
			}

			Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));
			Game.eventsOut.add(new EventBeginLevelCountdown());
			cancelCountdown = false;
		}
		ready = true;
	}
	);


	Button enterShop = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, 350, 40, "Shop", new Runnable()
	{
		@Override
		public void run()
		{
			if (shopList != null)
			{
				newItemsNotification = false;
				cancelCountdown = true;
				shopScreen = true;
			}
		}
	}, "New items available in shop!"
	);

	Button pause = new Button(0, -1000, 70, 70, "", () ->
	{
		paused = true;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button zoom = new Button(0, -1000, 70, 70, "", () ->
	{
		Panel.autoZoom = false;
		Panel.zoomTarget = -1;
		Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
	});

	Button zoomAuto = new Button(0, -1000, 70, 70, "", () ->
	{
		Panel.autoZoom = !Panel.autoZoom;
		if (!Panel.autoZoom)
			Panel.zoomTarget = -1;
	});

	Button resume = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Continue playing", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button resumeLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "Continue playing", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button closeMenu = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Close menu", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button closeMenuLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "Close menu", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button closeMenuClient = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Close menu", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button newLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Generate a new level", () ->
	{
		playing = false;
		paused = false;

		if (ScreenPartyHost.isServer)
		{
			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}

		if (versus)
		{
			Game.cleanUp();
			new Level(LevelGeneratorVersus.generateLevelString()).loadLevel();
		}
		else
		{
			Game.cleanUp();
			Game.loadRandomLevel();
		}

		Game.startTime = Game.currentLevel.startTime;
		Game.screen = new ScreenGame();
	}
	);
	Button quitCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, ScreenPartyHost.isServer ? "Quit to party" : "Quit to menu", () ->
	{
		Crusade.currentCrusade.quit();
		Game.cleanUp();
		Game.screen = new ScreenCrusades();
	}
			, "Note! You will lose a life for quitting---in the middle of a level.------Your crusade progress will be saved.");

	Button restartLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart this level", restart.function);

	Button restartTutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart this level", () ->
	{
		Game.silentCleanUp();
		new Tutorial().loadTutorial(ScreenInterlevel.tutorialInitial, Game.game.window.touchscreen);
	}
	);

	Button edit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Edit the level", () ->
	{
		Game.cleanUp();
		ScreenLevelEditor s = new ScreenLevelEditor(name, Game.currentLevel);
		Game.screen = s;
		Game.loadLevel(Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + name), s);
	}
	);

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to my levels", () ->
	{
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;

		if (ScreenInterlevel.tutorial)
			Game.screen = new ScreenPlaySingleplayer();
		else if (ScreenInterlevel.fromMinigames)
			Game.screen = new ScreenMinigames();
		else if (ScreenInterlevel.fromSavedLevels || name != null)
			Game.screen = (ScreenPartyHost.isServer ? new ScreenPlaySavedLevels() : new ScreenSavedLevels());
		else
			Game.screen = new ScreenPlaySingleplayer();

		Game.reset();
		ScreenInterlevel.fromSavedLevels = false;

		if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
			Game.eventsOut.add(new EventReturnToLobby());
		}
	}
	);

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Quit to menu", back.function);

	Button quitHigherPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, ScreenPartyHost.isServer ? "Quit to party" : "Quit to title", () ->
	{
		Game.exitToTitle();
		ScreenInterlevel.tutorial = false;
	}
	);

	Button quitPartyGame = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Back to party", () ->
	{
		Game.reset();
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Game.screen = ScreenPartyHost.activeScreen;
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();
		Game.eventsOut.add(new EventReturnToLobby());
		versus = false;
	}
	);

	Button exitParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace / 2, this.objWidth, this.objHeight, "Leave party", () ->
	{
		Game.reset();
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Drawing.drawing.playSound("leave.ogg");
		ScreenPartyLobby.isClient = false;
		Game.screen = new ScreenJoinParty();

		Client.handler.close();

		ScreenPartyLobby.connections.clear();
	}
	);
	Button quitCrusadeFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, ScreenPartyHost.isServer ? "Quit to party" : "Quit to menu", () ->
	{
		Crusade.currentCrusade.quit();
		Game.cleanUp();
		Game.screen = new ScreenCrusades();
	}
			, "Note! You will lose a life for quitting---in the middle of a level.------Since you do not have any other lives left,---your progress will be lost!");
	Button restartCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level", () ->
	{
		paused = false;

		if (!finishedQuick && playing)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy)
					((TankPlayer) Game.movables.get(i)).player.remainingLives--;
				else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy)
					((TankPlayerRemote) Game.movables.get(i)).player.remainingLives--;
			}
		}

		playing = false;

		if (ScreenPartyHost.isServer)
		{
			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}

		Crusade.currentCrusade.recordPerformance(ScreenGame.lastTimePassed, false);
		Crusade.currentCrusade.retry = true;
		this.saveRemainingTanks();

		Crusade.currentCrusade.saveHotbars();
		Crusade.currentCrusade.crusadePlayers.get(Game.player).saveCrusade();
		Game.silentCleanUp();

		Crusade.currentCrusade.loadLevel();
		ScreenGame s = new ScreenGame(Crusade.currentCrusade.getShop());
		s.name = name;
		Game.screen = s;
	}
			, "Note! You will lose a life for restarting!");
	Button options = new Button(0, 0, 60, 60, "", () -> Game.screen = new ScreenOptions(), "Options (o)");

	Button restartCrusadeFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level",
			"You can't restart the level because---you have only one life left!");

	Button quitCrusadeParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to party", () ->
	{
		Crusade.currentCrusade.retry = true;
		Crusade.currentCrusade.quit();
		Panel.panel.zoomTimer = 0;
		Game.cleanUp();

		Game.screen = ScreenPartyHost.activeScreen;
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();
		Game.eventsOut.add(new EventReturnToLobby());
	}
			, "Note! All players will lose a life for---quitting in the middle of a level.");


	Button restartCrusadeParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level", () ->
	{
		if (!finishedQuick)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy)
					((TankPlayer) Game.movables.get(i)).player.remainingLives--;
				else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy)
					((TankPlayerRemote) Game.movables.get(i)).player.remainingLives--;
			}
		}

		playing = false;
		paused = false;

		ready = false;
		readyButton.enabled = true;
		cancelCountdown = true;

		Crusade.currentCrusade.recordPerformance(ScreenGame.lastTimePassed, false);
		Crusade.currentCrusade.retry = true;
		this.saveRemainingTanks();

		Panel.panel.zoomTimer = 0;
		Game.silentCleanUp();
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();

		Crusade.currentCrusade.loadLevel();
		Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
	}
			, "Note! All players will lose a life for---restarting in the middle of a level.");

	Button restartCrusadePartyFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level",
			"You can't restart the level because---nobody has more than one life left!");

	Button quitCrusadePartyFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to party", () ->
	{
		Crusade.currentCrusade.retry = true;
		Crusade.crusadeMode = false;
		Crusade.currentCrusade = null;

		Panel.panel.zoomTimer = 0;
		Game.cleanUp();
		Game.screen = ScreenPartyHost.activeScreen;
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();
		Game.eventsOut.add(new EventReturnToLobby());
	}
			, "Note! All players will lose a life for---quitting in the middle of a level.------Since nobody has any other lives left,---the crusade will end!");
	private boolean finishedFirstFrame = true;

	public static double shopOffset = -25;

	Button exitShop = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300 + shopOffset, 350, 40, "Exit shop", () ->
	{
		shopScreen = false;
		npcShopScreen = false;
	});

	public ArrayList<Button> shopItemButtons = new ArrayList<>();
	public ButtonList shopList;
	public ButtonList npcShopList = new ButtonList(new ArrayList<>(), 0, 0, (int) shopOffset, -30);

	public ScreenGame()
	{
		this.selfBatch = false;
		this.enableMargins = !Game.followingCam;

		if (Crusade.crusadeMode && !ScreenPartyLobby.isClient)
		{
			if ((Crusade.currentCrusade.currentLevel + 1) % Crusade.currentCrusade.bonusLifeFrequency == 0 && Crusade.currentCrusade.currentLevel + 1 < Crusade.currentCrusade.levelSize)
                endText = new EndText(EndText.crusade.winTitle, EndText.crusade.loseTitle, "", "",
						String.format("You gained a life for clearing Battle %d!", Crusade.currentCrusade.currentLevel + 1), "");
			else
				endText = EndText.crusade;
		}

		for (int i = 0; i < drawables.length; i++)
			drawables[i] = new ArrayList<>();
		for (int i = 0; i < drawBeforeTerrain.length; i++)
			drawBeforeTerrain[i] = new ArrayList<>();

		options.image = "icons/gear.png";
		options.imageSizeX = 50;
		options.imageSizeY = 50;
		options.fullInfo = true;

		if (Level.isDark())
		{
			this.title = new TextWithStyling("", 255, 255, 255, 127, 100);
			this.subtitle = new TextWithStyling("", 255, 255, 255, 127, 50);
		}

		introMusicEnd = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/ready_music_intro_length.txt").get(0));
		introBattleMusicEnd = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/battle_intro_length.txt").get(0));

		if (Game.framework == Game.Framework.libgdx)
			introBattleMusicEnd -= 40;

		this.drawDarkness = false;

		if (Game.currentGame != null)
		{
			ScreenInterlevel.fromMinigames = true;
			ScreenInterlevel.fromSavedLevels = false;
			back.setText("Back to minigames");
			quit.setText("Quit to minigames");
		}
		else
			ScreenInterlevel.fromMinigames = false;

		Game.startTime = Game.currentLevel.startTime;
		ScreenGame.lastTimePassed = 0;

		if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
		{
			this.music = "waiting_music.ogg";
			cancelCountdown = true;
		}

		ScreenGame.finishTimer = ScreenGame.finishTimerMax;

		slantRotation = new RotationAboutPoint(Game.game.window, 0, 0, 0, 0.5, 0.5, -1);
		slantTranslation = new Translation(Game.game.window, 0, 0, 0);

		if (!Crusade.crusadeMode)
		{
			boolean shop = false;
			boolean startingItems = false;

			if (!Game.currentLevel.shop.isEmpty())
			{
				shop = true;
				this.initShop(Game.currentLevel.shop);
			}

			if (!Game.currentLevel.startingItems.isEmpty())
				startingItems = true;

			for (Player p: Game.players)
			{
				p.hotbar.enabledItemBar = startingItems || shop;
				if (ItemBar.overrideState)
					p.hotbar.enabledItemBar = ItemBar.enabled;

				p.hotbar.enabledCoins = false;
				p.hotbar.itemBar = new ItemBar(p);

				if (startingItems)
				{
					for (Item i : Game.currentLevel.startingItems)
						p.hotbar.itemBar.addItem(i);
				}

				if (shop)
				{
					p.hotbar.enabledCoins = true;
					p.hotbar.coins = Game.currentLevel.startingCoins;
					Game.eventsOut.add(new EventUpdateCoins(p));
				}

				if (p != Game.player)
					Game.eventsOut.add(new EventSetupHotbar(p));
			}
		}

		if (Drawing.drawing.interfaceScaleZoom > 1)
		{
			startNow.sizeX *= 0.7;
			startNow.posX -= 20;
		}

		if (Game.currentLevel != null && Game.currentLevel.timed)
			this.timeRemaining = Game.currentLevel.timer;
	}

	public ScreenGame(String s)
	{
		this();
		this.name = s;
	}

	public ScreenGame(ArrayList<Item> shop)
	{
		this();
		this.initShop(shop);
	}

	public void initShop(ArrayList<Item> shop)
	{
		this.shop = shop;

		for (int i = 0; i < this.shop.size(); i++)
		{
			final int j = i;
			Item item = this.shop.get(j);
			if (item instanceof ItemRemote)
				continue;

			Button b = new Button(0, 0, 350, 40, item.name, () ->
			{
				int pr = shop.get(j).price;
				if (Game.player.hotbar.coins >= pr)
				{
					if (Game.player.hotbar.itemBar.addItem(shop.get(j)))
						Game.player.hotbar.coins -= pr;
				}
			}
			);

			int p = item.price;

			if (p == 0)
				b.setSubtext("Free!");
			else if (p == 1)
				b.setSubtext("1 coin");
			else
				b.setSubtext("%d coins", p);

			this.shopItemButtons.add(b);

			Game.eventsOut.add(new EventAddShopItem(i, item.name, b.rawSubtext, p, item.icon));
		}

		this.initializeShopList();

		Game.eventsOut.add(new EventSortShopButtons());
	}

	public void initializeShopList()
	{
		StringBuilder s = new StringBuilder();
		for (Button b: this.shopItemButtons)
			s.append(b.text);

		if (!lastShop.contentEquals(s))
			newItemsNotification = true;

		lastShop = s.toString();

		this.shopList = new ButtonList(this.shopItemButtons, 0, 0, (int) shopOffset, -30);
	}

	@Override
	public void setupLights()
	{
		for (Obstacle o: Game.obstacles)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.startHeight + 25) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}

		for (Movable o: Game.movables)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.posZ + 25) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}

		for (Effect o: Game.effects)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.posZ) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}
	}

	@Override
	public void update()
    {
        if (ScreenPartyHost.isServer && this.shop.isEmpty() && Game.autoReady && !this.ready)
            this.readyButton.function.run();

		if (Game.game.window.pressedKeys.contains(InputCodes.KEY_F7))
		{
			Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_F7);
			controlPlayer = !controlPlayer;
		}

		options.setPosition(50, Drawing.drawing.interfaceSizeY - 50);

        if (Game.game.input.zoom.isValid())
        {
            zoomScrolled = false;
            zoomPressed = true;
            Game.game.input.zoom.invalidate();
        }

        if (Game.game.input.freecam.isValid() && Game.game.window.shift)
        {
            Game.game.input.freecam.invalidate();
            freecam = !freecam;

            if (freecam)
            {
				controlPlayer = false;
                x = -0.08;
                y = 0.22;
                z = -0.07;
                yaw = -0.4;
                pitchAdd = 1;
                roll = -0.27;
            }
        }

        showDefaultMouse = finishedQuick || paused || !playing || shopScreen || npcShopScreen ||
				(!Game.followingCam && !Game.angledView) || focusedTank() == null || focusedTank().destroy;
        Game.game.window.moveMouseToOtherSide = !showDefaultMouse;

        if (playing && (!paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient))
        {
			if (randomTickCounter <= 0)
			{
				randomTickCounter = 200;

				if (Math.random() < 0.2)
					Game.clouds.add(new Cloud());
			}

			randomTickCounter -= Panel.frameFrequency;
        }


//        windChangeTimer -= Panel.frameFrequency;

		Tank tank = focusedTank();
        if (playing && tank != null && !tank.destroy)
        {
            if (Game.game.input.zoomIn.isPressed())
            {
                if (Panel.autoZoom)
                    Panel.zoomTarget = Panel.panel.zoomTimer;

                Panel.autoZoom = false;
                zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollUp = false;
				Panel.zoomTarget = Panel.panel.zoomTimer = Math.min(1, Panel.zoomTarget + 0.02 * Panel.frameFrequency * Drawing.drawing.unzoomedScale);
			}

			if (Game.game.input.zoomOut.isPressed())
			{
				if (Panel.autoZoom)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Panel.autoZoom = false;
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollDown = false;
				Panel.zoomTarget = Panel.panel.zoomTimer = Math.max(0, Panel.zoomTarget - 0.02 * Panel.frameFrequency * Drawing.drawing.unzoomedScale);
			}

			if (Panel.autoZoom)
			{
				Tank.AutoZoom z = tank.getAutoZoom();
				Panel.zoomTarget = z.zoom();
				Panel.panTargetX = z.panX();
				Panel.panTargetY = z.panY();
			}
		}

		if (tank == null || tank.destroy || finishedQuick)
            fcPitch = Math.max(0, fcPitch - 0.02 * Panel.frameFrequency);

		if (Game.game.input.perspective.isValid())
		{
			if (Game.game.window.shift)
			{
				ScreenOptionsGraphics.viewNum--;

				if (ScreenOptionsGraphics.viewNum < 0)
					ScreenOptionsGraphics.viewNum = 3;
			}
			else if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_CONTROL))
				ScreenOptionsGraphics.viewNum = 0;
			else
				ScreenOptionsGraphics.viewNum = (ScreenOptionsGraphics.viewNum + 1) % 4;

			switch (ScreenOptionsGraphics.viewNum)
			{
				case 0:
					Game.angledView = false;
					Game.followingCam = false;
					Game.firstPerson = false;
					break;
				case 1:
					Game.angledView = true;
					Game.followingCam = false;
					Game.firstPerson = false;
					break;
				case 2:
					Game.angledView = false;
					Game.followingCam = true;
					Game.firstPerson = false;
					break;
				case 3:
					Game.angledView = false;
					Game.followingCam = true;
					Game.firstPerson = true;
					break;
			}

			if (Game.followingCam)
			{
				Drawing.drawing.movingCamera = true;
				Panel.autoZoom = false;
				Panel.zoomTarget = -1;
			}

			this.enableMargins = !Game.followingCam;
			Game.game.input.perspective.invalidate();
		}

		if (Game.game.input.zoom.isPressed() && playing)
		{
			if (Panel.autoZoom)
				Panel.zoomTarget = Panel.panel.zoomTimer;

			Panel.autoZoom = false;

			if (Game.game.window.validScrollUp)
			{
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollUp = false;
				Panel.zoomTarget = Math.min(1, Panel.zoomTarget + 0.1 * Drawing.drawing.unzoomedScale);
			}

			if (Game.game.window.validScrollDown)
			{
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollDown = false;
				Panel.zoomTarget = Math.max(0, Panel.zoomTarget - 0.1 * Drawing.drawing.unzoomedScale);
			}
		}
		else if (zoomPressed)
		{
			if (!zoomScrolled)
			{
				Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
				Panel.zoomTarget = -1;
				spectatingTank = null;
			}

			zoomPressed = false;
		}

		if (Game.game.input.zoomAuto.isValid() && playing)
		{
			Game.game.input.zoomAuto.invalidate();

			if (!Game.game.window.shift)
			{
				if (Panel.autoZoom)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Panel.autoZoom = !Panel.autoZoom;
			}
			else
				Panel.forceCenter = !Panel.forceCenter;
		}

		Game.player.hotbar.update();
		minimap.update();

		String prevMusic = this.music;
		this.music = null;
		this.musicID = null;

		if (this.playCounter >= 0 && this.playing)
		{
			if (!this.playedIntro)
			{
				this.playedIntro = true;
				if (this.introMusic != null)
					Drawing.drawing.playSound(this.introMusic, 1f, true);
				else if (Game.currentLevel != null && Game.currentLevel.timed)
					Drawing.drawing.playSound("battle_timed_intro.ogg", 1f, true);
				else if (Level.isDark(false))
					Drawing.drawing.playSound("battle_night_intro.ogg", 1f, true);
				else
					Drawing.drawing.playSound("battle_intro.ogg", 1f, true);
			}

			this.playCounter += Panel.frameFrequency;
		}

		if (this.playCounter * 10 >= introBattleMusicEnd)
		{
			Panel.forceRefreshMusic = true;
			this.playCounter = -1;
		}

		if (this.playCounter < 0 && !finishedQuick)
		{
			if (Game.currentLevel != null && Game.currentLevel.timed)
			{
				if (this.paused || Game.playerTank == null || Game.playerTank.destroy)
					this.music = "battle_timed_paused.ogg";
				else
					this.music = "battle_timed.ogg";

				this.musicID = "battle_timed";
			}
			else
			{
				if (this.paused || Game.playerTank == null || Game.playerTank.destroy)
					this.music = "battle_paused.ogg";
				else if (Level.isDark(false))
					this.music = "battle_night.ogg";
				else
					this.music = "battle.ogg";

				this.musicID = "battle";

				if (Level.isDark())
					this.musicID = "battle_night";


				if (!this.musicStarted)
					this.musicStarted = true;
				else
				{
					this.prevTankMusics.clear();
					this.prevTankMusics.addAll(this.tankMusics);
					this.tankMusics.clear();

					if (!this.paused)
					{
						for (Movable m : Game.movables)
						{
							if (m instanceof Tank && !m.destroy)
								this.tankMusics.addAll(((Tank) m).musicTracks);
						}
					}

					for (String m : this.prevTankMusics)
					{
						if (!this.tankMusics.contains(m))
							Drawing.drawing.removeSyncedMusic(m, 500);
					}

					for (String m : this.tankMusics)
					{
						if (!this.prevTankMusics.contains(m))
							Drawing.drawing.addSyncedMusic(m, Game.musicVolume, true, 500);
					}
				}
			}
		}

		if (finishedQuick)
		{
			this.finishQuickTimer += Panel.frameFrequency;

			this.musicID = null;

			if (this.endMusic && name == null)
			{
				if (Panel.win && this.finishQuickTimer >= 75)
					this.music = "waiting_win.ogg";

				if (!Panel.win && this.finishQuickTimer >= 150)
					this.music = "waiting_lose.ogg";
			}
		}

		if (Game.game.input.pause.isValid())
		{
			if (shopScreen || npcShopScreen)
			{
				shopScreen = false;
				npcShopScreen = false;
			}
			else
				this.paused = !this.paused;

			if (this.paused)
			{
				Game.game.window.setCursorLocked(false);
				Game.game.window.setShowCursor(!Panel.showMouseTarget);
			}
			else
			{
                Game.game.window.setCursorLocked(Game.followingCam);
                Game.game.window.setShowCursor(!Panel.showMouseTarget);

				this.prevCursorX = Drawing.drawing.getInterfaceMouseX();
				this.prevCursorY = Drawing.drawing.getInterfaceMouseY();
			}

			Game.game.input.pause.invalidate();
		}

		if (Game.game.input.hidePause.isValid())
		{
			this.screenshotMode = !this.screenshotMode;
			Game.game.input.hidePause.invalidate();
		}

		if (!finished)
		{
			if (Obstacle.draw_size == 0)
				Drawing.drawing.playSound("level_start.ogg");

			Obstacle.draw_size = Math.min(Game.tile_size, Obstacle.draw_size + Panel.frameFrequency);
		}

		if (freecam && !(paused && !screenshotMode) && !Game.game.window.pressedKeys.contains(InputCodes.KEY_F) && Game.screen == this)
			updateFreecam();

		if (npcShopScreen)
		{
			Game.player.hotbar.hidden = false;
			Game.player.hotbar.hideTimer = 100;

			this.exitShop.update();
			this.npcShopList.update();
		}

		if (paused)
		{
			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
			{
				if (!playing && Game.startTime >= 0)
					this.updateSingleplayerWaitingMusic();

				this.updateMusic(prevMusic);
			}

			if (!this.screenshotMode)
			{
				if (this.overlay != null)
					this.overlay.update();
				else
				{
					if (Game.game.window.textValidPressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
						options.update();

					if (Game.game.window.textValidPressedKeys.contains(InputCodes.KEY_O))
						options.function.run();

					if (ScreenPartyLobby.isClient)
					{
						closeMenuClient.update();
						exitParty.update();
					}
					else if (ScreenPartyHost.isServer)
					{
						if (ScreenInterlevel.fromSavedLevels || ScreenInterlevel.fromMinigames)
						{
							closeMenuLowerPos.update();
							restartLowerPos.update();
							back.update();
						}
						else if (Crusade.crusadeMode)
						{
							closeMenuLowerPos.update();

							if (Crusade.currentCrusade.finalLife())
							{
								restartCrusadePartyFinalLife.update();

								if (finishedQuick && Panel.win)
									quitCrusadeParty.update();
								else
									quitCrusadePartyFinalLife.update();
							}
							else
							{
								restartCrusadeParty.update();
								quitCrusadeParty.update();
							}
						}
						else
						{
							closeMenu.update();
							newLevel.update();
							restart.update();
							quitPartyGame.update();
						}
					}
					else if (ScreenInterlevel.tutorialInitial)
					{
						resumeLowerPos.update();
						restartTutorial.update();
					}
					else if (ScreenInterlevel.tutorial)
					{
						resumeLowerPos.update();
						restartTutorial.update();
						quitHigherPos.update();
					}
					else if (ScreenInterlevel.fromMinigames)
					{
						resumeLowerPos.update();
						restartLowerPos.update();
						back.update();
					}
					else if (Crusade.crusadeMode)
					{
						if (Crusade.currentCrusade.finalLife())
						{
							restartCrusadeFinalLife.update();
							quitCrusadeFinalLife.update();
						}
						else
						{
							restartCrusade.update();
							quitCrusade.update();
						}

						resumeLowerPos.update();
					}
					else if (name != null)
					{
						resume.update();
						edit.update();
						restart.update();
						quit.update();
					}
					else
					{
						resume.update();
						newLevel.update();
						restart.update();
						quit.update();
					}
				}
			}

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				return;

			Game.game.window.validPressedKeys.clear();
			Game.game.window.pressedKeys.clear();

			Game.game.window.validPressedButtons.clear();
			Game.game.window.pressedButtons.clear();

			Game.game.window.validScrollUp = false;
			Game.game.window.validScrollDown = false;

			if (Game.game.window.touchscreen)
			{
				TankPlayer.controlStick.activeInput = -1;
				TankPlayer.controlStick.inputIntensity = 0;
				TankPlayer.controlStick.update();

				for (InputPoint p : Game.game.window.touchPoints.values())
				{
					p.valid = false;
					p.tag = "backgroundscreen";
				}
			}
		}
		else if (Game.game.window.touchscreen && !shopScreen)
		{
			boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >= Game.game.window.absoluteHeight - Drawing.drawing.statsHeight;
			double vStep = 0;
			double hStep = 0;

			if (vertical)
				vStep = 100;
			else
				hStep = 100;

			pause.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - 50 - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
			pause.posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;
			pause.update();

			zoom.posX = pause.posX - hStep;
			zoom.posY = pause.posY + vStep;

			zoomAuto.posX = zoom.posX - hStep;
			zoomAuto.posY = zoom.posY + vStep;

			if (Drawing.drawing.enableMovingCamera)
			{
				zoom.update();

				if (!Panel.autoZoom)
					zoomAuto.update();
			}

			if (playing)
			{
				TankPlayer.controlStick.mobile = TankPlayer.controlStickMobile;
				TankPlayer.controlStick.snap = TankPlayer.controlStickSnap;
				TankPlayer.controlStick.update();
			}
		}

		if (!playing && Game.startTime >= 0)
		{
			if (shopScreen)
			{
				Game.player.hotbar.hidden = false;
				Game.player.hotbar.hideTimer = 100;

				this.exitShop.update();

				this.shopList.update();

				if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
				{
					this.music = "waiting_music.ogg";
					this.musicID = null;
				}
				else
				{
					this.music = "ready_music_1.ogg";
					this.musicID = "ready";
				}
			}
			else
			{
				if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient || Game.autostart) && !cancelCountdown)
					Game.startTime -= Panel.frameFrequency;

				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				{
					play.update();

					if (Game.game.input.play.isValid())
					{
						play.function.run();
						Game.game.input.play.invalidate();
					}

					this.updateSingleplayerWaitingMusic();
				}
				else
				{
					if (this.cancelCountdown)
					{
						readyButton.enabled = !this.ready;

						if (this.ready)
						{
							if (this.readyPanelCounter * 10 >= introMusicEnd)
							{
								this.music = "ready_music_1.ogg";
								this.musicID = "ready";
							}
							else
							{
								if (this.readyPanelCounter == 0)
									Drawing.drawing.playSound("ready_music_intro.ogg", 1f, true);

								this.music = null;
								this.musicID = null;
							}

							this.readyPanelCounter += Panel.frameFrequency;
							readyButton.setText("Waiting... (%d/%d)");
						}
						else
						{
							readyButton.setText("Ready (%d/%d)");
							this.music = "waiting_music.ogg";
							this.musicID = null;
						}

						if (ScreenPartyHost.isServer)
						{
							if (!ScreenPartyHost.includedPlayers.contains(Game.clientID))
							{
								readyButton.setText("Spectating... (%d/%d)");
								readyButton.enabled = false;
							}

							readyButton.setTextArgs(ScreenPartyHost.readyPlayers.size(), ScreenPartyHost.includedPlayers.size());
						}
						else
						{
							if (!ScreenPartyLobby.includedPlayers.contains(Game.clientID))
							{
								readyButton.setText("Spectating... (%d/%d)");
								readyButton.enabled = false;
							}

							readyButton.setTextArgs(ScreenPartyLobby.readyPlayers.size(), ScreenPartyLobby.includedPlayers.size());
						}
					}
					else
					{
						if (this.readyPanelCounter * 10 >= introMusicEnd)
						{
							this.music = "ready_music_2.ogg";
							this.musicID = "ready";
						}
						else
						{
							if (this.readyPanelCounter == 0)
								Drawing.drawing.playSound("ready_music_intro.ogg", 1f, true);

							this.music = null;
							this.musicID = null;
						}

						this.readyPanelCounter += Panel.frameFrequency;
						readyButton.enabled = false;
						readyButton.setText("Starting in %d", ((int)(Game.startTime / 100) + 1));
					}

					readyButton.update();

					if (Game.game.input.play.isValid() && readyButton.enabled)
					{
						readyButton.function.run();
						Game.game.input.play.invalidate();
					}
				}

				if (!this.shopItemButtons.isEmpty() && readyButton.enabled)
					enterShop.update();

				if (ScreenPartyHost.isServer && this.cancelCountdown)
					startNow.update();

				TankPlayer.controlStick.mobile = TankPlayer.controlStickMobile;
				TankPlayer.controlStick.snap = TankPlayer.controlStickSnap;
				TankPlayer.controlStick.update();
			}
		}
		else
		{
			if (Game.currentGame != null)
				Game.currentGame.update();

			if (!playing)
			{
				if (titleText != null)
				{
					titleText.remove();
					titleText = null;
				}

                if (subtitleText != null)
                {
                    subtitleText.remove();
                    subtitleText = null;
                }
            }

            playing = true;

            ItemBar b = Game.player.hotbar.itemBar;
            this.selectedArcBullet = b.selected > -1 && b.slots[b.selected] instanceof ItemBullet && BulletArc.class.isAssignableFrom(((ItemBullet) b.slots[b.selected]).bulletClass);

			if ((!freecam || controlPlayer) && Game.followingCam && focusedTank() != null && !focusedTank().destroy)
				updateFollowingCam();

            Obstacle.draw_size = Math.min(Game.tile_size, Obstacle.draw_size);
            HashSet<Team> aliveTeams = new HashSet<>();
            HashSet<Team> fullyAliveTeams = new HashSet<>();

            for (Effect e : Game.effects)
                e.update();

            for (Cloud c : Game.clouds)
                c.update();

			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_F3) && Game.game.window.pressedKeys.contains(InputCodes.KEY_F4))
				Game.movables.add(new Crate(new TankPlayer(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.angle)));

			for (int i = 0; i < Game.movables.size(); i++)
				Game.movables.get(i).preUpdate();

			Tank.shouldUpdate = Game.vanillaMode || System.currentTimeMillis() - Tank.lastUpdateTime > 1000 / Tank.updatesPerSecond;
			Tank.updatesPerFrame = 0;
			if (Tank.shouldUpdate)
				Tank.lastUpdateTime = System.currentTimeMillis();

			Tank.lastTankCount = Tank.tankCount;
			Tank.tankCount = 0;

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);

				if (m.skipNextUpdate)
				{
					m.skipNextUpdate = false;
					continue;
				}

				m.update();

				if (m instanceof Crate)
					m = ((Crate) m).tank;

				if (!(m instanceof Tank tank1 && tank1.mandatoryKill && !tank1.destroy))
					continue;

                Team t;

                if (m.team == null)
                {
                    if (m instanceof TankPlayer || m instanceof TankPlayerController)
                        t = new Team(Game.clientID.toString());
                    else if (m instanceof TankPlayerRemote)
                        t = new Team(((TankPlayerRemote) m).player.clientID.toString());
                    else
                        t = new Team("*");
                }
                else
                    t = m.team;

                aliveTeams.add(t);
                fullyAliveTeams.add(t);
            }

			for (Obstacle o : Game.obstacles)
			{
				if (o.update)
					o.update();
			}

			for (Effect e : Game.tracks)
				e.update();

			Game.player.hotbar.update();

			if (!finishedQuick)
			{
				this.timePassed += Panel.frameFrequency;
				lastTimePassed = this.timePassed;

				if (Crusade.crusadeMode)
					Crusade.currentCrusade.timePassed += Panel.frameFrequency;
			}

			if (Game.currentLevel != null && Game.currentLevel.timed)
			{
				if (!finishedQuick)
				{
					int seconds = (int) (timeRemaining / 100 + 0.5);
					int secondHalves = (int) (timeRemaining / 50);

					this.timeRemaining -= Panel.frameFrequency;

					int newSeconds = (int) (timeRemaining / 100 + 0.5);
					int newSecondHalves = (int) (timeRemaining / 50);

					if (seconds <= 5)
					{
						if (newSecondHalves < secondHalves)
							Drawing.drawing.playSound("tick.ogg", 2f, 0.5f);
					}
					else if (newSeconds < seconds && seconds <= 10)
						Drawing.drawing.playSound("tick.ogg", 2f, 0.5f);

					if (seconds > newSeconds && (newSeconds == 10 || newSeconds == 30 || newSeconds == 60))
						Drawing.drawing.playSound("timer.ogg");
				}

				if (this.timeRemaining <= 0)
				{
					this.saveRemainingTanks();

					for (int i = 0; i < Game.movables.size(); i++)
					{
						Movable m = Game.movables.get(i);

						m.destroy = true;

						if (m instanceof Tank)
							((Tank) m).health = 0;
					}
				}
			}

			if (Game.currentGame != null && Game.currentGame.endCondition != this.prevEndCond)
			{
				this.endCondition = Game.currentGame.endCondition;
				this.endCondition.aliveTeams = aliveTeams;
				this.endCondition.fullyAliveTeams = fullyAliveTeams;
				this.prevEndCond = this.endCondition;
			}

			if ((Game.screen == this || Game.screen instanceof ScreenOptionsOverlay) && !firstFrame && endCondition.finishedQuick())
			{
				if (!ScreenGame.finishedQuick)
				{
					endCondition.aliveTeams = aliveTeams;
					endCondition.fullyAliveTeams = fullyAliveTeams;

					Panel.win = false;

					if (Crusade.crusadeMode)
					{
						if (!ScreenPartyLobby.isClient)
						{
							for (Player p : Game.players)
							{
								if (endCondition.playerWon(p) || (p.tank != null && p.tank.team != null && endCondition.teamWon(p.tank.team)))
								{
									Panel.win = true;
									break;
								}
							}
						}

						if (Panel.win)
							ScreenInterlevel.title = endText.winTitle;
						else
							ScreenInterlevel.title = endText.loseTitle;

						if (!ScreenPartyLobby.isClient)
							Crusade.currentCrusade.levelFinished(Panel.win);
					}
					else if (Game.playerTank != null)
					{
						if (endCondition.teamWon(Game.playerTank.team) || endCondition.playerWon(Game.player))
						{
							ScreenInterlevel.title = endText.winTitle;

							if (!ScreenPartyLobby.isClient)
								Panel.win = true;
						}
						else
						{
							ScreenInterlevel.title = endText.loseTitle;

							if (!ScreenPartyLobby.isClient)
								Panel.win = false;
						}

						if (Game.currentGame != null)
							Game.currentGame.onLevelEnd(Panel.win);
					}

					Panel.forceRefreshMusic = true;

					if (Panel.win)
					{
						if (Crusade.crusadeMode && !Crusade.currentCrusade.respawnTanks)
						{
							restartCrusade.enabled = false;
							restartCrusadeParty.enabled = false;
						}

						if (!ScreenPartyLobby.isClient)
							Drawing.drawing.playSound(endCondition.winSound, 1.0f, true);
					}
					else
					{
						if (!ScreenPartyLobby.isClient)
							Drawing.drawing.playSound(endCondition.loseSound, 1.0f, true);
					}

					String s = "**";

					if (!fullyAliveTeams.isEmpty())
						s = fullyAliveTeams.iterator().next().name;

					if (ScreenPartyHost.isServer)
						Game.eventsOut.add(new EventLevelEndQuick(s));

					if (!Game.vanillaMode)
						Game.eventsOut.add(new EventUpdateLevelTime());
				}

				ScreenGame.finishedQuick = true;
				TankPlayer.shootStickHidden = false;
			}
			else
			{
				if (firstFrame && (name != null || ScreenInterlevel.fromSavedLevels))
					quit.setText("Quit to my levels");

				firstFrame = false;
				endCondition.aliveTeams = aliveTeams;
				endCondition.fullyAliveTeams = fullyAliveTeams;
			}

			if (endCondition.finished() && endCondition.finishedQuick())
			{
				ScreenGame.finished = true;
				Game.bulletLocked = true;

				if (ScreenGame.finishTimer > 0)
				{
					ScreenGame.finishTimer -= Panel.frameFrequency;
					if (ScreenGame.finishTimer < 0)
						ScreenGame.finishTimer = 0;
				}
				else
				{
					for (Item i : Game.player.hotbar.itemBar.slots)
					{
						if (i instanceof ItemBullet)
							((ItemBullet) i).liveBullets = 0;
					}

					boolean noMovables = true;

					for (Movable m : Game.movables)
					{
						if (m instanceof Bullet || m instanceof Mine)
						{
							noMovables = false;
							m.destroy = true;
						}
					}

					if (Game.effects.isEmpty() && noMovables)
					{
						if (name == null)
							Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);

						if (endFirstFrame)
							Drawing.drawing.playSound("level_end.ogg");

						this.saveRemainingTanks();

						for (Movable m: Game.movables)
							m.destroy = true;

						if ((name == null && Obstacle.draw_size <= 0) || Game.movables.isEmpty())
						{
							if (Crusade.crusadeMode)
								Crusade.currentCrusade.saveHotbars();

							if (ScreenPartyHost.isServer && finishedFirstFrame)
							{
								finishedFirstFrame = false;
								Game.silentCleanUp();

								String s = "**";

								HashSet<String> teamsWon = new HashSet<>();
								HashSet<String> playersWon = new HashSet<>();

								for (Player p : Game.players)
								{
									if (p.tank != null && p.tank.team != null && endCondition.teamWon(p.tank.team))
										teamsWon.add(p.tank.team.name);

									else if (endCondition.playerWon(p))
										playersWon.add(p.clientID.toString());
								}

								if (!teamsWon.isEmpty())
									s = String.join(",", teamsWon);

								else if (!playersWon.isEmpty())
									s = String.join(",", playersWon);

								ScreenPartyHost.readyPlayers.clear();

								for (Item i : Game.player.hotbar.itemBar.slots)
								{
									if (i instanceof ItemBullet)
										((ItemBullet) i).liveBullets = 0;
									else if (i instanceof ItemMine)
										((ItemMine) i).liveMines = 0;
								}

								if (Crusade.crusadeMode && !ScreenPartyLobby.isClient)
								{
									if (Crusade.currentCrusade.win || Crusade.currentCrusade.lose)
										Game.eventsOut.add(new EventShowCrusadeStats());

									for (int i = 0; i < Game.players.size(); i++)
										Game.eventsOut.add(new EventUpdateRemainingLives(Game.players.get(i)));
								}
								else
									Game.exitToInterlevel();

								Game.eventsOut.add(new EventLevelEnd(s, endText));
							}
							else if (Game.currentLevel != null && !Game.currentLevel.remote)
							{
								if (name != null)
									Game.exitToEditor(name);
								else
									Game.exitToInterlevel();
							}
						}

						endFirstFrame = false;
					}
				}
			}
			else
				Game.bulletLocked = false;
		}

		if (spectatingTank != null && spectatingTank.destroy && spectatingTank.destroyTimer <= 0)
			spectatingTank = null;

		if (!Game.game.window.touchscreen)
		{
			double mx = Drawing.drawing.getInterfaceMouseX();
			double my = Drawing.drawing.getInterfaceMouseY();

			boolean handled = checkMouse(mx, my, Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1));

			if (handled)
				Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
		}
		else
		{
			for (int i: Game.game.window.touchPoints.keySet())
			{
				InputPoint p = Game.game.window.touchPoints.get(i);

				if (p.tag.isEmpty())
				{
					double mx = Drawing.drawing.toGameCoordsX(Drawing.drawing.getInterfacePointerX(p.x));
					double my = Drawing.drawing.getInterfacePointerY(p.y);

					boolean handled = checkMouse(mx, my, p.valid);

					if (handled)
						p.tag = "spectate";
				}
			}
		}

		if (playing && !paused && !finishedQuick)
			this.shrubberyScale = Math.min(this.shrubberyScale + Panel.frameFrequency / 200, 1);

		if (finishedQuick)
			this.shrubberyScale = Math.max(this.shrubberyScale - Panel.frameFrequency / 200, 0.25);

		this.updateMusic(prevMusic);

		for (Movable m : Game.removeMovables)
			m.getTouchingChunks().forEach(chunk -> chunk.removeMovable(m));

		Game.movables.removeAll(Game.removeMovables);
		Game.clouds.removeAll(Game.removeClouds);
		ModAPI.fixedMenus.removeAll(ModAPI.removeMenus);

		for (Obstacle o: Game.removeObstacles)
		{
			if (o instanceof IAvoidObject)
				IAvoidObject.avoidances.remove(o);

			o.removed = true;
			Drawing.drawing.terrainRenderer.remove(o);

			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
			{
				if (Game.enable3d)
					Game.redrawGroundTiles.add(new int[]{x, y});

				Chunk.Tile t = Chunk.getTile(x, y);
				if (t.obstacle == o)
				{
					t.solid = false;
					t.height = 0;
					t.groundHeight = 0;
					t.unbreakable = false;
				}

				Game.removeObstacle(o);
				Game.removeSurfaceObstacle(o);
			}

			for (Obstacle o1 : o.getNeighbors())
				o1.onNeighborUpdate();

			Game.obstacles.remove(o);
		}

		for (Effect e : Game.removeEffects)
		{
			if (e.state == Effect.State.removed)
			{
				e.state = Effect.State.recycle;
				Game.effects.remove(e);
				Game.recycleEffects.add(e);
			}
		}

		Game.effects.addAll(Game.addEffects);
		Game.addEffects.clear();

		for (Effect e : Game.removeTracks)
		{
			if (e.state == Effect.State.removed)
			{
				e.state = Effect.State.recycle;
				Game.tracks.remove(e);
				Game.recycleEffects.add(e);
			}
		}

		Game.removeMovables.clear();
		Game.removeObstacles.clear();
		Game.removeEffects.clear();
		Game.removeTracks.clear();
		Game.removeClouds.clear();
		ModAPI.removeMenus.clear();

		for (FixedMenu menu : ModAPI.fixedMenus)
			menu.update();
	}

	public void updateMusic(String prevMusic)
	{
		if (this.music == null && prevMusic != null)
			Panel.forceRefreshMusic = true;

		if (this.music != null && prevMusic == null)
			Panel.forceRefreshMusic = true;

		if (this.music != null && !this.music.equals(prevMusic))
			Panel.forceRefreshMusic = true;
	}

	public void updateSingleplayerWaitingMusic()
	{
		if (ScreenInterlevel.tutorialInitial)
			return;

		if (this.readyPanelCounter * 10 >= introMusicEnd)
		{
			this.music = "ready_music_2.ogg";
			this.musicID = "ready";

			if (this.paused)
				this.music = "ready_music_1.ogg";
		}
		else
		{
			if (this.readyPanelCounter == 0)
				Drawing.drawing.playSound("ready_music_intro.ogg", 1f, true);

			this.music = null;
			this.musicID = null;
		}

		this.readyPanelCounter += Panel.frameFrequency;
	}

	public boolean checkMouse(double mx, double my, boolean valid)
	{
		if (!valid)
			return false;

		double x = Drawing.drawing.toGameCoordsX(mx);
		double y = Drawing.drawing.toGameCoordsY(my);

		if (!finishedQuick && (Game.playerTank == null || Game.playerTank.destroy) && (spectatingTank == null || !Drawing.drawing.movingCamera) && Panel.panel.zoomTimer <= 0)
		{
			Chunk c = Chunk.getChunk(x, y);
			if (c == null)
				return false;

			for (Movable m : c.movables)
			{
				if (!(m instanceof Tank && !m.destroy))
					continue;

				if (x >= m.posX - m.size && x <= m.posX + m.size &&
						y >= m.posY - m.size && y <= m.posY + m.size)
				{
					this.spectatingTank = (Tank) m;
					Panel.panel.pastPlayerX.clear();
					Panel.panel.pastPlayerY.clear();
					Panel.panel.pastPlayerTime.clear();
					Drawing.drawing.movingCamera = true;
					return true;
				}
			}
		}

		return false;
	}

	public void setPerspective()
	{
		if (Game.followingCam && Game.framework == Game.Framework.lwjgl)
        {
            double frac = Panel.panel.zoomTimer;

			Game.game.window.clipMultiplier = 1;
			Game.game.window.clipDistMultiplier = 100;

            if (freecam)
                Game.game.window.transformations.add(new Translation(Game.game.window, x, y, z));

			Tank t = Game.playerTank != null ? Game.playerTank : focusedTank();

            if (!Game.firstPerson)
            {
                Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, 0, freecam ? 0 : (frac * ((t.angle + Math.PI * 3 / 2) % (Math.PI * 2) - Math.PI)), 0, -Drawing.drawing.statsHeight / Game.game.window.absoluteHeight / 2, 0));
                Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.1 * frac, 0));
                Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, -Math.PI * 0.35 * frac + fcPitch, 0, fcPitch * 3, fcPitch * 3, -1));
                Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0, 0.5 * frac));

                if (fcZoom > 0)
                    Game.game.window.transformations.add(new ScaleAboutPoint(Game.game.window, 1, 1, fcZoom + 1, 0, 0, 0));
            }
            else
            {
                Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, 0, frac * ((t.angle + Math.PI * 3 / 2) % (Math.PI * 2) - Math.PI), 0, -Drawing.drawing.statsHeight / Game.game.window.absoluteHeight / 2, 0));
                Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.1 * frac, 0));
                Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, -Math.PI * 0.5 * frac, 0, 0, 0, -1));
                Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.0575 * frac, 0.9 * frac));

                if (fcZoom > 0)
                    Game.game.window.transformations.add(new ScaleAboutPoint(Game.game.window, 1, 1, 1 - fcZoom, 0, 0, 0));
            }

            if (freecam)
                Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, yaw, pitch, roll, 0, 0, 0));

            Game.game.window.loadPerspective();
        }
        else if (Game.angledView && Game.framework == Game.Framework.lwjgl)
        {
			if (this.playing && (!this.paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && !ScreenGame.finished)
				slant = Math.min(1, slant + 0.01 * Panel.frameFrequency);
			else if (ScreenGame.finished)
				slant = Math.max(0, slant - 0.01 * Panel.frameFrequency);

            this.slantRotation.pitch = this.slant * -Math.PI / 16;
            this.slantTranslation.y = -this.slant * 0.05;

            Game.game.window.transformations.add(this.slantTranslation);
            Game.game.window.transformations.add(this.slantRotation);

            Game.game.window.loadPerspective();
        }
	}

	@Override
	public void draw()
	{
        this.setPerspective();

        Drawing.drawing.setColor(174, 92, 16);

        double mul = 1;
        if (Game.angledView)
            mul = 2;

        Drawing.drawing.fillShadedInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2,
				mul * Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, mul * Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);

		this.drawDefaultBackground();

		Drawing drawing = Drawing.drawing;

//		drawables[0].addAll(Game.tracks);

		for (Movable m : Game.movables)
		{
			ArrayList<IDrawable>[] arr = m.drawBeforeObstacles() ? drawBeforeTerrain : drawables;
			arr[m.drawLevel].add(m);

			if (m.showName)
				arr[m.nameTag.drawLevel].add(m.nameTag);
		}

		for (Obstacle o : Game.obstacles)
		{
			if (!Game.enable3d || !o.batchDraw)
				drawables[o.drawLevel].add(o);
		}

		for (Effect e: Game.effects)
            drawables[e.drawLayer].add(e);

		for (Cloud c: Game.clouds)
			drawables[c.drawLevel].add(c);

		if (Game.game.window.touchscreen)
		{
			drawables[9].add(TankPlayer.controlStick);

			if (TankPlayer.shootStickEnabled && !TankPlayer.shootStickHidden)
				drawables[9].add(TankPlayer.shootStick);
		}

		for (int i = 0; i < this.drawables.length; i++)
		{
			for (IDrawable d: this.drawables[i])
			{
				if (d != null)
					d.draw();
			}

			if (Game.glowEnabled)
			{
				for (IDrawable d: this.drawables[i])
				{
					if (d instanceof IDrawableWithGlow && ((IDrawableWithGlow) d).isGlowEnabled())
						((IDrawableWithGlow) d).drawGlow();
				}
			}

			if (i == 9 && (Game.playerTank instanceof ILocalPlayerTank && ((ILocalPlayerTank) Game.playerTank).showTouchCircle()))
			{
				Drawing.drawing.setColor(255, 127, 0, 63);
				Drawing.drawing.fillInterfaceOval(Drawing.drawing.toInterfaceCoordsX(Game.playerTank.posX),
						Drawing.drawing.toInterfaceCoordsY(Game.playerTank.posY),
						((ILocalPlayerTank) Game.playerTank).getTouchCircleSize(), ((ILocalPlayerTank) Game.playerTank).getTouchCircleSize());
			}

			if (i == 9 && (Game.playerTank instanceof ILocalPlayerTank && ((ILocalPlayerTank) Game.playerTank).getDrawRange() >= 0) && !Game.game.window.drawingShadow)
			{
				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 50);
				else
					Drawing.drawing.setColor(0, 0, 0, 50);

				Mine.drawRange2D(Game.playerTank.posX, Game.playerTank.posY,
						((ILocalPlayerTank) Game.playerTank).getDrawRange());

				((ILocalPlayerTank) Game.playerTank).setDrawRange(-1);
			}

			ScreenGame g = ScreenGame.getInstance();
			if (i == 9 && Game.playerTank != null && !Game.playerTank.destroy
					&& g != null && !g.playing && Game.movables.contains(Game.playerTank))
			{
				double s = Game.startTime;

				if (cancelCountdown)
					s = 400;

				double fade = Math.max(0, Math.sin(Math.min(s, 50) / 100 * Math.PI));

				double frac = (System.currentTimeMillis() % 2000) / 2000.0;
				double size = Math.max(800 * (0.5 - frac), 0) * fade;
				Drawing.drawing.setColor(Game.playerTank.colorR, Game.playerTank.colorG, Game.playerTank.colorB, 64 * Math.sin(Math.min(frac * Math.PI, Math.PI / 2)) * fade);

				if (Game.enable3d)
					Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size / 2, size, size, false, false);
				else
					Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, size, size);

				double frac2 = ((250 + System.currentTimeMillis()) % 2000) / 2000.0;
				double size2 = Math.max(800 * (0.5 - frac2), 0) * fade;

				Drawing.drawing.setColor(Game.playerTank.secondaryColorR, Game.playerTank.secondaryColorG, Game.playerTank.secondaryColorB, 64 * Math.sin(Math.min(frac2 * Math.PI, Math.PI / 2)) * fade);

				if (Game.enable3d)
                    Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size / 2, size2, size2, false, false);
                else
                    Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, size2, size2);

                Drawing.drawing.setColor(Game.playerTank.colorR, Game.playerTank.colorG, Game.playerTank.colorB);
                this.drawSpinny(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size / 2, 200, 4, 0.3, 75 * fade, 0.5 * fade, false);
                Drawing.drawing.setColor(Game.playerTank.secondaryColorR, Game.playerTank.secondaryColorG, Game.playerTank.secondaryColorB);
                this.drawSpinny(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size / 2, 198, 3, 0.5, 60 * fade, 0.375 * fade, false);
            }

            if (i == 9 && Game.followingCam && Panel.panel.zoomTimer > 0.2 && selectedArcBullet)
            {
                Tank t = focusedTank();
                Drawing.drawing.setColor(t.colorR, t.colorG, t.colorB);
                Drawing.drawing.drawImage(t.angle + Math.PI / 4, "cursor.png", t.posX + Math.cos(t.angle) * fcArcAim, t.posY + Math.sin(t.angle) * fcArcAim, 50, 100, 100);
            }

            drawables[i].clear();
        }

		if (isUpdatingGame())
		{
			for (Chunk c : Chunk.chunkList)
				c.faces.clear();
		}

		for (Movable m : Game.movables)    // todo: fix sometime
		{
			AtomicInteger count = new AtomicInteger();
			m.getTouchingChunks().forEach(c ->
            {
				if (isUpdatingGame())
                    c.addMovable(m);

				count.getAndIncrement();
            });

			if (Chunk.debug)
			{
				Drawing.drawing.setColor(255, 255, 255);
				Drawing.drawing.drawText(m.posX, m.posY, m.size, count + "");
			}
		}

		if (Game.showHitboxes)
		{
			for (Chunk c : Chunk.chunkList)
			{
				for (Chunk.FaceList faceList : c.faceLists)
				{
					for (Face f : faceList.topFaces)
					{
						if (shouldHide(f)) continue;
						drawing.setColor(150, 50, 50);
						drawing.fillRect(0.5 * (f.endX + f.startX), f.startY, f.endX - f.startX, 5);
					}

					for (Face f : faceList.bottomFaces)
					{
						if (shouldHide(f)) continue;
						drawing.setColor(255, 50, 50);
						drawing.fillRect(0.5 * (f.endX + f.startX), f.startY, f.endX - f.startX, 5);
					}

					for (Face f : faceList.leftFaces)
					{
						if (shouldHide(f)) continue;
						drawing.setColor(50, 50, 150);
						drawing.fillRect(f.startX, 0.5 * (f.endY + f.startY), 5, f.endY - f.startY);
					}

					for (Face f : faceList.rightFaces)
					{
						if (shouldHide(f)) continue;
						drawing.setColor(50, 50, 255);
						drawing.fillRect(f.startX, 0.5 * (f.endY + f.startY), 5, f.endY - f.startY);
					}
				}
			}
		}

		Drawing.drawing.setColor(0, 0, 0, 127);

		if (Panel.darkness > 0)
		{
			Drawing.drawing.setColor(0, 0, 0, Math.max(0, Panel.darkness));
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);
		}

		if (Game.game.window.touchscreen && TankPlayer.shootStickEnabled)
		{
			double size = TankPlayer.mineButton.sizeX * Obstacle.draw_size / Game.tile_size;
			Drawing.drawing.setColor(255, 127, 0, 64);
			Drawing.drawing.fillInterfaceOval(TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, size, size);

			Drawing.drawing.setColor(255, 255, 0, 64);
			Drawing.drawing.fillInterfaceOval(TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, size * 0.8, size * 0.8);

			//Drawing.drawing.setColor(255, 255, 255, 64);
			//Drawing.drawing.drawInterfaceImage("/mine.png", TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, TankPlayer.mineButton.sizeX, TankPlayer.mineButton.sizeY);
		}

		Chunk.drawDebugStuff();

        if (Game.angledView && !showDefaultMouse)
            Panel.panel.drawMouseTarget(true);

        if (Game.framework == Game.Framework.lwjgl)
        {
            Game.game.window.transformations.clear();
            Game.game.window.loadPerspective();
        }

        minimap.draw();

        for (FixedMenu menu : ModAPI.fixedMenus)
            menu.draw();

        if (npcShopScreen)
        {
            Drawing.drawing.setColor(127, 178, 228, 64);
            Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

            Drawing.drawing.setInterfaceFontSize(this.titleSize);

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
			else
				Drawing.drawing.setColor(0, 0, 0);

			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 210 + shopOffset, "Shop");

			this.exitShop.draw();

			this.npcShopList.draw();
		}

		if (!playing)
		{
			if (Crusade.crusadeMode && !ScreenPartyLobby.isClient && !displayedTitle)
			{
				title.text = tanks.translation.Translation.translate("Battle %d", Crusade.currentCrusade.currentLevel + 1);

				if (Crusade.currentCrusade.showNames)
					subtitle.text = Crusade.currentCrusade.levels.get(Crusade.currentCrusade.currentLevel).levelName.replace("_", " ");
			}

			boolean events = ModAPI.sendEvents;
			ModAPI.sendEvents = false;

			if (!title.text.isEmpty() && !displayedTitle)
			{
				displayedTitle = true;
				titleText = new FixedText(FixedText.types.title, title, 0);
				titleText.shadowEffect = false;
				titleText.display();
			}

			if (!subtitle.text.isEmpty() && !displayedSubtitle)
			{
				displayedSubtitle = true;
				subtitleText = new FixedText(FixedText.types.subtitle, subtitle, 0);
				subtitleText.shadowEffect = false;
				subtitleText.display();
			}

			ModAPI.sendEvents = events;

			if (shopScreen)
			{
				Drawing.drawing.setColor(127, 178, 228, 64);
				Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

				Drawing.drawing.setInterfaceFontSize(this.titleSize);

				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255);
				else
					Drawing.drawing.setColor(0, 0, 0);

				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 210 + shopOffset, "Shop");

				this.exitShop.draw();
				this.shopList.draw();

				for (int i = Math.min((this.shopList.page + 1) * this.shopList.rows * this.shopList.columns, shopItemButtons.size()) - 1; i >= this.shopList.page * this.shopList.rows * this.shopList.columns; i--)
				{
					Button b = this.shopItemButtons.get(i);
					b.draw();
					Drawing.drawing.setColor(255, 255, 255);
					Drawing.drawing.drawInterfaceImage(this.shop.get(i).icon, b.posX - 135, b.posY, 40, 40);
				}
			}
			else
			{
				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
					play.draw();
				else
				{
					if (ScreenPartyHost.isServer)
					{
						readyPlayers.clear();

						for (Player p : ScreenPartyHost.readyPlayers)
							readyPlayers.add(p.getConnectedPlayer());
					}
					else
						readyPlayers = ScreenPartyLobby.readyPlayers;

					double s = Game.startTime;

					if (cancelCountdown)
						s = 400;

					double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
					double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

					Drawing.drawing.setColor(0, 0, 0, Math.max(0, 127 * Math.min(1, (readyPanelCounter * 10) / 200) * Math.min(s / 25, 1)));
					Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX + extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
					Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - Math.min(readyPanelCounter * 10, 200), Drawing.drawing.interfaceSizeY / 2,
							Math.min(readyPanelCounter * 20, 400), height);

					double c = readyPanelCounter - 35;

					double opacity = Math.max(Math.min(s / 25, 1) * 255, 0);
					if (c > 0)
					{
						Drawing.drawing.setColor(255, 255, 255, opacity);
						Drawing.drawing.setInterfaceFontSize(this.titleSize);
						Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 50, "Ready players:");
					}

					int includedPlayers = 0;

					if (ScreenPartyHost.isServer)
						includedPlayers = ScreenPartyHost.includedPlayers.size();
					else if (ScreenPartyLobby.isClient)
						includedPlayers = ScreenPartyLobby.includedPlayers.size();

					double spacing = readyNameSpacing;

					if (includedPlayers > 15)
						spacing = spacing / 2;

					if (includedPlayers > 30)
						spacing = spacing / 2;

					if (includedPlayers > 60)
						spacing = spacing / 2;


					if (readyPlayers.size() > readyNamesCount && c > lastNewReadyName + spacing)
					{
						lastNewReadyName = c;
						readyNamesCount++;
					}

					int slots = (int) ((Drawing.drawing.interfaceSizeY - 200) / 40) - 1;
					int base = 0;

					if (readyNamesCount >= includedPlayers)
						slots++;

					if (readyNamesCount > slots)
						base = readyNamesCount - slots;

					for (int i = 0; i < readyPlayers.size(); i++)
					{
						if (i < readyNamesCount)
						{
							Drawing.drawing.setColor(255, 255, 255, Math.max(Math.min(s / 25, 1) * 255, 0));
							Drawing.drawing.setInterfaceFontSize(this.textSize);

							if (i >= base)
							{
								ConnectedPlayer cp = readyPlayers.get(i);

								String name;
								if (Game.enableChatFilter)
									name = Game.chatFilter.filterChat(cp.username);
								else
									name = cp.username;

								Drawing.drawing.setBoundedInterfaceFontSize(this.textSize, 250, name);
								Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * (i - base) + 100, name);
								Tank.drawTank(Drawing.drawing.interfaceSizeX - 240 - Drawing.drawing.getStringWidth(name) / 2, 40 * (i - base) + 100, cp.colorR, cp.colorG, cp.colorB, cp.colorR2, cp.colorG2, cp.colorB2, opacity / 255 * 25);
							}
						}
					}

					if (c >= 0)
					{
						Drawing.drawing.setColor(255, 255, 255, Math.min(s / 25, 1) * 127);
						Drawing.drawing.setInterfaceFontSize(this.textSize);

						for (int i = readyNamesCount; i < Math.min(includedPlayers, slots); i++)
                            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * i + 100, "Waiting...");

						int extra = includedPlayers - Math.max(readyNamesCount, slots);
						if (extra > 0)
						{
							if (extra == 1)
								Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * slots + 100, "Waiting...");
							else
								Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * slots + 100, "%d waiting...", extra);
						}
					}

					if (prevReadyNames != readyNamesCount)
						Drawing.drawing.playSound("bullet_explode.ogg", 1.5f);

					prevReadyNames = readyNamesCount;

					readyButton.draw();
				}

				if (!this.shopItemButtons.isEmpty() && this.readyButton.enabled)
				{
					enterShop.enableHover = newItemsNotification;
					enterShop.fullInfo = true;
					enterShop.draw();

					if (newItemsNotification)
					{
						Button.drawGlow(enterShop.posX - enterShop.sizeX / 2 + enterShop.sizeY / 2, enterShop.posY + 2.5 + 1, enterShop.sizeY * 3 / 4, enterShop.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
						drawing.setInterfaceFontSize(this.textSize / Drawing.drawing.interfaceScaleZoom);
						drawing.setColor(255, 127, 0);
						drawing.fillInterfaceOval(enterShop.posX - enterShop.sizeX / 2 + enterShop.sizeY / 2, enterShop.posY, enterShop.sizeY * 3 / 4, enterShop.sizeY * 3 / 4);
						drawing.setColor(255, 255, 255);
						Drawing.drawing.drawInterfaceText(enterShop.posX - enterShop.sizeX / 2 + enterShop.sizeY / 2 + 0.5, enterShop.posY, "!");
					}
				}

				if (ScreenPartyHost.isServer && this.cancelCountdown)
					startNow.draw();

				if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient || Game.autostart) && !cancelCountdown)
				{
					Drawing.drawing.setColor(127, 127, 127);
					Drawing.drawing.fillInterfaceRect(play.posX, play.posY + play.sizeY / 2 - 5, play.sizeX * 32 / 35, 3);
					Drawing.drawing.setColor(255, 127, 0);
					Drawing.drawing.fillInterfaceProgressRect(play.posX, play.posY + play.sizeY / 2 - 5, play.sizeX * 32 / 35, 3, Math.max(Game.startTime / Game.currentLevel.startTime, 0));

					if (Game.glowEnabled)
                        Drawing.drawing.fillInterfaceGlow(play.posX + ((Game.startTime / Game.currentLevel.startTime - 0.5) * (play.sizeX * 32 / 35)), play.posY + play.sizeY / 2 - 5, 20, 20);
				}
			}
		}

		if (!paused && Game.game.window.touchscreen && !shopScreen)
		{
			pause.draw();
			Drawing.drawing.setColor(255, 255, 255);
			Drawing.drawing.drawInterfaceImage("icons/pause.png", pause.posX, pause.posY, 40, 40);

			if (Drawing.drawing.enableMovingCamera)
			{
				zoom.draw();

				if (!Panel.autoZoom)
					zoomAuto.draw();

				Drawing.drawing.setColor(255, 255, 255);
				if (Drawing.drawing.movingCamera)
					Drawing.drawing.drawInterfaceImage("icons/zoom_out.png", zoom.posX, zoom.posY, 40, 40);
				else
					Drawing.drawing.drawInterfaceImage("icons/zoom_in.png", zoom.posX, zoom.posY, 40, 40);

				if (!Panel.autoZoom)
					Drawing.drawing.drawInterfaceImage("icons/zoom_auto.png", zoomAuto.posX, zoomAuto.posY, 40, 40);
			}
		}

		if (!(paused && screenshotMode))
		{
			Game.player.hotbar.draw();

			if (Game.showSpeedrunTimer && !(paused && screenshotMode) && !(Game.currentGame != null && Game.currentGame.hideSpeedrunTimer))
				SpeedrunTimer.draw();
		}

		if (Game.deterministicMode && !ScreenPartyLobby.isClient)
		{
			if (Level.isDark() || (Game.screen instanceof IDarkScreen && Panel.win && Game.effectsEnabled))
				Drawing.drawing.setColor(255, 255, 255, 127);
			else
				Drawing.drawing.setColor(0, 0, 0, 127);

			double posX = Drawing.drawing.interfaceSizeX + (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2 - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale - 50;
			double posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;

			if (Drawing.drawing.interfaceScaleZoom > 1)
				posX -= 50;

			Drawing.drawing.setInterfaceFontSize(24);

			if (Game.deterministic30Fps)
				Drawing.drawing.drawInterfaceText(posX, posY, "Deterministic mode (30 FPS)", true);
			else
				Drawing.drawing.drawInterfaceText(posX, posY, "Deterministic mode (60 FPS)", true);
		}

		if (Game.currentGame != null)
			Game.currentGame.draw();

		if (paused && !screenshotMode)
		{
			boolean started = playing && !ScreenGame.finishedQuick;
			quitCrusade.enableHover = quitCrusadeParty.enableHover = started;
			quitCrusadePartyFinalLife.enableHover = restartCrusadePartyFinalLife.enableHover = started;
			restartCrusade.enableHover = restartCrusadeParty.enableHover = started;

			Drawing.drawing.setColor(127, 178, 228, 64);
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

			if (Game.game.window.textValidPressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
				options.draw();

			if (ScreenPartyLobby.isClient)
			{
				closeMenuClient.draw();
				exitParty.draw();
			}
			else if (ScreenPartyHost.isServer)
			{
				if (ScreenInterlevel.fromSavedLevels || ScreenInterlevel.fromMinigames)
				{
					closeMenuLowerPos.draw();
					restartLowerPos.draw();
					back.draw();
				}
				else if (Crusade.crusadeMode)
				{
					closeMenuLowerPos.draw();

					if (Crusade.currentCrusade.finalLife())
					{
						if (Panel.win && finishedQuick)
							quitCrusadeParty.draw();
						else
							quitCrusadePartyFinalLife.draw();

						restartCrusadePartyFinalLife.draw();
					}
					else
					{
						quitCrusadeParty.draw();
						restartCrusadeParty.draw();
					}
				}
				else
				{
					closeMenu.draw();
					newLevel.draw();
					restart.draw();
					quitPartyGame.draw();
				}
			}
			else if (ScreenInterlevel.tutorialInitial)
			{
				resumeLowerPos.draw();
				restartTutorial.draw();
			}
			else if (ScreenInterlevel.tutorial)
			{
				resumeLowerPos.draw();
				restartTutorial.draw();
				quitHigherPos.draw();
			}
			else if (ScreenInterlevel.fromMinigames)
			{
				resumeLowerPos.draw();
				restartLowerPos.draw();
				back.draw();
			}
			else if (Crusade.crusadeMode)
			{
				if (Crusade.currentCrusade.finalLife())
				{
					quitCrusadeFinalLife.draw();
					restartCrusadeFinalLife.draw();
				}
				else
				{
					quitCrusade.draw();
					restartCrusade.draw();
				}

				resumeLowerPos.draw();
			}
			else if (name != null)
			{
				resume.draw();
				edit.draw();
				restart.draw();
				quit.draw();
			}
			else
			{
				resume.draw();
				newLevel.draw();
				restart.draw();
				quit.draw();
			}

			Drawing.drawing.setInterfaceFontSize(this.titleSize);
			Drawing.drawing.setColor(0, 0, 0);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255);

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Game paused");
            else
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Game menu");
        }

        if (this.overlay != null)
            this.overlay.draw();

		if (Game.showUPFMeter)
		{
			if (isUpdatingGame() && Panel.panel.ageFrames % 5 == 0)
			{
				Tank.prevUPF.add(Tank.updatesPerFrame);
				if (Tank.prevUPF.size() > 100)
					Tank.prevUPF.pop();
			}

			Drawing.drawing.setColor(255, 0, 0);
			int i = 0;
			for (int num : Tank.prevUPF)
			{
				Drawing.drawing.fillInterfaceOval(i * 2 + 20, (100 - num) * 2 + 150, 5, 5);
				i++;
			}

			int br = Level.isDark() ? 255 : 0;
			Drawing.drawing.setColor(br, br, br);
			Drawing.drawing.setInterfaceFontSize(14);
			for (i = 0; i <= 150; i += 10)
				Drawing.drawing.drawInterfaceText(10, (100 - i) * 2 + 150, i + "");
			Drawing.drawing.drawInterfaceText(120, 360, "Tank count: " + Tank.lastTankCount);
		}

		Drawing.drawing.setInterfaceFontSize(this.textSize);
    }

	int[] bx = {0, 1, 0, -1}, by = {-1, 0, 1, 0};
	int[] bsx = {2, 1, 2, 1}, bsy = {1, 2, 1, 0};

	public void drawBorders()
	{
		double frac = Obstacle.draw_size / Game.tile_size;
		Drawing.drawing.setColor(174 * frac + Level.currentColorR * (1 - frac), 92 * frac + Level.currentColorG * (1 - frac), 16 * frac + Level.currentColorB * (1 - frac));
		for (Chunk c : Chunk.chunkList)
		{
			for (int side = 0; side < 4; side++)
			{
				Face bf = c.borderFaces[side];
				if (c.borderFaces[side] != null)
				{
					double sizeX = (bf.endX - bf.startX), sizeY = (bf.endY - bf.startY);
					double x = bf.startX + sizeX / 2, y = bf.startY + sizeY / 2;
					Drawing.drawing.fillBox(this,
							x + Game.tile_size * 0.5 * bx[side],
							y + Game.tile_size * 0.5 * by[side], -Game.tile_size,
							sizeX + Game.tile_size * bsx[side],
							sizeY + Game.tile_size * bsy[side], Game.tile_size * 2, (byte) 1);
				}
			}
		}
	}

	public static boolean shouldHide(Face f)
	{
		return (f.owner instanceof Tank t && (t.canHide || t.hidden)) || (f.owner instanceof TankAIControlled t1 && t1.invisible);
	}

	public static boolean isUpdatingGame()
	{
		ScreenGame g = ScreenGame.getInstance();
		return g != null && (!g.paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient);
	}

    public void updateFreecam()
    {
		if (controlPlayer)
			return;

		if (!Game.followingCam || focusedTank() == null || focusedTank().destroy)
			freecam = false;

        int fwd = Game.game.input.moveUp.isPressed() ? 1 : 0;
		int bwd = Game.game.input.moveDown.isPressed() ? -1 : 0;
		int left = Game.game.input.moveLeft.isPressed() ? -1 : 0;
		int right = Game.game.input.moveRight.isPressed() ? 1 : 0;
		double speed = Game.game.window.pressedKeys.contains(InputCodes.KEY_R) && fwd != 0 ? 2 : 1;
		boolean up = Game.game.window.pressedKeys.contains(InputCodes.KEY_SPACE);
		boolean down = Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT);

		if (up && down)
			speed *= 0.5;

		if (fwd == 0 && bwd == 0 && left == 0 && right == 0)
			speed = 0;

		double angle = yaw + Math.PI / 2;
		double strafe = fwd + bwd == 0 ? 1 : (fwd + bwd == 1 ? 0.5 : -0.5);

		if (fwd + bwd == -1) angle -= Math.PI;
		if (left + right == 1) angle += Math.PI / 2 * strafe;
		else if (left + right == -1) angle -= Math.PI / 2 * strafe;

		x += Math.cos(angle) * speed * (Panel.frameFrequency / 150);
		y += Math.sin(angle) * speed * (Panel.frameFrequency / 150);

        yaw = (yaw + (Drawing.drawing.getInterfaceMouseX() - prevCursorX) / 1000) % (Math.PI * 2);
        pitchAdd = (pitchAdd + (Drawing.drawing.getInterfaceMouseY() - prevCursorY) / 1000) % (Math.PI * 2);
		roll = 0.5 * Math.sin(yaw);
		pitchAdd = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, pitchAdd));
		pitch = 0.5 * -Math.cos(Math.PI / 3 * yaw) + pitchAdd;

        if (down)
            z += Panel.frameFrequency / 200;

        if (up)
            z -= Panel.frameFrequency / 200;

        this.prevCursorX = Drawing.drawing.getInterfaceMouseX();
        this.prevCursorY = Drawing.drawing.getInterfaceMouseY();
    }

    @Override
    public void onMouseConstrain()
    {
        this.prevCursorX = Drawing.drawing.getInterfaceMouseX();
        this.prevCursorY = Drawing.drawing.getInterfaceMouseY();
    }

    public void updateFollowingCam()
    {
        Panel.autoZoom = false;
        Game.playerTank.angle += (Drawing.drawing.getInterfaceMouseX() - prevCursorX) / 130 * (Game.firstPerson ? 1 - fcZoom : 1) * sensitivity;

        if (Game.game.input.tilt.isPressed())
            fcPitch += (Drawing.drawing.getInterfaceMouseY() - this.prevCursorY) * sensitivity * 0.005;
        else if (selectedArcBullet)
            fcArcAim += (this.prevCursorY - Drawing.drawing.getInterfaceMouseY()) * (sensitivity * 3);

        fcPitch = Math.max(0, Math.min(0.5, fcPitch));
		if (selectedArcBullet)
		{
			ItemBar b = Game.player.hotbar.itemBar;
			fcArcAim = Math.max(0, Math.min(((ItemBullet) b.slots[b.selected]).getRange(), fcArcAim));
		}

		this.prevCursorX = Drawing.drawing.getInterfaceMouseX();
        this.prevCursorY = Drawing.drawing.getInterfaceMouseY();

        fcZoomPressed = Game.game.input.fcZoom.isPressed();

        if (fcZoomPressed)
        {
			if (Game.game.input.fcZoom.isValid())
			{
				if (System.currentTimeMillis() - fcZoomLastTap < 500)
					fcTargetZoom = 0;

				fcZoomLastTap = System.currentTimeMillis();
				Game.game.input.fcZoom.invalidate();
			}

			if (Game.game.window.validScrollUp && fcTargetZoom < 0.9)
			{
				fcTargetZoom += 0.1;
				Game.game.window.validScrollUp = false;
			}

			if (Game.game.window.validScrollDown && fcTargetZoom > 0)
			{
				fcTargetZoom -= 0.1;
				Game.game.window.validScrollDown = false;
			}
		}

		if (Math.abs(fcTargetZoom - fcZoom) < 0.05)
			fcZoom = fcTargetZoom;
		else
			fcZoom += (fcTargetZoom - fcZoom) / 10;
	}

	@Override
	public void onFocusChange(boolean focused)
	{
		if (!focused && Panel.pauseOnDefocus && ((Game.autostart && !cancelCountdown) || (playing && !shopScreen && !npcShopScreen)))
			paused = true;
	}

	public void saveRemainingTanks()
	{
		if (!savedRemainingTanks && Crusade.crusadeMode && Crusade.currentCrusade != null)
		{
			Crusade.currentCrusade.livingTankIDs.clear();
			for (Movable m : Game.movables)
			{
				if (m instanceof Tank && !m.destroy && ((Tank) m).crusadeID >= 0)
					Crusade.currentCrusade.livingTankIDs.add(((Tank) m).crusadeID);
			}
		}
		savedRemainingTanks = true;
	}

	@Override
	public double getOffsetX()
	{
		return Drawing.drawing.getPlayerOffsetX() + Panel.panel.panX;
	}

	@Override
	public double getOffsetY()
	{
		return Drawing.drawing.getPlayerOffsetY() + Panel.panel.panY;
	}

	@Override
	public double getScale()
	{
		return Drawing.drawing.scale * (1 - Panel.panel.zoomTimer) + Drawing.drawing.interfaceScale * Panel.panel.zoomTimer;
	}

	public void drawSpinny(double x, double y, double z, int max, int parts, double speed, double size, double dotSize, boolean invert)
	{
		for (int i = 0; i < max; i++)
		{
			double frac = (System.currentTimeMillis() / 1000.0 * speed + i * 1.0 / max) % 1;
			double s = Math.max(Math.abs((i % (max * 1.0 / parts)) / 10.0 * parts), 0);

			if (invert)
			{
				frac = -frac;
			}

			double v = size * Math.cos(frac * Math.PI * 2);
			double v1 = size * Math.sin(frac * Math.PI * 2);

			if (Game.enable3d)
				Drawing.drawing.fillOval(x + v, y + v1, z, s * dotSize, s * dotSize, false, false);
			else
				Drawing.drawing.fillOval(x + v, y + v1, s * dotSize, s * dotSize);
		}
	}

	/** The tank that the camera is following */
	public static Tank focusedTank()
	{
		ScreenGame g = getInstance();
		if (g == null || Game.playerTank == null)
			return Game.playerTank;

		if (Game.playerTank.destroy && g.spectatingTank != null)
			return g.spectatingTank;

		return Game.playerTank;
	}

	@Override
	public boolean onEnter(String username, UUID clientID)
	{
		return false;

		/*if (Game.currentLevel == null)
			return true;

		Game.eventsOut.add(new EventLoadLevel(Game.currentLevel).setTargetClient(clientID));
		Game.eventsOut.add(new EventEnterLevel().setTargetClient(clientID));
		return true;*/
	}

	public static ScreenGame getInstance()
	{
		if (Game.screen instanceof ScreenGame g)
			return g;
        if (Game.screen instanceof ScreenOptionsOverlay o)
            return o.game;
        return null;
	}
}
