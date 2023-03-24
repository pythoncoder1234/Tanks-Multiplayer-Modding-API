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

You can download the JAR file [here](https://onedrive.live.com/download?cid=1E1C6A69D73A57B9&resid=1E1C6A69D73A57B9%21136&authkey=ACQuBT0dXqlDsCM).

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
---

- Updated to Tanks 1.5.0
- Removed ModLevel and renamed ModGame to Minigame


- Fields can now be synced across the network, which means you can change it without sending an event. Read the JavaDoc on the `ISyncable` and `SyncedFieldMap` classes.
- Added synced fields to the text and scoreboard. To use them, set their `syncEnabled` property to true.
- Added animations! These can change the zoom, position, and opacity of text or shapes.
- Scoreboards are now sorted, and their sorting can be customized.
- `TextWithStyling`: Displays text with styling. All fields are synced.
- `EndCondition`: Override to change the condition for teams or players to win. 
- `EndText`: Override to change the text shown on `ScreenInterlevel`
- `EventListener`: Listens for network events, such as `EventShootBullet` for bullet firing.
- `MusicState`: Changes the music that is currently being played on the current `Game.screen`.
- `IModdedTank`: Implement to give the tank a custom creation network event.
- Movables now have a `CustomPropertiesMap` in which you can store data in each movable.
- Bug fixes and other minor improvements


[Older Changelogs](changelog.md)