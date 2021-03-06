package application;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_INTER_AREA;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

/**
 * Detects the face/s and starts the camera so we can do something in the application 
 * @author Nicklas
 *
 */
public class FaceDetector implements Runnable {

	private Database database = new Database();
	private ArrayList<String> user;

	private FaceRecognizer faceRecognizer = new FaceRecognizer();
	private MotionDetector motionDetector = new MotionDetector();
	private OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
	private Java2DFrameConverter paintConverter = new Java2DFrameConverter();

	private Exception exception = null;

	private String classiferName;
	private File classifierFile;

	private boolean saveFace = false;
	private boolean isRecFace = false;
	private boolean isMotion = false;
	private boolean isEyeDetection = false;
	private boolean stop = false;

	private CvHaarClassifierCascade classifier = null;
	private CvHaarClassifierCascade classifierEye = null;
	private CvHaarClassifierCascade classifierSideFace = null;
	private CvHaarClassifierCascade classifierEyeglass = null;

	private CvMemStorage storage = null;
	private FrameGrabber grabber = null;
	private IplImage grabbedImage = null, temp, grayImage = null, smallImage = null;
	private ImageView frames;

	private CvSeq faces = null;
	private CvSeq eyes = null;

	private int recogniseCode;
	private int code;
	private int reg;
	private int age;

	private String fname;
	private String Lname; 
	private String sec; 

	/**
	 * initialize the HaarCascade
	 */
	public void init() {
		faceRecognizer.init();

		setClassifier("haar/haarcascade_frontalface_alt.xml");
		setClassifierEye("haar/haarcascade_eye.xml");
		setClassifierEyeGlass("haar/haarcascade_eye_tree_eyeglasses.xml");
		setClassifierSideFace("haar/haarcascade_profileface.xml");

	}

	/*
	 * Start a new thread for FaceDetector
	 */
	public void start() {
		try {
			new Thread(this).start();
		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}
	}

