package archiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.concurrent.Task;
import javafx.concurrent.Service;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compressor extends Service<Void>
{
    HashSet<String> fileList = new HashSet<>();
    private static String outputZipFile;
    private static String sourceFolder = null;
	private static int compressionLevel = 0;
	/**
	*AppZip
	*@param sourceFolder directory to read files from
	*@param outputZipFile the location of the new zip file
	*@param setZipExtension determines whether or not a ".zip" is appended to outputZipFile	
	*/
    void initVariables(String outputZipFile, boolean setZipExtension){
        this.sourceFolder = sourceFolder;
        if(setZipExtension)
            this.outputZipFile = outputZipFile + ".zip";
        else
            this.outputZipFile = outputZipFile;       
    }
    Compressor(String sourceFolder, String outputZipFile, boolean setZipExtension){
		fileList = new HashSet<String>();
        initVariables(outputZipFile, setZipExtension);
    }
    Compressor(ArrayList<String> newFileList, String outputZipFile, boolean setZipExtension){
        fileList.addAll(newFileList);
        initVariables(outputZipFile, setZipExtension);
    }
    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(-1, fileList.size());
                //fileList.addAll(generateFileList(new File(sourceFolder)));
                try{
                    Files.deleteIfExists(Paths.get(outputZipFile));
                    ZipFile zipFile = new ZipFile(outputZipFile);
                    HashSet<String> copy = (HashSet<String>)fileList.clone();
                    for(String file : copy){
                        generateFileList(new File(file));
                    }
                    ZipParameters parameters = new ZipParameters();
                    parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                    parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);                 
                    int counter = 0;
                    for(String file : fileList){
                        updateMessage(String.valueOf(counter) + "/" + fileList.size() + System.getProperty("line.separator") + "Adding " + file);
                        updateProgress(counter, fileList.size());
                        zipFile.addFile(new File(file), parameters);
                        counter++;
                    }      
                    updateProgress(1,1);
                    updateMessage("Done!");
                }
                catch(ZipException exc){
                    exc.printStackTrace();
                }
                return null;
            }
        };
    }

    public void generateFileList(File node){
            //add file only
        if(node.isFile()){
            fileList.add(node.getAbsolutePath());
        }
            
        if(node.isDirectory()){
            String[] subNote = node.list();
            for(String filename : subNote){
                generateFileList(new File(node, filename));
            }
        }     
    }
}
