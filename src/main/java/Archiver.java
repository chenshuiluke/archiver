package archiver;

import java.util.Scanner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.application.Platform;

public class Archiver extends Application{
	public static void main(String[] args){
		Application.launch(args);	
	}
	public void start(Stage primaryStage){
		primaryStage.setTitle("Archiver");
		Parent root = null;
		String sceneFile = "Archiver.fxml";
		URL url = null;	
		try{
			url = getClass().getClassLoader().getResource(sceneFile);
			root = FXMLLoader.load(url);
			System.out.println("fxmlResource = " + sceneFile);
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(e -> {
				Platform.exit();
				System.exit(0);
			});
			primaryStage.show();
		}
		catch(Exception ex)
		{
		    System.out.println( "Exception on FXMLLoader.load()" );
		    System.out.println( "  * url: " + url );
		    System.out.println( "  * " + ex );
		    System.out.println( "    ----------------------------------------\n" );
		}
	}
	public static void commandLine(){
		try(Scanner inputScanner = new Scanner(System.in)){
			System.out.println("Please enter the name of a directory to compress:");
			String input = inputScanner.nextLine();
			Compressor compressor = new Compressor(input, "output", true);
			compressor.compress(0);
		}

	}
}
