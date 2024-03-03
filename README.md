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

You can download the JAR file [here](https://onedrive.live.com/download?resid=1E1C6A69D73A57B9%21291&authkey=!ADnrRb8wNzdDHy8).

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
---

- New way to aim arc bullets when in third person
- All movables implement `ISolidObject` now, you can override `rayCollision` to turn on ray collision
- Tank optimization begins - Tank updates alternate between frames now, and update ≥ 30 times per second
- Pressing left alt/option makes your trace ray ignore tanks
- Added `Drawing.drawing.playGameSound` - adjusts sound volume based on how far away it is to the player tank
- `Movable.distanceBetween` now supports using two xy-coordinate pairs and two obstacles
- Implemented `IExplodable` using some of F6's code
- Added `Movable.squaredDistanceBetween`
- Added `updateFrequency`, use when modifying or overriding `TankAIControlled`'s update function
- Fixed many, many bugs

[Older Changelogs](changelog.md)