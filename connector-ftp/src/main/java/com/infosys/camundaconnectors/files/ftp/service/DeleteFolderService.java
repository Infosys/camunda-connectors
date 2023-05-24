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

public class DeleteFolderService implements FTPRequestData {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFolderService.class);
	
	@NotBlank
	private String parentFolderPath;
	@NotBlank
	private String folderName;
	
	public String getParentFolderPath() {
		return this.parentFolderPath;
	}
	
	public void setParentFolderPath(String path) {
		this.parentFolderPath = path;
	}
	
	public String getFolderName() {
		return this.folderName;
	}
	
	public void setFolderName(String path) {
		this.folderName = path;
	}
	
	private boolean checkFolderExists(String folderPath, FTPClient client) throws IOException {
		return client.changeWorkingDirectory(folderPath);
	}
	
	private void removeFolder(String folderPath, FTPClient client) throws IOException {
		FTPFile[] files = client.listFiles(folderPath);
		for(FTPFile file : files) {
			if(file.isFile()) {
				client.deleteFile(folderPath+File.separator+file.getName());
			} else {
				removeFolder(folderPath+File.separator+file.getName(),client);
			}
		}
		client.removeDirectory(folderPath);
	}
	
	public FTPResponse<String> invoke(FTPClient client) {
		try {
			client.changeWorkingDirectory(File.separator);
			String remoteFolderPath = parentFolderPath + File.separator + folderName;
			if(!checkFolderExists(remoteFolderPath, client)) {
				LOGGER.error("Folder Not Found!!");
				throw new RuntimeException("Folder Not found!!");
			}
			removeFolder(remoteFolderPath,client);
			LOGGER.info("Folder Successfully Deleted!!");
			return new FTPResponse<>("Folder Deletion Successful!!");
		} catch (Exception e) {
			LOGGER.error("Error in deleting folder : " + e.getMessage());
			throw new RuntimeException("Error: " + e.getMessage());
		} finally {
			try {
                if (client.isConnected()) {
                    client.logout();
                    client.disconnect();
                    LOGGER.debug("Successfully disconnected from the server");
                }
            } catch (IOException ex) {
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
	  return Objects.hash(parentFolderPath, folderName);
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    DeleteFolderService other = (DeleteFolderService) obj;
	    return Objects.equals(parentFolderPath, other.folderName);
	}
}
