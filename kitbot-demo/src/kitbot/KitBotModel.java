package kitbot;

import comm.MapleComm;
import comm.MapleIO.SerialPortType;
import devices.actuators.Cytron;
import devices.sensors.Encoder;
import devices.sensors.Gyroscope;
import devices.sensors.Ultrasonic;

import vision.BallTrack;

public class KitBotModel {
	final static double TURN_THRESH = Math.PI / 18;
	final static double MIN_POWER = 0.05;
	final static double POWER_SCALE = 0.45;

	private MapleComm maple;
	// Motor cytrons
	private Cytron motorACytron = new Cytron(2, 1);
	private Cytron motorBCytron = new Cytron(7, 6);
	// Gyro
	private Gyroscope gyro = new Gyroscope(1, 9);
	// Encoder
	private Encoder encoder = new Encoder(26, 25);
	// Ultrasonic
	private Ultrasonic sonic1 = new Ultrasonic(23, 24);
	private Ultrasonic sonic2 = new Ultrasonic(35, 36);

    private ErrorCalculator ec;
    private double current = 0;
    private double desired = 0;
    private double f = 0;
    private long lastTime = System.currentTimeMillis();

    private double currentDistance = 0;
    private double currentRight = 0;
    private double encoderTotal = 0;

	public KitBotModel() {
		BallTrack.setup();
		try {
			maple = new MapleComm(SerialPortType.LINUX);
			maple.registerDevice(motorACytron);
			maple.registerDevice(motorBCytron);
			maple.registerDevice(gyro);
			maple.registerDevice(sonic1);
			//maple.registerDevice(encoder);
			maple.registerDevice(sonic2);
			maple.initialize();

			// Continually update the sensor data.
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						maple.updateSensorData();
						long time = System.currentTimeMillis();
						long millis = time - lastTime;
						current += (millis * gyro.getOmega()) / 1000.0;
						lastTime = time;
						System.out.println("S1: " + sonic1.getDistance());
						System.out.println("S2: " + sonic2.getDistance());
						double s1 = sonic1.getDistance();
						if (s1 > 0.03 && Math.abs(currentDistance - s1) < 0.5) {
							currentDistance = s1;
						}
						double s2 = sonic2.getDistance();
						if (s2 > 0.03 && Math.abs(currentDistance - s2) < 0.5) {
							currentRight = s2;
						}
						//encoderTotal = encoder.getTotalAngularDistance();
						Thread.yield();
					}
				}
			}).start();

            ec = new ErrorCalculator() {
                @Override
                public float getError() {
                	return (float)normalize(current - desired);
                }
            };
            // TODO: Tune these parameters based on experimentation.
           new PidController(1, 0, 0.75f, ec, new ErrorHandler() {
				@Override
				public void handleError(float error) {
					double m1 = error / -Math.PI;
					double m2 = error / Math.PI;
					//System.out.println(error);
					setMotors(m1 + f, m2 + f);
					//setMotors(f, f);
				}
            });

            // Go forward
            // forward(0.2 /* Speed */, 750 /* Time in ms */);

            // Turn.
            // turn(Math.PI / 2 /* Radians clockwise */);

            // Draw a square.
            // drawSquare();

            // Track closest red ball.
            // trackRed();

            // Track the closest green ball.
            // trackGreen();

            // Forward and backwards 5 feet.
            // forwardAndBack(0.3, 60);

            // Move to wall and stop.
            // moveToWall(0.3, 0.2);

            // Wall follow.
            // wallFollow(0.1, 0.2);
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
	}

	// Distance in meters.
	public void wallFollow(double distance, double speed) {
		while (currentDistance == 0.0) {
			Thread.yield();
		}
		while (true) {
			if (currentDistance > distance) {
				// Go forward.
				f = 0.2;
				// Positive if too far away; negative if too close.
				double error = (currentRight - distance);
				System.out.println("err: " + error);
				desired = desired - 0.01 * error;
			}
			else {
				System.out.println("HIT WALL " + currentDistance + " < " + distance + (currentDistance < distance));
				// Turn left (counter-clockwise);
				f = 0;
				turn(-Math.PI / 2);
			}
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void trackRed() {
		while (true) {
			double bearing = BallTrack.getBearing(true);
			// If bearing is not a number, we don't see any balls.
			// TODO: Spin looking for balls here in the case of NaN?
			if (!Double.isNaN(bearing)) {
				desired = current + (bearing * Math.PI / 180);
			}
			Thread.yield();
		}
	}

	public void trackGreen() {
		while (true) {
			double bearing = BallTrack.getBearing(false);
			// If bearing is not a number, we don't see any balls.
			// TODO: Spin looking for balls here in the case of NaN?
			if (!Double.isNaN(bearing)) {
				desired = current + (bearing * Math.PI / 180);
			}
			Thread.yield();
		}
	}

	private void drawSquare() {
		turn(0);
		for (int i = 0; i < 4; i++) {
			forward(0.25, 3000);
			turn(Math.PI / 2);
		}
	}

	private void moveToWall(double distanceMeters, double speed) {
		while (currentDistance < 0.03) {
			Thread.yield();
		}
		f = speed;
		int k = 0;
		while (k < 10) {
			if (currentDistance - distanceMeters < 0.01) {
				k++;
			}
			else {
				k = 0;
			}
			System.out.println("MOVING");
			try {
				Thread.sleep(15);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			Thread.yield();
		}
		f = 0;
	}

	private void forwardAndBack(double speed, double inches) {
		final double r = 3.875 / 2;
		double theta = inches / r;
		double initial = encoderTotal;
		f = speed;
		desired = current + Math.PI / 180;
		while (encoderTotal - initial < theta) {
			//System.out.println(encoderTotal - initial);
			f = Math.min(speed, 1.0 * (theta - (encoderTotal - initial)));
			Thread.yield();
		}
		f = 0;
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		f = -speed;
		desired = current - Math.PI / 180;
		while (encoderTotal - initial > 0) {
			Thread.yield();
		}
		f = 0;
	}

	private void forward(double speed, int millis) {
		try {
			f = speed;
			Thread.sleep(millis);
			f = 0;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * In radians! Clockwise.
	 * @param d
	 */
	private void turn(double radiansClockwise) {
		// TODO: Why is this clockwise? Seems wrong but it works that way.
		// Could just add a negative sign but we should try to understand why
		// this is happening.
		desired += radiansClockwise;
		waitUntilHeadingNormal();
	}

	private void waitUntilHeadingNormal() {
		while (Math.abs(normalize(current - desired)) > TURN_THRESH) { /*System.out.println(Math.abs(current - desired));*/ Thread.yield(); }
	}

	public void setMotors( double powerA, double powerB ) {
		// Note: motorA is left motor, motorB is right Motor.
		powerA = Math.min(Math.max(-1, powerA), 1);
		powerB = Math.min(Math.max(-1, powerB), 1);
		powerA = MIN_POWER * Math.signum(powerA) + POWER_SCALE * powerA;
		powerB = MIN_POWER * Math.signum(powerB) + POWER_SCALE * powerB;
		if (Math.abs(powerA) - MIN_POWER < 0.01) {
			powerA = 0;
		}
		if (Math.abs(powerB) - MIN_POWER < 0.01) {
			powerB = 0;
		}
		// TODO: Some wires got reversed somewhere down the line. We need
		// these negative signs now.
		motorACytron.setSpeed(-powerA);
		motorBCytron.setSpeed(-powerB);
		modified();
	}
	
	public void modified() {
		maple.transmit();
	}

	public double normalize(double d) {
		while (d > 180) {
			d -= 360;
		}
		while (d < -180) {
			d += 360;
		}
		return d;
	}
}
