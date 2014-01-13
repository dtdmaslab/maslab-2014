package vision;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

public class BallTrack {
	static VideoCapture camera;
	private static boolean DEBUG_MODE = false;;

	protected static Mat getVideoPicture() {
		/*if (camera == null) {
			camera = new VideoCapture();
			camera.open(0);
		}*/
		Mat image = new Mat();
		// Wait until the camera has a new frame
		while (!camera.read(image)) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return image;
	}

	public static double getBearing() {
		// Load the OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Setup the camera
		camera = new VideoCapture();
		camera.open(0);

		// Create GUI windows to display camera output and OpenCV output
		JLabel cameraPane = null;
		JLabel opencvPane = null;
		if (DEBUG_MODE) {
			int width = (int) (camera.get(Highgui.CV_CAP_PROP_FRAME_WIDTH));
			int height = (int) (camera.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
			cameraPane = createWindow("Camera output", width, height);
			opencvPane = createWindow("OpenCV output", width, height);
		}

		Mat m = getVideoPicture();
		Mat pm = ImageProcessor.findRedBalls(m);
		double bearing = ImageProcessor.getBearing(pm);
		if (DEBUG_MODE) {
			updateWindow(cameraPane, m);
			updateWindow(opencvPane, pm);
			System.out.println(bearing);
		}
		return bearing;
	}

    private static JLabel createWindow(String name, int width, int height) {    
        JFrame imageFrame = new JFrame(name);
        imageFrame.setSize(width, height);
        imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel imagePane = new JLabel();
        imagePane.setLayout(new BorderLayout());
        imageFrame.setContentPane(imagePane);

        imageFrame.setVisible(true);
        return imagePane;
    }

    private static void updateWindow(JLabel imagePane, Mat mat) {
    	int w = (int) (mat.size().width);
    	int h = (int) (mat.size().height);
    	if (imagePane.getWidth() != w || imagePane.getHeight() != h) {
    		//imagePane.setSize(w, h);
    	}
    	BufferedImage bufferedImage = Mat2Image.getImage(mat);
    	imagePane.setIcon(new ImageIcon(bufferedImage));
    }
}
