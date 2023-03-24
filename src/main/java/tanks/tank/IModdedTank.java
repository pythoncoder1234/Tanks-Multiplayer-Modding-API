package tanks.tank;

import tanks.network.event.EventTankCreate;
import tanks.network.event.EventTankUpdate;

public interface IModdedTank
{
    default Class<? extends EventTankCreate> getCreateEvent()
    {
        return EventTankCreate.class;
    }

    default Class<? extends EventTankUpdate> getUpdateEvent()
    {
        return EventTankUpdate.class;
    }
}
