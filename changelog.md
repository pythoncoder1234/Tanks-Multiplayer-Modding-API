Changelog
---

**Mod API 1.2.2**
- New way to aim arc bullets when in third person
- All movables implement `ISolidObject` now, you can override `rayCollision` to turn on ray collision
- Tank optimization begins - Tank updates alternate between frames now, and update ≥ 30 times per second
- Pressing left alt/option makes your trace ray ignore tanks
- Added `Drawing.drawing.playGameSound` - adjusts sound volume based on how far away it is to the player tank
- `Movable.distanceBetween` now supports using two xy-coordinate pairs and two obstacles
- Implemented `IExplodable` using some of F6's code
- Added `Movable.squaredDistanceBetween`
- Added `updateFrequency`, use when modifying or overriding `TankAIControlled`'s update function
- Lowest instantaneous FPS counter
- Fixed many, many bugs

**Mod API 1.2.1**
- Ported to Java 8

**Mod API 1.2.0**
- Double-click the editor tool's button or keybind to open its submenu, if it has one.
- Using +/- on a selection changes it for all objects in the selection
- Magic wand select
- Undo/Redo 10x or 50x
- Circle tool, square tool, line tool
- Alt/Opt + scroll = zoom, Alt/Opt + drag = pan
- Added double-click functionality to input bindings and buttons
- Changed `LevelEditorSelector.init` access to protected, use `initBase` instead
- Changed `Minigame.start` access to protected, use `startBase` instead
- Changed `LevelEditorSelector.changeMetadata` access to protected, use `changeMeta` instead
- Renamed `EndCondition.defaultEndCondition` to `EndCondition.normal`
- Moved `Game.ModAPIVersion` to `ModAPI.version`

**Mod API 1.2.e**
- **Reference spawns**
  - Hold shift on the exit buttons for level and crusade editor to save without tank references (info icon should appear while doing so)
- Getting closer and closer to full 1.5.2d multiplayer compatibility!
- Crusade Import overhaul
- Minimap overhaul
- Fixed multiplayer chat
- F3+D = clear chat (like in minecraft)
- Improved Tank AI
- Did some merging
- Fixed bugs

**Mod API v1.2.d**
- New Tank AI (black tanks are really op now)
- Smartness parameter in custom tanks
- Stole the respawn player function from arcade mode
- Bug fixes and other minor improvements

**Mod API v1.2.c**
- Hotbar swap item and drop item
- Added images for horizontal flip and vertical flip
- Bug fixes

**Mod API v1.2.0**
- *w*a*v*y bushes and water (shaders are cool)
- `Game.getObstacle(x, y)` and `Game.getSurfaceObstacle(x, y)` fetches the tile or surface tile at the specified grid location
- Lots of bug fixes (thanks furret)
- 
**Mod API v1.2.b**:
- Implemented new renderer
- A bit of multiplayer testing
- "Import from crusade" button in level options
  - And also un-import button
- You can edit a crusade without resetting its progress now
- Crusade descriptions
- Press shift in editor and drag to pan, or scroll to zoom
- Flip and rotate buttons in editor
- "Sync levels" and "Sync item" buttons in crusade editor
- Option to overwrite item and tank templates on save
- Tanks that can lay mines now shoot their own mines
- Custom tank compression
- Fixed conveyor belts
- Press shift + your copy keybind while in level options to copy the level's level string
- Fixed event listeners
- Scoreboards use event listeners now

**Mod API v1.2.a**:
- `LevelEditorSelector`: Customizable and modular editor properties
- `ScreenLevelEditor.buttons`: Made adding buttons to the editor much simpler.
- A variety of selectors already added in
- Registered `ObstacleText`: ability to add text to level editor
- Made some super smooth transitions
- Extensions menu in options!
- Minimap input screen is finally back
  - default panning controls: u, h, j, and k

- Many hotbar improvements:
  - Shows individual item, mine, and shield cooldown
- Mine collision! This means throwable mines, blowable mines, freezable mines, and more!
- Implemented lava obstacle
- Many, many bug fixes
  
<br>

**Mod API v1.1.4**:
- Revamped multiplayer crusades
- Fixed multiplayer crusade bugs
- Lowered third person turning sensitivity
- Added zoom controls to first and third person (press c and scroll)
- :train:
- Added two methods to `Tank`s: `sendCreateEvent()` and `sendUpdateEvent()`.
- Removed `IModdedTank`

**Mod API v1.1.3**
- Updated to Tanks 1.5.1
- Added an option to fix the number of network events tanks send every second
- `ItemBar.setEnabled`: sets the state of the item bar for all players.
- Added the `enableRemote` variable into `Minigame` to determine whether the client side should launch the minigame as well.
- Tanks now shoot their mines
- Fixed multiple multiplayer bugs

<br>

