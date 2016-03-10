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
    List<String> fileList;
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
	void compress(){
    	generateFileList(new File(sourceFolder));
    	zipIt(outputZipFile);
	}

    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    public void zipIt(String zipFile){

     byte[] buffer = new byte[1024];
    	
     try{
    		
    	FileOutputStream fos = new FileOutputStream(zipFile);
    	ZipOutputStream zos = new ZipOutputStream(fos);
		zos.setLevel(9);
    		
    	System.out.println("Output to Zip : " + zipFile);
    		
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
