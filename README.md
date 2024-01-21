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

You can download the JAR file [here](https://onedrive.live.com/download?resid=1E1C6A69D73A57B9%21260&authkey=!AJp3wMJcfvURVhw).

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
---

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

[Older Changelogs](changelog.md)