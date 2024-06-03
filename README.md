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

Only the JAR is needed if playing with others or making an extension.

Installation
---

You can download the JAR file [here](https://1drv.ms/u/c/1e1c6a69d73a57b9/IQMC8RGNAAneSZw_hNmUpGU3ATOWYRwNB1c363Fp10eVpMI).

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
---

- Chunk loading system
- Huge optimizations
- Lots of bug fixes
- Removed `Game.obstacleGrid` and `Game.surfaceTileGrid` (use `Game.getObstacle` and `Game.getSurfaceObstacle` instead
- Freecam control player (`F7` to toggle)
- Added `Obstacle.afterAdd` and `Obstacle.onNeighborUpdate`
- `F3`+`F8` turns on Record mode, which hides the info bar and speedrun timer
- Target type tank property - choices are allies or enemies, but maybe more in the future
- Removed update skipping for now
- Renamed `Game.sampleTerrainGroundHeight` to `Game.sampleDefaultGroundHeight`

[Older Changelogs](changelog.md)