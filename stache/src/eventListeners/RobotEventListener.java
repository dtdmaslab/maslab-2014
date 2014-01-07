package eventListeners;

import java.util.EventListener;

import events.CommandTimeoutEvent;
import events.MapUpdateEvent;

public interface RobotEventListener extends EventListener {
    public void mapUpdateEventOccurred(MapUpdateEvent e);
    public void commandTimeoutEventOccurred(CommandTimeoutEvent e);
    // TODO: Think about what kinds of events the logic is going to want to act
    // on.
}
