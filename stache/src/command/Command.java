package command;

import common.Robot;

public interface Command {
    public void execute(Robot robot);
    public boolean isComplete();

    /**
     * This value should be constant for a given Command object, though not
     * necessarily for all Commands of a given class.
     */
    public float getTimeout();
}
