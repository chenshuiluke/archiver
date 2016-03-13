package archiver;

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
				File preset = new File("presets");
				if(!preset.isDirectory()){
					preset.mkdir();
				}

				Path path = preset.toPath();
				FileSystem fs = path.getFileSystem();
				try(WatchService service = fs.newWatchService()) {
					
					// We register the path to the service
					// We watch for creation events
					path.register(service, ENTRY_CREATE, ENTRY_DELETE);
					
					// Start the infinite polling loop
					WatchKey key = null;
					while(true) {
						key = service.take();
						
						// Dequeueing events
						Kind<?> kind = null;
						for(WatchEvent<?> watchEvent : key.pollEvents()) {
							// Get the type of the event
							kind = watchEvent.kind();
							if (OVERFLOW == kind) {
								continue; //loop
							} else if (ENTRY_CREATE == kind) {
								// A new Path was created 
								Path newPath = ((WatchEvent<Path>) watchEvent).context();
								// Output
								System.out.println("New path created: " + newPath);
								String fileName = newPath.toString();
								String extension = getExtension(fileName);
								if(extension.equals("json")){
										backupList.getItems().add(fileName);
								}
							}
							else if(ENTRY_DELETE == kind){
								// A new Path was created 
								Path newPath = ((WatchEvent<Path>) watchEvent).context();
								// Output
								System.out.println("path deleted: " + newPath);
								String fileName = newPath.toString();
								String extension = getExtension(fileName);
								if(extension.equals("json")){
										backupList.getItems().remove(fileName);
								}
							}
						}
						
						if(!key.reset()) {
							break; //loop
						}
					}
					
				} 
				catch(IOException ioe) {
					ioe.printStackTrace();
				} 
				catch(InterruptedException ie) {
					ie.printStackTrace();
				}
				return null;
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
			String[] list = presets.list();
			for(String file : list){
				if(getExtension(file).equals("json")){
					backupList.getItems().add(file);
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
		Thread thread = new Thread(task);
		thread.start();
        assert createBackup != null : "fx:id=\"createBackup\" was not injected: check your FXML file 'Archiver.fxml'.";
        assert backupList != null : "fx:id=\"backupList\" was not injected: check your FXML file 'Archiver.fxml'.";
		initializeBackupFileList();
    }
}
