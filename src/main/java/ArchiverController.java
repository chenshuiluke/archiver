package archiver;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

public class ArchiverController {

    @FXML 
    private MenuItem createBackup;

    @FXML
    private void showCreateNewBackupWindow(){
	System.out.println("Here");
		try{
			URL url = getClass().getClassLoader().getResource("CreateBackup.fxml");
			Parent root1 = (Parent) FXMLLoader.load(url);
			Stage stage = new Stage();
			stage.setTitle("ABC");
			stage.setScene(new Scene(root1));  
			stage.show();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}

    }
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

    }
}
