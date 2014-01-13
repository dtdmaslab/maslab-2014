package kitbot;

import jssc.SerialPort;
import jssc.SerialPortException;

public class KitBotModel {
	private SerialPort serialPort;
    private byte motorA = 0;
    private byte motorB = 0;
    private PidController pid;
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
					// TODO Auto-generated method stub
					return current - desired;
				}
            	
            };
            pid = new PidController(1, 0, 1, ec, new ErrorHandler() {
				@Override
				public void handleError(float error) {
					double m1 = error / 180.0;
					double m2 = error / -180.0;
					setMotors(m1 + f, m2 + f);
				}
            	
            });
            System.out.println("????");
            //drawSquare();
            forward(0.5, 1000);
        }
        catch (Exception ex){
            System.out.println(ex);
        }
	}
	
	private void drawSquare() {
		turn(0);
		for (int i = 0; i < 4; i++) {
			forward(0.5, 500);
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
		desired += degrees;
		waitUntilHeadingNormal();
	}

	private void waitUntilHeadingNormal() {
		while (Math.abs(current - desired) > 10) { System.out.println(Math.abs(current - desired)); Thread.yield(); }
	}

	public void setMotors( double powerA, double powerB ) {
		powerA = Math.min(Math.max(-1, powerA), 1);
		powerB = Math.min(Math.max(-1, powerB), 1);
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
