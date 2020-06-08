package org.timo.chunkedupload.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.timo.chunkedupload.model.RangeData;

@Service
public class FileService {

	public static int sizeOfFiles = 1000 * 1000* 10;// under 10MB, 10MB = 1024 * 10024* 10  
	public static int partCounter = 0;//I like to name parts from 001, 002, 003, ... //you can change it to 0 if you want 000, 001, ...
	
    public List<RangeData> splitFile(File f) throws IOException {
        String fileName = f.getName();
        //try-with-resources to ensure closing stream
        List<String> splitedFiles = new ArrayList<>(); // me(hd) add 
        try (FileInputStream fis = new FileInputStream(f);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            int bytesAmount = 0;
            byte[] buffer = new byte[sizeOfFiles];
            while ((bytesAmount = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                String filePartName = String.format("%s.%04d", fileName, partCounter++);
                File splitFile = new File(f.getParent(), filePartName);
                try (FileOutputStream out = new FileOutputStream(splitFile)) {
                    out.write(buffer, 0, bytesAmount);
                }
                splitedFiles.add(splitFile.getAbsolutePath());// me(hd) add
            }
        }
        // me(hd) add below code
        List<RangeData> rangeDatas = new ArrayList<>(); 
        for (String splitedFilename : splitedFiles) {
        	rangeDatas.add(new RangeData(fileName,splitedFilename,splitedFiles.indexOf(splitedFilename),sizeOfFiles,f.length()));
		}
        return rangeDatas;
    }
}
