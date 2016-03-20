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
import javafx.scene.input.MouseEvent;
import javax.json.*;
import java.io.InputStream;
import java.io.FileInputStream;
import javafx.scene.text.Text;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Service;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class ArchiverController{
	LoadBackupService loadBackup = new LoadBackupService();
	@FXML
	private ProgressBar progressBar;

	@FXML
	private ListView backupList;

	@FXML 
	private MenuItem createBackup;

	@FXML
	private Label statusText;

	@FXML
	private Text backupFileName;

	@FXML
	private ListView<String> backupFileList;

    @FXML
    private Text fileNumberBox;

    @FXML
    private Text backupDestinationBox;

    @FXML
    private Button runBackupButton;

	private void setStatusText(String status){
		Platform.runLater(new Runnable() {
			@Override public void run() {
				statusText.setText(status);
			}
		});    	
	}
	private void setBackupButtonDisable(boolean value){
		Platform.runLater(new Runnable() {
			@Override public void run() {
				runBackupButton.setDisable(value);
			}
		});    			
	}
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
					Thread.sleep(10000);
					
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

	@FXML
	void viewBackupDetails(MouseEvent event) {
		//System.out.println("Hi");
		setBackupButtonDisable(true);
		//Can't call ,start() if its state is SUCCEEDED, so make a new one.
		if(loadBackup.getState().toString().equals("SUCCEEDED")){
			loadBackup = new LoadBackupService();
		}
		if(!loadBackup.getState().toString().equals("RUNNING")){
			loadBackup.start();	
			setBackupButtonDisable(false);		
		}

	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		(new Thread(task)).start();
		assert createBackup != null : "fx:id=\"createBackup\" was not injected: check your FXML file 'Archiver.fxml'.";
		assert backupList != null : "fx:id=\"backupList\" was not injected: check your FXML file 'Archiver.fxml'.";
		initializeBackupFileList();
	}
	private void toggleProgressBar(){
		Platform.runLater(new Runnable() {
			@Override public void run() {
				progressBar.setVisible(!progressBar.isVisible());
			}
		});    	
	}
	private class LoadBackupService extends Service<Void> {
 
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					toggleProgressBar();
					setBackupButtonDisable(true);
					String selectedItem = (String)backupList.getSelectionModel().getSelectedItem();
					if(selectedItem != null){

						File backupFile = new File("presets/" + selectedItem);
						if(backupFile.isFile()){
							System.out.println(backupFile.getAbsoluteFile().toString());  
							//http://www.journaldev.com/2315/java-json-processing-api-example-tutorial
							try{
								setStatusText("Reading json file.");

								FileInputStream jsonInputStream = new FileInputStream(backupFile);
								JsonReader jsonReader = Json.createReader(jsonInputStream);      				
								JsonObject jsonFileContent = jsonReader.readObject();

								jsonInputStream.close();
								jsonReader.close();
								
								System.out.println(jsonFileContent.getString("name"));
								backupFileName.setText("Name: " + jsonFileContent.getString("name"));
								JsonArray jsonBackupFilesArray = jsonFileContent.getJsonArray("files");
								
								setStatusText("Clearing current list.");
								Platform.runLater(new Runnable() {
									@Override public void run() {
										backupFileList.getItems().clear();	
										fileNumberBox.setText("No. Files: " + String.valueOf(jsonBackupFilesArray.size()));
										backupDestinationBox.setText("Destination: " + "destination");
									}
								});

								setStatusText("Populating ListView.");
								for(JsonValue file : jsonBackupFilesArray){
									String current = file.toString().replaceAll("\"","");
									Platform.runLater(new Runnable(){
										@Override public void run(){
											backupFileList.getItems().add(current);
										}
									});
								}
								setStatusText("Done.");
								System.gc();
							}			
							catch(IOException  exc){
								exc.printStackTrace();
							}
						}
						else{
							backupFileName.setText("");
						}
					}
					toggleProgressBar();
					setBackupButtonDisable(false);
					return null;
				}
			};
		}
	}
}
