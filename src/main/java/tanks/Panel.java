package tanks;

import basewindow.BaseWindow;
import basewindow.InputCodes;
import basewindow.ShaderGroup;
import tanks.eventlistener.EventListener;
import tanks.extension.Extension;
import tanks.gui.Button;
import tanks.gui.ChatMessage;
import tanks.gui.ScreenElement.CenterMessage;
import tanks.gui.ScreenElement.Notification;
import tanks.gui.TextBox;
import tanks.gui.screen.*;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.hotbar.Hotbar;
import tanks.network.Client;
import tanks.network.MessageReader;
import tanks.network.NetworkEventMap;
import tanks.network.event.EventBeginLevelCountdown;
import tanks.network.event.INetworkEvent;
import tanks.network.event.IStackableEvent;
import tanks.network.event.online.IOnlineServerEvent;
import tanks.obstacle.Obstacle;
import tanks.rendering.*;
import tanks.tank.*;

import java.util.*;

public class Panel
{
    public static boolean onlinePaused;
    public static LinkedList<Notification> notifs = new LinkedList<>();
	public static long lastNotifTime = 0;
	public static CenterMessage currentMessage;
	public static String lastWindowTitle = "";

	public double zoomTimer = 0;
	public static double zoomTarget = -1;
	public static boolean forceCenter = false;
	public static boolean autoZoom = false;
	public static double lastAutoZoomSpeed = 0;

	public double panX, panY, panSpeed, panSpeedTarget;
	public static double panTargetX, panTargetY;

	public static double windowWidth = 1400;
	public static double windowHeight = 900;

	public static boolean showMouseTarget = true;
	public static boolean showMouseTargetHeight = false;
	public static boolean pauseOnDefocus = true;

	public static Panel panel;
	public static boolean forceRefreshMusic;
	public static boolean win = false;
	public static double darkness = 0;
	public static double skylight;

	public static TextBox selectedTextBox;
	public static Button draggedButton;

	/** Important value used in calculating game speed. Larger values are set when the frames are lower, and game speed is increased to compensate.*/
	public static double frameFrequency = 1;

	//ArrayList<Double> frameFrequencies = new ArrayList<Double>();

	public int frames = 0;

	public double frameSampling = 1;

