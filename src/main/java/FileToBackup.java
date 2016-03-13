package archiver;

import javafx.beans.property.SimpleStringProperty;

public class FileToBackup{
	SimpleStringProperty name;
	SimpleStringProperty location;
	FileToBackup(String name){
		this.name = new SimpleStringProperty(name);
		this.location = new SimpleStringProperty("");
	}
	FileToBackup(String name, String location){
		this.name = new SimpleStringProperty(name);
		this.location = new SimpleStringProperty(location);
	}
	String getName(){
		return name.get();
	}
	String getLocation(){
		return location.get();
	}
}
