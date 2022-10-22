# Tanks Multiplayer Modding API

This is a multiplayer modding API for Tanks. I'll be calling this project Mod API for short.

I've often wanted to use more than the default obstacles and tanks to make my level ideas come true.
I wanted to make modding easier, while also having support in the base game.
Thus, I created this modding API to allow others to create their own custom levels and games more easily.

Hopefully the Mod API will eventually be added into the base game, too, so people can play custom levels without needing to download the Mod API client!

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

You can download the JAR file [here](https://onedrive.live.com/download?cid=1E1C6A69D73A57B9&resid=1E1C6A69D73A57B9%21133&authkey=AJ_Hzn30cBgN7RU).

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
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

[Older Changelogs](changelog.md)