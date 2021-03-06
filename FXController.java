package Rage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import Utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The controller for our application, where the application logic is
 * implemented. It handles the button for starting/stopping the camera and the
 * acquired video stream.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a> (minor fixes)
 * @version 2.0 (2016-09-17)
 * @since 1.0 (2013-10-20)
 *
 */
public class FXController
{

	// getting the hsv values for the gui sliders
	@FXML
	private Slider hueStart, saturationStart, valueStart, hueStop, saturationStop, valueStop;
	
	// the FXML button
	@FXML
	private Button button;
	
	// the FXML image views
	@FXML
	private ImageView currentFrame, morphProp, maskProp, contoursImg;
	
	// output for HSV values
	@FXML
	private Label hsvValuesProp;
	
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	
	// the OpenCV object that realizes the video capture
	private VideoCapture capture = new VideoCapture();
	
	// a flag to change the button behavior
	private boolean cameraActive = false;
	
	// the id of the camera to be used
	private static int cameraId = 0;

//	private int hueStart, saturationStart, valueStart, hueStop, saturationStop, valueStop;
	
//	print stuff
	public static void print( Object... values){
		   for(Object c : values){
		      System.out.println(c);
		   }
		}
	
	/**
	 * The action triggered by pushing the button on the GUI
	 *
	 * @param event
	 *            the push button event
	 */
	@FXML
	protected void startCamera(ActionEvent event)
	{
		if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open(cameraId);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				// tells code that the camera is actively grabbing frames
				this.cameraActive = true;
				
				// this sets the dimensions of the video feed
				this.capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT,720);
				this.capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH,1280);
				this.capture.set(Videoio.CAP_PROP_SATURATION, 0);
				
				// grab a frame every 17 ms (60 frames/sec)
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(new frameGrabber(), 0, 17, TimeUnit.MILLISECONDS);
//				this.timer.scheduleAtFixedRate(new testx(), 0, 17, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.button.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			
			// update again the button content
			this.button.setText("Start Camera");
			
			// stop the timer
			this.stopAcquisition();
		}
	}
	
	public class frameGrabber implements Runnable {
		
		
		// allocate some blank images
		Mat blurredImage = new Mat();
		Mat hsvImage = new Mat();
		Mat mask = new Mat();
		Mat morphOutput = new Mat();
		Mat frame;
		
		public void run()
		{
			// effectively grab and process a single frame
			Mat frame = grabFrame();
			// convert and show the frame
			Image imageToShow = Utils.mat2Image(frame);
//			updateImageView(currentFrame, imageToShow);
//			updateImageView(maskProp, Utils.mat2Image(mask));

			// blurs the image with the 8 surrounding pixels
//			Imgproc.blur(frame, blurredImage, new Size(3, 3));
//			updateImageView(currentFrame, Utils.mat2Image(blurredImage));

			// convert the frame to HSV
//			Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);
		
			//get thresholding values from the UI
			// remember: H ranges 0-180, S and V range 0-255
			Scalar minValues = new Scalar(hueStart.getValue(), saturationStart.getValue(), valueStart.getValue());
			Scalar maxValues = new Scalar(hueStop.getValue(), saturationStop.getValue(), valueStop.getValue());
//
//			// show the current selected HSV range
//			String valuesToPrint = String.format("Hue range: %1-%2\tSaturation range: %3-%4\tValue range: %5-%6", minValues.val[0], maxValues.val[0], minValues.val[1], maxValues.val[1], minValues.val[2], maxValues.val[2]);
//			hsvValuesProp.setText(valuesToPrint); 

			// threshold HSV image to select tennis balls
//			Core.inRange(hsvImage, minValues, maxValues, mask);
			
			// show the partial output
			updateImageView(maskProp, Utils.mat2Image(mask));
			
			// morphological operators
			// dilate with large element, erode with small ones
			// creates an object to your for dilation and erosion
//			 Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
//			 Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

//			 // the actual functions for dilation and erosion
//			 Imgproc.dilate(mask, morphOutput, dilateElement);
//			 Imgproc.erode(morphOutput, morphOutput, erodeElement);
			 
			 // show the partial output
//			 updateImageView(morphProp, Utils.mat2Image(morphOutput));
			 
			// init
			List<MatOfPoint> contours = new ArrayList<>();
			Mat hierarchy = new Mat();

			// find contours
			Imgproc.findContours(morphOutput, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

			// if any contour exist...
			if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
			{
//				print("made it to contours");
			        // for each contour, display it in blue
					// update the color to green and include color indices for the scalar
			        for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
			        {
			                Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
			        }
			}

	        // updates contour image
	        updateImageView(currentFrame, Utils.mat2Image(frame));
		}
	}
	
	// gets a frame for a video
	private Mat grabFrame()
	{
		
		// creates an image to work with
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
					// debugging
//					Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
				}
				
			}
			catch (Exception e)
			{
				
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		return frame;
	}
	
	// stops camera
	private void stopAcquisition()
	{
		if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(17, TimeUnit.MILLISECONDS);
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
//		System.out.println("updating image");
		Utils.onFXThread(view.imageProperty(), image);
	}
	
	// stops camera when the application is closed
	protected void setClosed()
	{
		this.stopAcquisition();
	}
	
}
