package common;

import java.util.Vector;

import javax.swing.event.EventListenerList;

import eventListeners.RobotEventListener;

public class Robot {
    protected RobotDriver driver;
    protected float x;
    protected float y;
    protected EventListenerList listeners;

    /**
     * Constructor.
     */
    public Robot() {
        listeners = new EventListenerList();
    }

    /**
     * Adds a listener for Robot events.
     */
    public void addRobotEventListener(RobotEventListener listener) {
        listeners.add(RobotEventListener.class, listener);
    }

    /**
     * Removes a listener.
     */
    public void removeRobotEventListener(RobotEventListener listener) {
        listeners.remove(RobotEventListener.class, listener);
    }

    /**
     * Calculates the heading from the raw data and normalizes it.
     *
     * @return A float representing the heading in radians, between -pi and pi.
     */
    public float getHeading() {
        // TODO, write actual code.
        return 0; // driver.getGyro();
    }
    // TODO: Finish this.

    /**
     * Returns an estimate of robot's position.
     */
    public Vector<Float> getPosition() {
        Vector<Float> position = new Vector<Float>();
        position.add(x);
        position.add(y);
        return position;
    }

}
