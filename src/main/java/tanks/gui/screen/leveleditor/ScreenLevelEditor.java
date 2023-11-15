package tanks.gui.screen.leveleditor;

import basewindow.BaseFile;
import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.editorselector.LevelEditorSelector;
import tanks.editorselector.StackHeightSelector;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.*;
import tanks.hotbar.item.Item;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleUnknown;
import tanks.registry.RegistryObstacle;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankPlayer;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unchecked", "unused"})
public class ScreenLevelEditor extends Screen implements ILevelPreviewScreen
{
	public static HashMap<String, LevelEditorSelector<?>> selectors = new HashMap<>();
	public static Placeable currentPlaceable = Placeable.enemyTank;
	public static Clipboard[] clipboards = new Clipboard[5];
	public static Clipboard clipboard = new Clipboard();
	public static int selectedNum = 0;

	public ArrayList<Action> undoActions = new ArrayList<>();
	public ArrayList<Action> redoActions = new ArrayList<>();
	public int redoLength = -1;

	public int tankPage = 0;
	public int obstaclePage = 0;

	public int tankNum = 0;
	public int obstacleNum = 0;
	public Tank mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
	public Obstacle mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
	public Obstacle hoverObstacle = null;
	public EditorButtons buttons = new EditorButtons(this);
	public boolean stagger = false;
	public boolean oddStagger = false;
	public boolean paused = false;
	public boolean objectMenu = false;
	public boolean modified = false;

	public double clickCooldown = 0;
	public ArrayList<Team> teams = new ArrayList<>();
	public ArrayList<TankSpawnMarker> spawns = new ArrayList<>();

	public Level level;
	public String name;
	public boolean movePlayer = true;
	public boolean eraseMode = false;
	public boolean changeCameraMode = false;
	public boolean selectMode = false;
	public boolean pasteMode = false;
	public boolean symmetrySelectMode = false;

	public double selectX1;
	public double selectY1;
	public double selectX2;
	public double selectY2;
	public HashMap<String, EditorButton> addedShortcutButtons = new HashMap<>();
	public boolean initialized = false;

	public SymmetryType symmetryType = SymmetryType.none;

	public double symmetryX1;
	public double symmetryY1;
	public double symmetryX2;
	public double symmetryY2;

	public boolean selectHeld = false;
	public boolean selectInverted = false;
	public boolean selection = false;
	public boolean selectAdd = true;
	public boolean selectSquare = false;
	public boolean[][] selectedTiles;
	public boolean showControls = true;
	public double controlsSizeMultiplier = 0.75;

	public boolean panDown;
	public double panX;
	public double panY;
	public double panCurrentX;
	public double panCurrentY;
	public double zoomCurrentX;
	public double zoomCurrentY;
	public boolean zoomDown;
	public double zoomDist;
	public double offsetX;
	public double offsetY;
	public double zoom = 1;
	public int validZoomFingers = 0;

	public HashSet<String> prevTankMusics = new HashSet<>();
	public HashSet<String> tankMusics = new HashSet<>();
	public double mouseObstacleStartHeight;

	EditorButton pause = new EditorButton(buttons.topRight, "pause.png", 40, 40, () ->
	{
		paused = true;
		Game.screen = new OverlayEditorMenu(Game.screen, (ScreenLevelEditor) Game.screen);
	}, "Pause (%s)", Game.game.input.editorPause
	);
	public HashMap<Float, Obstacle>[][] grid = new HashMap[Game.currentSizeX][Game.currentSizeY];

	public double fontBrightness = 0;
	EditorButton menu = new EditorButton(buttons.topRight, "menu.png", 50, 50, () ->
	{
		this.paused = true;
		this.objectMenu = true;
		Game.screen = new OverlayObjectMenu(Game.screen, this);
	},
			() -> false, () -> this.level.editable && (!paused || objectMenu),
			"Object menu (%s)", Game.game.input.editorObjectMenu
	);
	EditorButton playControl = new EditorButton(buttons.topRight, "play.png", 30, 30,
			this::play, () -> this.spawns.isEmpty(), "Play (%s)", Game.game.input.editorPlay);

