package util;

import command.Command;
import common.Robot;

/**
 * Convenience wrapper for Commands that lets them be Runnable.
 */
public class RunCommand implements Runnable {
    protected Command command;
    protected Robot robot;
    public RunCommand(Command command, Robot robot) {
        this.command = command;
        this.robot = robot;
    }

    @Override
    public void run() {
        command.execute(robot);
    }
}