**Mod API v1.1.2**
- Updated to Tanks 1.5.0
- Removed ModLevel and renamed ModGame to Minigame
- Fields can now be synced across the network, which means you can change it without sending an event. Read the JavaDoc on the `ISyncable` and `SyncedFieldMap` classes.
- Added synced fields to the text and scoreboard. To use them, set their `syncEnabled` property to true.
- Added animations! These can change the zoom, position, and opacity of text or shapes.
- Scoreboards are now sorted, and their SortOrder can be customized.
- `TextWithStyling`: Displays text with styling. All fields are synced.
- `EndCondition`: Override to change the condition for teams or players to win.
- `EndText`: Override to change the text shown on `ScreenInterlevel`
- `EventListener`: Listens for network events, such as `EventShootBullet` for bullet firing.
- `MusicState`: Changes the music that is currently being played on the current `Game.screen`.
- `IModdedTank`: Implement to give the tank a custom creation network event.
- Movables now have a `CustomPropertiesMap` in which you can store data in each movable.
- Bug fixes and other minor improvements

<br>

**Mod API v1.1.1**
- Mapmaking mode: Run with the `mapmaking` launch argument or enable the feature in debug menu!
- Gives access to hidden obstacles:
- Path obstacle, changes its color automatically based on the level background
- Hill obstacle, changes its color ot the same level as a background, but has changeable stack height
- Updated game version support to Tanks 1.4.1
- Press F to move the third person camera up and down with the cursor
- Level editor improvements (shift click Block Height to change the starting height of an obstacle!)
- Added functions in ModGame to override the "Victory!" message
- The base damage for healing rays and flamethrowers is now 1, and changing the value will multiply how much damage the bullet will do.
- MapLoader class, stitches levels together to create a map (unfinished, buggy)
- Added documentation to ModAPI class
- Bug fixes and other minor improvements
- `getSeverity()` in `IAvoidObject` now takes a `Tank` parameter (to fix, replace `posX` with `t.posX`)
- `Game.tileDrawables` removed, you can use `Game.obstacleMap[x][y] != null` instead

<br>

**Mod API v1.1.1b**
- Shift click `Block Height` in the level editor for something cool ;D
- Updates to MapLoader
- Added documentation
- Bug fixes and other minor improvements
- `getSeverity()` in `IAvoidObject` now takes a `Tank` parameter (to fix, replace `posX` with `t.posX`)
- `Game.tileDrawables` removed, you can use `Game.obstacleMap[x][y] != null` instead


**Mod API v1.1.1a**
- Updated game version support to Tanks 1.4.1b
- MapLoader class, stitches levels together to create a map (singleplayer only)
- Press F to move the third person camera up and down with the cursor
- Level editor improvements
- Bug fixes and other minor improvements


**Mod API v1.1.0:**
- Now using Tanks v1.3.1!
- Added the `ModGame` class.
    - Not limited to one level.
    - Can change the condition in which the level ends.
    - Can disable shooting and laying mines.
- Added the `CustomMovable` class, which sends draw instructions through the network.
- The NPC tank can now forcibly display its message via a function call, and change its messages.
- Added kill messages in `ModGame`s and `ModLevel`s, only visible in multiplayer.
- Added the `CustomShape` class, which draws shapes on the screen
- Added the `TransitionEffect` class, which makes fading effects on the screen
- Added the `Transparent Tall Tiles` option to make obstacles that are above ground semi-transparent
- Added the `Colors` class, which contains commonly used colors in Tanks.
- Teams are now split evenly (can be disabled in a level's Team option)
- Options are now accessible within parties, including name and tank color changing
- Added custom zoom and auto zoom, along with messages that display when you change it.
- Added the _Events per Second_ setting to Party Host Options
- Added the ability to pause the game if the window loses focus
- Improved Tank AI
- Improved water

**Mod API v1.0.1:**
- Added light mode to minimap (toggle theme with the L key)
- Added panning to minimap. Default controls are the numpad 8, 4, 6, and 2.
- Added a Tanks mode to the minimap, toggle with the P key, which does not show tiles such as mud and ice.
- Added a clouds and sky for the game.
- NPC tanks now support a customizable shop, name tags, and animated text.
- Improved the paste function in the level editor
- Fixed actionbar text
- Added a lot more scoreboard objective types
- Changed the max height of obstacles to 8
- Fixed a bug where Medic Tanks do not heal you after you put on shields

**Mod API v1.0.0:**
- Extensions now support custom levels!
- A minimap with changeable zoom (use the - or = key or their equivalent keys on the numpad)
- Text boxes to search up the names of levels and crusades
- Changed the max height of obstacles to 6.0
- Changed the custom level a bit
- Published first person mode and immersive camera (go to Options - Graphics Options - view)
- or use F5 key to toggle perspective

**Mod API v0.2.0:**
- A scoreboard
- Title, subtitle, and actionbar text
- A new custom level instead of the tutorial
- Copy, cut, and paste in the level editor
- A confirm prompt of saving when you try to exit the game while in a level editor
- Other minor bug fixes and tweaks

**Mod API v0.1.0:**
- An NPC Tank (drive close to it and press e to talk with it)
- The ability to add a text obstacle
- Sand and Water obstacles because they seem to be popular modding ideas
- A guide to this Modding API that may instead be changed to be online
- A few minor tweaks to the code to allow them to support custom levels
