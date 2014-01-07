package events;

import java.util.EventObject;

public class CommandTimeoutEvent extends EventObject {
    private static final long serialVersionUID = 1L;

    public CommandTimeoutEvent(Object source) {
        super(source);
        // TODO Auto-generated constructor stub
    }

}
