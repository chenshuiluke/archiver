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
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeItem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javax.json.*;
import java.io.OutputStream;
import java.io.FileOutputStream;
import javafx.scene.control.ButtonType;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;

public class CreateBackupController {

    @FXML
    private TreeTableView<FileToBackup> fileTable;
	@FXML
	private TreeTableColumn<FileToBackup, String> filesColumn;
	@FXML
	private TreeTableColumn<FileToBackup, String> locationsColumn;

	private String backupDestination = "";

    @FXML
    private Button destinationButton;

    @FXML
    void setDestination() {
    	DirectoryChooser chooseDestination = new DirectoryChooser();
    	chooseDestination.setTitle("Choose backup destination");
    	File destination = chooseDestination.showDialog(null);
    	if(destination != null){
    		backupDestination = destination.getAbsoluteFile().toString();
    	}
    }

    @FXML
	private TextField backupName;
    @FXML
	private void addFile(){
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose a File");
		File file = chooser.showOpenDialog(null);
		if(file != null){
			TreeItem<FileToBackup> row = new TreeItem<>(new FileToBackup(file.getName(), file.getAbsoluteFile().toString()));
			fileTable.getRoot().getChildren().add(row);
		}
	}
    @FXML
    private void addFolder(){
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Directory");
		File directory = chooser.showDialog(null);
		if(directory != null){
			Alert box = new Alert(AlertType.CONFIRMATION, "Should I try to list the folder's files?"
			 + System.getProperty("line.separator") + "It could take a while...");
			box.showAndWait().ifPresent(response -> {
				 if (response == ButtonType.OK) {
					fileTable.getRoot().getChildren().add(addFilesRecursively(directory));
				 }
				else{
					fileTable.getRoot().getChildren().add(new TreeItem<>(new FileToBackup(directory.getName(),
						directory.getAbsoluteFile().toString())));
				}
			 });
		}
    }
	@FXML
	private void removeSelected(){
		TreeItem item = fileTable.getSelectionModel().getSelectedItem();
		if(item != null){
			item.getParent().getChildren().remove(item);
		}
	}
	private TreeItem<FileToBackup> addFilesRecursively(File file){
		//add file only
		if(file.isFile()){
			TreeItem<FileToBackup> row = new TreeItem<>(new FileToBackup(file.getName(), file.getAbsoluteFile().toString()));
			return row;
		}
		else if(file.isDirectory()){
				String[] subNote = file.list();
				TreeItem<FileToBackup> level = new TreeItem<>(new FileToBackup(file.getName(), file.getAbsoluteFile().toString()));
				if(subNote != null){
					for(String filename : subNote){
						File temp = new File(file, filename);
						TreeItem<FileToBackup> row = new TreeItem<>(new FileToBackup(temp.getName(), temp.getAbsoluteFile().toString()));
						level.getChildren().add(addFilesRecursively(temp));
					}
				}
				return level;
		}
		return null;
	}
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
		if(fileTable.getRoot() == null){
			TreeItem<FileToBackup> root = new TreeItem<FileToBackup>(new FileToBackup("Files"));
			root.setExpanded(true);
			fileTable.setRoot(root);
		}
		fileTable.setShowRoot(true);
		filesColumn.setCellValueFactory((CellDataFeatures<FileToBackup, String> p) -> new ReadOnlyStringWrapper(
					p.getValue().getValue().getName()));
		locationsColumn.setCellValueFactory((CellDataFeatures<FileToBackup, String> p) -> new ReadOnlyStringWrapper(
					p.getValue().getValue().getLocation()));
/*
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
					fileTable.scrollTo(0);
		            System.out.println("Handler caught exception: "+throwable.getMessage());
					        });
*/
    }
	@FXML
	private void save() throws java.io.FileNotFoundException{
		//Checks the number of added files/folders:
		int numberOfDirectRootChildren = fileTable.getRoot().getChildren().size();
		if(backupName.getText().equals("")){
			Alert box = new Alert(AlertType.ERROR, "You must specify a backup name!");
			box.showAndWait();
		}
		else if(backupDestination.equals("")){
			Alert box = new Alert(AlertType.ERROR, "Choose a backup destination.");
			box.showAndWait();
		}
		else if(numberOfDirectRootChildren > 0){
			ArrayList<String> list = getAllChildren(fileTable.getRoot());
			System.out.printf("Backup text: %s|\n", backupName.getText());
			//ArrayList<String> list = getAllChildren(fileTable.getRoot());
			//http://stackoverflow.com/questions/18983185/how-to-create-correct-jsonarray-in-java-using-jsonobject
			try{
				File presetFolder = new File("presets");
		        if(!presetFolder.isDirectory()){
		          Files.createDirectory(Paths.get("presets"));
		        }
				BufferedWriter writer = Files.newBufferedWriter(Paths.get("presets/" + backupName.getText() + ".txt"), 
					Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		        
		        writer.write(backupName.getText());
		        writer.newLine();
		        writer.write(backupDestination);
		        writer.newLine();
		        for(String file : list){
		        	writer.write(file);
		        	writer.newLine();
		        }
		        writer.close();
			}
			catch(java.io.IOException exc){
				exc.printStackTrace();
			}

		}
		else{
			Alert box =  new Alert(AlertType.ERROR, "You must add a file/folder before you can save!");
			box.showAndWait();
		}
	}
    private ArrayList<String> getAllChildren(TreeItem<FileToBackup> root){
		ArrayList<String> list = new ArrayList<>();
		if(root != null){
			System.out.println("Current Parent :" + root.getValue());
			list.add(root.getValue().getLocation());
			for(TreeItem<FileToBackup> child: root.getChildren()){
				if(child != null && child.getChildren() != null && child.getChildren().isEmpty()){
					list.add(child.getValue().getLocation());
			//		System.out.println(child.getValue().getLocation());
				} else {
					list.addAll(getAllChildren(child));
				}
			}
		}
		return list;
    }
}
