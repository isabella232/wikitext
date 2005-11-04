package org.eclipse.mylar.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.mylar.core.MylarPlugin;

/**
 * Contains utility methods for working with zip files
 * 
 * @author Wesley Coelho
 * @author Shawn Minto (Wrote methods that were moved here)
 */
public class ZipFileUtil {

	/**
	 * @param zipFile Destination zipped file
	 * @param files List of files to add to the zip file
	 * @author Shawn Minto
	 */
	public static void createZipFile(File zipFile, List<File> files) throws FileNotFoundException, IOException{
        if(zipFile.exists()){
        	zipFile.delete();
        }
        
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
        
        for(File file: files){
        	try{
        		addZipEntry(zipOut, file);
        	}catch (Exception e){
        		MylarPlugin.log(e, "Could not add " + file.getName() + " to zip");
        	}
        }
    
        // Complete the ZIP file
        zipOut.close();
	}
	
	/**
	 * @author Shawn Minto
	 */
	private static void addZipEntry(ZipOutputStream zipOut, File file) throws FileNotFoundException, IOException {

        // Create a buffer for reading the files
        byte[] buf = new byte[1024];
        
        // Compress the files
        FileInputStream in = new FileInputStream(file);

        // Add ZIP entry to output stream.
        zipOut.putNextEntry(new ZipEntry(file.getName()));

        // Transfer bytes from the file to the ZIP file
        int len;
        while ((len = in.read(buf)) > 0) {
        	zipOut.write(buf, 0, len);
        }

        // Complete the entry
        zipOut.closeEntry();
        in.close();
	}
	
}
