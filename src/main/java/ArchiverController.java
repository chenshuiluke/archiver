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
import java.io.BufferedReader;
import java.io.BufferedWriter;


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
import java.nio.charset.Charset;
import com.google.common.collect.HashBiMap; //http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/HashBiMap.html
import javafx.scene.control.ButtonBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.nio.file.StandardOpenOption;

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

    @FXML
    private ButtonBar backupModificationBar;

    private HashBiMap<String, TreeItem<String>> fileToTreeItemHashMap = HashBiMap.create(); //http://stackoverflow.com/questions/2574685/java-how-to-use-googles-hashbimap
    private HashMap<String, Compressor> backupToRunningBackupThreadMap = new HashMap<>();


    private Backup backupDetails = new Backup();
    @FXML
    void cancelRunningBackup() {
    	String backupName = backupDetails.getName();
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
				cancelRunningBackupButton.setDisable(!value);
				
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
					Thread.sleep(1000);
					
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
			e.printStackTrace();
		}

	}
	private ArrayList<String> addFilesRecursively(File file){
		ArrayList<String>list = new ArrayList<>();
		//add file only
		if(file.isFile()){
			list.add(file.getAbsolutePath());
		}
		else if(file.isDirectory()){
				String[] subNote = file.list();
				list.add(file.getAbsolutePath());
				if(subNote != null){
					for(String filename : subNote){
						File temp = new File(file, filename);
						
						list.addAll(addFilesRecursively(temp));
					}
				}
		}
		return list;
	}
    @FXML
    void selectDestination() {
    	DirectoryChooser chooser = new DirectoryChooser();
    	chooser.setTitle("Choose directory");
    	File file = chooser.showDialog(null);
    	if(file != null){
	    	try{
		    	ArrayList<String> fileList = new ArrayList<>();
				BufferedReader reader = Files.newBufferedReader(Paths.get("presets/" + backupDetails.getName() + ".txt"), Charset.forName("UTF-8"));

				String line = null;
				while ((line = reader.readLine()) != null){
						fileList.add(line);
				}
				reader.close();
				fileList.set(1, file.getAbsolutePath());
				BufferedWriter writer = Files.newBufferedWriter(Paths.get("presets/" + backupDetails.getName() + ".txt"), Charset.forName("UTF-8")
						, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
				for(String item : fileList){
					writer.write(item);
					writer.newLine();
				}
				writer.close();
	    	}
	    	catch(IOException exc){
	    		exc.printStackTrace();
	    	}
	    	viewBackupDetails();
	    }
    }

    @FXML
    void removeItem() {
    	ObservableList<TreeItem<String>> selected = backupFileList.getSelectionModel().getSelectedItems();
    	ArrayList<String> listToBeRemovedFromPreset = new ArrayList<>();
    	System.out.println(selected);
    	if(selected != null){
	      	for(TreeItem<String> item : selected){

	      		//System.out.println(item.getValue());
	    		TreeItem<String> parent = item.getParent();
	    		String fileToRemove = fileToTreeItemHashMap.inverse().get(item);
	    		if(fileToRemove == null){
	    			continue;
	    		}
	    		listToBeRemovedFromPreset.add(fileToRemove);
	    		listToBeRemovedFromPreset.addAll(addFilesRecursively(new File(fileToRemove)));
	    		Platform.runLater(new Runnable(){
					@Override public void run(){
			    		if(parent != null){
			    			parent.getChildren().remove(item);	
			    		}	   
			    	}
			    });
	    	}  		
	    	for(String item : listToBeRemovedFromPreset){
	    		removeFromPreset(item);
	    	}
	    	viewBackupDetails();
    	}
    	

    }
    void removeFromPreset(String input){
    	try{
	    	ArrayList<String> fileList = new ArrayList<>();
			BufferedReader reader = Files.newBufferedReader(Paths.get("presets/" + backupDetails.getName() + ".txt"), Charset.forName("UTF-8"));
			//System.out.println(input);
			String line = null;
			while ((line = reader.readLine()) != null){
				if(!line.equals(input))
					fileList.add(line);
			}
			reader.close();
			BufferedWriter writer = Files.newBufferedWriter(Paths.get("presets/" + backupDetails.getName() + ".txt"), Charset.forName("UTF-8")
					, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
			for(String item : fileList){
				writer.write(item);
				writer.newLine();
			}
			writer.close();
    	}
    	catch(IOException exc){
    		exc.printStackTrace();
    	}

    }
    @FXML
    void addFile() {
    	DirectoryChooser chooser = new DirectoryChooser();
    	chooser.setTitle("Choose directory");
    	File file = chooser.showDialog(null);
    	if(file != null){
    		addToPreset(file.getAbsolutePath());
    	}
    	viewBackupDetails();
    }

    @FXML
    void addFolder() {
    	DirectoryChooser chooser = new DirectoryChooser();
    	chooser.setTitle("Choose directory");
    	File directory = chooser.showDialog(null);
    	if(directory != null){
    		ArrayList<String> subFiles = addFilesRecursively(directory);
    		for(String file : subFiles){
    			addToPreset(file);
    		}
    	}
    	viewBackupDetails();
    }
	void addToPreset(String input){
		try{
			BufferedWriter writer = Files.newBufferedWriter(Paths.get("presets/" + backupDetails.getName() + ".txt"), 
						Charset.forName("UTF-8"), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
			writer.write(input);
			writer.newLine();
			writer.close();	
			//viewBackupDetails();
		}
		catch(IOException exc){
			exc.printStackTrace();
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
				if(getExtension(file).equals("txt")){
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
	void viewBackupDetails() {
		//System.out.println("Hi");
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

		}
		else{
			backupProgressText.textProperty().unbind();
			setBackupProgressText("");
			setBackupButtonDisable(true);	
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
		backupFileList.getRoot().setExpanded(true);

		backupFileName.textProperty().bind(backupDetails.nameProperty());
		backupDestinationBox.textProperty().bind(backupDetails.locationProperty());
	}

    @FXML
    void runBackup() {
    	setBackupButtonDisable(true);
 		String backupName = backupDetails.getName();
 		String backupDestination = backupDetails.getLocation();
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
				
				if(backupToRunningBackupThreadMap.get(backupDetails.getName()) != null){
					backupProgressText.textProperty().bind(backupToRunningBackupThreadMap
						.get(backupDetails.getName()).messageProperty());						
				}
				

			}
		});
	}

	private void updateProgressTextToThread(){
		Platform.runLater(new Runnable(){
			@Override public void run() {
				backupProgressText.textProperty().unbind();
				backupProgressText.setText(backupToRunningBackupThreadMap.get(backupDetails.getName()).getMessage());				
	
				backupProgressText.textProperty().bind(backupToRunningBackupThreadMap
					.get(backupDetails.getName()).messageProperty());			
			}
		});
	}

	private void updateProgressBarToThread(){
		Platform.runLater(new Runnable(){
			@Override public void run() {
				runningBackupProgressBar.setProgress(backupToRunningBackupThreadMap.get(backupDetails.getName()).getProgress());
				runningBackupProgressBar.progressProperty().unbind();
				runningBackupProgressBar.progressProperty().bind(backupToRunningBackupThreadMap
					.get(backupDetails.getName()).progressProperty());			
			}
		});
	}
    private ArrayList<String> getAllChildren(TreeItem<String> root){
		ArrayList<String> list = new ArrayList<>();
		if(root != null){
			System.out.println("Current Parent :" + root.getValue());
			for(TreeItem<String> child: root.getChildren()){
				if(child != null && child.getChildren() != null && child.getChildren().isEmpty()){
					list.add(fileToTreeItemHashMap.inverse().get(child));
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
					fileToTreeItemHashMap.clear();
					setProgressBar(true);
					setBackupButtonDisable(true);
					String selectedItem = (String)backupList.getSelectionModel().getSelectedItem();
					if(selectedItem != null){

						File backupFile = new File("presets/" + selectedItem);
						if(backupFile.isFile()){
							System.out.println(backupFile.getAbsoluteFile().toString());  
							//http://www.journaldev.com/2315/java-json-processing-api-example-tutorial
							try{

								ArrayList<String> fileList = new ArrayList<>();

								setStatusText("Reading file.");
								BufferedReader reader = Files.newBufferedReader(Paths.get(backupFile.getPath()), Charset.forName("UTF-8"));
								backupDetails.setName(reader.readLine());
								backupDetails.setLocation(reader.readLine());
								int interval = Integer.valueOf(reader.readLine());
								String line = null;
								while ((line = reader.readLine()) != null)
									fileList.add(line);
								reader.close();
							
								setStatusText("Clearing current list.");
								Platform.runLater(new Runnable() {
									@Override public void run() {
										backupFileList.getRoot().getChildren().clear();	
										fileNumberBox.setText("No. Files: " + String.valueOf(fileList.size()));
										deleteBackupButton.setDisable(false);
									}
								});

								setStatusText("Populating TreeView.");
								fileToTreeItemHashMap.put("root", backupFileList.getRoot());
								for(String current : fileList){
									Platform.runLater(new Runnable(){
										@Override public void run(){									
											populateTreeViewFromList(current.replaceAll("\"", ""));
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
							backupDetails.setName("");
						}
					}
					if(backupToRunningBackupThreadMap.get(backupDetails.getName()) != null){
						updateProgressBarToThread();
						updateProgressTextToThread();
						setBackupButtonDisable(true);
					}
					else{
						//setStatusText("Not found");
						setBackupButtonDisable(false);
						backupProgressText.textProperty().unbind();
						setBackupProgressText("");
					}
					setProgressBar(false);
					
					return null;
				}
			};
		}
	}
}
