package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.ModAPI;
import tanks.Panel;
import tanks.gui.Button;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;
import tanks.translation.Translation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ScreenOptions extends ScreenOptionsOverlay
{
    public static final String onText = "\u00A7000200000255on";
    public static final String offText = "\u00A7200000000255off";
    public static ArrayList<String> extraOptions = new ArrayList<>();

    TankPlayer preview = new TankPlayer(0, 0, 0);
	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Back", () ->
    {
        saveOptions(Game.homedir);

        if (game != null)
        {
            game.screenshotMode = false;
            Game.screen = game;
        }
        else
            Game.screen = new ScreenTitle();
    }
	);

    public ScreenOptions()
    {
        if (!Game.game.window.soundsEnabled)
			soundOptions.enabled = false;
	}


	Button multiplayerOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Multiplayer options", () -> Game.screen = new ScreenOptionsMultiplayer()
	);

	Button miscOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Miscellaneous options", () -> Game.screen = new ScreenOptionsMisc()
	);

	Button graphicsOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Graphics options", () -> Game.screen = new ScreenOptionsGraphics()
	);

	Button soundOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Sound options", () -> Game.screen = new ScreenOptionsSound()
	);

	Button inputOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Input options", () ->
	{
		if (Game.game.window.touchscreen)
			Game.screen = new ScreenOptionsInputTouchscreen();
		else
			Game.screen = ScreenOverlayControls.lastControlsScreen;
	});

    Button personalize = new Button(this.centerX, this.centerY - this.objYSpace * 2.4, this.objWidth * 1.5, this.objHeight * 2, "", () ->
    {
        if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
            Game.screen = new ScreenOptionsPlayerColor();
        else
            Game.screen = new ScreenOptionsPersonalize();
    });

	Button windowOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Window options", () -> Game.screen = new ScreenOptionsWindow());
	Button interfaceOptionsMobile = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 0, this.objWidth, this.objHeight, "Interface options", () -> Game.screen = new ScreenOptionsWindowMobile());

	public static void saveOptions(String homedir)
    {
        String path = homedir + Game.optionsPath;

        try
        {
            boolean fullscreen = Game.game.fullscreen;

            if (Game.game.window != null)
                fullscreen = Game.game.window.fullscreen;

			BaseFile f = Game.game.fileManager.getFile(path);
			f.startWriting();
			f.println("# This file stores game settings that you have set");
			f.println("username=" + Game.player.username);
			f.println("fancy_terrain=" + Game.fancyTerrain);
			f.println("effects=" + Game.effectsEnabled);
			f.println("effect_multiplier=" + (int) Math.round(Game.effectMultiplier * 100));
			f.println("bullet_trails=" + Game.bulletTrails);
			f.println("fancy_bullet_trails=" + Game.fancyBulletTrails);
			f.println("glow=" + Game.glowEnabled);
			f.println("3d=" + Game.enable3d);
			f.println("3d_ground=" + Game.enable3dBg);
			f.println("shadows_enabled=" + Game.shadowsEnabled);
			f.println("shadow_quality=" + Game.shadowQuality);
			f.println("vsync=" + Game.vsync);
			f.println("max_fps=" + Game.maxFPS);
			f.println("antialiasing=" + Game.antialiasing);
			f.println("perspective=" + ScreenOptionsGraphics.viewNum);
			f.println("preview_crusades=" + Game.previewCrusades);
			f.println("tank_textures=" + Game.tankTextures);
			f.println("xray_bullets=" + Game.xrayBullets);
			f.println("mouse_target=" + Panel.showMouseTarget);
			f.println("mouse_target_height=" + Panel.showMouseTargetHeight);
			f.println("constrain_mouse=" + Game.constrainMouse);
			f.println("fullscreen=" + fullscreen);
			f.println("vibrations=" + Game.enableVibrations);
			f.println("mobile_joystick=" + TankPlayer.controlStickMobile);
			f.println("snap_joystick=" + TankPlayer.controlStickSnap);
			f.println("dual_joystick=" + TankPlayer.shootStickEnabled);
			f.println("sound=" + Game.soundsEnabled);
			f.println("sound_volume=" + Game.soundVolume);
			f.println("music=" + Game.musicEnabled);
			f.println("music_volume=" + Game.musicVolume);
			f.println("layered_music=" + Game.enableLayeredMusic);
			f.println("auto_start=" + Game.autostart);
			f.println("full_stats=" + Game.fullStats);
			f.println("timer=" + Game.showSpeedrunTimer);
			f.println("deterministic=" + Game.deterministicMode);
			f.println("deterministic_30fps=" + Game.deterministic30Fps);
			f.println("warn_before_closing=" + Game.warnBeforeClosing);
			f.println("info_bar=" + Drawing.drawing.enableStats);
			f.println("port=" + Game.port);
			f.println("last_party=" + Game.lastParty);
			f.println("last_online_server=" + Game.lastOnlineServer);
			f.println("show_ip=" + Game.showIP);
			f.println("chat_filter=" + Game.enableChatFilter);
			f.println("auto_ready=" + Game.autoReady);
			f.println("anticheat=" + TankPlayerRemote.checkMotion);
			f.println("anticheat_weak=" + TankPlayerRemote.weakTimeCheck);
			f.println("disable_party_friendly_fire=" + Game.disablePartyFriendlyFire);
			f.println("party_countdown=" + Game.partyStartTime);
			f.println("tank_secondary_color=" + Game.player.enableSecondaryColor);
			f.println("tank_red=" + Game.player.colorR);
			f.println("tank_green=" + Game.player.colorG);
			f.println("tank_blue=" + Game.player.colorB);
			f.println("tank_red_2=" + Game.player.turretColorR);
			f.println("tank_green_2=" + Game.player.turretColorG);
			f.println("tank_blue_2=" + Game.player.turretColorB);
			f.println("translation=" + (Translation.currentTranslation == null ? "null" : Translation.currentTranslation.fileName));
			f.println("last_version=" + Game.lastVersion);
			f.println("enable_extensions=" + Game.enableExtensions);
			f.println("auto_load_extensions=" + Game.autoLoadExtensions);
			f.stopWriting();
		}
		catch (FileNotFoundException e)
		{
			Game.exitToCrash(e);
		}
    }

    public static void loadOptions(String homedir)
    {
        String path = homedir + Game.optionsPath;

        try
        {
            BaseFile f = Game.game.fileManager.getFile(path);
            f.startReading();
			while (f.hasNextLine())
			{
				String line = f.nextLine();
				String[] optionLine = line.split("=");

				if (optionLine[0].charAt(0) == '#')
					continue;

                switch (optionLine[0].toLowerCase())
                {
                    case "username" ->
                    {
                        if (optionLine.length >= 2)
                            Game.player.username = optionLine[1];
                        else
                            Game.player.username = "";
                    }
                    case "fancy_terrain" -> Game.fancyTerrain = Boolean.parseBoolean(optionLine[1]);
                    case "effects" -> Game.effectsEnabled = Boolean.parseBoolean(optionLine[1]);
                    case "effect_multiplier" -> Game.effectMultiplier = Integer.parseInt(optionLine[1]) / 100.0;
                    case "bullet_trails" -> Game.bulletTrails = Boolean.parseBoolean(optionLine[1]);
                    case "fancy_bullet_trails" -> Game.fancyBulletTrails = Boolean.parseBoolean(optionLine[1]);
                    case "glow" -> Game.glowEnabled = Boolean.parseBoolean(optionLine[1]);
                    case "3d" -> Game.enable3d = Boolean.parseBoolean(optionLine[1]);
                    case "3d_ground" -> Game.enable3dBg = Boolean.parseBoolean(optionLine[1]);
                    case "shadows_enabled" -> Game.shadowsEnabled = Boolean.parseBoolean(optionLine[1]);
                    case "shadow_quality" -> Game.shadowQuality = Integer.parseInt(optionLine[1]);
                    case "vsync" -> Game.vsync = Boolean.parseBoolean(optionLine[1]);
                    case "max_fps" -> Game.maxFPS = Integer.parseInt(optionLine[1]);
                    case "antialiasing" -> Game.antialiasing = Boolean.parseBoolean(optionLine[1]);
                    case "mouse_target" -> Panel.showMouseTarget = Boolean.parseBoolean(optionLine[1]);
                    case "mouse_target_height" -> Panel.showMouseTargetHeight = Boolean.parseBoolean(optionLine[1]);
                    case "pause_on_lost_focus" -> Panel.pauseOnDefocus = Boolean.parseBoolean(optionLine[1]);
                    case "constrain_mouse" -> Game.constrainMouse = Boolean.parseBoolean(optionLine[1]);
                    case "enable_vibrations" -> Game.enableVibrations = Boolean.parseBoolean(optionLine[1]);
                    case "mobile_joystick" -> TankPlayer.controlStickMobile = Boolean.parseBoolean(optionLine[1]);
                    case "snap_joystick" -> TankPlayer.controlStickSnap = Boolean.parseBoolean(optionLine[1]);
                    case "dual_joystick" -> TankPlayer.setShootStick(Boolean.parseBoolean(optionLine[1]));
                    case "sound" -> Game.soundsEnabled = Boolean.parseBoolean(optionLine[1]);
                    case "music" -> Game.musicEnabled = Boolean.parseBoolean(optionLine[1]);
                    case "sound_volume" -> Game.soundVolume = Float.parseFloat(optionLine[1]);
                    case "music_volume" -> Game.musicVolume = Float.parseFloat(optionLine[1]);
                    case "auto_start" -> Game.autostart = Boolean.parseBoolean(optionLine[1]);
                    case "full_stats" -> Game.fullStats = Boolean.parseBoolean(optionLine[1]);
                    case "timer" -> Game.showSpeedrunTimer = Boolean.parseBoolean(optionLine[1]);
                    case "deterministic" -> Game.deterministicMode = Boolean.parseBoolean(optionLine[1]);
                    case "info_bar" -> Drawing.drawing.showStats(Boolean.parseBoolean(optionLine[1]));
                    case "warn_before_closing" -> Game.warnBeforeClosing = Boolean.parseBoolean(optionLine[1]);
                    case "perspective" ->
                    {
                        ScreenOptionsGraphics.viewNum = Integer.parseInt(optionLine[1]);
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
                        }
                    }
                    case "bullet_indicator" -> Game.xrayBullets = Boolean.parseBoolean(optionLine[1]);
                    case "tank_textures" -> Game.tankTextures = Boolean.parseBoolean(optionLine[1]);
                    case "preview_crusades" -> Game.previewCrusades = Boolean.parseBoolean(optionLine[1]);
                    case "fullscreen" -> Game.game.fullscreen = Boolean.parseBoolean(optionLine[1]);
                    case "port" -> Game.port = Integer.parseInt(optionLine[1]);
                    case "last_party" ->
                    {
                        if (optionLine.length >= 2)
                            Game.lastParty = optionLine[1];
                    }
                    case "layered_music" -> Game.enableLayeredMusic = Boolean.parseBoolean(optionLine[1]);
                    case "last_online_server" ->
                    {
                        if (optionLine.length >= 2)
                            Game.lastOnlineServer = optionLine[1];
                    }
                    case "show_ip" -> Game.showIP = Boolean.parseBoolean(optionLine[1]);
                    case "chat_filter" -> Game.enableChatFilter = Boolean.parseBoolean(optionLine[1]);
                    case "auto_ready" -> Game.autoReady = Boolean.parseBoolean(optionLine[1]);
                    case "tps" -> Tank.updatesPerSecond = Integer.parseInt(optionLine[1]);
                    case "anticheat" -> TankPlayerRemote.checkMotion = Boolean.parseBoolean(optionLine[1]);
                    case "anticheat_weak" -> TankPlayerRemote.weakTimeCheck = Boolean.parseBoolean(optionLine[1]);
                    case "disable_party_friendly_fire" ->
                            Game.disablePartyFriendlyFire = Boolean.parseBoolean(optionLine[1]);
                    case "party_countdown" -> Game.partyStartTime = Double.parseDouble(optionLine[1]);
                    case "tank_secondary_color" ->
                            Game.player.enableSecondaryColor = Boolean.parseBoolean(optionLine[1]);
                    case "tank_red" -> Game.player.colorR = Integer.parseInt(optionLine[1]);
                    case "tank_green" -> Game.player.colorG = Integer.parseInt(optionLine[1]);
                    case "tank_blue" -> Game.player.colorB = Integer.parseInt(optionLine[1]);
                    case "tank_red_2" -> Game.player.turretColorR = Integer.parseInt(optionLine[1]);
                    case "tank_green_2" -> Game.player.turretColorG = Integer.parseInt(optionLine[1]);
                    case "tank_blue_2" -> Game.player.turretColorB = Integer.parseInt(optionLine[1]);
                    case "chroma" -> Game.player.chromaaa = Boolean.parseBoolean(optionLine[1]);
                    case "translation" -> Translation.setCurrentTranslation(optionLine[1]);
                    case "last_version" -> Game.lastVersion = optionLine[1];
                    case "enable_extensions" -> Game.enableExtensions = Boolean.parseBoolean(optionLine[1]);
                    case "auto_load_extensions" -> Game.autoLoadExtensions = Boolean.parseBoolean(optionLine[1]);
                    case "modapi_auto_load" -> ModAPI.autoLoadExtensions = Boolean.parseBoolean(optionLine[1]);
                    default -> extraOptions.add(line);
                }
			}
			f.stopReading();

			if (Game.framework == Game.Framework.libgdx)
				Panel.showMouseTarget = false;

			if (!Game.soundsEnabled)
				Game.soundVolume = 0;

			if (!Game.musicEnabled)
				Game.musicVolume = 0;

			if (TankPlayerRemote.weakTimeCheck)
				TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatStrongTimeOffset;
			else
				TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatWeakTimeOffset;
		}
		catch (Exception e)
		{
			Game.logger.println (new Date() + " (syswarn) options file is nonexistent or broken, using default:");
			e.printStackTrace(Game.logger);
		}
	}

	@Override
	public void update()
    {
        soundOptions.update();
        miscOptions.update();

		if (Game.framework == Game.Framework.libgdx)
			interfaceOptionsMobile.update();
		else
			windowOptions.update();

        graphicsOptions.update();
        inputOptions.update();
        multiplayerOptions.update();
        personalize.update();

        back.update();

		super.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		back.draw();
		multiplayerOptions.draw();
		inputOptions.draw();
		graphicsOptions.draw();

		if (Game.framework == Game.Framework.libgdx)
			interfaceOptionsMobile.draw();
		else
			windowOptions.draw();

		miscOptions.draw();
		soundOptions.draw();
		personalize.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(brightness, brightness, brightness);

		if (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, Game.player.username) / Drawing.drawing.interfaceScale > personalize.sizeX - 240)
			Drawing.drawing.setInterfaceFontSize(this.titleSize * (personalize.sizeX - 240) / (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, Game.player.username) / Drawing.drawing.interfaceScale));

		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4, "Options");

		if (Game.player.colorR + Game.player.colorG + Game.player.colorB >= 220 * 3 && !Game.player.username.isEmpty())
		{
			Drawing.drawing.setColor(200, 200, 200);
			double s = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, Game.player.username) / Drawing.drawing.interfaceScale;
			Drawing.drawing.fillInterfaceRect(personalize.posX, personalize.posY + personalize.sizeY * 0.1, s, 40);
			Drawing.drawing.fillInterfaceOval(personalize.posX - (s) / 2, personalize.posY + personalize.sizeY * 0.1, 40, 40);
			Drawing.drawing.fillInterfaceOval(personalize.posX + (s) / 2, personalize.posY + personalize.sizeY * 0.1, 40, 40);
		}

		preview.drawForInterface(personalize.posX - personalize.sizeX / 2 + personalize.sizeY * 0.7, personalize.posY, 1);

		Drawing.drawing.setColor(Game.player.turretColorR, Game.player.turretColorG, Game.player.turretColorB);
		Drawing.drawing.drawInterfaceText(personalize.posX + 2, personalize.posY + personalize.sizeY * 0.1 + 2, Game.player.username);
		Drawing.drawing.setColor(Game.player.colorR, Game.player.colorG, Game.player.colorB);
		Drawing.drawing.drawInterfaceText(personalize.posX, personalize.posY + personalize.sizeY * 0.1, Game.player.username);

		if (Game.player.username.isEmpty())
		{
			Drawing.drawing.setColor(127, 127, 127);
			Drawing.drawing.displayInterfaceText(personalize.posX, personalize.posY + personalize.sizeY * 0.1, "Pick a username...");
		}

		Drawing.drawing.setInterfaceFontSize(this.titleSize * 0.65);
		Drawing.drawing.setColor(80, 80, 80);
		Drawing.drawing.displayInterfaceText(personalize.posX, personalize.posY - personalize.sizeY * 0.3, "My profile");
	}

	public static void initOptions(String homedir)
	{
		String path = homedir + Game.optionsPath;

		try
		{
			Game.game.fileManager.getFile(path).create();
		}
		catch (IOException e)
		{
			Game.logger.println (new Date() + " (syserr) file permissions are broken! cannot initialize options file.");
			System.exit(1);
		}

		saveOptions(homedir);
	}

	public static void saveExtensions()
	{
		ScreenOptionsExtensions.modified = false;
		ArrayList<String> arr = new ArrayList<>();
		for (int i = 0; i < ScreenOptionsExtensions.extensionNames.size(); i++)
		{
			if (ScreenOptionsExtensions.selectedExtensions[i])
				arr.add(ScreenOptionsExtensions.extensionNames.get(i));
		}

		Game.extensionRegistry.saveRegistry(arr);
	}
}
