package command;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import util.RunCommand;

import common.Robot;

public class SequentialCommand implements Command {
    protected Queue<Command> commands;

    public SequentialCommand(Command command1, Command command2) {
        commands = new ConcurrentLinkedQueue<Command>();
        commands.add(command1);
        commands.add(command2);
    }

    public SequentialCommand(Collection<Command> commands) {
        this.commands = new ConcurrentLinkedQueue<Command>(commands);  
    }

    /**
     * Execute all commands sequentially.
     */
    @Override
    public void execute(Robot robot) {
        while (!commands.isEmpty()) {
            // Just peek for now; if we remove the item, we may indicate that
            // we are complete when in fact we are not.
            Command command = commands.peek();
            try {
                new Thread(new RunCommand(command, robot)).join((long)command.getTimeout());
            } catch (InterruptedException e) {
                // This will never happen.
                throw new RuntimeException("Cannot occur");
            }
            commands.remove();
        }
    }

    /**
     * Timeout is the sum of the timeout for all commands.
     */
    @Override
    public float getTimeout() {
        // Set it to a value greater than 0 so that it is impossible for this
        // thread to be interrupted, since all the child threads will be
        // interrupted if they go over their time limit.
        float timeout = 1;
        for (Command c : commands) {
            timeout += c.getTimeout();
        }
        return timeout;
    }

    @Override
    public boolean isComplete() {
        return commands.isEmpty();
    }
}
