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
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequestData;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;

public class MoveFileService implements FTPRequestData{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MoveFileService.class);
	@NotBlank
	private String sourceFolderPath;
	@NotBlank
	private String sourceFileName;
	@NotBlank
	private String targetFolderPath;
	@NotBlank
	private String actionIfFileExists;
	@NotBlank
	private String createTargetFolder;
	
	
	public void setSourceFolderPath(String srcPath) {
		this.sourceFolderPath = srcPath;
	}
	
	public String getSourceFolderPath() {
		return this.sourceFolderPath;
	}
	
	public void setSourceFileName(String srcFile) {
		this.sourceFileName = srcFile;
	}
	
	public String getSourceFileName() {
		return this.sourceFileName;
	}
	
	public void setTargetFolderPath(String destPath) {
		this.targetFolderPath = destPath;
	}
	
	public String getTargetFolderPath() {
		return this.targetFolderPath;
	}
	
	public void setActionIfFileExists(String action) {
		this.actionIfFileExists = action;
	}
	
	public String getActionIfFileExists() {
		return this.actionIfFileExists;
	}
	
	public void setCreateTargetFolder(String create) {
		this.createTargetFolder = create;
	}
	
	public String getCreateTargetFolder() {
		return this.createTargetFolder;
	}
	
	private boolean checkFolderExists(String folderPath, FTPClient client) throws IOException {
		return client.changeWorkingDirectory(sourceFolderPath);
	}
	
	private boolean checkFileExists(String filePath, FTPClient client) throws IOException {
		FTPFile[] files = client.listFiles(filePath);
		return files.length==1 && files[0].isFile();
	}
	
	public FTPResponse<String> invoke(FTPClient client) {
        try {
	        if (!checkFolderExists(sourceFolderPath, client)) {
	        	throw new RuntimeException("Source Folder doesn't exist!!");
	        } else if(!checkFileExists(sourceFolderPath+File.separator+sourceFileName, client)) {
	        	throw new RuntimeException("Source File doesn't exist!!");
	        } else if(!checkFolderExists(targetFolderPath, client)) {
	        	if(createTargetFolder.equals("true")) {
	        		client.changeWorkingDirectory(File.separator);
	        		client.makeDirectory(targetFolderPath);
	        	} else {
	        		throw new RuntimeException("Target Folder doesn't exist!!");
	        	}
	        } else {
	        	LOGGER.info("All Set!!");
	        }
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException("Error: " + e.getMessage());
		}
        try {
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            String remoteSourcePath = sourceFolderPath + File.separator + sourceFileName;
            String remoteTargetPath;
            if(checkFileExists(targetFolderPath+File.separator+sourceFileName, client)) {
            	if(actionIfFileExists.equals(null) || actionIfFileExists.equals("rename")) {
            		String fileName = sourceFileName.split("\\.")[0];
            		String extn = sourceFileName.split("\\.")[1];
            		int cnt = 1;
            		String remoteFileName = fileName+"("+Integer.toString(cnt)+")."+extn;
            		while(checkFileExists(targetFolderPath+File.separator+remoteFileName, client)) {
            			cnt++;
            			remoteFileName = fileName+"("+Integer.toString(cnt)+")."+extn;
            		}
                    remoteTargetPath = targetFolderPath + File.separator + remoteFileName;
            	} else if(actionIfFileExists.equals("replace")) {
            		remoteTargetPath = targetFolderPath + File.separator + sourceFileName;
        	    	boolean deleted = client.deleteFile(remoteTargetPath);
        			if (!deleted) {
        				throw new RuntimeException("Could not move the file!!");
        			}
            	} else {
            		return new FTPResponse<>("Operation skipped!!");            		
            	}
            } else {
            	remoteTargetPath = targetFolderPath + File.separator + sourceFileName;
            }
            LOGGER.info(remoteTargetPath);
            boolean success = client.rename(remoteSourcePath, remoteTargetPath);
            if (success) {
                return new FTPResponse<>("File moved successfully!!");
            } else {
                throw new RuntimeException("Failed to move file!!");
            }
        } catch (Exception ex) {
        	LOGGER.error("Error: " + ex.getMessage());
            throw new RuntimeException("Error: " + ex.getMessage());
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
	  return Objects.hash(actionIfFileExists, sourceFolderPath, sourceFileName, targetFolderPath, actionIfFileExists);
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    MoveFileService other = (MoveFileService) obj;
	    return Objects.equals(actionIfFileExists, other.actionIfFileExists)
	        && Objects.equals(sourceFolderPath, other.sourceFolderPath)
	        && Objects.equals(sourceFileName, other.sourceFileName)
	        && Objects.equals(targetFolderPath, other.targetFolderPath)
	        && Objects.equals(actionIfFileExists, other.actionIfFileExists);
	}
	
}
