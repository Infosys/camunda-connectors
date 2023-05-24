/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequestData;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;

public class DeleteFileService implements FTPRequestData {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFileService.class);
	@NotBlank
	private String folderPath;
	@NotBlank
	private String fileName;
	
	public String getFolderPath() {
		return this.folderPath;
	}
	
	public void setFolderPath(String path) {
		this.folderPath = path;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public void setFileName(String path) {
		this.fileName = path;
	}
	
	private boolean checkFileExists(String path, String filename, FTPClient client) throws IOException {
		FTPFile[] files = client.listFiles(path+File.separator+fileName);
		return files.length==1 && files[0].isFile();
	}
	
	public FTPResponse<String> invoke(FTPClient client) {
		try {
			boolean isFolder = client.changeWorkingDirectory(folderPath);
		    if (!isFolder) {
			    throw new RuntimeException("The given path doesn't corresponds to a folder!!");
		    }
		} catch(Exception e) {
			LOGGER.error("Not a folder!!" + e.getMessage());
			throw new RuntimeException("Error: " + e.getMessage());
		}
	    try {
			boolean fileExist = checkFileExists(folderPath, fileName, client);
		    if(fileExist==false)
			    throw new RuntimeException("File Not Found!!");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException("Error: " + e.getMessage());
		}
	    boolean deleted;
	    try {
	    	String filePath = folderPath + File.separator + fileName;
	    	deleted = client.deleteFile(filePath);
			if (deleted) {
				LOGGER.info("File Deleted Successfully");
				return new FTPResponse<>("File Deleted Successfully");
			} else {
				LOGGER.info("Could not delete the  file, it may not exist.");
				throw new RuntimeException("Could not delete the  file, it may not exist.");
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException("Error: " + e.getMessage());
		} finally {
			try {
                if (client.isConnected()) {
                    client.logout();
                    client.disconnect();
                    LOGGER.debug("Successfully disconnected from the server");
                }
            } catch (Exception ex) {
            	LOGGER.error("Failed to disconnect from the server!!");
            }
		}
	}

	@Override
	public FTPResponse<String> invoke(FTPClient client1, FTPClient client2) {
		return null;
	}
	
	@Override
	public int hashCode() {
	  return Objects.hash(fileName, folderPath);
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    DeleteFileService other = (DeleteFileService) obj;
	    return Objects.equals(fileName, other.fileName) && Objects.equals(folderPath, other.folderPath);
	}
}
