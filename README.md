# Tanks Multiplayer Modding API

This is a multiplayer modding API for Tanks: The Crusades. I'll call this project Mod API for short.

I've often wanted to use more than the default obstacles and tanks to make my level ideas come true.
I wanted to make modding easier, while also having support in the base game.
So I created this modding API to allow others to create their own custom levels and games more easily.


How it works
---
Many custom modding objects will be added per each version. However, these are only for use when modding, and will not be accessible otherwise.
Once a custom game has been made, if it only uses features from the Mod API, it will be fully functional, even if a server hosts and plays the game with the clients.
This is done by sending the objects through the network.

How to use
---

To play with others as a client, only the JAR file is needed.
To create a custom game or level, the source code is needed.

Installation
---

You can download the JAR file [here](https://onedrive.live.com/download?cid=1E1C6A69D73A57B9&resid=1E1C6A69D73A57B9%21134&authkey=ADQ8bnLBKs1ntck).

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
---

**Mod API v1.1.1**
- Mapmaking mode: Run with the `mapmaking` launch argument or enable the feature in debug menu!
- Gives access to hidden obstacles:
- Path obstacle, changes its color automatically based on the level background
- Hill obstacle, changes its color ot the same level as a background, but has changeable stack height
- Updated game version support to Tanks 1.4.1
- Press F to move the third person camera up and down with the cursor
- Level editor improvements (shift click Block Height to change the starting height of an obstacle!)
- Added functions to override the "Victory!" message in ModGame
- The base damage for healing rays and flamethrowers is now 1, and changing the value will multiply how much damage the bullet will do.
- MapLoader class, stitches levels together to create a map (unfinished, buggy)
- Added documentation to ModAPI class
- Bug fixes and other minor improvements


- `getSeverity()` in `IAvoidObject` now takes a `Tank` parameter (to fix, replace `posX` with `t.posX`)
- `Game.tileDrawables` removed, you can use `Game.obstacleMap[x][y] != null` instead


[Older Changelogs](changelog.md)