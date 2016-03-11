package archiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Compressor
{
    ArrayList<String> fileList;
    private static String outputZipFile;
    private static String sourceFolder = null;
	
	/**
	*AppZip
	*@param sourceFolder directory to read files from
	*@param outputZipFile the location of the new zip file
	*@param setZipExtension determines whether or not a ".zip" is appended to outputZipFile	
	*/
    Compressor(String sourceFolder, String outputZipFile, boolean setZipExtension){
		fileList = new ArrayList<String>();
		this.sourceFolder = sourceFolder;
		if(setZipExtension)
			this.outputZipFile = outputZipFile + ".zip";
		else
			this.outputZipFile = outputZipFile;
    }
	void compress(int compressionLevel){
        if(compressionLevel < 0 || compressionLevel > 9){
            System.out.println("Compression level must be 0-9");
            return;
        }
    	generateFileList(new File(sourceFolder));
    	zipIt(outputZipFile, compressionLevel);
	}
    private void clearLine(){
        System.out.print("\r");
        for(int counter = 0; counter < 110; counter++){
            System.out.print(" ");
        }
        System.out.print("\r");
    }
    public ArrayList<String> getList(){
        return fileList;
    }
    public void setList(ArrayList<String> list){
        fileList = list;
    }
    private double estimatedTimeRemaining(double totalTime,int numDone, int numRemaining){
        return (totalTime / numDone) * numRemaining;
    }
    private double numFilesPerSecond(double totalTime, int numDone){
        return numDone > 0 ? totalTime/numDone : 0;
    }
    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    public void zipIt(String zipFile, int compressionLevel){

     byte[] buffer = new byte[1024];
    	
     try{
    		
    	FileOutputStream fos = new FileOutputStream(zipFile);
    	ZipOutputStream zos = new ZipOutputStream(fos);
		zos.setLevel(compressionLevel);
    		
    	System.out.println("Output to Zip : " + zipFile);
        long startTime = 0;
        long endTime = 0;
        double timeDiffs = 0;
        for(int counter = 0; counter < this.fileList.size(); counter++){
            String file = fileList.get(counter);
            try{                
                clearLine();
                timeDiffs += (endTime - startTime)/1000000000;
                double eta = estimatedTimeRemaining(timeDiffs, counter, fileList.size());
                System.out.printf("\rAdding %d / %d ETA: %.0f seconds | %.2f minutes | %.2f hours rate: %f files/second %s",
                 counter, fileList.size(), eta, eta/60, eta / 3600, numFilesPerSecond(timeDiffs, counter), file);
                startTime = System.nanoTime();
               // System.out.println("File Added : " + file);
                ZipEntry ze= new ZipEntry(file);
                zos.putNextEntry(ze);
                   
                FileInputStream in = 
                           new FileInputStream(sourceFolder + File.separator + file);
               
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                   
                in.close();      
                endTime = System.nanoTime();
                try {
                    Thread.sleep(200);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            catch(FileNotFoundException exc){
                System.out.println("\nError reading " + file);
            }
            catch(IOException exc){
                System.out.println("\nFile too large: " + file);
            }

        }
    	/*
    	for(String file : this.fileList){
    			
    		System.out.println("File Added : " + file);
    		ZipEntry ze= new ZipEntry(file);
        	zos.putNextEntry(ze);
               
        	FileInputStream in = 
                       new FileInputStream(sourceFolder + File.separator + file);
       	   
        	int len;
        	while ((len = in.read(buffer)) > 0) {
        		zos.write(buffer, 0, len);
        	}
               
        	in.close();
    	}
    	*/
    	zos.closeEntry();
    	//remember close it
    	zos.close();
          
    	System.out.println("Done");
    }
    catch(IOException ex){
       ex.printStackTrace();   
    }
   }
    
    /**
     * Traverse a directory and get all files,
     * and add the file into fileList  
     * @param node file or directory
     */
    public void generateFileList(File node){

    	//add file only
	if(node.isFile()){
		fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
	}
		
	if(node.isDirectory()){
		String[] subNote = node.list();
		for(String filename : subNote){
			generateFileList(new File(node, filename));
		}
	}
 
    }

    /**
     * Format the file path for zip
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file){
    	return file.substring(sourceFolder.length()+1, file.length());
    }
}