	/**
	 * run Facedetector class
	 */
	public void run() {
		try {
			try {
				grabber = OpenCVFrameGrabber.createDefault(0); // parameter 0 default camera , 1 for secondary

				grabber.setImageWidth(700);
				grabber.setImageHeight(700);
				grabber.start();

				grabbedImage = grabberConverter.convert(grabber.grab());

				storage = CvMemStorage.create();
			} catch (Exception e) {
				if (grabber != null)
					grabber.release();
				grabber = new OpenCVFrameGrabber(0);
				grabber.setImageWidth(700);
				grabber.setImageHeight(700);
				grabber.start();
				grabbedImage = grabberConverter.convert(grabber.grab());

			}
			int count = 15;
			grayImage = cvCreateImage(cvGetSize(grabbedImage), 8, 1); // converting image to grayscale
			// reducing the size of the image to speed up the processing
			smallImage = cvCreateImage(cvSize(grabbedImage.width() / 4, grabbedImage.height() / 4), 8, 1);

			stop = false;
			
			while (!stop && (grabbedImage = grabberConverter.convert(grabber.grab())) != null) {

				Frame frame = grabberConverter.convert(grabbedImage);
				BufferedImage image = paintConverter.getBufferedImage(frame, 2.2 / grabber.getGamma());
				Graphics2D g2 = image.createGraphics();

				if (faces == null) {
					cvClearMemStorage(storage);

					// creating a temporary image
					temp = cvCreateImage(cvGetSize(grabbedImage), grabbedImage.depth(), grabbedImage.nChannels());

					cvCopy(grabbedImage, temp);

					cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
					cvResize(grayImage, smallImage, CV_INTER_AREA);

					faces = cvHaarDetectObjects(smallImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
					// face detection

					CvPoint org = null;
					if (grabbedImage != null) {

						if (isEyeDetection) { // eye detection logic
							eyes = cvHaarDetectObjects(smallImage, classifierEye, storage, 1.1, 3,
									CV_HAAR_DO_CANNY_PRUNING);

							if (eyes.total() == 0) {
								eyes = cvHaarDetectObjects(smallImage, classifierEyeglass, storage, 1.1, 3,
										CV_HAAR_DO_CANNY_PRUNING);

							}

							printEyeResult(eyes, eyes.total(), g2);

						}

						if (faces.total() == 0) {
							faces = cvHaarDetectObjects(smallImage, classifierSideFace, storage, 1.1, 3,
									CV_HAAR_DO_CANNY_PRUNING);

						}

						if (faces != null) {
							g2.setColor(Color.green);
							g2.setStroke(new BasicStroke(2));
							int total = faces.total();

							for (int i = 0; i < total; i++) {

								// printing rectange box where face detected
								// frame by frame
								CvRect r = new CvRect(cvGetSeqElem(faces, i));
								g2.drawRect((r.x() * 4), (r.y() * 4), (r.width() * 4), (r.height() * 4));

								CvRect re = new CvRect((r.x() * 4), r.y() * 4, (r.width() * 4), r.height() * 4);

								cvSetImageROI(temp, re);

								org = new CvPoint(r.x(), r.y());

								if (isRecFace) {
									this.recogniseCode = faceRecognizer.recognize(temp);

									// getting recognised user from the database
									database.init();
									user = new ArrayList<String>();
									user = database.getUser(this.recogniseCode);

									// printing recognised person name into the
									// frame
									g2.setColor(Color.WHITE);
									g2.setFont(new Font("Arial Black", Font.BOLD, 20));
									String names = user.get(1) + " " + user.get(2);
									g2.drawString(names, (r.x()*4),(r.y()*4));
								}

								if (saveFace) { // saving captured face to the
												// disk
									// keep it in mind that face code should be
									// unique to each person
									String fName = "faces/" + code + "-" + fname + "_" + Lname + "_" + count + ".jpg";
									cvSaveImage(fName, temp);
									count++;

								}

							}
							this.saveFace = false;
							faces = null;
						}

						WritableImage showFrame = SwingFXUtils.toFXImage(image, null);

						frames.setImage(showFrame);

						if (isMotion) {
							new Thread(() -> {

								try {

									motionDetector.init(grabbedImage, g2);

								} catch (InterruptedException ex) {
								} catch (Exception e) {

									e.printStackTrace();
								}

							}).start();

						}
						isMotion = false;

					}
					cvReleaseImage(temp);
				}

			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}
	}

	/**
	 * stopp the facedetector and camera
	 */
	public void stop() {
		stop = true;

		grabbedImage = grayImage = smallImage = null;
		try {
			grabber.stop();
		} catch (FrameGrabber.Exception e) {

			e.printStackTrace();
		}
		try {
			grabber.release();
		} catch (FrameGrabber.Exception e) {

			e.printStackTrace();
		}
		grabber = null;
	}

	private void setClassifier(String name) {
		try {

			setClassiferName(name);
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}

	private void setClassifierEye(String name) {
		try {

			classiferName = name;
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierEye = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}

	private void printEyeResult(CvSeq data, int total, Graphics2D g2) {
		for (int j = 0; j < total; j++) {
			CvRect eye = new CvRect(cvGetSeqElem(eyes, j));

			g2.drawOval((eye.x() * 4), (eye.y() * 4), (eye.width() * 4), (eye.height() * 4));

		}
	}

	private void setClassifierSideFace(String name) {

		try {

			classiferName = name;
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierSideFace = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}

	private void setClassifierEyeGlass(String name) {

		try {

			setClassiferName(name);
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierEyeglass = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}

	public void setClassiferName(String classiferName) {
		this.classiferName = classiferName;
	}

	public void setEyeDetection(boolean isEyeDetection) {
		this.isEyeDetection = isEyeDetection;
	}

	public boolean isMotion() {
		return isMotion;
	}

	public void setMotion(boolean isMotion) {
		this.isMotion = isMotion;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return Lname;
	}

	public void setLname(String lname) {
		Lname = lname;
	}

	public int getReg() {
		return reg;
	}

	public void setReg(int reg) {
		this.reg = reg;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getSec() {
		return sec;
	}

	public void setSec(String sec) {
		this.sec = sec;
	}

	public void setFrame(ImageView frame) {
		this.frames = frame;
	}

	public void setSaveFace(Boolean f) {
		this.saveFace = f;
	}

	public void setIsRecFace(Boolean isRecFace) {
		this.isRecFace = isRecFace;
	}

}