	public Button recenter = new Button(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 1.5, 300, 35, "Re-center", new Runnable()
	{
		@Override
		public void run()
		{
			zoom = 1;
			offsetX = 0;
			offsetY = 0;
		}
	}
	);
	EditorButton place = new EditorButton(buttons.topLeft, "pencil.png", 40, 40, () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectMode = changeCameraMode = eraseMode = pasteMode = false;
	},
			() -> !selectMode && !changeCameraMode && !eraseMode && !pasteMode,
			"Build (%s)", Game.game.input.editorBuild
	);
	EditorButton erase = new EditorButton(buttons.topLeft, "eraser.png", 40, 40, () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectMode = changeCameraMode = pasteMode = false;
		eraseMode = true;
	},
			() -> eraseMode, "Erase (%s)", Game.game.input.editorErase
	);
	EditorButton panZoom = new EditorButton(buttons.topLeft, "zoom_pan.png", 40, 40, () ->
			changeCameraMode = !changeCameraMode,
			() -> changeCameraMode, "Adjust camera (%s)", Game.game.input.editorCamera
	);
	EditorButton select = new EditorButton(buttons.topLeft, "select.png", 40, 40, () ->
	{
		if (this.changeCameraMode || this.pasteMode)
			this.selectMode = true;
		else
			this.selectMode = !this.selectMode;

		this.changeCameraMode = false;
		this.pasteMode = false;
	}, () -> selectMode, "Select (%s)", Game.game.input.editorSelect
	);
	EditorButton undo = new EditorButton(buttons.bottomLeft, "undo.png", 40, 40, () ->
	{
		if (undoActions.isEmpty())
			return;

		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		Action a = undoActions.remove(undoActions.size() - 1);
		a.undo();
		redoActions.add(a);
		redoLength = undoActions.size();
	}, () -> undoActions.isEmpty(), "Undo (%s)", Game.game.input.editorUndo
	);
	EditorButton redo = new EditorButton(buttons.bottomLeft, "redo.png", 40, 40, () ->
	{
		if (redoActions.isEmpty())
			return;

		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		Action a = redoActions.remove(redoActions.size() - 1);
		a.redo();
		undoActions.add(a);
		redoLength = undoActions.size();
	}, () -> redoActions.isEmpty(), "Redo (%s)", Game.game.input.editorRedo
	);
	EditorButton copy = new EditorButton(buttons.topRight, "copy.png", 50, 50, () -> this.copy(false),
			() -> false, () -> selection, "Copy (%s)", Game.game.input.editorCopy);
	EditorButton cut = new EditorButton(buttons.topRight, "cut.png", 50, 50, () -> this.copy(true),
			() -> false, () -> selection, "Cut (%s)", Game.game.input.editorCut);
	EditorButton paste = new EditorButton(buttons.topLeft, "paste.png", 50, 50, () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectMode = false;
		changeCameraMode = false;
		eraseMode = false;
		pasteMode = true;
	}, () -> pasteMode, "Paste (%s)", Game.game.input.editorPaste
	);

	EditorButton flipHoriz = new EditorButton(buttons.topRight, "flip_horizontal.png", 50, 50, () -> clipboard.flipHorizontal(),
			() -> false, () -> pasteMode, "Flip horizontal (%s)", Game.game.input.editorFlipHoriz);
	EditorButton flipVert = new EditorButton(buttons.topRight, "flip_vertical.png", 50, 50, () -> clipboard.flipVertical(),
			() -> false, () -> pasteMode, "Flip vertical (%s)", Game.game.input.editorFlipVert);
	EditorButton rotate = new EditorButton(buttons.topRight, "rotate_obstacle.png", 50, 50, () -> clipboard.rotate(),
			() -> false, () -> pasteMode, "Rotate clockwise (%s)", Game.game.input.editorRotateClockwise);

	@Override
	public void update()
	{
 		if (!initialized)
			initialize();

		if (Game.game.input.editorCopy.isPressed())
		{
			if (Game.game.input.hotbar1.isValid())
			{
				Game.game.input.hotbar1.invalidate();
				selectedNum = 0;
				clipboard = clipboards[0];
			}
			else if (Game.game.input.hotbar2.isValid())
			{
				Game.game.input.hotbar2.invalidate();
				selectedNum = 1;
				clipboard = clipboards[1];
			}
			else if (Game.game.input.hotbar3.isValid())
			{
				Game.game.input.hotbar3.invalidate();
				selectedNum = 2;
				clipboard = clipboards[2];
			}
			else if (Game.game.input.hotbar4.isValid())
			{
				Game.game.input.hotbar4.invalidate();
				selectedNum = 3;
				clipboard = clipboards[3];
			}
			else if (Game.game.input.hotbar5.isValid())
			{
				Game.game.input.hotbar5.invalidate();
				selectedNum = 4;
				clipboard = clipboards[4];
			}
		}

		if (clipboard == null)
			clipboard = new Clipboard();

		allowClose = this.undoActions.isEmpty() && !modified;

		clickCooldown = Math.max(0, clickCooldown - Panel.frameFrequency);

		this.updateMusic(true);

		buttons.update();

		if (Game.game.input.editorPause.isValid() && this.level.editable)
		{
			this.paused = true;
			Game.game.input.editorPause.invalidate();
			Game.screen = new OverlayEditorMenu(Game.screen, this);
		}

		if (Game.game.input.editorObjectMenu.isValid() && this.level.editable && (!paused || objectMenu))
			Game.game.input.editorObjectMenu.invalidate();

		for (Effect e : Game.effects)
			e.update();

		if (this.paused)
			return;

		(currentPlaceable == Placeable.obstacle ? mouseObstacle : mouseTank).forAllSelectors(s ->
		{
			if (s.keybind != null && s.keybind.isValid())
			{
				s.keybind.invalidate();
				s.onSelect();
			}
		});

		if (Game.game.input.editorRevertCamera.isValid())
		{
			zoom = 1;
			offsetX = 0;
			offsetY = 0;

			Game.game.input.editorRevertCamera.invalidate();
		}

		if (changeCameraMode || Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
		{
			if (Game.game.input.editorZoomOut.isPressed())
			{
				zoom *= Math.pow(0.99, Panel.frameFrequency);
			}
			else if (Game.game.window.validScrollDown)
			{
				Game.game.window.validScrollDown = false;
				zoom *= 0.975;
			}

			if (Game.game.input.editorZoomIn.isPressed())
			{
				zoom *= Math.pow(1.01, Panel.frameFrequency);
			}
			else if (Game.game.window.validScrollUp)
			{
				Game.game.window.validScrollUp = false;
				zoom *= 1.025;
			}

			if (Game.game.window.touchscreen)
				recenter.update();
		}
		else if (!selectMode && !eraseMode && !pasteMode)
		{
			boolean up = false;
			boolean down = false;

			if (Game.game.input.editorNextType.isValid())
			{
				Game.game.input.editorNextType.invalidate();
				down = true;
			}
			else if (Game.game.window.validScrollDown)
			{
				Game.game.window.validScrollDown = false;
				down = true;
			}

			if (Game.game.input.editorPrevType.isValid())
			{
				Game.game.input.editorPrevType.invalidate();
				up = true;
			}
			else if (Game.game.window.validScrollUp)
			{
				Game.game.window.validScrollUp = false;
				up = true;
			}

			if (down && currentPlaceable == Placeable.enemyTank)
			{
				tankNum = (tankNum + 1) % (Game.registryTank.tankEntries.size() + this.level.customTanks.size());
				this.refreshMouseTank();
			}
			else if (down && currentPlaceable == Placeable.obstacle)
			{
				obstacleNum = (obstacleNum + 1) % Game.registryObstacle.obstacleEntries.size();
				mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
				cloneSelectorProperties();
			}

			if (up && currentPlaceable == Placeable.enemyTank)
			{
				tankNum = ((tankNum - 1) + Game.registryTank.tankEntries.size() + this.level.customTanks.size()) % (Game.registryTank.tankEntries.size() + this.level.customTanks.size());
				this.refreshMouseTank();
			}
			else if (up && currentPlaceable == Placeable.obstacle)
			{
				obstacleNum = ((obstacleNum - 1) + Game.registryObstacle.obstacleEntries.size()) % Game.registryObstacle.obstacleEntries.size();
				mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
				cloneSelectorProperties();
			}

			if ((up || down) && !(up && down))
				this.movePlayer = !this.movePlayer;

			boolean right = false;
			boolean left = false;

			if (Game.game.input.editorNextObj.isValid())
			{
				Game.game.input.editorNextObj.invalidate();
				right = true;
			}

			if (Game.game.input.editorPrevObj.isValid())
			{
				Game.game.input.editorPrevObj.invalidate();
				left = true;
			}

			if (right)
			{
				if (currentPlaceable == Placeable.enemyTank)
				{
					currentPlaceable = Placeable.obstacle;
				}
				else if (currentPlaceable == Placeable.obstacle)
				{
					currentPlaceable = Placeable.playerTank;
					mouseTank = new TankPlayer(0, 0, 0);
					((TankPlayer) mouseTank).setDefaultColor();
				}
				else if (currentPlaceable == Placeable.playerTank)
				{
					currentPlaceable = Placeable.enemyTank;
					this.refreshMouseTank();
				}
			}

			if (left)
			{
				if (currentPlaceable == Placeable.playerTank)
				{
					currentPlaceable = Placeable.obstacle;
				}
				else if (currentPlaceable == Placeable.obstacle)
				{
					currentPlaceable = Placeable.enemyTank;
					this.refreshMouseTank();
				}
				else if (currentPlaceable == Placeable.enemyTank)
				{
					currentPlaceable = Placeable.playerTank;
					mouseTank = new TankPlayer(0, 0, 0);
					((TankPlayer) mouseTank).setDefaultColor();
				}
			}

			if (Game.game.input.editorPrevMeta.isValid())
			{
				Game.game.input.editorPrevMeta.invalidate();

				if (currentPlaceable == Placeable.obstacle)
				{
					if (mouseObstacle.selectorCount() > 0)
						mouseObstacle.selectors.get(0).changeMeta(-1);
				}
				else if (mouseTank.selectorCount() > 0)
					mouseTank.selectors.get(0).changeMeta(-1);
			}

			if (Game.game.input.editorNextMeta.isValid())
			{
				Game.game.input.editorNextMeta.invalidate();

				if (currentPlaceable == Placeable.obstacle)
				{
					if (mouseObstacle.selectorCount() > 0)
						mouseObstacle.selectors.get(0).changeMeta(1);
				}
				else if (mouseTank.selectorCount() > 0)
					mouseTank.selectors.get(0).changeMeta(1);
			}
		}

		double prevZoom = zoom;
		double prevOffsetX = offsetX;
		double prevOffsetY = offsetY;

		boolean prevPanDown = panDown;
		boolean prevZoomDown = zoomDown;

		panDown = false;
		zoomDown = false;
		validZoomFingers = 0;

		if (!Game.game.window.touchscreen && Game.obstacleGrid != null)
		{
			double mx = Drawing.drawing.getMouseX();
			double my = Drawing.drawing.getMouseY();

			int x = (int) (mx / 50);
			int y = (int) (my / 50);

			if (x >= 0 && y >= 0 && x < Game.obstacleGrid.length && y < Game.obstacleGrid[0].length)
				hoverObstacle = Game.obstacleGrid[x][y];
			else
				hoverObstacle = null;

			boolean[] handled = checkMouse(mx, my,
					Game.game.input.editorUse.isPressed(),
					Game.game.input.editorAction.isPressed(),
					Game.game.input.editorUse.isValid(),
					Game.game.input.editorAction.isValid());

			if (handled[0])
				Game.game.input.editorUse.invalidate();

			if (handled[1])
				Game.game.input.editorAction.invalidate();
		}
		else
		{
			boolean input = false;

			for (int i: Game.game.window.touchPoints.keySet())
			{
				InputPoint p = Game.game.window.touchPoints.get(i);

				if (p.tag.isEmpty() || p.tag.equals("levelbuilder"))
				{
					input = true;

					double mx = Drawing.drawing.toGameCoordsX(Drawing.drawing.getInterfacePointerX(p.x));
					double my = Drawing.drawing.toGameCoordsY(Drawing.drawing.getInterfacePointerY(p.y));

					boolean[] handled = checkMouse(mx, my, true, false, !p.tag.equals("levelbuilder"), false);

					if (handled[0])
						p.tag = "levelbuilder";
				}
			}

			if (!input)
				checkMouse(0, 0, false, false, false, false);

			if (validZoomFingers == 0)
				panDown = false;
		}

		if (!zoomDown && panDown)
		{
			if (prevPanDown && !prevZoomDown)
			{
				offsetX += panCurrentX - panX;
				offsetY += panCurrentY - panY;
			}

			panX = Drawing.drawing.getMouseX();
			panY = Drawing.drawing.getMouseY();
		}


		if (zoomDown)
		{
			double x = (panCurrentX + zoomCurrentX) / 2;
			double y = (panCurrentY + zoomCurrentY) / 2;
			double d = Math.sqrt(Math.pow(Drawing.drawing.toInterfaceCoordsX(panCurrentX) - Drawing.drawing.toInterfaceCoordsX(zoomCurrentX), 2)
					+ Math.pow(Drawing.drawing.toInterfaceCoordsY(panCurrentY) - Drawing.drawing.toInterfaceCoordsY(zoomCurrentY), 2));

			if (prevZoomDown)
			{
				offsetX += x - panX;
				offsetY += y - panY;
				zoom *= d / zoomDist;
			}

			panX = x;
			panY = y;
			zoomDist = d;
		}

		zoom = Math.max(0.75, Math.min(Math.max(2 / (Drawing.drawing.unzoomedScale / Drawing.drawing.interfaceScale), 1), zoom));

		offsetX = Math.min(Game.currentSizeX * Game.tile_size / 2, Math.max(-Game.currentSizeX * Game.tile_size / 2, offsetX));
		offsetY = Math.min(Game.currentSizeY * Game.tile_size / 2, Math.max(-Game.currentSizeY * Game.tile_size / 2, offsetY));

		if ((zoom == 0.75 || zoom == 2 / (Drawing.drawing.unzoomedScale / Drawing.drawing.interfaceScale)) && prevZoom != zoom)
			Drawing.drawing.playVibration("click");

		if (Math.abs(offsetX) == Game.currentSizeX * Game.tile_size / 2 && prevOffsetX != offsetX)
			Drawing.drawing.playVibration("click");

		if (Math.abs(offsetY) == Game.currentSizeY * Game.tile_size / 2 && prevOffsetY != offsetY)
			Drawing.drawing.playVibration("click");

		if (Game.game.input.editorDeselect.isValid())
		{
			if (selection)
			{
				this.clearSelection();
			}
			else if (Game.game.window.shift)
			{
				Arrays.fill(clipboards, null);
				clipboard = new Clipboard();
			}
		}

		if (Game.game.input.editorToggleControls.isValid())
		{
			this.showControls = !this.showControls;
			Game.game.input.editorToggleControls.invalidate();
		}

		if (Game.game.input.editorPaste.isValid() && !pasteMode && !clipboard.isEmpty())
		{
			this.changeCameraMode = false;
			this.eraseMode = false;
			this.selectMode = false;
			this.pasteMode = true;
			Game.game.input.editorPaste.invalidate();
		}

		if (!redoActions.isEmpty() && redoLength != undoActions.size())
		{
			redoActions.clear();
			redoLength = -1;
		}

		Game.effects.removeAll(Game.removeEffects);
		Game.removeEffects.clear();

		Game.movables.removeAll(Game.removeMovables);
		Game.removeMovables.clear();

		for (Obstacle o : Game.removeObstacles)
		{
			o.removed = true;
			Drawing.drawing.terrainRenderer.remove(o);

			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
			{
				Game.redrawGroundTiles.add(new int[]{x, y});

				Game.obstacleGrid[x][y] = null;
				Game.surfaceTileGrid[x][y] = null;

				if (o.bulletCollision)
				{
					Game.game.solidGrid[x][y] = false;
					Game.game.unbreakableGrid[x][y] = false;
				}
			}
		}

		Game.obstacles.removeAll(Game.removeObstacles);
		Game.removeObstacles.clear();
	}

	public static void refreshItemButtons(ArrayList<Item> items, ButtonList buttonList, boolean omitPrice)
	{
		buttonList.buttons.clear();

		for (int i = 0; i < items.size(); i++)
		{
			int j = i;

			Button b = new Button(0, 0, 350, 40, items.get(i).name, () ->
			{
				ScreenEditItem s = new ScreenEditItem(items.get(j), (IItemScreen) Game.screen, omitPrice, true);
				s.drawBehindScreen = true;
				Game.screen = s;
			});

			b.image = items.get(j).icon;
			b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
			b.imageSizeX = b.sizeY;
			b.imageSizeY = b.sizeY;

			if (!omitPrice)
			{
				int p = items.get(i).price;

				if (p == 0)
					b.setSubtext("Free!");
				else if (p == 1)
					b.setSubtext("1 coin");
				else
					b.setSubtext("%d coins", p);
			}

			buttonList.buttons.add(b);
		}

		buttonList.sortButtons();
	}

	Button selectSquareToggle = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectSquare = !selectSquare;
	}, "Lock square selecting (Hold: %s, Toggle: %s)", Game.game.input.editorHoldSquare.getInputs(), Game.game.input.editorLockSquare.getInputs()
	);

	Button selectAddToggle = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectAdd = !selectAdd;
	}, "Toggle select/deselect (%s)", Game.game.input.editorSelectAddToggle.getInputs()
	);

	Button selectClear = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		clearSelection();
	}, "Clear selection (%s)", Game.game.input.editorDeselect.getInputs()
	);

	@SuppressWarnings("unchecked")
	protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

	public ScreenLevelEditor(String lvlName, Level level)
	{
		this.selfBatch = false;
		this.drawDarkness = false;

		this.music = "battle_editor.ogg";
		this.musicID = "editor";

		if (Game.game.window.touchscreen)
			controlsSizeMultiplier = 1.0;

		this.level = level;

		selectClear.sizeX *= controlsSizeMultiplier;
		selectClear.sizeY *= controlsSizeMultiplier;
		selectClear.fullInfo = true;

		selectAddToggle.sizeX *= controlsSizeMultiplier;
		selectAddToggle.sizeY *= controlsSizeMultiplier;
		selectAddToggle.fullInfo = true;

		selectSquareToggle.sizeX *= controlsSizeMultiplier;
		selectSquareToggle.sizeY *= controlsSizeMultiplier;
		selectSquareToggle.fullInfo = true;

		this.enableMargins = false;

		for (int i = 0; i < drawables.length; i++)
			drawables[i] = new ArrayList<>();

		Obstacle.draw_size = Game.tile_size;

		Game.game.window.validScrollDown = false;
		Game.game.window.validScrollUp = false;

		this.name = lvlName;
	}

	public void updateMusic(boolean tanks)
	{
		this.prevTankMusics.clear();
		this.prevTankMusics.addAll(this.tankMusics);
		this.tankMusics.clear();

		if (tanks)
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
				Drawing.drawing.addSyncedMusic(m, Game.musicVolume * 0.5f, true, 500);
		}
	}

	public boolean[] checkMouse(double mx, double my, boolean left, boolean right, boolean validLeft, boolean validRight)
	{
		boolean[] handled = new boolean[]{false, false};

		double posX = Math.round((mx) / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2;
		double posY = Math.round((my) / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2;
		mouseTank.posX = posX;
		mouseTank.posY = posY;
		mouseObstacle.posX = posX;
		mouseObstacle.posY = posY;

		if (changeCameraMode || (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) && validLeft))
		{
			if (validLeft)
			{
				if (validZoomFingers == 0)
				{
					panDown = true;
					panCurrentX = mx;
					panCurrentY = my;
				}
				else if (validZoomFingers == 1)
				{
					zoomDown = true;
					zoomCurrentX = mx;
					zoomCurrentY = my;
				}

				validZoomFingers++;
			}
		}
		else if (selectMode && !pasteMode)
		{
			if (!selection)
				selectAdd = true;

			boolean pressed = left || right;
			boolean valid = validLeft || validRight;

			if (valid)
			{
				selectX1 = clampTileX(mouseObstacle.posX);
				selectY1 = clampTileY(mouseObstacle.posY);
				selectHeld = true;
				handled[0] = true;
				handled[1] = true;

				Drawing.drawing.playVibration("selectionChanged");
			}

			if (pressed && selectHeld)
			{
				double prevSelectX2 = selectX2;
				double prevSelectY2 = selectY2;

				selectX2 = clampTileX(mouseObstacle.posX);
				selectY2 = clampTileY(mouseObstacle.posY);

				if (selectSquare || Game.game.input.editorHoldSquare.isPressed())
				{
					double size = Math.min(Math.abs(selectX2 - selectX1), Math.abs(selectY2 - selectY1));
					selectX2 = Math.signum(selectX2 - selectX1) * size + selectX1;
					selectY2 = Math.signum(selectY2 - selectY1) * size + selectY1;
				}

				if (prevSelectX2 != selectX2 || prevSelectY2 != selectY2)
					Drawing.drawing.playVibration("selectionChanged");
			}

			if (!pressed && selectHeld)
			{
				Drawing.drawing.playVibration("click");
				selectHeld = false;

				double lowX = Math.min(selectX1, selectX2);
				double highX = Math.max(selectX1, selectX2);
				double lowY = Math.min(selectY1, selectY2);
				double highY = Math.max(selectY1, selectY2);

				ArrayList<Integer> px = new ArrayList<>();
				ArrayList<Integer> py = new ArrayList<>();

				for (double x = lowX; x <= highX; x += Game.tile_size)
				{
					for (double y = lowY; y <= highY; y += Game.tile_size)
					{
						if (selectedTiles[(int)(x / Game.tile_size)][(int)(y / Game.tile_size)] == selectInverted)
						{
							px.add((int)(x / Game.tile_size));
							py.add((int)(y / Game.tile_size));
						}

						selectedTiles[(int)(x / Game.tile_size)][(int)(y / Game.tile_size)] = !selectInverted;
					}
				}

				this.undoActions.add(new Action.ActionSelectTiles(this, !selectInverted, px, py));

				this.refreshSelection();
			}
			else
			{
				if (selection && Game.game.input.editorSelectAddToggle.isValid())
				{
					Game.game.input.editorSelectAddToggle.invalidate();
					selectAddToggle.function.run();
				}

				selectInverted = selection && ((!selectAdd && !right) || (selectAdd && right));
			}

			if (Game.game.input.editorLockSquare.isValid())
			{
				Game.game.input.editorLockSquare.invalidate();
				selectSquareToggle.function.run();
			}
		}
		else if (symmetrySelectMode && !pasteMode)
		{
			if (validLeft)
			{
				selectX1 = clampTileX(mouseObstacle.posX);
				selectY1 = clampTileY(mouseObstacle.posY);
				selectHeld = true;
				handled[0] = true;
				handled[1] = true;

				Drawing.drawing.playVibration("selectionChanged");
			}

			if (left && selectHeld)
			{
				double prevSelectX2 = selectX2;
				double prevSelectY2 = selectY2;

				selectX2 = clampTileX(mouseObstacle.posX);
				selectY2 = clampTileY(mouseObstacle.posY);

				if (symmetryType == SymmetryType.flip8 || symmetryType == SymmetryType.rot90)
				{
					double size = Math.min(Math.abs(selectX2 - selectX1), Math.abs(selectY2 - selectY1));
					selectX2 = Math.signum(selectX2 - selectX1) * size + selectX1;
					selectY2 = Math.signum(selectY2 - selectY1) * size + selectY1;
				}

				if (prevSelectX2 != selectX2 || prevSelectY2 != selectY2)
					Drawing.drawing.playVibration("selectionChanged");
			}

			if (!left && selectHeld)
			{
				Drawing.drawing.playVibration("click");
				selectHeld = false;

				double lowX = Math.min(selectX1, selectX2);
				double highX = Math.max(selectX1, selectX2);
				double lowY = Math.min(selectY1, selectY2);
				double highY = Math.max(selectY1, selectY2);

				this.symmetryX1 = lowX;
				this.symmetryX2 = highX;
				this.symmetryY1 = lowY;
				this.symmetryY2 = highY;
			}
		}
		else
		{
			int x = (int) (mouseObstacle.posX / Game.tile_size);
			int y = (int) (mouseObstacle.posY / Game.tile_size);

			if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
			{
				if (selectedTiles[x][y] && (validLeft || validRight) && !(currentPlaceable == Placeable.playerTank && this.movePlayer))
				{
					double ox = mouseObstacle.posX;
					double oy = mouseObstacle.posY;

					ArrayList<Action> actions = this.undoActions;
					this.undoActions = new ArrayList<>();

					for (int i = 0; i < selectedTiles.length; i++)
					{
						for (int j = 0; j < selectedTiles[i].length; j++)
						{
							if (selectedTiles[i][j])
							{
								mouseObstacle.posX = (i + 0.5) * Game.tile_size;
								mouseObstacle.posY = (j + 0.5) * Game.tile_size;

								mouseTank.posX = mouseObstacle.posX;
								mouseTank.posY = mouseObstacle.posY;

								handled = handlePlace(handled, left, right, validLeft, validRight, true);
							}
						}
					}

					if (!this.undoActions.isEmpty())
					{
						Action a = new Action.ActionGroup(this, this.undoActions);
						actions.add(a);
						Drawing.drawing.playVibration("click");
					}

					this.undoActions = actions;

					mouseObstacle.posX = ox;
					mouseObstacle.posY = oy;

					mouseTank.posX = ox;
					mouseTank.posY = oy;
				}
				else
					handled = handlePlace(handled, left, right, validLeft, validRight, false);
			}
		}

		return handled;
	}

	public void initialize()
	{
		if (!this.initialized)
		{
			this.initialized = true;
			this.clickCooldown = 50;

			if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
			{
				if (this.mouseObstacle.selectors.isEmpty())
					this.mouseObstacle.registerSelectors();

				this.mouseObstacle.initSelectors(this);
				this.mouseObstacle.forAllSelectors(LevelEditorSelector::addShortcutButton);
			}
			else
			{
				if (this.mouseTank.selectors.isEmpty())
					this.mouseTank.registerSelectors();

				this.mouseTank.initSelectors(this);
				this.mouseTank.forAllSelectors(LevelEditorSelector::addShortcutButton);
			}

			this.cloneSelectorProperties();
		}
	}

	public boolean[] handlePlace(boolean[] handled, boolean left, boolean right, boolean validLeft, boolean validRight, boolean batch, boolean paste)
	{
		ArrayList<Double> posX = new ArrayList<>();
		ArrayList<Double> posY = new ArrayList<>();
		ArrayList<Double> orientations = new ArrayList<>();

		double originalX = mouseTank.posX;
		double originalY = mouseTank.posY;
		double originalOrientation = mouseTank.orientation;

		posX.add(mouseTank.posX);
		posY.add(mouseTank.posY);
		orientations.add(mouseTank.orientation);

		if (mouseTank.posX >= symmetryX1 && mouseTank.posX <= symmetryX2 && mouseTank.posY >= symmetryY1 && mouseTank.posY <= symmetryY2)
		{
			if (symmetryType == SymmetryType.flipHorizontal || symmetryType == SymmetryType.flipBoth || symmetryType == SymmetryType.flip8)
			{
				for (int i = 0; i < posX.size(); i++)
				{
					posY.add(symmetryY1 + (symmetryY2 - posY.get(i)));
					posX.add(posX.get(i));

					if (orientations.get(i) == 1 || orientations.get(i) == 3)
						orientations.add((orientations.get(i) + 2) % 4);
					else
						orientations.add(orientations.get(i));
				}
			}

			if (symmetryType == SymmetryType.flipVertical || symmetryType == SymmetryType.flipBoth || symmetryType == SymmetryType.flip8)
			{
				for (int i = 0; i < posX.size(); i++)
				{
					posX.add(symmetryX1 + (symmetryX2 - posX.get(i)));
					posY.add(posY.get(i));

					if (orientations.get(i) == 0 || orientations.get(i) == 2)
						orientations.add((orientations.get(i) + 2) % 4);
					else
						orientations.add(orientations.get(i));
				}
			}
		}

		for (int oi = 0; oi < orientations.size(); oi++)
		{
			mouseTank.posX = posX.get(oi);
			mouseTank.posY = posY.get(oi);
			mouseObstacle.posX = mouseTank.posX;
			mouseObstacle.posY = mouseTank.posY;
			mouseTank.orientation = orientations.get(oi);

			if (mouseTank.posX > 0 && mouseTank.posY > 0 && mouseTank.posX < Game.tile_size * Game.currentSizeX && mouseTank.posY < Game.tile_size * Game.currentSizeY)
			{
				if (validLeft && pasteMode && !paste)
				{
					paste();

					mouseTank.posX = originalX;
					mouseTank.posY = originalY;
					mouseTank.orientation = originalOrientation;

					mouseObstacle.posX = originalX;
					mouseObstacle.posY = originalY;

					return new boolean[]{true, true};
				}

				if (!pasteMode && (right || (eraseMode && left)))
				{
					boolean skip = false;

					if (validRight || (eraseMode && validLeft))
					{
						for (int i = 0; i < Game.movables.size(); i++)
						{
							Movable m = Game.movables.get(i);
							if (m.posX == mouseTank.posX && m.posY == mouseTank.posY && m instanceof Tank && !(this.spawns.contains(m) && this.spawns.size() == 1))
							{
								skip = true;

								if (m instanceof TankSpawnMarker)
								{
									this.spawns.remove(m);
									this.undoActions.add(new Action.ActionPlayerSpawn(this, (TankSpawnMarker) m, false));
								}
								else
									this.undoActions.add(new Action.ActionTank((Tank) m, false));

								Game.removeMovables.add(m);

								if (!batch)
								{
									Drawing.drawing.playVibration("click");

									for (int z = 0; z < 100 * Game.effectMultiplier; z++)
									{
										Effect e = Effect.createNewEffect(m.posX, m.posY, m.size / 2, Effect.EffectType.piece);
										double var = 50;
										e.colR = Math.min(255, Math.max(0, ((Tank) m).colorR + Math.random() * var - var / 2));
										e.colG = Math.min(255, Math.max(0, ((Tank) m).colorG + Math.random() * var - var / 2));
										e.colB = Math.min(255, Math.max(0, ((Tank) m).colorB + Math.random() * var - var / 2));

										if (Game.enable3d)
											e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * 2);
										else
											e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2);

										e.maxAge /= 2;
										Game.effects.add(e);
									}
								}

								break;
							}
						}
					}

					if (!skip)
					{
						for (int i = 0; i < Game.obstacles.size(); i++)
						{
							Obstacle m = Game.obstacles.get(i);
							if (Movable.distanceBetween(m, mouseTank) < 50)
							{
								skip = true;
								this.undoActions.add(new Action.ActionObstacle(m, false));
								Game.removeObstacles.add(m);

								if (!batch)
									Drawing.drawing.playVibration("click");

								break;
							}
						}
					}

					if (!batch && !Game.game.window.touchscreen && !skip && validRight)
					{
						int add = Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) ? -1 : 1;

						if (currentPlaceable == Placeable.obstacle)
						{
							if (mouseObstacle.selectorCount() > 1)
								mouseObstacle.selectors.get(1).changeMeta(add);
						}
						else if (mouseTank.selectorCount() > 1)
							mouseTank.selectors.get(1).changeMeta(add);
					}

					if (Game.game.window.touchscreen)
						handled[0] = true;

					handled[1] = true;
				}

				if (!eraseMode && clickCooldown <= 0 && (validLeft || (!pasteMode && left && currentPlaceable == Placeable.obstacle && this.mouseObstacle.draggable)))
				{
					boolean skip = false;

					double mx = mouseTank.posX;
					double my = mouseTank.posY;

					if (currentPlaceable == Placeable.obstacle)
					{
						mx = mouseObstacle.posX;
						my = mouseObstacle.posY;
					}

					if (mouseObstacleStartHeight < 1 && mouseObstacle.tankCollision || currentPlaceable != Placeable.obstacle)
					{
						for (Movable m : Game.movables)
						{
							if (m.posX == mx && m.posY == my)
							{
								skip = true;
								break;
							}
						}
					}

					if (!skip)
					{
						if (mouseObstacleStartHeight == 0 || currentPlaceable != Placeable.obstacle)
						{
							Obstacle o = Game.obstacleGrid[(int) (mx / Game.tile_size)][(int) (my / Game.tile_size)];
							Obstacle s = Game.surfaceTileGrid[(int) (mx / Game.tile_size)][(int) (my / Game.tile_size)];

							if (o != null || s != null)
							{
								if (o == null)
									o = s;

								if (!validRight)
								{
									if (currentPlaceable == Placeable.obstacle)
									{
										if (o.tankCollision || mouseObstacle.tankCollision || o.isSurfaceTile == mouseObstacle.isSurfaceTile || o.getClass() == mouseObstacle.getClass())
										{
											if (o.isSurfaceTile || mouseObstacleStartHeight >= o.startHeight && mouseObstacleStartHeight < o.stackHeight + o.startHeight)
												skip = true;
										}
									}
									else
										skip = o.tankCollision;
								}
								else
								{
									this.undoActions.add(new Action.ActionObstacle(o, false));
									Game.removeObstacles.add(o);
								}
							}
						}
						else
						{
							// old way
							for (Obstacle o : Game.obstacles)
							{
								if (o.posX == mx && o.posY == my)
								{
									if (!validRight)
									{
										double y1 = o.startHeight;
										double y2 = o.startHeight + o.stackHeight;
										double y3 = mouseObstacleStartHeight;
										double y4 = mouseObstacleStartHeight + mouseObstacle.stackHeight;

										if (((o.tankCollision || mouseObstacle.tankCollision || o.getClass() == mouseObstacle.getClass()) &&
												(mouseObstacle.isSurfaceTile != o.isSurfaceTile ||
														(Game.lessThan(y1, y3, y2) || Game.lessThan(y1, y4, y2)) || (Game.lessThan(true, y1, y3, y2) && Game.lessThan(true, y1, y4, y2)))))
											skip = true;
									}
									else
									{
										this.undoActions.add(new Action.ActionObstacle(o, false));
										Game.removeObstacles.add(o);
									}
								}
							}
						}
					}

					if (!skip || (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) && validLeft))
					{
						if (currentPlaceable == Placeable.enemyTank)
						{
							Tank t;

							if (paste)
								t = mouseTank;
							else
							{
								if (tankNum < Game.registryTank.tankEntries.size())
									t = Game.registryTank.getEntry(tankNum).getTank(mouseTank.posX, mouseTank.posY, mouseTank.angle);
								else
									t = ((TankAIControlled) mouseTank).instantiate(mouseTank.name, mouseTank.posX, mouseTank.posY, mouseTank.angle);
							}

							if (mouseTank.hasCustomSelectors())
							{
								AtomicInteger i = new AtomicInteger();
								t.forAllSelectors(c -> c.cloneProperties(mouseTank.selectors.get(i.getAndIncrement())));
							}

							this.undoActions.add(new Action.ActionTank(t, true));
							Game.movables.add(t);

							if (!batch)
								Drawing.drawing.playVibration("click");
						}
						else if (currentPlaceable == Placeable.playerTank)
						{
							ArrayList<TankSpawnMarker> spawnsClone = (ArrayList<TankSpawnMarker>) spawns.clone();
							if (this.movePlayer && !paste)
							{
								Game.removeMovables.addAll(this.spawns);
								this.spawns.clear();
							}

							TankSpawnMarker t = new TankSpawnMarker("player", mouseTank.posX, mouseTank.posY, mouseTank.angle);
							t.registerSelectors();

							if (mouseTank.hasCustomSelectors())
							{
								AtomicInteger i = new AtomicInteger();
								t.forAllSelectors(c -> c.cloneProperties(mouseTank.selectors.get(i.getAndIncrement())));
							}

							this.spawns.add(t);

							if (this.movePlayer && !paste)
								this.undoActions.add(new Action.ActionMovePlayer(this, spawnsClone, t));
							else
								this.undoActions.add(new Action.ActionPlayerSpawn(this, t, true));

							Game.movables.add(t);

							if (!batch)
								Drawing.drawing.playVibration("click");

							if (this.movePlayer)
								t.drawAge = 50;
						}
						else if (currentPlaceable == Placeable.obstacle)
						{
							Obstacle o = !paste ? Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0) : mouseObstacle;
							o.posX = mouseObstacle.posX;
							o.posY = mouseObstacle.posY;
							o.startHeight = mouseObstacleStartHeight;
							o.initSelectors(this);

							if (mouseObstacle.hasCustomSelectors())
							{
								AtomicInteger i = new AtomicInteger();
								o.forAllSelectors(s -> s.cloneProperties(mouseObstacle.selectors.get(i.getAndIncrement())));
							}

							if (o.enableStacking)
							{
								StackHeightSelector s = (StackHeightSelector) o.selectors.get(0);
								s.number = mouseObstacle.stackHeight;

								if (this.stagger && !paste)
								{
									if ((((int) (o.posX / Game.tile_size) + (int) (o.posY / Game.tile_size)) % 2 == (this.oddStagger ? 1 : 0)))
										s.number -= 0.5;
								}
							}

							this.undoActions.add(new Action.ActionObstacle(o, true));
							Game.addObstacle(o);

							if (!batch)
								Drawing.drawing.playVibration("click");
						}
					}

					handled[0] = true;
					handled[1] = true;
				}
			}
		}

		return handled;
	}

	public boolean[] handlePlace(boolean[] handled, boolean left, boolean right, boolean validLeft, boolean validRight, boolean batch)
	{
		return handlePlace(handled, left, right, validLeft, validRight, batch, false);
	}

	public void save()
	{
		OverlayObjectMenu.saveSelectors(this);

		if (undoActions.isEmpty() && redoActions.isEmpty() && !modified)
			return;

		StringBuilder level = new StringBuilder("{");

		level.append(this.level.sizeX).append(",").append(this.level.sizeY).append(",").append(this.level.colorR).append(",").append(this.level.colorG).append(",").append(this.level.colorB).append(",").append(this.level.colorVarR).append(",").append(this.level.colorVarG).append(",").append(this.level.colorVarB)
				.append(",").append((int) (this.level.timer / 100)).append(",").append((int) Math.round(this.level.light * 100)).append(",").append((int) Math.round(this.level.shadow * 100)).append("|");

		grid = new HashMap[Game.currentSizeX][Game.currentSizeY];

		ArrayList<Obstacle> unmarked = new ArrayList<>();

		HashSet<Class<? extends Obstacle>> classes = new HashSet<>();
		HashMap<Class<? extends Obstacle>, String> names = new HashMap<>();
		for (RegistryObstacle.ObstacleEntry o : Game.registryObstacle.obstacleEntries)
		{
			classes.add(o.obstacle);
			names.put(o.obstacle, o.name);
		}

		for (Obstacle o : Game.obstacles)
		{
			// using this variable to determine one of two states: handled by compression or unmarked
			if (o.removed)
				continue;

			if (o.isSurfaceTile)
				o.startHeight = -1;

			int x = (int) (o.posX / 50);
			int y = (int) (o.posY / 50);

			if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY || !classes.contains(o.getClass()))
			{
				unmarked.add(o);
				o.removed = true;
			}
			else
			{
				if (grid[x][y] == null)
					grid[x][y] = new HashMap<>();

				if (!o.enableGroupID)
					grid[x][y].put((float) o.startHeight, o);
				else
					grid[x][y].put((float) Math.pow(o.startHeight, o.groupID), o);
			}
		}

		for (int x = 0; x < this.level.sizeX; x++)
		{
			for (int y = 0; y < this.level.sizeY; y++)
			{
				if (this.grid[x][y] == null)
					continue;

				for (float layer: this.grid[x][y].keySet())
				{
					Obstacle o = this.grid[x][y].get(layer);

					// using this variable to determine whether handled by compression or unmarked
					if (o == null || o.removed)
						continue;

					compress1D(x, y, layer);

					if (endX > 0)
						level.append(x).append("...").append(endX-1).append("-").append(y);
					else if (endY > 0)
						level.append(x).append("-").append(y).append("...").append(endY-1);
					else
						level.append(x).append("-").append(y);

					level.append("-").append(names.get(o.getClass()));
					level.append("-").append(o.getMetadata());
					level.append(",");
				}
			}
		}

		for (Obstacle o : unmarked)
		{
			level.append((int) (o.posX / Game.tile_size)).append("-").append((int) (o.posY / Game.tile_size));
			level.append("-").append(o.name);

			if (o instanceof ObstacleUnknown && ((ObstacleUnknown) o).metadata != null)
				level.append("-").append(((ObstacleUnknown) o).metadata);
			else if (o.enableStacking)
				level.append("-").append(o.stackHeight);
			else if (o.enableGroupID)
				level.append("-").append(o.groupID);

			if (o.startHeight > 0)
				level.append("-").append(o.startHeight);

			level.append(",");
		}

		if (level.charAt(level.length() - 1) == ',')
			level = new StringBuilder(level.substring(0, level.length() - 1));

		level.append("|");

		for (int i = 0; i < Game.movables.size(); i++)
		{
			if (Game.movables.get(i) instanceof Tank t)
			{
				int x = (int) (t.posX / Game.tile_size);
				int y = (int) (t.posY / Game.tile_size);

				level.append(x).append("-").append(y).append("-").append(t.name).append("-");
				level.append(t.getMetadata()).append(",");
			}
		}

		if (Game.movables.isEmpty())
			level.append("|");

		level = new StringBuilder(level.substring(0, level.length() - 1));

		level.append("|");

		for (Team t : teams)
		{
			level.append(t.name).append("-").append(t.friendlyFire);
			if (t.enableColor)
				level.append("-").append(t.teamColorR).append("-").append(t.teamColorG).append("-").append(t.teamColorB);

			level.append(",");
		}

		level = new StringBuilder(level.substring(0, level.length() - 1));

		level.append("}");

		for (Item i: this.level.shop)
			i.exportProperties();

		for (Item i: this.level.startingItems)
			i.exportProperties();

		if (this.level.startingCoins > 0)
			level.append("\ncoins\n").append(this.level.startingCoins);

		if (!this.level.shop.isEmpty())
		{
			level.append("\nshop");

			for (Item i : this.level.shop)
				level.append("\n").append(i.toString());
		}

		if (!this.level.startingItems.isEmpty())
		{
			level.append("\nitems");

			for (Item i : this.level.startingItems)
				level.append("\n").append(i.toString());
		}

		if (!this.level.customTanks.isEmpty())
		{
			level.append("\ntanks");

			for (Tank t : this.level.customTanks)
				level.append("\n").append(t.toString());
		}

		if (!this.level.properties.isEmpty())
		{
			for (String s : this.level.properties)
				level.append("\n").append(s);
		}

		Game.currentLevelString = level.toString();

		BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + name);
		if (file.exists())
		{
			if (!this.level.editable)
				return;

			file.delete();
		}

		try
		{
			file.create();

			file.startWriting();
			file.println(level.toString());
			file.stopWriting();
		}
		catch (IOException e)
		{
			Game.exitToCrash(e);
		}
	}

	public void paste()
	{
		Drawing.drawing.playVibration("click");

		ArrayList<Action> actions = this.undoActions;
		this.undoActions = new ArrayList<>();

		boolean[] handled = new boolean[2];

		Tank prevMouseTank = mouseTank;
		Obstacle prevMouseObstacle = mouseObstacle;
		Placeable placeable = currentPlaceable;

		double mx = prevMouseObstacle.posX;
		double my = prevMouseObstacle.posY;

		for (Obstacle o : clipboard.obstacles)
		{
			currentPlaceable = Placeable.obstacle;

			try
			{
				Obstacle n = o.getClass().getConstructor(String.class, double.class, double.class).newInstance(o.name, (o.posX + prevMouseObstacle.posX) / 50 - 0.5, (o.posY + prevMouseObstacle.posY) / 50 - 0.5);
				n.selectors = o.selectors;
				n.stackHeight = o.stackHeight;
				n.startHeight = o.startHeight;
				mouseObstacle = n;
				mouseObstacle.stackHeight = n.stackHeight;
				mouseObstacleStartHeight = n.startHeight;
				mouseTank.posX = mouseObstacle.posX;
				mouseTank.posY = mouseObstacle.posY;

				handlePlace(handled, true, false, true, false, true, true);
			}
			catch (Exception e)
			{
				Game.exitToCrash(e.getCause());
			}
		}

		for (Tank o : clipboard.tanks)
		{
			currentPlaceable = Placeable.enemyTank;

			try
			{
				Tank n;

				if (o.getClass().equals(TankAIControlled.class))
				{
					n = new TankAIControlled(o.name, o.posX + prevMouseObstacle.posX, o.posY + prevMouseObstacle.posY, o.size, o.colorR, o.colorG, o.colorB, o.angle, ((TankAIControlled) o).shootAIType);
					((TankAIControlled) o).cloneProperties((TankAIControlled) n);
				}
				else
					n = o.getClass().getConstructor(String.class, double.class, double.class, double.class).newInstance(o.name, o.posX + prevMouseObstacle.posX, o.posY + prevMouseObstacle.posY, o.angle);

				n.team = o.team;
				n.destroy = o.destroy;
				mouseTank = n;

				if (n instanceof TankSpawnMarker || n instanceof TankPlayer)
					currentPlaceable = Placeable.playerTank;

				handlePlace(handled, true, false, true, false, true, true);
				//Game.movables.add(n);
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}
		}

		prevMouseObstacle.posX = mx;
		prevMouseObstacle.posY = my;
		prevMouseTank.posX = mx;
		prevMouseTank.posY = my;

		currentPlaceable = placeable;
		mouseTank = prevMouseTank;
		mouseObstacle = prevMouseObstacle;

		mouseObstacleStartHeight = mouseObstacle.startHeight;

		ArrayList<Action> tempActions = this.undoActions;
		this.undoActions = actions;

		this.undoActions.add(new Action.ActionPaste(this, tempActions));
	}

	int endX, endY;

	public void compress1D(int x, int y, float layer)
	{
		Obstacle compare = grid[x][y].get(layer);
		String metadata = compare.getMetadata();
		compare.removed = true;

		ArrayList<Obstacle> compX = new ArrayList<>();
		for (endX = x + 1; endX < Game.currentSizeX; endX++)
		{
			if (grid[endX][y] == null)
				break;

			Obstacle o = grid[endX][y].get(layer);

			if (o == null || o.getClass() != compare.getClass() || !Objects.equals(o.getMetadata(), metadata))
				break;

			compX.add(o);
		}

		ArrayList<Obstacle> compY = new ArrayList<>();
		for (endY = y + 1; endY < Game.currentSizeY; endY++)
		{
			if (grid[x][endY] == null)
				break;

			Obstacle o = grid[x][endY].get(layer);

			if (o == null || o.getClass() != compare.getClass() || !Objects.equals(o.getMetadata(), compare.getMetadata()))
				break;

			compY.add(o);
		}

		if (endX - x <= 1 && endY - y <= 1)
		{
			endX = -1;
			endY = -1;
			return;
		}

		if (endX >= endY)
		{
			endY = -1;
			for (Obstacle o : compX)
				o.removed = true;
		}
		else
		{
			endX = -1;
			for (Obstacle o : compY)
				o.removed = true;
		}
	}

	@Override
	public void draw()
	{
		if (Level.isDark(true))
			this.fontBrightness = 255;
		else
			this.fontBrightness = 0;

		for (Obstacle o : Game.obstacles)
			o.baseGroundHeight = Game.sampleGroundHeight(o.posX, o.posY);

		if (Game.enable3d)
		{
			for (Obstacle o : Game.obstacles)
			{
				o.postOverride();

				if (o.startHeight > 1)
					continue;

				int x = (int) (o.posX / Game.tile_size);
				int y = (int) (o.posY / Game.tile_size);

				if (!(!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY))
					Game.game.heightGrid[x][y] = Math.max(o.getTileHeight(), Game.game.heightGrid[x][y]);
			}
		}

		Drawing.drawing.setColor(174, 92, 16);

		double mul = 1;
		if (Game.angledView)
			mul = 2;

		Drawing.drawing.fillShadedInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2,
				mul * Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, mul * Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);

		this.drawDefaultBackground();

		for (Movable m: Game.movables)
			drawables[m.drawLevel].add(m);

		mouseTank.updateSelectors();
		mouseObstacle.updateSelectors();

		for (Obstacle o : Game.obstacles)
		{
			if (!o.batchDraw)
				drawables[o.drawLevel].add(o);
		}

		for (Effect e: Game.effects)
			drawables[7].add(e);

		for (int i = 0; i < this.drawables.length; i++)
		{
			if (i == 5 && Game.enable3d)
			{
				Drawing drawing = Drawing.drawing;
				Drawing.drawing.setColor(174, 92, 16);
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, -Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, Drawing.drawing.sizeY + Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(-Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX + Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
			}

			for (IDrawable d: this.drawables[i])
			{
				d.draw();

				if (d instanceof Movable)
					((Movable) d).drawTeam();
			}

			if (Game.glowEnabled)
			{
				for (int j = 0; j < this.drawables[i].size(); j++)
				{
					IDrawable d = this.drawables[i].get(j);

					if (d instanceof IDrawableWithGlow && ((IDrawableWithGlow) d).isGlowEnabled())
						((IDrawableWithGlow) d).drawGlow();
				}
			}

			drawables[i].clear();
		}

		if (!paused && !Game.game.window.touchscreen)
		{
			if (eraseMode && !selectMode && !changeCameraMode)
			{
				Drawing.drawing.setColor(255, 0, 0, 64, 0.3);

				if (Game.enable3d)
				{
					if (hoverObstacle == null)
						Drawing.drawing.fillBox(mouseObstacle.posX, mouseObstacle.posY, 0, Game.tile_size, Game.tile_size, Game.tile_size, (byte) 64);
					else
						hoverObstacle.draw3dOutline(255, 0, 0, 64);
				}
				else
					Drawing.drawing.fillRect(mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);
			}
			else if (pasteMode && !changeCameraMode)
			{
				Drawing.drawing.setColor(255, 255, 255, 127, 0.3);
				Drawing.drawing.drawImage("icons/paste.png", mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);

				for (Obstacle o : clipboard.obstacles)
				{
					Drawing.drawing.setColor(o.colorR, o.colorG, o.colorB, 64, 0.5);
					Drawing.drawing.fillRect(o.posX + mouseTank.posX, o.posY + mouseTank.posY, /*0,*/ Game.tile_size, Game.tile_size/*, ((Obstacle) o).stackHeight * Game.tile_size, (byte) 64*/);
				}

				for (Tank t : clipboard.tanks)
				{
					t.drawOutlineAt(t.posX + mouseTank.posX, t.posY + mouseTank.posY);
				}
			}
			else if ((currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank)  && !selectMode && !changeCameraMode)
			{
				mouseTank.drawOutline();
				mouseTank.drawTeam();

				if (currentPlaceable == Placeable.playerTank && !this.movePlayer)
				{
					Drawing.drawing.setColor(0, 200, 255, 127);
					Drawing.drawing.drawImage("emblems/player_spawn.png", mouseTank.posX, mouseTank.posY, mouseTank.size * 0.7, mouseTank.size * 0.7);
				}
			}
			else if (currentPlaceable == Placeable.obstacle && !selectMode && !changeCameraMode)
			{
				int x = (int) (mouseObstacle.posX / Game.tile_size);
				int y = (int) (mouseObstacle.posY / Game.tile_size);

				if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY && Game.obstacleGrid[x][y] == null)
				{
					mouseObstacle.startHeight = mouseObstacleStartHeight;

					if (Game.enable3d)
					{
						if (Game.lessThan(-1, x, Game.currentSizeX) && Game.lessThan(-1, y, Game.currentSizeY) &&
								(Game.obstacleGrid[x][y] == null || !Game.lessThan(Game.obstacleGrid[x][y].startHeight, mouseObstacle.stackHeight + mouseObstacleStartHeight, Game.obstacleGrid[x][y].stackHeight)))
							mouseObstacle.draw3dOutline(mouseObstacle.colorR, mouseObstacle.colorG, mouseObstacle.colorB, 100);
					}

					mouseObstacle.drawOutline();

					if (mouseObstacle.enableStacking)
					{
						Drawing.drawing.setFontSize(16);
						Drawing.drawing.drawText(mouseObstacle.posX, mouseObstacle.posY, mouseObstacle.stackHeight + "");
					}
				}
			}
		}

		double extra = Math.sin(System.currentTimeMillis() * Math.PI / 1000.0) * 25;

		if (selectedTiles != null)
		{
			for (int x = 0; x < selectedTiles.length; x++)
			{
				for (int y = 0; y < selectedTiles[x].length; y++)
				{
					if (selectedTiles[x][y])
					{
						if (Game.enable3d)
						{
							if (!(x < Game.currentSizeX && y < Game.currentSizeY && Game.obstacleGrid[x][y] != null && Game.surfaceTileGrid[x][y] == null))
							{
								Drawing.drawing.setColor(230 + extra, 230 + extra, 230 + extra, 128, 0.5);
								Drawing.drawing.fillBox((x + 0.5) * Game.tile_size, (y + 0.5) * Game.tile_size, 15,
										Game.tile_size, Game.tile_size, 1);
							}
							else if (Game.surfaceTileGrid[x][y] != null)
								Game.surfaceTileGrid[x][y].draw3dOutline(230 + extra, 230 + extra, 230 + extra, 128);
							else
								Game.obstacleGrid[x][y].draw3dOutline(230 + extra, 230 + extra, 230 + extra, 128);
						}
						else
							Drawing.drawing.fillRect((x + 0.5) * Game.tile_size, (y + 0.5) * Game.tile_size, Game.tile_size, Game.tile_size);
					}
				}
			}
		}

		if (changeCameraMode && !paused)
		{
			Drawing.drawing.setColor(0, 0, 0, 127);

			Drawing.drawing.fillInterfaceRect(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2, 500 * objWidth / 350, 150 * objHeight / 40);

			Drawing.drawing.setColor(255, 255, 255);

			Drawing.drawing.setInterfaceFontSize(this.textSize);
			Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 - this.objYSpace / 2, "Drag to pan");

			if (Game.game.window.touchscreen)
			{
				Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 - 0, "Pinch to zoom");
				recenter.draw();
			}
			else
			{
				Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 - 0, "Scroll or press %s or %s to zoom", Game.game.input.editorZoomIn.getInputs(), Game.game.input.editorZoomOut.getInputs());
				Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 + this.objYSpace / 2, "Press %s to re-center", Game.game.input.editorRevertCamera.getInputs());
			}
		}

		if ((selectMode || symmetrySelectMode) && !changeCameraMode && !pasteMode && !this.paused)
		{
			if (!selectInverted)
				Drawing.drawing.setColor(255, 255, 255, 127, 0.3);
			else
				Drawing.drawing.setColor(0, 0, 0, 127, 0.3);

			if (!selectHeld)
			{
				if (!Game.game.window.touchscreen)
					Drawing.drawing.fillRect(mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);
			}
			else
			{
				double lowX = Math.min(selectX1, selectX2);
				double highX = Math.max(selectX1, selectX2);
				double lowY = Math.min(selectY1, selectY2);
				double highY = Math.max(selectY1, selectY2);

				for (double x = lowX; x <= highX; x += Game.tile_size)
				{
					for (double y = lowY; y <= highY; y += Game.tile_size)
					{
						int gridX = (int) (x / Game.tile_size);
						int gridY = (int) (y / Game.tile_size);

						if (Game.enable3d)
						{
							if (Game.obstacleGrid[gridX][gridY] != null)
								Game.obstacleGrid[gridX][gridY].draw3dOutline(230 + extra, 230 + extra, 230 + extra, 128);
							else
							{
								if (!selectInverted)
									Drawing.drawing.setColor(255, 255, 255, 127, 0.3);
								else
									Drawing.drawing.setColor(0, 0, 0, 127, 0.3);

								Drawing.drawing.fillBox(x, y, 15, Game.tile_size, Game.tile_size, 1);
							}
						}
						else
							Drawing.drawing.fillRect(x, y, Game.tile_size, Game.tile_size);
					}
				}

				Drawing.drawing.setColor(255, 255, 255);

				if (symmetryType == SymmetryType.flipBoth || symmetryType == SymmetryType.flipHorizontal || symmetryType == SymmetryType.rot180 || symmetryType == SymmetryType.rot90 || symmetryType == SymmetryType.flip8)
					Drawing.drawing.fillRect((selectX2 + selectX1) / 2, (selectY2 + selectY1) / 2, (selectX2 - selectX1), 10);

				if (symmetryType == SymmetryType.flipBoth || symmetryType == SymmetryType.flipVertical || symmetryType == SymmetryType.rot90 || symmetryType == SymmetryType.flip8)
					Drawing.drawing.fillRect((selectX2 + selectX1) / 2, (selectY2 + selectY1) / 2, 10, (selectX2 - selectX1));
			}
		}

		buttons.draw();

		if (Game.screen instanceof IOverlayScreen || this.paused)
		{
			Drawing.drawing.setColor(127, 178, 228, 64);
			//Drawing.drawing.setColor(0, 0, 0, 127);
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);
		}

		if (!paused && showControls)
		{
			pause.image = "icons/pause.png";
			pause.imageSizeX = 40 * controlsSizeMultiplier;
			pause.imageSizeY = 40 * controlsSizeMultiplier;

			selectClear.image = "icons/select_clear.png";
			selectClear.imageSizeX = 40 * controlsSizeMultiplier;
			selectClear.imageSizeY = 40 * controlsSizeMultiplier;

			if (selectAdd)
				selectAddToggle.image = "icons/select_add.png";
			else
				selectAddToggle.image = "icons/select_remove.png";

			selectAddToggle.imageSizeX = 40 * controlsSizeMultiplier;
			selectAddToggle.imageSizeY = 40 * controlsSizeMultiplier;

			if (selectSquare)
				selectSquareToggle.image = "icons/square_locked.png";
			else
				selectSquareToggle.image = "icons/square_unlocked.png";

			selectSquareToggle.imageSizeX = 40 * controlsSizeMultiplier;
			selectSquareToggle.imageSizeY = 40 * controlsSizeMultiplier;

			if (selectMode && !changeCameraMode && !pasteMode)
			{
				selectSquareToggle.draw();

				if (selection)
					selectAddToggle.draw();
			}
		}
	}

	public void cloneSelectorProperties()
	{
		GameObject o = (currentPlaceable == Placeable.obstacle ? mouseObstacle : mouseTank);
		o.initSelectors(this);

		o.forAllSelectors(s ->
		{
			LevelEditorSelector<?> s1 = ScreenLevelEditor.selectors.get(s.id);
			if (s1 != null)
				s.cloneProperties(s1);
			else if (s.modified())
				ScreenLevelEditor.selectors.put(s.id, s);
		});
	}

	public void play()
	{
		this.save();
		this.replaceSpawns();

		Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
		Game.game.unbreakableGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

		for (Obstacle o : Game.obstacles)
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
		}

		Game.currentLevel = new Level(Game.currentLevelString);
		Game.currentLevel.timed = level.timer > 0;
		Game.currentLevel.timer = level.timer;

		Game.resetNetworkIDs();
		for (Movable m: Game.movables)
		{
			if (m instanceof Tank)
				((Tank) m).registerNetworkID();
		}

		Game.screen = new ScreenGame(this.name);
		Game.player.hotbar.coins = this.level.startingCoins;
	}

	public void replaceSpawns()
	{
		int playerCount = 1;
		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
			playerCount += ScreenPartyHost.server.connections.size();

		ArrayList<Integer> availablePlayerSpawns = new ArrayList<>();

		for (int i = 0; i < playerCount; i++)
		{
			if (availablePlayerSpawns.isEmpty())
			{
				for (int j = 0; j < this.spawns.size(); j++)
					availablePlayerSpawns.add(j);
			}

			int spawn = availablePlayerSpawns.remove((int) (Math.random() * availablePlayerSpawns.size()));

			double x = this.spawns.get(spawn).posX;
			double y = this.spawns.get(spawn).posY;
			double angle = this.spawns.get(spawn).angle;
			Team team = this.spawns.get(spawn).team;

			if (ScreenPartyHost.isServer)
			{
				Game.addPlayerTank(Game.players.get(i), x, y, angle, team);
			}
			else if (!ScreenPartyLobby.isClient)
			{
				TankPlayer tank = new TankPlayer(x, y, angle);

				if (spawns.size() <= 1)
					tank.drawAge = Game.tile_size;

				Game.playerTank = tank;
				tank.team = team;
				Game.movables.add(tank);
			}
		}

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m instanceof TankSpawnMarker)
				Game.removeMovables.add(m);
		}
	}

	public void clearSelection()
	{
		selection = false;

		ArrayList<Integer> x = new ArrayList<>();
		ArrayList<Integer> y = new ArrayList<>();

		for (int i = 0; i < selectedTiles.length; i++)
		{
			for (int j = 0; j < selectedTiles[i].length; j++)
			{
				if (selectedTiles[i][j])
				{
					x.add(i);
					y.add(j);
				}

				selectedTiles[i][j] = false;
			}
		}

		this.undoActions.add(new Action.ActionSelectTiles(this, false, x, y));

	}

	public void refreshSelection()
	{
		selection = false;

		for (int x = 0; x < Game.currentSizeX; x++)
		{
			for (int y = 0; y < Game.currentSizeY; y++)
			{
				if (selectedTiles[x][y])
				{
					selection = true;
					break;
				}
			}
		}
	}

	public void copy(boolean cut)
	{
		ArrayList<Tank> tanks = new ArrayList<>();
		ArrayList<Obstacle> obstacles = new ArrayList<>();

		clipboard = new Clipboard();

		for (Obstacle o : Game.obstacles)
		{
			int x = (int) ((o.posX - 25) / 50);
			int y = (int) ((o.posY - 25) / 50);

			if (x >= 0 && y >= 0 && x < this.selectedTiles.length && y < this.selectedTiles[0].length && this.selectedTiles[x][y])
			{
				try
				{
					Obstacle n = (Obstacle) o.clone();
					clipboard.add(n);

					if (cut)
					{
						obstacles.add(o);
						Game.removeObstacles.add(o);
					}
				}
				catch (Exception e)
				{
					Game.exitToCrash(e);
				}
			}
		}

		for (Movable t : Game.movables)
		{
			if (!(t instanceof Tank))
				continue;

			int x = (int) ((t.posX - 25) / 50);
			int y = (int) ((t.posY - 25) / 50);

			if (x >= 0 && y >= 0 && x < this.selectedTiles.length && y < this.selectedTiles[0].length && this.selectedTiles[x][y])
			{
				try
				{
					Tank n;

					if (t.getClass().equals(TankAIControlled.class))
					{
						n = new TankAIControlled(((TankAIControlled) t).name, t.posX, t.posY, t.size, ((TankAIControlled) t).colorR, ((TankAIControlled) t).colorG, ((TankAIControlled) t).colorB, ((TankAIControlled) t).angle, ((TankAIControlled) t).shootAIType);
						((TankAIControlled) t).cloneProperties((TankAIControlled) n);
					}
					else
						n = (Tank) t.getClass().getConstructor(String.class, double.class, double.class, double.class).newInstance(((Tank) t).name, t.posX, t.posY, ((Tank) t).angle);

					n.team = t.team;
					n.destroy = t.destroy;
					clipboard.add(n);

					if (cut)
					{
						if (t instanceof TankSpawnMarker && this.spawns.size() > 1)
						{
							this.spawns.remove(t);
							tanks.add((Tank) t);
							Game.removeMovables.add(t);
						}
						else if (!(t instanceof TankSpawnMarker))
						{
							tanks.add((Tank) t);
							Game.removeMovables.add(t);
						}
					}

				}
				catch (Exception e)
				{
					Game.exitToCrash(e);
				}
			}
		}

		clipboard.updateParams();

		this.clearSelection();

		if (cut)
			undoActions.add(new Action.ActionCut(tanks, obstacles, (Action.ActionSelectTiles) this.undoActions.remove(this.undoActions.size() - 1)));

		if (!clipboard.isEmpty())
			this.pasteMode = true;

		clipboards[selectedNum] = clipboard;
	}

	@Override
	public void setupLights()
	{
		for (Obstacle o : Game.obstacles)
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

		for (Movable o : Game.movables)
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

		for (Effect o : Game.effects)
		{
			if (o != null && o.lit())
			{
				double[] l = o.getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.posZ) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}
	}

	public void refreshMouseTank()
	{
		Tank t;
		if (tankNum < Game.registryTank.tankEntries.size())
			t = Game.registryTank.getEntry(tankNum).getTank(mouseTank.posX, mouseTank.posY, mouseTank.angle);
		else
			t = this.level.customTanks.get(tankNum - Game.registryTank.tankEntries.size()).instantiate(mouseTank.name, mouseTank.posX, mouseTank.posY, mouseTank.angle);

		t.drawAge = mouseTank.drawAge;
		mouseTank = t;
		OverlayObjectMenu.loadSelectors(t, null, this);
	}

	public double clampTileX(double x)
	{
		return Math.max(Game.tile_size / 2, Math.min((Game.currentSizeX - 0.5) * Game.tile_size, x));
	}

	public double clampTileY(double y)
	{
		return Math.max(Game.tile_size / 2, Math.min((Game.currentSizeY - 0.5) * Game.tile_size, y));
	}

	@Override
	public ArrayList<TankSpawnMarker> getSpawns()
	{
		return this.spawns;
	}

	public enum Placeable {enemyTank, playerTank, obstacle}

	@Override
	public double getOffsetX()
	{
		return offsetX - Game.currentSizeX * Game.tile_size / 2 + Panel.windowWidth / Drawing.drawing.scale / 2;
	}

	@Override
	public double getOffsetY()
	{
		return offsetY - Game.currentSizeY * Game.tile_size / 2 + (Panel.windowHeight - Drawing.drawing.statsHeight) / Drawing.drawing.scale / 2;
	}

	@Override
	public double getScale()
	{
		return Drawing.drawing.unzoomedScale * zoom;
	}

	public static class EditorButton extends Button
	{
		public ToBooleanFunction disabledFunc;
		public ToBooleanFunction shownFunc;
		public InputBindingGroup keybind;

		public double baseImageSX;
		public double baseImageSY;
		public boolean shown;

		public EditorButton(ArrayList<EditorButton> location, String image, double imageSX, double imageSY, Runnable f, String description, InputBindingGroup keybind)
		{
			this(location, image, imageSX, imageSY, f, () -> false, () -> true, description, keybind);
		}

		public EditorButton(
				ArrayList<EditorButton> location, String image, double imageSX, double imageSY, Runnable f,
				ToBooleanFunction disabledFunc, String description, InputBindingGroup keybind
		)
		{
			this(location, image, imageSX, imageSY, f, disabledFunc, () -> true, description, keybind);
		}

		public EditorButton(
				String image, double imageSX, double imageSY, Runnable f,
				ToBooleanFunction disabledFunc, String description, InputBindingGroup keybind
		)
		{
			this(null, image, imageSX, imageSY, f, disabledFunc, () -> true, description, keybind);
		}

		public EditorButton(
				String image, double imageSX, double imageSY, Runnable f,
				ToBooleanFunction disabledFunc, ToBooleanFunction shownFunc,
				String description, InputBindingGroup keybind
		)
		{
			this(null, image, imageSX, imageSY, f, disabledFunc, () -> true, description, keybind);
		}

		public EditorButton(
				ArrayList<EditorButton> location, String image, double imageSX, double imageSY, Runnable f,
				ToBooleanFunction disabledFunc, ToBooleanFunction shownFunc,
				String description, InputBindingGroup keybind
		)
		{
			super(0, -1000, 70, 70, "", f, String.format(description, keybind.getInputs()));

			this.image = "icons/" + image;
			this.imageSizeX = imageSX;
			this.imageSizeY = imageSY;
			this.baseImageSX = imageSX;
			this.baseImageSY = imageSY;

			this.keybind = keybind;
			this.disabledFunc = disabledFunc;
			this.shownFunc = shownFunc;

			if (location != null)
				location.add(this);
		}
	}

	public static class EditorButtons
	{
		public ScreenLevelEditor editor;
		public ArrayList<EditorButton>[] buttons;

		public boolean prevShowControls = true;
		public boolean slideAnimation = false;
		public double animationTimer = 50;
		public double animationMultiplier = 0;

		public double[] xs;
		public double[] ys;

		public ArrayList<EditorButton> topLeft = new ArrayList<>();
		public ArrayList<EditorButton> topRight = new ArrayList<>();
		public ArrayList<EditorButton> bottomLeft = new ArrayList<>();
		public ArrayList<EditorButton> bottomRight = new ArrayList<>();

		public EditorButtons(ScreenLevelEditor editor)
		{
			this.editor = editor;
		}

		public void draw()
		{
			if (editor.paused)
				return;

			updateAnimation();

			if ((!editor.showControls && !slideAnimation) || buttons == null)
				return;

			for (ArrayList<EditorButton> bl : buttons)
				for (EditorButton b : bl)
					if (b.shown)
						b.draw();
		}

		public void update()
		{
			buttons = new ArrayList[]{topLeft, topRight, bottomLeft, bottomRight};

			if (Game.game.window.hasResized)
				refreshButtons();

			for (ArrayList<EditorButton> bl : buttons)
			{
				for (EditorButton b : bl)
				{
					if (!b.fullInfo)
						refreshButtons();

					boolean prev = b.shown;
					//noinspection AssignmentUsedAsCondition
					if (b.shown = b.shownFunc.apply())
					{
						b.enabled = !b.disabledFunc.apply();

						if (editor.showControls)
							b.update();

						if (b.keybind.isValid())
						{
							b.function.run();
							b.keybind.invalidate();
						}
					}

					if (b.shown != prev)
						refreshButtons();
				}
			}
		}

		public void updateAnimation()
		{
			if (editor.showControls != prevShowControls)
			{
				slideAnimation = true;
				animationTimer = 50 - animationTimer;
				prevShowControls = editor.showControls;
			}

			if (slideAnimation)
			{
				animationTimer += Panel.frameFrequency * 2;
				animationMultiplier = Math.sin(animationTimer / 50 * Math.PI / 2);
				refreshButtons();

				if (animationTimer > 50)
					slideAnimation = false;
			}
		}

		public void refreshButtons()
		{
			if (buttons == null)
				return;

			updateCornerCoords();

			boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >= Game.game.window.absoluteHeight - Drawing.drawing.statsHeight;
			double vStep = 0;
			double hStep = 0;

			if (vertical)
				vStep = 100 * editor.controlsSizeMultiplier;
			else
				hStep = 100 * editor.controlsSizeMultiplier;

			for (int i = 0; i < 4; i++)
				setPositionAndParams(buttons[i], i, hStep, vStep);
		}

		public void updateCornerCoords()
		{
			double x1 = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50 * editor.controlsSizeMultiplier;
			double y1 = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale
					- Drawing.drawing.interfaceSizeY) / 2 + 50 * editor.controlsSizeMultiplier;
			double x2 = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - 50 * editor.controlsSizeMultiplier - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
			double y2 = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
					+ Drawing.drawing.interfaceSizeY - 50 * editor.controlsSizeMultiplier;

			xs = new double[]{x1, x2, x1, x2};
			ys = new double[]{y1, y1, y2, y2};
		}

		public void setPositionAndParams(ArrayList<EditorButton> arr, int pos, double h, double v)
		{
			if (pos == 1 || pos == 3)
				h = -h;

			if (pos == 2 || pos == 3)
				v = -v;

			if (slideAnimation)
			{
				double[] axis = v != 0 ? xs : ys;
				boolean direction = v != 0 ? pos % 2 == 0 : pos < 2;
				if (!editor.showControls)
					direction = !direction;

				axis[pos] += (animationMultiplier * (direction ? 1 : -1) * 100 * editor.controlsSizeMultiplier) + (editor.showControls ? -1 : 0) * (pos % 2 == 1 ? -1 : 1) * 100 * editor.controlsSizeMultiplier;
			}

			EditorButton prev = null;

			for (int i = 0; i < arr.size(); i++)
			{
				EditorButton b = arr.get(i);
				if ((v != 0 ? v : h) < 0)
					b = arr.get(arr.size() - i - 1);

				if (i > 0 && prev != null)
					b.setPosition(prev.posX + h, prev.posY + v);
				else
					b.setPosition(xs[pos], ys[pos]);

				b.fullInfo = true;
				b.sizeX = b.sizeY = 70 * editor.controlsSizeMultiplier;
				b.imageSizeX = b.baseImageSX * editor.controlsSizeMultiplier;
				b.imageSizeY = b.baseImageSY * editor.controlsSizeMultiplier;

				//noinspection AssignmentUsedAsCondition
				if (b.shown = b.shownFunc.apply())
					prev = b;
			}
		}
	}

	public enum SymmetryType
	{none, flipHorizontal, flipVertical, flipBoth, flip8, rot180, rot90}

	public static class Clipboard
	{
		public ArrayList<Tank> tanks = new ArrayList<>();
		public ArrayList<Obstacle> obstacles = new ArrayList<>();

		public double centerX, centerY;
		public double minX;
		public double minY;
		public double maxX;
		public double maxY;

		public void add(Object o)
		{
			if (o instanceof Obstacle)
				obstacles.add((Obstacle) o);
			else
				tanks.add((Tank) o);
		}

		public void remove(Object o)
		{
			if (o instanceof Obstacle)
				obstacles.remove(o);
			else
				tanks.remove(o);
		}

		public int size()
		{
			return tanks.size() + obstacles.size();
		}

		public boolean isEmpty()
		{
			return tanks.isEmpty() && obstacles.isEmpty();
		}

		public void updateParams()
		{
			minX = minY = 9999;
			maxX = maxY = -9999;

			for (Obstacle o : obstacles)
			{
				minX = Math.min(minX, o.posX);
				minY = Math.min(minY, o.posY);
				maxX = Math.max(maxX, o.posX);
				maxY = Math.max(maxY, o.posY);
			}

			for (Tank t : tanks)
			{
				minX = Math.min(minX, t.posX);
				minY = Math.min(minY, t.posY);
				maxX = Math.max(maxX, t.posX);
				maxY = Math.max(maxY, t.posY);
			}

			centerX = (maxX - minX) / 2;
			centerY = (maxY - minY) / 2;

			for (Obstacle o : obstacles)
			{
				o.posX -= minX;
				o.posY -= minY;
			}

			for (Tank t : tanks)
			{
				t.posX -= minX;
				t.posY -= minY;
			}
		}

		public void flipHorizontal()
		{
			for (Obstacle o : obstacles)
				o.posX = -(o.posX - centerX) + centerX;

			for (Tank t : tanks)
				t.posX = -(t.posX - centerX) + centerX;
		}

		public void flipVertical()
		{
			for (Obstacle o : obstacles)
				o.posY = -(o.posY - centerY) + centerY;

			for (Tank t : tanks)
				t.posY = -(t.posY - centerY) + centerY;
		}

		public void rotate()
		{
			double ts = Game.tile_size;

			for (Obstacle o : obstacles)
			{
				double x = o.posX - centerX, y = o.posY - centerY;
				o.posX = -y + centerX;
				o.posY = x + centerY;
			}

			for (Tank t : tanks)
			{
				double x = t.posX - centerX, y = t.posY - centerY;
				t.posX = -y + centerX;
				t.posY = x + centerY;
				t.angle += Math.PI / 2;
			}

			updateParams();
		}
	}

	@Override
	public void onAttemptClose()
	{
		paused = true;
		Game.screen = new OverlayConfirmSave(Game.screen, this);
	}

	public static abstract class Action
	{
		public abstract void undo();

		public abstract void redo();

		static class ActionObstacle extends Action
		{
			public boolean add;
			public Obstacle obstacle;

			public ActionObstacle(Obstacle o, boolean add)
			{
				this.obstacle = o;
				this.add = add;
			}

			@Override
			public void undo()
			{
				if (!add)
				{
					Game.addObstacle(this.obstacle);
					this.obstacle.removed = false;
				}
				else
					Game.removeObstacles.add(this.obstacle);
			}

			@Override
			public void redo()
			{
				if (add)
				{
					Game.addObstacle(this.obstacle);
					this.obstacle.removed = false;
				}
				else
				{
					Game.removeObstacles.add(this.obstacle);
				}
			}
		}

		static class ActionTank extends Action
		{
			public boolean add;
			public Tank tank;

			public ActionTank(Tank t, boolean add)
			{
				this.tank = t;
				this.add = add;
			}

			@Override
			public void undo()
			{
				if (add)
					Game.removeMovables.add(this.tank);
				else
					Game.movables.add(this.tank);
			}

			@Override
			public void redo()
			{
				if (!add)
					Game.removeMovables.add(this.tank);
				else
					Game.movables.add(this.tank);
			}
		}

		static class ActionPlayerSpawn extends Action
		{
			public ScreenLevelEditor screenLevelEditor;
			public boolean add;
			public TankSpawnMarker tank;

			public ActionPlayerSpawn(ScreenLevelEditor s, TankSpawnMarker t, boolean add)
			{
				this.screenLevelEditor = s;
				this.tank = t;
				this.add = add;
			}

			@Override
			public void undo()
			{
				if (add)
				{
					Game.removeMovables.add(this.tank);
					screenLevelEditor.spawns.remove(this.tank);
				}
				else
				{
					Game.movables.add(this.tank);
					screenLevelEditor.spawns.add(this.tank);
				}
			}

			@Override
			public void redo()
			{
				if (!add)
				{
					Game.removeMovables.add(this.tank);
					screenLevelEditor.spawns.remove(this.tank);
				}
				else
				{
					Game.movables.add(this.tank);
					screenLevelEditor.spawns.add(this.tank);
				}
			}
		}

		static class ActionMovePlayer extends Action
		{
			public ScreenLevelEditor screenLevelEditor;
			public ArrayList<TankSpawnMarker> oldSpawns;
			public TankSpawnMarker newSpawn;

			public ActionMovePlayer(ScreenLevelEditor s, ArrayList<TankSpawnMarker> o, TankSpawnMarker n)
			{
				this.screenLevelEditor = s;
				this.oldSpawns = o;
				this.newSpawn = n;
			}

			@Override
			public void undo()
			{
				Game.removeMovables.add(newSpawn);
				screenLevelEditor.spawns.clear();

				for (TankSpawnMarker t: oldSpawns)
				{
					screenLevelEditor.spawns.add(t);
					Game.movables.add(t);
				}
			}

			@Override
			public void redo()
			{
				Game.removeMovables.addAll(oldSpawns);

				screenLevelEditor.spawns.clear();

				Game.movables.add(newSpawn);
				screenLevelEditor.spawns.add(newSpawn);
			}
		}

		static class ActionSelectTiles extends Action
		{
			public ScreenLevelEditor screenLevelEditor;
			public ArrayList<Integer> x;
			public ArrayList<Integer> y;
			public boolean select;

			public ActionSelectTiles(ScreenLevelEditor s, boolean select, ArrayList<Integer> x, ArrayList<Integer> y)
			{
				this.screenLevelEditor = s;
				this.select = select;
				this.x = x;
				this.y = y;
			}

			@Override
			public void undo()
			{
				for (int i = 0; i < this.x.size(); i++)
				{
					screenLevelEditor.selectedTiles[this.x.get(i)][this.y.get(i)] = !select;
				}

				screenLevelEditor.refreshSelection();
			}

			@Override
			public void redo()
			{
				for (int i = 0; i < this.x.size(); i++)
				{
					screenLevelEditor.selectedTiles[this.x.get(i)][this.y.get(i)] = select;
				}

				screenLevelEditor.refreshSelection();
			}
		}

		static class ActionGroup extends Action
		{
			public ScreenLevelEditor screenLevelEditor;
			public ArrayList<Action> actions;

			public ActionGroup(ScreenLevelEditor s, ArrayList<Action> actions)
			{
				this.screenLevelEditor = s;
				this.actions = actions;
			}

			@Override
			public void undo()
			{
				for (Action a: this.actions)
					a.undo();
			}

			@Override
			public void redo()
			{
				for (Action a: this.actions)
					a.redo();
			}
		}

		static class ActionDeleteCustomTank extends Action
		{
			public ScreenLevelEditor screenLevelEditor;
			public ArrayList<Action> actions;
			public TankAIControlled tank;

			public ActionDeleteCustomTank(ScreenLevelEditor s, ArrayList<Action> actions, TankAIControlled t)
			{
				this.screenLevelEditor = s;
				this.actions = actions;
				this.tank = t;
			}

			@Override
			public void undo()
			{
				for (Action a: this.actions)
					a.undo();

				this.screenLevelEditor.level.customTanks.add(this.tank);
			}

			@Override
			public void redo()
			{
				for (Action a: this.actions)
					a.redo();

				this.screenLevelEditor.level.customTanks.remove(this.tank);
			}
		}


		static class ActionPaste extends Action
		{
			public ScreenLevelEditor levelEditor;
			public ArrayList<Action> actions;

			public ActionPaste(ScreenLevelEditor s, ArrayList<Action> actions)
			{
				this.levelEditor = s;
				this.actions = actions;
			}

			@Override
			public void undo()
			{
				for (int i = this.actions.size() - 1; i >= 0; i--)
					this.actions.get(i).undo();
			}

			@Override
			public void redo()
			{
				for (Action a: actions)
					a.redo();
			}
		}

		static class ActionCut extends Action
		{
			public ArrayList<Tank> tanks;
			public ArrayList<Obstacle> obstacles;
			public ActionSelectTiles deselect;

			public ActionCut(ArrayList<Tank> tanks, ArrayList<Obstacle> obstacles, ActionSelectTiles deselect)
			{
				this.tanks = tanks;
				this.obstacles = obstacles;
				this.deselect = deselect;
			}

			@Override
			public void undo()
			{
				for (Obstacle o : Game.obstacles)
					Game.addObstacle(o);
				Game.movables.addAll(this.tanks);
				this.deselect.undo();
			}

			@Override
			public void redo()
			{
				Game.removeObstacles.addAll(this.obstacles);

				for (int i = 0; i < Game.movables.size(); i++)
				{
					if (Game.movables.get(i) instanceof Tank)
					{
						for (Tank o : this.tanks)
						{
							if (Game.movables.get(i).equals(o))
								Game.movables.remove(i);
						}
					}
				}

				this.deselect.redo();
			}
		}
	}

}