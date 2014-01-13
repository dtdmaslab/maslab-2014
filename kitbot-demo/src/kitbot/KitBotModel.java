package kitbot;

import jssc.SerialPort;
import jssc.SerialPortException;

public class KitBotModel {
	final static double TURN_THRESH = 10;
	final static double MIN_POWER = 0.2;
	final static double POWER_SCALE = 0.3;

	private SerialPort serialPort;
    private byte motorA = 0;
    private byte motorB = 0;
    private ErrorCalculator ec;
    private int current = 5;
    private int desired = 0;
    private double f = 0;

	public KitBotModel() {
		try {
			serialPort = new SerialPort("/dev/ttyACM0");
            serialPort.openPort();
            serialPort.setParams(115200, 8, 1, 0);
            new Thread(new SerialPortListener(this, serialPort)).start();
            ec = new ErrorCalculator() {
				@Override
				public float getError() {
					return normalize(current - desired);
				}
            };
            // TODO: Tune these parameters based on experimentation.
            new PidController(1, 0, 0.75f, ec, new ErrorHandler() {
				@Override
				public void handleError(float error) {
					double m1 = error / 180.0;
					double m2 = error / -180.0;
					setMotors(m1 + f, m2 + f);
				}
            });

            // Go forward.
            // forward(0.5 /* Speed */, 1500 /* Time in ms */);

            // Turn.
            // turn(90 /* Degrees clockwise */);

            // Draw a square.
            // drawSquare();

            // Track closest red ball.
            // trackRed();
        }
        catch (Exception ex){
            System.out.println(ex);
        }
	}

	private void drawSquare() {
		turn(0);
		for (int i = 0; i < 4; i++) {
			forward(0.5, 1500);
			turn(90);
		}
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

	private void turn(int degrees) {
		// TODO: Why is this clockwise? Seems wrong but it works that way.
		// Could just add a negative sign but we should try to understand why
		// this is happening.
		desired += degrees;
		waitUntilHeadingNormal();
	}

	private void waitUntilHeadingNormal() {
		while (Math.abs(normalize(current - desired)) > TURN_THRESH) { System.out.println(Math.abs(current - desired)); Thread.yield(); }
	}

	public void setMotors( double powerA, double powerB ) {
		powerA = Math.min(Math.max(-1, powerA), 1);
		powerB = Math.min(Math.max(-1, powerB), 1);
		powerA = MIN_POWER * Math.signum(powerA) + POWER_SCALE * powerA;
		powerB = MIN_POWER * Math.signum(powerB) + POWER_SCALE * powerB;
		motorA = (byte)(-powerA*127);
		motorB = (byte)(powerB*127);
		modified();
	}
	
	public void modified() {
		try {
			byte[] data = new byte[4];
			data[0] = 'S';		// Start signal "S"
			data[1] = motorA;	// Motor A data
			data[2] = motorB;	// Motor B data
			data[3] = 'E';		// End signal "E"
			serialPort.writeBytes(data);
		} catch ( Exception ex ) {
			System.out.println(ex);
		}
	}
	
	public void finalize() {
		try {
			serialPort.closePort();
		} catch ( Exception ex ) {
			System.out.println(ex);
		}
	}

	public void adjustMotors(int gyroAngle) {
		current = gyroAngle;
	}

	public int normalize(int angle) {
		while (angle > 180) {
			angle -= 360;
		}
		while (angle < -180) {
			angle += 360;
		}
		return angle;
	}
}
