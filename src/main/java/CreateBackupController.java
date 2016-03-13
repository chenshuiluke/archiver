package archiver;

import java.net.URL;
import java.util.ResourceBundle;
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

public class CreateBackupController {

    @FXML 
    private TreeTableView<FileToBackup> fileTable;
	@FXML
	private TreeTableColumn<FileToBackup, String> filesColumn;
	@FXML
	private TreeTableColumn<FileToBackup, String> locationsColumn;
	
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
			fileTable.getRoot().getChildren().add(addFilesRecursively(directory));
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
	private void save(){
		printChildren(fileTable.getRoot());
	}
    private void printChildren(TreeItem<FileToBackup> root){
		if(root != null){
			System.out.println("Current Parent :" + root.getValue());
			for(TreeItem<FileToBackup> child: root.getChildren()){
				if(child != null && child.getChildren() != null && child.getChildren().isEmpty()){
					System.out.println(child.getValue().getLocation());
				} else {
					printChildren(child);
				}
			}
		}
    }
}