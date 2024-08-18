package tanks.gui.screen;

import java.util.UUID;

public interface IPartyGameScreen
{
    // if a screen class implements this, players will not be able to join a party while this is being displayed
    default boolean onEnter(String username, UUID clientID) { return false; }
}
