package archiver;

import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

public class Backup{
	StringProperty name = new SimpleStringProperty();
	StringProperty location = new SimpleStringProperty();
	IntegerProperty interval = new SimpleIntegerProperty();
	ArrayList<String> files;

	Backup(){
		name.set("");
		location.set("");
		interval.set(0);
		files = new ArrayList<>();
	}

	void setName(String name){
		this.name.set(name);
	}

	void setLocation(String location){
		this.location.set(location);
	}

	void setInterval(int interval){
		this.interval.set(interval);
	}

	void setFiles(ArrayList<String> files){
		this.files.clear();
		this.files = new ArrayList<String>(files);
	}

	String getName(){
		return name.get();
	}

	String getLocation(){
		return location.get();
	}

	int getInterval(){
		return interval.get();
	}

	ArrayList<String> getFiles(){
		return files;
	}

	StringProperty nameProperty(){
		return name;
	}

	StringProperty locationProperty(){
		return location;
	}

	IntegerProperty IntegerProperty(){
		return interval;
	}
}