	public long firstFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);
	public long lastFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);

	public long startTime = System.currentTimeMillis();

	public long lastFrameNano = 0;

	public int lastFPS = 0;
	public int lastWorstFPS = 0;
	public double worstFPS = 0;

	public ScreenOverlayOnline onlineOverlay;

	protected static boolean initialized = false;

	public boolean firstFrame = true;
	public boolean startMusicPlayed = false;
	public long introMusicEnd;

	public ArrayList<Double> pastPlayerX = new ArrayList<>();
	public ArrayList<Double> pastPlayerY = new ArrayList<>();
	public ArrayList<Double> pastPlayerTime = new ArrayList<>();

	public double age = 0;
	public long ageFrames = 0;

	public boolean started = false;
	public boolean settingUp = true;

	protected boolean prevFocused = true;
	protected Screen lastDrawnScreen = null;

	public ArrayList<double[]> lights = new ArrayList<>();
	HashMap<Integer, IStackableEvent> stackedEventsIn = new HashMap<>();

	public static void initialize()
	{
		if (!initialized)
			panel = new Panel();

		initialized = true;
	}

	private Panel()
	{

	}

	public void setUp()
	{
		Game.game.shaderIntro = new ShaderGroundIntro(Game.game.window);
		Game.game.shaderOutOfBounds = new ShaderGroundOutOfBounds(Game.game.window);
 		Game.game.shaderTracks = new ShaderTracks(Game.game.window);

		try
		{
			Game.game.shaderIntro.initialize();
			Game.game.shaderOutOfBounds.initialize();
			Game.game.shaderTracks.initialize();
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		Drawing.drawing.terrainRenderer = new TerrainRenderer();
		Drawing.drawing.trackRenderer = new TrackRenderer();

		ModAPI.setUp();
		Game.resetTiles();
		Chunk.initialize();

		if (Game.game.fullscreen)
			Game.game.window.setFullscreen(Game.game.fullscreen);

		Game.game.window.setIcon("/images/icon64.png");

		if (Game.game.window.soundPlayer == null)
		{
			Game.soundsEnabled = false;
			Game.musicEnabled = false;
		}

		double scale = 1;
		if (Game.game.window.touchscreen && Game.game.window.pointHeight > 0 && Game.game.window.pointHeight <= 500)
		{
			scale = 1.25;

			Drawing.drawing.objWidth *= 1.4;
			Drawing.drawing.objHeight *= 1.4;
			Drawing.drawing.objXSpace *= 1.4;
			Drawing.drawing.objYSpace *= 1.4;

			Drawing.drawing.textSize = Drawing.drawing.objHeight * 0.6;
			Drawing.drawing.titleSize = Drawing.drawing.textSize * 1.25;
		}

		Drawing.drawing.setInterfaceScaleZoom(scale);
		TankPlayer.setShootStick(TankPlayer.shootStickEnabled);
		TankPlayer.controlStick.mobile = TankPlayer.controlStickMobile;
		TankPlayer.controlStick.snap = TankPlayer.controlStickSnap;

		Hotbar.toggle.posX = Drawing.drawing.interfaceSizeX / 2;
		Hotbar.toggle.posY = Drawing.drawing.interfaceSizeY - 20;

		Game.createModels();

		Game.dummyTank = new TankDummy("dummy",0, 0, 0);
		Game.dummyTank.team = null;

		for (Extension e : Game.extensionRegistry.extensions)
			e.loadResources();

		Game.screen = new ScreenIntro();

		Game.loadTankMusic();

		if (Game.game.window.soundsEnabled)
		{
			Game.game.window.soundPlayer.musicPlaying = true;

			for (int i = 1; i <= 5; i++)
				Game.game.window.soundPlayer.registerCombinedMusic("/music/menu_" + i + ".ogg", "menu");

			Game.game.window.soundPlayer.registerCombinedMusic("/music/menu_options.ogg", "menu");

			for (int i = 1; i <= 2; i++)
				Game.game.window.soundPlayer.registerCombinedMusic("/music/ready_music_" + i + ".ogg", "ready");

			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle.ogg", "battle");
			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_paused.ogg", "battle");

			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_night.ogg", "battle_night");
			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_paused.ogg", "battle_night");

			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_timed.ogg", "battle_timed");
			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_timed_paused.ogg", "battle_timed");

			//Game.game.window.soundPlayer.registerCombinedMusic("/music/editor.ogg", "editor");
			//Game.game.window.soundPlayer.registerCombinedMusic("/music/editor_paused.ogg", "editor");
		}

		if (Game.game.window.soundsEnabled)
		{
			Game.game.window.soundPlayer.loadMusic("/music/ready_music_1.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/ready_music_2.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle_night.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle_timed.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle_paused.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle_timed_paused.ogg");

			Game.game.window.soundPlayer.loadMusic("/music/battle.ogg");

			for (int i = 1; i <= 8; i++)
				Game.game.window.soundPlayer.loadMusic("/music/arcade/rampage" + i + ".ogg");
		}

		settingUp = false;
	}

	public void update()
    {
        if (firstFrame)
            this.setUp();

        firstFrame = false;

		if (Game.screen == Game.prevScreen && !Game.screen.windowTitle.equals(lastWindowTitle))
		{
			lastWindowTitle = Game.screen.windowTitle;
			Game.game.window.setWindowTitle("Tanks" + lastWindowTitle);
		}

        Game.prevScreen = Game.screen;
        Obstacle.lastDrawSize = Obstacle.draw_size;

        if (!started && (Game.game.window.validPressedKeys.contains(InputCodes.KEY_F) || !Game.cinematic))
        {
            started = true;

//			this.startTime = System.currentTimeMillis() + splash_duration;
//			Drawing.drawing.playSound("splash_jingle.ogg");
//			Drawing.drawing.playMusic("menu_intro.ogg", Game.musicVolume, false, "intro", 0, false);
		}

		if (!started)
			this.startTime = System.currentTimeMillis();

		int maxFps = Game.maxFPS;
		if (Game.deterministicMode && Game.deterministic30Fps)
			maxFps = 30;
		else if (Game.deterministicMode)
			maxFps = 60;

		if (maxFps > 0)
		{
			int frameTime = 1000000000 / maxFps;
			while (System.nanoTime() - lastFrameNano < frameTime)
			{

			}
		}

		lastFrameNano = System.nanoTime();

		Game.game.window.constrainMouse = Game.constrainMouse && ((Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).paused && ((ScreenGame) Game.screen).playing && Game.playerTank != null && !Game.playerTank.destroy) || Game.screen instanceof ScreenLevelEditor);

		if (!Game.shadowsEnabled)
			Game.game.window.setShadowQuality(0);
		else
			Game.game.window.setShadowQuality(Game.shadowQuality / 10.0 * 1.25);

		Screen prevScreen = Game.screen;

		if (!startMusicPlayed && Game.game.window.soundsEnabled && (System.currentTimeMillis() > introMusicEnd || !("menu".equals(Game.screen.musicID))))
		{
			startMusicPlayed = true;
			this.playScreenMusic(0);
		}

		Panel.windowWidth = Game.game.window.absoluteWidth;
		Panel.windowHeight = Game.game.window.absoluteHeight;

		Drawing.drawing.scale = Math.min(Panel.windowWidth / Game.currentSizeX, (Panel.windowHeight - Drawing.drawing.statsHeight) / Game.currentSizeY) / 50.0;
		Drawing.drawing.unzoomedScale = Drawing.drawing.scale;
		Drawing.drawing.interfaceScale = Drawing.drawing.interfaceScaleZoom * Math.min(Panel.windowWidth / 28, (Panel.windowHeight - Drawing.drawing.statsHeight) / 18) / 50.0;
		Game.game.window.absoluteDepth = Drawing.drawing.interfaceScale * Game.absoluteDepthBase;

		if (Game.deterministicMode && Game.deterministic30Fps)
			Panel.frameFrequency = 100.0 / 30;
		else if (Game.deterministicMode)
			Panel.frameFrequency = 100.0 / 60;
		else
			Panel.frameFrequency = Game.game.window.frameFrequency;

		Game.game.window.showKeyboard = false;

//		Panel.frameFrequency *= 5;

		synchronized (Game.eventsIn)
		{
			stackedEventsIn.clear();

			for (int i = 0; i < Game.eventsIn.size(); i++)
			{
				INetworkEvent e = Game.eventsIn.get(i);

				if (ScreenPartyLobby.isClient)
				{
					ArrayList<EventListener> arr = Game.eventListeners.get(e.getClass());
					if (arr != null)
					{
						for (EventListener l : arr)
						{
							if (l != null)
								l.eventsThisFrame.add(e);
						}
					}
				}

				if (!(e instanceof IOnlineServerEvent))
				{
					if (e instanceof IStackableEvent)
						stackedEventsIn.put(IStackableEvent.f(NetworkEventMap.get(e.getClass()) + IStackableEvent.f(((IStackableEvent) e).getIdentifier())), (IStackableEvent) e);
					else
						e.execute();
				}
			}

			for (INetworkEvent e : stackedEventsIn.values())
                e.execute();

			stackedEventsIn.clear();
			Game.eventsIn.clear();

			if (ScreenPartyLobby.isClient)
			{
				for (EventListener l : Game.eventListenerSet)
					l.func.apply(l.eventsThisFrame);
			}
		}

		if (ScreenPartyHost.isServer)
		{
			synchronized (ScreenPartyHost.disconnectedPlayers)
			{
				for (int i = 0; i < ScreenPartyHost.disconnectedPlayers.size(); i++)
				{
					for (int j = 0; j < Game.movables.size(); j++)
					{
						Movable m = Game.movables.get(j);
						if (m instanceof TankPlayerRemote && ((TankPlayerRemote) m).player.clientID.equals(ScreenPartyHost.disconnectedPlayers.get(i)))
							((TankPlayerRemote) m).health = 0;
					}

					ScreenPartyHost.includedPlayers.remove(ScreenPartyHost.disconnectedPlayers.get(i));

					for (Player p : Game.players)
					{
						if (p.clientID.equals(ScreenPartyHost.disconnectedPlayers.get(i)))
						{
							ScreenPartyHost.readyPlayers.remove(p);

							if (Crusade.currentCrusade != null)
							{
								if (Crusade.currentCrusade.crusadePlayers.containsKey(p))
								{
									Crusade.currentCrusade.crusadePlayers.get(p).coins = p.hotbar.coins;
									Crusade.currentCrusade.disconnectedPlayers.add(Crusade.currentCrusade.crusadePlayers.remove(p));
								}
							}
						}
					}

					Game.removePlayer(ScreenPartyHost.disconnectedPlayers.get(i));
				}

                if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.includedPlayers.size() && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).cancelCountdown)
                {
                    Game.eventsOut.add(new EventBeginLevelCountdown());
                    ((ScreenGame) Game.screen).cancelCountdown = false;
                }

                ScreenPartyHost.disconnectedPlayers.clear();
            }
        }

        if (!Interval.gameIntervals.isEmpty())
        {
            ArrayList<String> removeIntervals = new ArrayList<>();
            for (Interval i : Interval.gameIntervals.values())
            {
                if (i.run())
                    removeIntervals.add(i.name);
            }

            for (String i : removeIntervals)
                Interval.gameIntervals.remove(i);
        }

        if (!Interval.levelIntervals.isEmpty())
        {
            ArrayList<String> removeIntervals = new ArrayList<>();
            for (Interval i : Interval.levelIntervals.values())
            {
                if (i.run())
                    removeIntervals.add(i.name);
            }

            for (String i : removeIntervals)
                Interval.levelIntervals.remove(i);
        }

        if (Game.player.hotbar.coins < 0)
            Game.player.hotbar.coins = 0;

        if (!(Game.screen instanceof ScreenInfo))
        {
            if (!(Game.screen instanceof ScreenGame) || Panel.zoomTarget < 0 ||
                    ((Game.playerTank == null || Game.playerTank.destroy) && (((ScreenGame) Game.screen).spectatingTank == null)) || !((ScreenGame) Game.screen).playing)
                this.zoomTimer -= 0.02 * Panel.frameFrequency;
        }

		if (((Game.playerTank != null && !Game.playerTank.destroy) || (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).spectatingTank != null)) && !ScreenGame.finished
				&& (Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale || Game.followingCam)
				&& Game.screen instanceof ScreenGame && (((ScreenGame) (Game.screen)).playing || ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && Game.startTime < Game.currentLevel.startTime)))
		{
			Drawing.drawing.enableMovingCamera = Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale;

			if (Game.playerTank == null || Game.playerTank.destroy)
			{
				Drawing.drawing.playerX = ((ScreenGame) Game.screen).spectatingTank.posX;
				Drawing.drawing.playerY = ((ScreenGame) Game.screen).spectatingTank.posY;
			}
			else
			{
				Drawing.drawing.playerX = Game.playerTank.posX;
				Drawing.drawing.playerY = Game.playerTank.posY;

				if (Game.playerTank instanceof TankPlayerController)
				{
					Drawing.drawing.playerX = ((TankPlayerController) Game.playerTank).interpolatedPosX;
					Drawing.drawing.playerY = ((TankPlayerController) Game.playerTank).interpolatedPosY;
				}
			}

			this.pastPlayerX.add(Drawing.drawing.playerX);
			this.pastPlayerY.add(Drawing.drawing.playerY);
			this.pastPlayerTime.add(this.age);

			while (Panel.panel.pastPlayerTime.size() > 1 && Panel.panel.pastPlayerTime.get(1) < Panel.panel.age - Drawing.drawing.getTrackOffset())
			{
				Panel.panel.pastPlayerX.remove(0);
				Panel.panel.pastPlayerY.remove(0);
				Panel.panel.pastPlayerTime.remove(0);
			}

			if (Drawing.drawing.movingCamera)
			{
				if (!(Game.screen instanceof ScreenGame) || Panel.zoomTarget < 0 ||
						((Game.playerTank == null || Game.playerTank.destroy) && (((ScreenGame) Game.screen).spectatingTank == null)) ||
						!((ScreenGame) Game.screen).playing)
					this.zoomTimer += 0.04 * Panel.frameFrequency;

				double mul = Panel.zoomTarget;
				if (mul < 0)
					mul = 1;

				if (Game.startTime > 0 && (ScreenPartyHost.isServer || ScreenPartyLobby.isClient))
					this.zoomTimer = Math.min(this.zoomTimer, mul * (1 - Game.startTime / Game.currentLevel.startTime));
			}
		}
		else
		{
			Drawing.drawing.enableMovingCamera = false;
		}

		this.zoomTimer = Math.min(Math.max(this.zoomTimer, 0), 1);
		double d = Math.pow(1.01, Panel.frameFrequency);

		if (Game.screen instanceof ScreenGame && Drawing.drawing.enableMovingCamera && Panel.zoomTarget >= 0 && (((ScreenGame) Game.screen).spectatingTank != null || (Game.playerTank != null && !Game.playerTank.destroy)) && ((ScreenGame) Game.screen).playing)
		{
			double speed = 0.3 * Drawing.drawing.unzoomedScale;
			double accel = 0.0003 * Drawing.drawing.unzoomedScale;
			double distDampen = 2;

			if (Panel.autoZoom && !Panel.forceCenter)
			{
				double dispX = Panel.panTargetX - panX;
				double dispY = Panel.panTargetY - panY;
				double dist = Math.sqrt(dispX*dispX + dispY*dispY);

				panSpeedTarget = Math.min(0.01, dist * 0.5);
				panSpeed += 0.05 * Panel.frameFrequency * Math.signum(panSpeedTarget - panSpeed);

				Tank t = ScreenGame.focusedTank();
				if (t == null || Drawing.drawing.scale >= 0.9)
				{
					this.panX /= d;
					this.panY /= d;
				}
				else if (Math.abs(dispX) + Math.abs(dispY) < Math.abs(panSpeed) * 2)
				{
					this.panX = Panel.panTargetX;
					this.panY = Panel.panTargetY;
				}
				else
				{
					this.panX += panSpeed * Math.signum(dispX) * Panel.frameFrequency;
					this.panY += panSpeed * Math.signum(dispY) * Panel.frameFrequency;
				}

				speed /= 4;

				if (speed - Panel.lastAutoZoomSpeed > accel * Panel.frameFrequency)
					speed = Panel.lastAutoZoomSpeed + accel * Panel.frameFrequency;

				if (-speed + Panel.lastAutoZoomSpeed > accel * Panel.frameFrequency)
					speed = Panel.lastAutoZoomSpeed - accel * Panel.frameFrequency;

				double zoomDist = Math.abs(this.zoomTimer - Panel.zoomTarget) / Drawing.drawing.unzoomedScale;
				if (zoomDist < distDampen)
					speed *= Math.pow(zoomDist / distDampen, Panel.frameFrequency / 20);

				Panel.lastAutoZoomSpeed = speed;

				if (Math.abs(Panel.zoomTarget - this.zoomTimer) < speed)
				{
					this.zoomTimer = Panel.zoomTarget;
				}
				else
				{
					speed *= Math.signum(Panel.zoomTarget - this.zoomTimer);
					this.zoomTimer += speed * Panel.frameFrequency;
				}
			}
			else
			{
				double nzt = this.zoomTimer + 0.02 * Math.signum(Panel.zoomTarget - this.zoomTimer) * Panel.frameFrequency;

				if (this.zoomTimer > Panel.zoomTarget)
					this.zoomTimer = Math.max(nzt, Panel.zoomTarget);
				else
					this.zoomTimer = Math.min(nzt, Panel.zoomTarget);

				this.panX /= d;
				this.panY /= d;
			}
		}
		else
		{
			this.panX /= d;
			this.panY /= d;
		}

		Drawing.drawing.scale = Game.screen.getScale();

		Drawing.drawing.enableMovingCameraX = (Panel.windowWidth < Game.currentSizeX * Game.tile_size * Drawing.drawing.scale);
		Drawing.drawing.enableMovingCameraY = ((Panel.windowHeight - Drawing.drawing.statsHeight) < Game.currentSizeY * Game.tile_size * Drawing.drawing.scale);

		if (Game.connectedToOnline && Panel.selectedTextBox == null)
		{
			if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ESCAPE))
			{
				Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);

				onlinePaused = !onlinePaused;
			}
		}
		else
			onlinePaused = false;

		ScreenOverlayChat.update(!(Game.screen instanceof IHiddenChatboxScreen));

		if (Game.screen.interfaceScaleZoomOverride > 0)
			Drawing.drawing.interfaceScaleZoom = Game.screen.interfaceScaleZoomOverride;
		else
			Drawing.drawing.interfaceScaleZoom = Drawing.drawing.interfaceScaleZoomDefault;

		Drawing.drawing.interfaceSizeX = Drawing.drawing.baseInterfaceSizeX / Drawing.drawing.interfaceScaleZoom;
		Drawing.drawing.interfaceSizeY = Drawing.drawing.baseInterfaceSizeY / Drawing.drawing.interfaceScaleZoom;

		if (Game.game.window.focused != prevFocused)
		{
			prevFocused = Game.game.window.focused;
			Game.screen.onFocusChange(prevFocused);
		}

		if (!onlinePaused)
			Game.screen.update();
		else
			this.onlineOverlay.update();

		if (Game.game.input.fullscreen.isValid())
		{
			Game.game.input.fullscreen.invalidate();
			Game.game.window.setFullscreen(!Game.game.window.fullscreen);
		}

		if (ScreenPartyLobby.isClient)
            Client.handler.reply();

		if (Game.steamNetworkHandler.initialized)
			Game.steamNetworkHandler.update();

		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
		{
			synchronized (ScreenPartyHost.server.connections)
			{
				for (int j = 0; j < ScreenPartyHost.server.connections.size(); j++)
				{
					synchronized (ScreenPartyHost.server.connections.get(j).events)
					{
						ScreenPartyHost.server.connections.get(j).events.addAll(Game.eventsOut);
					}

					ScreenPartyHost.server.connections.get(j).reply();
				}
			}
		}

		if (!ScreenPartyLobby.isClient)
		{
			for (EventListener l : Game.eventListenerSet)
				l.eventsThisFrame.clear();

			for (INetworkEvent e : Game.eventsOut)
			{
				ArrayList<EventListener> arr = Game.eventListeners.get(e.getClass());
				if (arr != null)
				{
					for (EventListener l : arr)
					{
						if (l != null)
							l.eventsThisFrame.add(e);
					}
				}
			}
		}

		Game.eventsOut.clear();

		if (!ScreenPartyLobby.isClient)
		{
			for (EventListener l : Game.eventListenerSet)
				l.func.apply(l.eventsThisFrame);
		}

		if (prevScreen != Game.screen)
		{
			Drawing.drawing.interfaceSizeX = Drawing.drawing.baseInterfaceSizeX / Drawing.drawing.interfaceScaleZoom;
			Drawing.drawing.interfaceSizeY = Drawing.drawing.baseInterfaceSizeY / Drawing.drawing.interfaceScaleZoom;

			Panel.selectedTextBox = null;
			Panel.draggedButton = null;
		}

		if (forceRefreshMusic || (prevScreen != null && prevScreen != Game.screen && Game.screen != null && !Game.stringsEqual(prevScreen.music, Game.screen.music) && !(Game.screen instanceof IOnlineScreen)))
		{
			if (Game.stringsEqual(prevScreen.musicID, Game.screen.musicID))
				this.playScreenMusic(500);
			else
				this.playScreenMusic(0);
		}

		forceRefreshMusic = false;

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_F12) && Game.game.window.validPressedKeys.contains(InputCodes.KEY_LEFT_ALT) && Game.debug)
		{
			Game.game.window.validPressedKeys.clear();
			Game.exitToCrash(new Exception("Manually initiated crash"));
		}

		if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
			Game.eventsOut.clear();
	}

	public void playScreenMusic(long fadeTime)
	{
		if (Game.screen instanceof IOnlineScreen)
			return;

		if (Game.screen.music == null)
			Drawing.drawing.stopMusic();
		else if (Panel.panel.startMusicPlayed)
			Drawing.drawing.playMusic(Game.screen.music, Game.musicVolume, true, Game.screen.musicID, fadeTime);
	}

	public void draw()
	{
		if (Drawing.drawing.terrainRenderer == null)
			Drawing.drawing.terrainRenderer = new TerrainRenderer();

		if (Drawing.drawing.trackRenderer == null)
			Drawing.drawing.trackRenderer = new TrackRenderer();

		if (lastDrawnScreen != Game.screen)
		{
			lastDrawnScreen = Game.screen;
			Drawing.drawing.trackRenderer.reset();
			Drawing.drawing.terrainRenderer.reset();
		}

		if (!(Game.screen instanceof ScreenGame))
		{
			Drawing.drawing.scale = Math.min(Panel.windowWidth / Game.currentSizeX, (Panel.windowHeight - Drawing.drawing.statsHeight) / Game.currentSizeY) / 50.0;
			Drawing.drawing.unzoomedScale = Drawing.drawing.scale;
			Drawing.drawing.scale = Game.screen.getScale();
			Drawing.drawing.interfaceScale = Drawing.drawing.interfaceScaleZoom * Math.min(Panel.windowWidth / 28, (Panel.windowHeight - Drawing.drawing.statsHeight) / 18) / 50.0;
			Game.game.window.absoluteDepth = Drawing.drawing.interfaceScale * Game.absoluteDepthBase;
		}

		double t = 3;
		skylight = (t - Math.pow(t, 1 - Level.currentLightIntensity)) / (t - 1) * 0.95;

		if (!(Game.screen instanceof ScreenExit))
		{
			if (Game.followingCam && Game.screen instanceof ScreenGame && Game.currentLevel != null)
				Drawing.drawing.setColor(135 * skylight, 206 * skylight, 235 * skylight);
			else
				Drawing.drawing.setColor(174, 92, 16);

			Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);
		}

		Drawing.drawing.setLighting(Level.currentLightIntensity, Level.currentShadowIntensity);
		this.lights.clear();
		Game.screen.setupLights();
		Game.game.window.createLights(this.lights, Drawing.drawing.scale);

		if (!Game.game.window.drawingShadow)
		{
			long time = (long) (System.currentTimeMillis() * frameSampling / 1000);
			if (lastFrameSec < time && lastFrameSec != firstFrameSec)
			{
				lastFPS = (int) (frames * 1.0 * frameSampling);
				lastWorstFPS = (int) worstFPS;
				worstFPS = lastFPS;
				frames = 0;
			}

			lastFrameSec = time;
			frames++;
			ageFrames++;
			worstFPS = Math.min(worstFPS, 100 / Panel.frameFrequency);
			Game.screen.screenAge += Panel.frameFrequency;
		}

        if (onlinePaused)
            this.onlineOverlay.draw();
        else
            Game.screen.draw();

		Chunk.drawDebugStuff();

		if (Game.enableExtensions)
            for (Extension e : Game.extensionRegistry.extensions)
                e.draw();

		if (!(Game.screen instanceof ScreenExit || Game.screen instanceof ScreenIntro))
			this.drawBar();

        if (!notifs.isEmpty())
		{
			double sy = 0;
			for (int i = 0; i < notifs.size(); i++)
			{
				if (i == 1 && notifs.get(0).fadeStart)
					sy -= Math.min(750, System.currentTimeMillis() - lastNotifTime) * (notifs.get(0).sY + 100) / 750;

				Notification n = notifs.get(i);
				n.draw(sy);
				sy += n.sY + 15;
			}

			if (notifs.get(0).age > notifs.get(0).duration)
                notifs.pop();
		}

		if (currentMessage != null)
			currentMessage.draw();

        ScreenOverlayChat.draw(!(Game.screen instanceof IHiddenChatboxScreen));

		if (Game.screen.showDefaultMouse)
			this.drawMouseTarget();

		if (Game.framework == Game.Framework.libgdx)
		{
			Drawing.drawing.setColor(0, 0, 0, 0);
			Drawing.drawing.fillInterfaceRect(0, 0, 0, 0);
		}

		Drawing.drawing.setColor(255, 255, 255);
		Game.screen.drawPostMouse();

		if (!Game.game.window.drawingShadow && (Game.screen instanceof ScreenGame && !(((ScreenGame) Game.screen).paused && !ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)))
			this.age += Panel.frameFrequency;

		if (Game.game.window.pressedKeys.contains(InputCodes.KEY_F3))
		{
			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_F8))
			{
				Game.recordMode = !Game.recordMode;
				Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_F8);
			}

			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_P))
			{
				Panel.pauseOnDefocus = !Panel.pauseOnDefocus;
				notifs.add(new Notification("Pause on lost focus: \u00a7255200000255"
						+ (Panel.pauseOnDefocus ? "enabled" : "disabled")).setColor(255, 255, 128));
				Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_P);
			}

			if (Game.debug && Game.game.window.shift && Game.game.window.pressedKeys.contains(InputCodes.KEY_S))
			{
				System.out.println(Game.screen.getClass().getSimpleName());
				Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_S);
			}

			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_B))
			{
				Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_B);
				Game.showHitboxes = !Game.showHitboxes;
				notifs.add(new Notification("Collision boxes: \u00a7255200000255"
						+ (Game.showHitboxes ? "shown" : "hidden"), 200).setColor(255, 255, 128));
			}

			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_G))
			{
				Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_G);
				Chunk.debug = !Chunk.debug;
				notifs.add(new Notification("Chunk borders: \u00a7255200000255"
						+ (Chunk.debug ? "shown" : "hidden"), 200).setColor(255, 255, 128));
			}

			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_D))
			{
				ArrayList<ChatMessage> chat = null;
				Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_D);

				if (ScreenPartyLobby.isClient)
                    chat = ScreenPartyLobby.chat;
				else if (ScreenPartyHost.isServer)
                    chat = ScreenPartyHost.chat;

				if (chat != null)
				{
					synchronized (chat)
					{
						chat.clear();
					}
					notifs.add(new Notification("Chat cleared", 200).setColor(255, 255, 128));
				}
			}

			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_A))
			{
				Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_A);
				Drawing.drawing.terrainRenderer.reset();
				notifs.add(new Notification("Terrain reloaded!").setColor(255, 255, 128));
			}

			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_T))
			{
				Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_T);

				HashMap<Class<? extends ShaderGroup>, ShaderGroup> newShaders = new HashMap<>();
				for (Map.Entry<Class<? extends ShaderGroup>, ShaderGroup> entry : Game.game.shaderInstances.entrySet())
				{
					try
					{
						ShaderGroup s;
						try
						{
							s = entry.getKey().getConstructor(BaseWindow.class)
									.newInstance(Game.game.window);
						}
						catch (NoSuchMethodException e)
						{
							s = entry.getKey().getConstructor(BaseWindow.class, String.class)
									.newInstance(Game.game.window, entry.getValue().name);
						}

						s.initialize();
						newShaders.put(entry.getKey(), s);
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				}

				Game.game.shaderInstances = newShaders;
				Drawing.drawing.terrainRenderer.reset();
				notifs.add(new Notification("Shaders reloaded! (Remember to Cmd+F9)").setColor(255, 255, 128));
			}

			int brightness = 0;
			if (Game.currentLevel != null && Level.isDark())
				brightness = 255;

			Drawing.drawing.setColor(brightness, brightness, brightness);
			Drawing.drawing.setInterfaceFontSize(16);

			double mx = Game.game.window.absoluteMouseX, my = Game.game.window.absoluteMouseY;

			String text;
			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_P))
				text = "(" + (int) Game.game.window.absoluteWidth + ", " + (int) Game.game.window.absoluteHeight + ")";

			else if (Game.game.window.pressedKeys.contains(InputCodes.KEY_S))
				text = "(" + (int) mx + ", " + (int) my + ")  " + Drawing.drawing.interfaceScale + ", " + Drawing.drawing.interfaceScaleZoom;

			else {
				int posX = (int) (((Math.round(Drawing.drawing.getMouseX() / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2) - 25) / 50);
				int posY = (int) (((Math.round(Drawing.drawing.getMouseY() / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2) - 25) / 50);

				if (Game.screen instanceof ScreenLevelEditor) {
					posX = (int) (((ScreenLevelEditor) Game.screen).mouseObstacle.posX / Game.tile_size - 0.5);
					posY = (int) (((ScreenLevelEditor) Game.screen).mouseObstacle.posY / Game.tile_size - 0.5);
				}

				text = "(" + posX + ", " + posY + ")";

				if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
				{
					Chunk.Tile t1 = Chunk.getTile(posX, posY);

					if (Game.glowEnabled && Level.isDark() && t1.obstacle != null)
					{
						Obstacle o = t1.obstacle;
						if ((o.colorR + o.colorG + o.colorB + o.colorA / 2) / 4 > 200)
                            Drawing.drawing.setColor(0, 0, 0, 128);
					}

					if (t1 != null)
                        text += " O: " + (t1.obstacle != null ? t1.obstacle.name : "none") + " SO: " + (t1.surfaceObstacle != null ? t1.surfaceObstacle.name : "none");
					Game.game.window.fontRenderer.drawString(mx + 10, my + 30, Drawing.drawing.fontSize, Drawing.drawing.fontSize, "H: " + (int) t1.height + " GH: " + (int) t1.groundHeight);
				}
			}

			Game.game.window.fontRenderer.drawString(mx + 10, my + 10, Drawing.drawing.fontSize, Drawing.drawing.fontSize, text);
		}
	}

	public void drawMouseTarget()
	{
		drawMouseTarget(false);
	}

	public void drawMouseTarget(boolean force)
	{
		if (Game.game.window.touchscreen)
			return;

		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

		if (showMouseTarget || force)
		{
			if (Level.isDark())
			{
				if (Game.glowEnabled)
				{
					Drawing.drawing.setColor(0, 0, 0, 128);
					Drawing.drawing.fillInterfaceGlow(mx, my, 64, 64, true);
				}

				Drawing.drawing.setColor(255, 255, 255);
			}
			else
			{
				if (Game.glowEnabled)
				{
					double v = 1;
					if (Game.screen instanceof ScreenIntro)
						v = Obstacle.draw_size;

					if (v > 0.05)
					{
						Drawing.drawing.setColor(255, 255, 255, 128);
						Drawing.drawing.fillInterfaceGlow(mx, my, 64 * v, 64 * v);
					}
				}

				Drawing.drawing.setColor(0, 0, 0);
			}

			Drawing.drawing.drawInterfaceImage("cursor.png", mx, my, 48, 48);
		}

		if (Game.enable3d && ((Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).paused && !((ScreenGame) Game.screen).shopScreen && Game.playerTank != null) || Game.screen instanceof ScreenLevelEditor) && Panel.showMouseTargetHeight)
		{
			double c = 127 * Obstacle.draw_size / Game.tile_size;

            double a = 255;

			double r2 = 0;
			double g2 = 0;
			double b2 = 0;
			double a2 = 0;

			Drawing.drawing.setColor(c, c, c, a, 1);
			Game.game.window.shapeRenderer.setBatchMode(true, false, true, true, false);

			double size = 12 * Drawing.drawing.interfaceScale / Drawing.drawing.scale;
			double height = 100;
			double thickness = 2;

			double x = Drawing.drawing.toGameCoordsX(mx);
			double y = Drawing.drawing.toGameCoordsY(my);

			Game.game.window.shapeRenderer.setBatchMode(false, false, true, true, false);
			Game.game.window.shapeRenderer.setBatchMode(true, true, true, true, false);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - size, y - thickness, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x, y - thickness, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + size, y - thickness, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x, y - thickness, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - size, y + thickness, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x, y - thickness, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + size, y + thickness, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x, y + thickness, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - size, y - thickness, 0);
			Drawing.drawing.addVertex(x - size, y + thickness, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + size, y - thickness, 0);
			Drawing.drawing.addVertex(x + size, y + thickness, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - thickness, y - size, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x - thickness, y, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - thickness, y + size, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x - thickness, y, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + thickness, y - size, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x + thickness, y, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + thickness, y + size, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x + thickness, y, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - thickness, y - size, 0);
			Drawing.drawing.addVertex(x + thickness, y - size, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - thickness, y + size, 0);
			Drawing.drawing.addVertex(x + thickness, y + size, 0);
			Drawing.drawing.setColor(c, c, c, a, 1);
			Drawing.drawing.addVertex(x, y, height);

			double res = 40;
			double height2 = height * 0.75;
			for (int i = 0; i < res; i++)
			{
				Drawing.drawing.setColor(c, c, c, a, 1);
				double x1 = Math.cos(i / res * Math.PI * 2) * size;
				double x2 = Math.cos((i + 1) / res * Math.PI * 2) * size;
				double y1 = Math.sin(i / res * Math.PI * 2) * size;
				double y2 = Math.sin((i + 1) / res * Math.PI * 2) * size;

				Drawing.drawing.addVertex(x + x1, y + y1, 0);
				Drawing.drawing.addVertex(x + x2, y + y2, 0);

				Drawing.drawing.setColor(r2, g2, b2, a2, 1);
				Drawing.drawing.addVertex(x + x2, y + y2, height2);
				Drawing.drawing.addVertex(x + x1, y + y1, height2);
			}

			Game.game.window.shapeRenderer.setBatchMode(false, true, false);
		}
	}

	public void drawBar()
	{
		drawBar(0);
	}

	public void drawBar(double offset)
	{
		if (!Drawing.drawing.enableStats || Game.recordMode)
			return;

		Drawing.drawing.setColor(87, 46, 8);
		Game.game.window.shapeRenderer.fillRect(0, offset + (int) (Panel.windowHeight - 40), (int) (Panel.windowWidth), 40);

		Drawing.drawing.setColor(255, 227, 186);

		Drawing.drawing.setInterfaceFontSize(12);

		double boundary = Game.game.window.getEdgeBounds();

		if (Game.framework == Game.Framework.libgdx)
			boundary += 40;

		Game.game.window.fontRenderer.drawString(boundary + 10, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, Game.version);
		Game.game.window.fontRenderer.drawString(boundary + 10, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "FPS: " + lastFPS + "§255227186032/" + lastWorstFPS + "§255227186255");

		Game.game.window.fontRenderer.drawString(boundary + 600, offset + (int) (Panel.windowHeight - 40 + 10), 0.6, 0.6, Game.screen.screenHint);

		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - free;

		Game.game.window.fontRenderer.drawString(boundary + 150, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, ModAPI.version);
		Game.game.window.fontRenderer.drawString(boundary + 150, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "Memory used: " + used / 1048576 + "/" + total / 1048576 + "MB");

		double partyIpLen = 10;
		if (ScreenPartyLobby.isClient && !Game.connectedToOnline)
		{
			String s = "Connected to party";

			if (Game.showIP)
				s = "In party: " + (Game.lastParty.isEmpty() ? "localhost" : Game.lastParty) + (!Game.lastParty.contains(":") ? ":" + Game.port : "");

			partyIpLen = Game.game.window.fontRenderer.getStringSizeX(0.4, s) + 10 + offset;
			Game.game.window.fontRenderer.drawString(Panel.windowWidth - partyIpLen, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, s);
			partyIpLen += 50;

			s = "Latency: " + Client.handler.lastLatency + "ms";
			double[] col = getLatencyColor(Client.handler.lastLatency);
			Drawing.drawing.setColor(col[0], col[1], col[2]);
			Game.game.window.fontRenderer.drawString(Panel.windowWidth - Game.game.window.fontRenderer.getStringSizeX(0.4, s) - 10 - offset, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, s);
		}

		if (ScreenPartyLobby.isClient || ScreenPartyHost.isServer)
		{
			Drawing.drawing.setColor(255, 227, 186);

			String s = "Upstream: " + MessageReader.upstreamBytesPerSec / 1024 + "KB/s";
			Game.game.window.fontRenderer.drawString(Panel.windowWidth - partyIpLen - Game.game.window.fontRenderer.getStringSizeX(0.4, s) - offset, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, s);

			s = "Downstream: " + MessageReader.downstreamBytesPerSec / 1024 + "KB/s";
			Game.game.window.fontRenderer.drawString(Panel.windowWidth - partyIpLen - Game.game.window.fontRenderer.getStringSizeX(0.4, s) - offset, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, s);
		}
	}

	public double[] getLatencyColor(long l)
	{
		double[] col = new double[3];

		if (l <= 40)
		{
			col[1] = 255;
			col[2] = 255 * (1 - l / 40.0);
		}
		else if (l <= 80)
		{
			col[0] = 255 * ((l - 40) / 40.0);
			col[1] = 255;
		}
        else if (l <= 160)
        {
            col[0] = 255;
            col[1] = 255 * (1 - (l - 80) / 80.0);
        }
        else
            col[0] = 255;

        return col;
    }
}