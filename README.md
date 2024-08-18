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

You can download the JAR file from GitHub releases.

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
---

- Fixed a LOT of bugs
- Hold editor button keybind and scroll to access sub-tools (e.g. b + scroll for more building tools)
  - You can still press 0 to use the default tool
- Textboxes allow the § character now
- Renamed `Movable.squaredDistanceBetween` to `Movable.sqDistBetw`

[Older Changelogs](changelog.md)