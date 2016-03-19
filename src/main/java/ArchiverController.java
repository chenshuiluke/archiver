package archiver;

import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.io.File;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class ArchiverController{

   	@FXML
    private ListView backupList;

    @FXML 
    private MenuItem createBackup;
	private String getExtension(String fileName){
		String extension = "";
		int indexOfDot = fileName.lastIndexOf(".");
		if (indexOfDot > 0) {
			extension = fileName.substring(indexOfDot+1);
		}
		return extension;
	}
	Task<Void> task = new Task<Void>(){
		@Override
		protected Void call() throws Exception{
			while(true){
				Platform.runLater(new Runnable() {
					@Override public void run() {
						
							initializeBackupFileList();
	
									
						
					}
				});
							
	    		try{
	    			Thread.sleep(5000);
	    			
	    		}
	    		
				catch(InterruptedException ie) {
					ie.printStackTrace();
				}	
			}
			//return null;
		}
	};
    @FXML
    private void showCreateNewBackupWindow(){
		try{
			URL url = getClass().getClassLoader().getResource("CreateBackup.fxml");
			Parent root1 = (Parent) FXMLLoader.load(url);
			Stage stage = new Stage();
			stage.setTitle("Create New Backup");
			stage.setScene(new Scene(root1));  
			stage.show();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}

    }
	private void initializeBackupFileList(){
		File presets = new File("presets");
		if(presets.isDirectory()){
			ObservableList<String> oldPresetList = backupList.getItems();
			ObservableList<String> newPresetList = FXCollections.observableArrayList();
			//backupList.getItems().removeAll(backupList.getItems());
			String[] list = presets.list();

			//Get contents of presets folder
			for(String file : list){
				if(getExtension(file).equals("json")){
					newPresetList.add(file);
				}
			}
			//Adds any new items to the backupList
			for(String item : newPresetList){
				if(!oldPresetList.contains(item)){ //If the old list doesnt contain...
					oldPresetList.add(item);
				}
			}
			//Deletes any old items that were deleted from the presets folder
			for(String item : oldPresetList){
				if(!newPresetList.contains(item)){
					oldPresetList.remove(item);
				}
			}

		}
	}
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
		(new Thread(task)).start();
        assert createBackup != null : "fx:id=\"createBackup\" was not injected: check your FXML file 'Archiver.fxml'.";
        assert backupList != null : "fx:id=\"backupList\" was not injected: check your FXML file 'Archiver.fxml'.";
		initializeBackupFileList();
    }
}
