Older Changelogs
---

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
