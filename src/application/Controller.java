package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

import application.ObjectTracker;
import application.Database;
import application.FaceDetector;
import application.SquareDetector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

/**
 * This class is the controller class. Controls what's happening in the
 * application
 * 
 * @author Nicklas
 *
 */
public class Controller {

	// **********************************************************************************************
	// Mention The file location path where the face will be saved & retrieved

	public String filePath = "./faces";

	// **********************************************************************************************
	@FXML
	private Button startCam;
	@FXML
	private Button stopBtn;
	@FXML
	private Button motionBtn;
	@FXML
	private Button eyeBtn;
	@FXML
	private Button shapeBtn;
	@FXML
	private Button gesture;
	@FXML
	private Button gestureStop;
	@FXML
	private Button saveBtn;
	@FXML
	private Button recogniseBtn;
	@FXML
	private Button stopRecBtn;
	@FXML
	private ImageView frame;
	@FXML
	private TitledPane dataPane;
	@FXML
	private TextField fname;
	@FXML
	private TextField lname;
	@FXML
	private TextField code;
	@FXML
	private TextField reg;
	@FXML
	private TextField sec;
	@FXML
	private TextField age;
	@FXML
	private ListView<String> logList;
	@FXML
	private ProgressIndicator pb;
	@FXML
	private Label savedLabel;
	@FXML
	private Label warning;
	@FXML
	private TilePane tile;

	// **********************************************************************************************
	private FaceDetector faceDetect = new FaceDetector(); // Creating Face detector
													// object
	private ObjectTracker ot = new ObjectTracker(); // Creating Object Tracker object
	private Database database = new Database(); // Creating Database object

	private ArrayList<String> user = new ArrayList<String>();
	private ImageView imageView1;

	private ObservableList<String> event = FXCollections.observableArrayList();

	private boolean isDBready = false;

	// **********************************************************************************************
	/**
	 * Puts String data in to a arraylist that update the eventlog 
	 * @param data
	 */
	public void putOnLog(String data) {

		Instant now = Instant.now();

		String logs = now.toString() + ":\n" + data;

		event.add(logs);

		logList.setItems(event);

	}

	@FXML
	protected void startCamera() throws SQLException {

		// *******************************************************************************************
		// initializing objects from start camera button event
		faceDetect.init();

		faceDetect.setFrame(frame);

		faceDetect.start();

		if (!database.init()) {

			putOnLog("Error: Database Connection Failed ! ");

		} else {
			isDBready = true;
			putOnLog("Success: Database Connection Succesful ! ");
		}

		// *******************************************************************************************
		// Activating other buttons
		startCam.setVisible(false);
		eyeBtn.setDisable(false);
		stopBtn.setVisible(true);
		motionBtn.setDisable(false);
		gesture.setDisable(false);
		saveBtn.setDisable(false);
		shapeBtn.setDisable(false);
		stopRecBtn.setDisable(true);

		if (isDBready) {
			recogniseBtn.setDisable(false);
		}

		dataPane.setDisable(false);

		// *******************************************************************************************

		tile.setPadding(new Insets(15, 15, 55, 15));
		tile.setHgap(30);

		// **********************************************************************************************
		// Picture Gallary

		String path = filePath;

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		// Image reader from the mentioned folder
		for (final File file : listOfFiles) {

			imageView1 = createImageView(file);
			tile.getChildren().addAll(imageView1);
		}
		putOnLog("Real Time WebCam Stream Started !");

		// **********************************************************************************************
	}

	@FXML
	protected void faceRecognise() {

		recogniseBtn.setDisable(true);
		faceDetect.setIsRecFace(true);

		putOnLog("Face Recognition Activated !");

		stopRecBtn.setDisable(false);

	}

	@FXML
	protected void stopRecognise() {

		faceDetect.setIsRecFace(false);

		this.user.clear();
		recogniseBtn.setDisable(false);
		stopRecBtn.setDisable(true);

		putOnLog("Face Recognition Deactivated !");

	}

