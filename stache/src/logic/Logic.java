package logic;

import java.util.Collection;

import util.RunCommand;

import command.Command;
import command.SequentialCommand;
import common.Robot;

/**
 * Classes that wish to define behavior for the robot should extend this class.
 * They should add commands to the queue, either in the constructor or
 * registered on Robot events (see Robot.java for details). The commands will
 * be executed sequentially.
 */
public abstract class Logic {
    public final void execute(Robot robot) {
        Command command = new SequentialCommand(this.getCommands());
        try {
            new Thread(new RunCommand(command, robot)).join((long) command.getTimeout());
        } catch (InterruptedException e) {
            // This will never happen.
            throw new RuntimeException("Impossible.");
        }
    }

    protected abstract Collection<Command> getCommands();
}
