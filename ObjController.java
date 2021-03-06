package Rage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import Utils.Utils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class ObjController
{
	// FXML camera button
	@FXML
	private Button cameraButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	// the FXML area for showing the mask
	@FXML
	private ImageView maskImage;
	// the FXML area for showing the output of the morphological operations
	@FXML
	private ImageView morphImage;
	// FXML slider for setting HSV ranges
	@FXML
	private Slider hueStart;
	@FXML
	private Slider hueStop;
	@FXML
	private Slider saturationStart;
	@FXML
	private Slider saturationStop;
	@FXML
	private Slider valueStart;
	@FXML
	private Slider valueStop;
	
	// FXML label to show the current values set with the sliders
	@FXML
	private Label hsvCurrentValues;
	
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	
	// the OpenCV object that performs the video capture
	private VideoCapture capture = new VideoCapture();
	
	// a flag to change the button behavior
	private boolean cameraActive;
	
	// property for object binding
	private ObjectProperty<String> hsvValuesProp;
	
	// filepath for saving images
	private String filePath;
	private int imageNumber;
	private MatOfByte mob;
		
	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	private void startCamera()
	{
		this.capture.set(Videoio.CV_CAP_PROP_AUTO_EXPOSURE, 1);
		this.capture.set(Videoio.CV_CAP_PROP_EXPOSURE, -2);
//		this.capture.set(Videoio.CV_CAP_MODE_BGR, -0.75);
//		int fourcc = VideoWriter.fourcc('M', 'J', 'P', 'G');
//		capture.set(Videoio.CAP_PROP_FOURCC, fourcc);
//		capture.set(Videoio.CAP_MODE_BGR, Videoio.CAP_MODE_BGR);
		// bind a text property with the string containing the current range of
		// HSV values for object detection
		hsvValuesProp = new SimpleObjectProperty<>();
		this.hsvCurrentValues.textProperty().bind(hsvValuesProp);
		
		this.filePath = "C://Users/1747/Pictures/Training Image/TrainingImage";
		this.imageNumber = 0;
		this.mob = new MatOfByte();
				
		// set a fixed width for all the image to show and preserve image ratio
		this.imageViewProperties(this.originalFrame, 400);
		this.imageViewProperties(this.maskImage, 200);
		this.imageViewProperties(this.morphImage, 200);
		
		if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open(0);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				Runnable frameGrabber = new Runnable() {
					@Override
					public void run()
					{
						// effectively grab and process a single frame
						Mat frame = grabFrame();
						// convert and show the frame
//						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_GRAY2BGR);
						Image imageToShow = Utils.mat2Image(frame);
//						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_GRAY2BGR);
						updateImageView(originalFrame, imageToShow);
						
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				// grab a frame every 33 ms (30 frames/sec)
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.cameraButton.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Failed to open the camera connection...");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.cameraButton.setText("Start Camera");
			
			// stop the timer
			this.stopAcquisition();
		}
	}
	
	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Mat grabFrame()
	{
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened())
		{
			try
			{
				// read the current frame
				this.capture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty())
				{
//					try {
						MatOfByte mob=new MatOfByte();
						Imgcodecs.imencode(".jpg", frame, mob);
						byte ba[]=mob.toArray();
						BufferedImage bi=ImageIO.read(new ByteArrayInputStream(ba));
//						BufferedImage gray = new BufferedImage(frame.width(), frame.height(), BufferedImage.TYPE_BYTE_GRAY);
						
						ImageIO.write(bi, "jpg", new File("C://Users/1747/Pictures/Training Image/TrainingImage" + String.valueOf(imageNumber) + ".jpg"));
						imageNumber ++;
//					} catch (IOException e) {
//						System.out.println("Exception: \t" + e.getMessage());
//					}
					
					// init
					Mat blurredImage = new Mat();
					Mat hsvImage = new Mat();
					Mat mask = new Mat();
					Mat morphOutput = new Mat();
					
					// remove some noise
					Imgproc.blur(frame, blurredImage, new Size(3, 3));
					
					// convert the frame to HSV
					Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);
					
					// get thresholding values from the UI
					// remember: H ranges 0-180, S and V range 0-255
					Scalar minValues = new Scalar(this.hueStart.getValue(), this.saturationStart.getValue(),
							this.valueStart.getValue());
					Scalar maxValues = new Scalar(this.hueStop.getValue(), this.saturationStop.getValue(),
							this.valueStop.getValue());
					
					// show the current selected HSV range
					String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0]
							+ "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: "
							+ minValues.val[2] + "-" + maxValues.val[2];
					Utils.onFXThread(this.hsvValuesProp, valuesToPrint);
					
					// threshold HSV image to select tennis balls
					Core.inRange(hsvImage, minValues, maxValues, mask);
					// show the partial output
					this.updateImageView(this.maskImage, Utils.mat2Image(mask));
					
					// morphological operators
					// dilate with large element, erode with small ones
					Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
					Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
					
					Imgproc.erode(mask, morphOutput, erodeElement);
					Imgproc.erode(morphOutput, morphOutput, erodeElement);
					
					Imgproc.dilate(morphOutput, morphOutput, dilateElement);
					Imgproc.dilate(morphOutput, morphOutput, dilateElement);
					
					// show the partial output
					this.updateImageView(this.morphImage, Utils.mat2Image(morphOutput));
					
					// find the tennis ball(s) contours and show them
//					this.findAndDrawBalls(frame);
//					this.HoughCirclesRun(morphOutput);
//					this.HoughCirclesRun(mask);
					this.HoughCirclesRun(frame);
//
				}
				
			}
			catch (Exception e)
			{
				// log the (full) error
				System.err.print("Exception during the image elaboration...");
				e.printStackTrace();
			}
		}
		
		return frame;
	}
	
	/**
	 * Given a binary image containing one or more closed surfaces, use it as a
	 * mask to find and highlight the objects contours
	 * 
	 * @param maskedImage
	 *            the binary image to be used as a mask
	 * @param frame
	 *            the original {@link Mat} image to be used for drawing the
	 *            objects contours
	 * @return the {@link Mat} image with the objects contours framed
	 */
	/*
	private Mat findAndDrawBalls(Mat frame)
	{
		// init
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Mat circles = new Mat();
		
		// find contours
//		Imgproc.findContours(frame, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_TC89_KCOS);
		Imgproc.HoughCircles(frame, circles, Imgproc.HOUGH_GRADIENT, 1.0, frame.rows()/16, 200, 100, 0, 0);
		
		for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(frame, center, 1, new Scalar(0,100,100), 3, 8, 0 );
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(circles, center, radius, new Scalar(250, 0, 0));
        }
		
		// if any contour exist...
//		if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
//		{
//			// for each contour, display it in blue
//			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
//			{
//				Imgproc.drawContours(frame, contours, idx, new Scalar(250, 250, 0));
//			}
//		}
		
		return frame;
	}
	
	*/
	
	/**
	 * Set typical {@link ImageView} properties: a fixed width and the
	 * information to preserve the original image ration
	 * 
	 * @param image
	 *            the {@link ImageView} to use
	 * @param dimension
	 *            the width of the image to set
	 */
	
	private void HoughCirclesRun(Mat frame) {
	        // Load an image
	        Mat src = frame;
	        Mat gray = new Mat();
	        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
	        Imgproc.medianBlur(gray, gray, 5);
	        Mat circles = new Mat();
	        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
	                (double)gray.rows()/16, // change this value to detect circles with different distances to each other
	                100.0, 62.5, 40, 160); //s change the last two parameters
	                // (min_radius & max_radius) to detect larger circles
	        for (int x = 0; x < circles.cols(); x++) {
	            double[] c = circles.get(0, x);
	            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
	            // circle center
	            Imgproc.circle(src, center, 1, new Scalar(0,100,100), 3, 8, 0 );
	            // circle outline
	            int radius = (int) Math.round(c[2]);
	            Imgproc.circle(src, center, radius, new Scalar(255,0,255), 3, 8, 0 );
	            System.out.println(radius);
	        }
	    }
	
	private void imageViewProperties(ImageView image, int dimension)
	{
		// set a fixed width for the given ImageView
		image.setFitWidth(dimension);
		// preserve the image ratio
		image.setPreserveRatio(true);
	}
	
	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition()
	{
		if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
		}
	}
	
	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view
	 *            the {@link ImageView} to update
	 * @param image
	 *            the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image)
	{
		Utils.onFXThread(view.imageProperty(), image);
	}
	
	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed()
	{
		this.stopAcquisition();
	}
	
}
