package archiver;

import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.ArrayDeque;
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
import java.util.HashMap;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import com.google.common.collect; //http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/BiMap.html


public class ArchiverController{
	LoadBackupService loadBackup = new LoadBackupService();
	@FXML
	private ProgressBar progressBar;

    @FXML
    private Button deleteBackupButton;

	@FXML
	private ProgressBar runningBackupProgressBar;

	@FXML
	private ListView backupList;

	@FXML 
	private MenuItem createBackup;

	@FXML
	private Label statusText;

	@FXML
	private Text backupFileName;

	@FXML
	private TreeView<String> backupFileList;

    @FXML
    private Text fileNumberBox;

    @FXML
    private Text backupDestinationBox;

    @FXML
    private Button runBackupButton;

    @FXML
    private Button cancelRunningBackupButton;

    private HashMap<String, TreeItem<String>> fileToTreeItemHashMap = new HashMap<>();
    private HashMap<String, Compressor> backupToRunningBackupThreadMap = new HashMap<>();

    @FXML
    void cancelRunningBackup() {
    	String backupName = backupFileName.getText();
    	if(backupToRunningBackupThreadMap.get(backupName) != null){
    		backupToRunningBackupThreadMap.get(backupName).cancel();
    	}
    }

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
				if(!loadBackup.getState().toString().equals("RUNNING")){					
					cancelRunningBackupButton.setDisable(!value);
				}
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
			if(statusText.getText().equals("Done.")){
				setProgressBar(false);
			}
		}
	}
	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;


    @FXML
    private Text backupProgressText;

    @FXML
    void deleteSelectedBackup() {
    	try{
	 		String selectedItem = (String)backupList.getSelectionModel().getSelectedItem();
			if(selectedItem != null){ 
				Files.delete(Paths.get("presets/" + selectedItem));
				backupList.getItems().remove(selectedItem);
				backupFileList.getRoot().getChildren().clear();
			}   		
    	}
    	catch(IOException e){
    		e.printStackTrace();
    	}
   	
    }

	@FXML
	void viewBackupDetails(MouseEvent event) {
		//System.out.println("Hi");
		setBackupButtonDisable(true);
		deleteBackupButton.setDisable(true);
		//Can't call ,start() if its state is SUCCEEDED, so make a new one.
		if(loadBackup.getState().toString().equals("SUCCEEDED") || loadBackup.getState().toString().equals("FAILED")){
			loadBackup = new LoadBackupService();
			setProgressBar(false);
		}
		if(!loadBackup.getState().toString().equals("RUNNING")){
			loadBackup.start();	
			setBackupButtonDisable(false);		
			runningBackupProgressBar.progressProperty().unbind();
			runningBackupProgressBar.setProgress(0.0F);
			backupProgressText.textProperty().unbind();
			setBackupProgressText("");
		}

	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		(new Thread(task)).start();
		assert createBackup != null : "fx:id=\"createBackup\" was not injected: check your FXML file 'Archiver.fxml'.";
		assert backupList != null : "fx:id=\"backupList\" was not injected: check your FXML file 'Archiver.fxml'.";
		initializeBackupFileList();
		backupFileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		backupFileList.setRoot(new TreeItem<String>("Backup Contents"));
	}

    @FXML
    void runBackup() {
    	setBackupButtonDisable(true);
 		String backupName = backupFileName.getText();
 		String backupDestination = backupDestinationBox.getText();
 		String backupOutputFile = backupDestination + File.separator + backupName;
    	ArrayList<String> tempList = new ArrayList<>();
    	tempList.addAll(getAllChildren(backupFileList.getRoot()));
    	//If no backup job is running for the current backup...
    	if(backupToRunningBackupThreadMap.get(backupName) == null){
    		backupToRunningBackupThreadMap.put(backupName, 
    			new Compressor(tempList, backupOutputFile, true));

    		runningBackupProgressBar.progressProperty().unbind();
    		runningBackupProgressBar.progressProperty().bind(backupToRunningBackupThreadMap.get(backupName).progressProperty());
    		backupToRunningBackupThreadMap.get(backupName).start();
    		backupProgressText.textProperty().unbind();
    		setBackupProgressText("");
			backupProgressText.textProperty().bind(backupToRunningBackupThreadMap.get(backupName).messageProperty());

    		backupToRunningBackupThreadMap.get(backupName).setOnCancelled(
    			new EventHandler<WorkerStateEvent>() {
           			@Override
		            public void handle(WorkerStateEvent t) {
		                backupToRunningBackupThreadMap.remove(backupName);
		                setBackupButtonDisable(false);
		            }
       		});

    		backupToRunningBackupThreadMap.get(backupName).setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {
           			@Override
		            public void handle(WorkerStateEvent t) {
		                backupToRunningBackupThreadMap.remove(backupName);
		                setBackupButtonDisable(false);
		            }
       		});
    		backupToRunningBackupThreadMap.get(backupName).setOnFailed(
    			new EventHandler<WorkerStateEvent>() {
           			@Override
		            public void handle(WorkerStateEvent t) {
		                backupToRunningBackupThreadMap.remove(backupName);
		                setBackupButtonDisable(false);
		            }
       		});

    	}
    }

	private void toggleProgressBar(){
		Platform.runLater(new Runnable() {
			@Override public void run() {
				progressBar.setVisible(!progressBar.isVisible());
			}
		});    	
	}

	private void setProgressBar(boolean value){
		Platform.runLater(new Runnable() {
			@Override public void run() {
				progressBar.setVisible(value);
			}
		});    			
	}

	private void setBackupProgressText(String text){
		Platform.runLater(new Runnable(){
			@Override public void run(){
				backupProgressText.textProperty().unbind();
				backupProgressText.setText(text);
				if(backupToRunningBackupThreadMap.get(backupFileName.getText()) != null){
					backupProgressText.textProperty().bind(backupToRunningBackupThreadMap
						.get(backupFileName.getText()).messageProperty());						
				}


			}
		});
	}

	private void updateProgressTextToThread(){
		Platform.runLater(new Runnable(){
			@Override public void run() {
				backupProgressText.textProperty().unbind();
				backupProgressText.setText(backupToRunningBackupThreadMap.get(backupFileName.getText()).getMessage());				
	
				backupProgressText.textProperty().bind(backupToRunningBackupThreadMap
					.get(backupFileName.getText()).messageProperty());			
			}
		});
	}

	private void updateProgressBarToThread(){
		Platform.runLater(new Runnable(){
			@Override public void run() {
				runningBackupProgressBar.setProgress(backupToRunningBackupThreadMap.get(backupFileName.getText()).getProgress());
				runningBackupProgressBar.progressProperty().unbind();
				runningBackupProgressBar.progressProperty().bind(backupToRunningBackupThreadMap
					.get(backupFileName.getText()).progressProperty());			
			}
		});
	}
    private ArrayList<String> getAllChildren(TreeItem<String> root){
		ArrayList<String> list = new ArrayList<>();
		if(root != null){
			System.out.println("Current Parent :" + root.getValue());
			for(TreeItem<String> child: root.getChildren()){
				if(child != null && child.getChildren() != null && child.getChildren().isEmpty()){
					list.add(child.getValue());
			//		System.out.println(child.getValue().getLocation());
				} else {
					list.addAll(getAllChildren(child));
				}
			}
		}
		return list;
    }

    private void populateTreeViewFromList(String stringFile){
    	File file = new File(stringFile);
    	String parentString = file.getParent();
    	//System.out.println(parentString);
    	if(fileToTreeItemHashMap.size() == 0){
    			fileToTreeItemHashMap.put(stringFile, new TreeItem<String>(stringFile));
    		    fileToTreeItemHashMap.get("root").getChildren().add(fileToTreeItemHashMap.get(stringFile));
    	}
    	else{
    		if(parentString != null){
    			fileToTreeItemHashMap.put(stringFile, new TreeItem<String>(file.getName()));
    			if(fileToTreeItemHashMap.get(parentString) != null){
    				fileToTreeItemHashMap.get(parentString).getChildren().add(fileToTreeItemHashMap.get(stringFile));
    			}
    			else{
    				fileToTreeItemHashMap.get("root").getChildren().add(fileToTreeItemHashMap.get(stringFile));
    			}
    			//Reverse search by recursively getting parents or perhaps a two-way hashmap
    			//http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/BiMap.html
    		}
    	}
    }
	private class LoadBackupService extends Service<Void> {
 
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					setProgressBar(true);
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
								backupFileName.setText(jsonFileContent.getString("name"));
								backupDestinationBox.setText(jsonFileContent.getString("destination"));
								JsonArray jsonBackupFilesArray = jsonFileContent.getJsonArray("files");
								
								setStatusText("Clearing current list.");
								Platform.runLater(new Runnable() {
									@Override public void run() {
										backupFileList.getRoot().getChildren().clear();	
										fileNumberBox.setText("No. Files: " + String.valueOf(jsonBackupFilesArray.size()));
										deleteBackupButton.setDisable(false);
									}
								});

								setStatusText("Populating TreeView.");
								fileToTreeItemHashMap.put("root", backupFileList.getRoot());
								for(JsonValue current : jsonBackupFilesArray){
									Platform.runLater(new Runnable(){
										@Override public void run(){									
											populateTreeViewFromList(current.toString().replaceAll("\"", ""));
										}
									});									
								}

							
								setStatusText("Done.");
								System.gc();
							}			
							catch(IOException  exc){
								exc.printStackTrace();
								setProgressBar(false);
							}
						}
						else{
							backupFileName.setText("");
						}
					}
					if(backupToRunningBackupThreadMap.get(backupFileName.getText()) != null){
						updateProgressBarToThread();
						updateProgressTextToThread();
						setBackupButtonDisable(true);
					}
					else{
						//setStatusText("Not found");
						setBackupButtonDisable(false);
					}
					setProgressBar(false);
					
					return null;
				}
			};
		}
	}
}
