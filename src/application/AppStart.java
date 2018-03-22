package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * This class is for starting the application
 * @author Nicklas
 *
 */
public class AppStart extends Application{

	/**
	 * starts the Application
	 * @param args = null
	 */
	public static void main(String[] args) {
		launch(args);

	}

	/**
	 * Start javaFx panel thru sample.fxml
	 */
	@Override
	public void start(Stage stage) throws Exception {
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Sample.fxml"));
			Scene scene = new Scene(root,1350,720);

	        stage.setTitle("FaceRecognizerOpenCVJava");
			
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