	@FXML
	protected void startMotion() {
		motionBtn.setDisable(true);
		faceDetect.setMotion(true);
		putOnLog("Motion Detector Activated !");

	}

	@FXML
	protected void saveFace() throws SQLException {

		// Input Validation
		if (fname.getText().trim().isEmpty() || reg.getText().trim().isEmpty() || code.getText().trim().isEmpty()) {

			new Thread(() -> {

				try {
					warning.setVisible(true);

					Thread.sleep(2000);

					warning.setVisible(false);

				} catch (InterruptedException ex) {
				}

			}).start();

		} else {
			// Progressbar
			pb.setVisible(true);

			savedLabel.setVisible(true);

			new Thread(() -> {

				try {

					faceDetect.setFname(fname.getText());

					faceDetect.setFname(fname.getText());
					faceDetect.setLname(lname.getText());
					faceDetect.setSec(sec.getText());
					try {
						faceDetect.setAge(Integer.parseInt(age.getText()));
					} catch (Exception e) {
						faceDetect.setAge(0);
					}
					try {
						faceDetect.setCode(Integer.parseInt(code.getText()));
					} catch (Exception e) {
						faceDetect.setCode(0);
					}
					try {
						faceDetect.setReg(Integer.parseInt(reg.getText()));
					} catch (Exception e) {
						faceDetect.setReg(0);
					}

					database.setFname(fname.getText());
					database.setLname(lname.getText());
					database.setSec(sec.getText());
					try {
						database.setAge(Integer.parseInt(age.getText()));
					} catch (Exception e) {
						database.setAge(0);
					}
					try {
						database.setCode(Integer.parseInt(code.getText()));
					} catch (Exception e) {
						database.setCode(0);
					}
					try {
						database.setReg(Integer.parseInt(reg.getText()));
					} catch (Exception e) {
						database.setReg(0);
					}

					database.insert();

					pb.setProgress(100);

					savedLabel.setVisible(true);
					Thread.sleep(2000);

					pb.setVisible(false);

					savedLabel.setVisible(false);

				} catch (InterruptedException ex) {
				}

			}).start();

			faceDetect.setSaveFace(true);

		}

	}

	@FXML
	protected void stopCam() throws SQLException {

		faceDetect.stop();

		startCam.setVisible(true);
		stopBtn.setVisible(false);

		putOnLog("Cam Stream Stopped!");

		motionBtn.setDisable(true);
		gesture.setDisable(true);
		recogniseBtn.setDisable(true);
		saveBtn.setDisable(true);
		dataPane.setDisable(true);
		stopRecBtn.setDisable(true);
		eyeBtn.setDisable(true);
		shapeBtn.setDisable(true);

		database.db_close();
		putOnLog("Database Connection Closed");
		isDBready = false;
	}

	@FXML
	protected void startGesture() {

		faceDetect.stop();
		ot.init();

		Thread th = new Thread(ot);
		th.start();

		gesture.setVisible(false);
		gestureStop.setVisible(true);

	}

	@FXML
	protected void startEyeDetect() {

		faceDetect.setEyeDetection(true);
		eyeBtn.setDisable(true);

	}

	@FXML
	protected void stopGesture() {

		ot.stop();
		faceDetect.start();

		gesture.setVisible(true);
		gestureStop.setVisible(false);

	}

	@FXML
	protected void shapeStart() {

		faceDetect.stop();

		SquareDetector shapeFrame = new SquareDetector();
		shapeFrame.loop();

	}

	private ImageView createImageView(final File imageFile) {

		try {
			final Image img = new Image(new FileInputStream(imageFile), 120, 0, true, true);
			imageView1 = new ImageView(img);

			imageView1.setStyle("-fx-background-color: BLACK");
			imageView1.setFitHeight(120);

			imageView1.setPreserveRatio(true);
			imageView1.setSmooth(true);
			imageView1.setCache(true);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return imageView1;
	}
}