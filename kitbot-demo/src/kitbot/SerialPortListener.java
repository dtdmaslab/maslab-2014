package kitbot;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialPortListener implements Runnable {
	private KitBotModel model;
	private SerialPort serialPort;

	private boolean readingGyro = false;
	private String gyroReading = "";

	private int gyroAngle = 0;

	public SerialPortListener(KitBotModel model, SerialPort serialPort) {
		this.model = model;
		this.serialPort = serialPort;
	}

	@Override
	public void run() {
		while (true) {
			try {
				byte[] data = serialPort.readBytes();
				if (data != null) {
					//System.out.println("Read " + data.length + " bytes!");
					for (byte b : data) {
						if (b == (byte)'G') {
							// Start reading gyro data.
							readingGyro = true;
						}
						else if (readingGyro) {
							if ((b == (byte)'\r') && gyroReading.length() > 0) {
								gyroAngle = Integer.parseInt(gyroReading);
								model.adjustMotors(gyroAngle);
								readingGyro = false;
								gyroReading = "";
							}
							else if (b != (byte)'\n' && b != (byte)'\r') {
								gyroReading = gyroReading.concat(Character.toString((char)b));
							}
						}
					}
				}
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.yield();
		}
	}
}
