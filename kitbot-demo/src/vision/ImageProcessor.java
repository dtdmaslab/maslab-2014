package vision;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class ImageProcessor {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	// Input: an image from the camera
	// Output: the OpenCV-processed image

	// (In practice it's a little different:
	//  the output image will be for your visual reference,
	//  but you will mainly want to output a list of the locations of detected objects.)
	public static Mat findRedBalls(Mat rawImage) {
		//Mat processedImage = new Mat();
		Mat hsv = preprocess(rawImage);
		Mat bw = new Mat();
		Imgproc.cvtColor(hsv, bw, Imgproc.COLOR_HSV2BGR);
		Imgproc.cvtColor(bw, bw, Imgproc.COLOR_BGR2GRAY);
		List<Mat> hsv_layers = new ArrayList<Mat>();
		Core.split(hsv, hsv_layers);

		Mat h = hsv_layers.get(0);
		Mat s = hsv_layers.get(1);
		Mat v = hsv_layers.get(2);
		Mat of_interest = new Mat();
		Imgproc.threshold(s, of_interest, 50, 255, Imgproc.THRESH_BINARY);
		Mat red = new Mat();
		Mat red2 = new Mat();
		Imgproc.threshold(h, red, 10, 255, Imgproc.THRESH_BINARY_INV);
		Imgproc.threshold(h, red2, 170, 255, Imgproc.THRESH_BINARY);
		Core.bitwise_or(red, red2, red);
		Core.bitwise_and(red, of_interest, red);
		return red;
		/*List<Mat> three = new ArrayList<Mat>();
		three.add(red);
		three.add(red);
		three.add(red);
		Core.merge(three, red);
		Core.bitwise_and(red, rawImage, red);
		return red;*/
	}

	public static double getBearing(Mat binImg) {
		Rect bb = largestBlob(binImg.clone());
		if (bb == null) {
			return Double.NaN;
		}
		int centerPixelX = bb.x + bb.width / 2;
		final double CAMERA_ANGLE = 120;
		double deg = CAMERA_ANGLE * (double)centerPixelX / (double)binImg.width() - CAMERA_ANGLE / 2;
		return deg;
	}

	public static Rect largestBlob(Mat binaryImg) {
		double maxArea = 0;
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		MatOfPoint largestContour = null;
		Imgproc.findContours(binaryImg, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		for (MatOfPoint contour : contours) {
		    double area = Imgproc.contourArea(contour);
		    if (area > maxArea) {
		        maxArea = area;
		        largestContour = contour;
		    }
		}
		if (largestContour != null) {
			return Imgproc.boundingRect(largestContour);
		}
		else {
			return null;
		}
	}

	public static Mat preprocess(Mat img) {
		Mat processed = new Mat();
		// Gaussian blur to drastically reduce noise.
		Imgproc.GaussianBlur(img, processed, new Size(9, 9), 3);
		Imgproc.cvtColor(processed, processed, Imgproc.COLOR_BGR2HSV);
		return processed;
	}

	public static Mat process_canny(Mat img) {
		Imgproc.Canny(img, img, 60, 45);
		Imgproc.erode(img, img, Mat.ones(1, 1, 1));
		return img;
	}

	/**
	 * TODO: Get this to work.
	 * @param img
	 * @return
	 */
	public static Mat process_hough(Mat img) {
		Mat circles = new Mat();
		Imgproc.HoughCircles(img, circles, Imgproc.CV_HOUGH_GRADIENT, 2 /* Increase this if its too slow */, 1);
		return circles;
	}
}