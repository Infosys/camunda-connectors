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

public class MoveFolderService implements FTPRequestData {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MoveFolderService.class);
	@NotBlank
	private String sourcePath;
	@NotBlank
	private String targetPath;
	@NotBlank
	private String actionIfFolderExists;
	@NotBlank
	private String createTargetFolder;

	public String getSourcePath() {
		return this.sourcePath;
	}
	
	public void setSourcePath(String path) {
		this.sourcePath = path;
	}
	
	public String getTargetPath() {
		return this.targetPath;
	}
	
	public void setTargetPath(String path) {
		this.targetPath = path;
	}
	
	public String getActionIfFolderExists() {
		return this.actionIfFolderExists;
	}
	
	public void setActionIfFolderExists(String action) {
		this.actionIfFolderExists = action;
	}
	
	public String getCreateTargetFolder() {
		return this.sourcePath;
	}
	
	public void setCreateTargetFolder(String path) {
		this.sourcePath = path;
	}
	
	private boolean checkValidPath(String folderPath, FTPClient client) throws IOException {
		return client.changeWorkingDirectory(folderPath);
	}
	
	private boolean checkFolderExists(String path, String srcFolderName, FTPClient client) throws IOException {
		return checkValidPath(path+File.separator+srcFolderName, client);
	}
	
    private static void moveFolder(FTPClient client, String srcFolderPath, String targetFolderPath) throws IOException {
        FTPFile[] files = client.listFiles(srcFolderPath);
        for (FTPFile file : files) {
            String srcFilePath = srcFolderPath + File.separator + file.getName();
            String targetFilePath = targetFolderPath + File.separator + file.getName();
            if (file.isFile()) {
            	client.rename(srcFolderPath, targetFolderPath);
            } else if (file.isDirectory()) {
                moveFolder(client, srcFilePath, targetFilePath);
            }
        }
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
        	if (!checkValidPath(sourcePath, client)) {
	        	throw new RuntimeException("Invalid sourcePath!!");
	        } else if(!checkValidPath(targetPath, client)) {
	        	if(createTargetFolder.equals("true")) {
	        		client.changeWorkingDirectory(File.separator);
	        		client.makeDirectory(targetPath);
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
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            String remoteTargetPath;
            String srcFolderName = "";
            for(int i=0;i<sourcePath.length();i++) {
            	if(Character.toString(sourcePath.charAt(i)).equals(File.separator)) {
            		srcFolderName = "";
            	} else {
            		srcFolderName += sourcePath.charAt(i);
            	}
            }
            try {
            	if(checkFolderExists(targetPath, srcFolderName, client)) {
                	if(actionIfFolderExists.equals(null) || actionIfFolderExists.equals("rename")) {
                		String folderName = srcFolderName;
                		int cnt = 1;
                		String remoteFolderName = folderName + "("+Integer.toString(cnt)+")";
                		while(checkFolderExists(targetPath, remoteFolderName, client)) {
                			cnt++;
                			remoteFolderName = folderName+"("+Integer.toString(cnt)+")";
                		}
            			remoteTargetPath = targetPath + File.separator + remoteFolderName;
                		client.makeDirectory(remoteTargetPath);
                	} else if(actionIfFolderExists.equals("replace")) {
                		remoteTargetPath = targetPath + File.separator + srcFolderName;
                        client.changeWorkingDirectory(File.separator);
                        removeFolder(remoteTargetPath, client);
                        client.makeDirectory(remoteTargetPath);
                	} else {
                		LOGGER.info("Operation skipped!!");
                		return new FTPResponse<>("Operation skipped!!");            		
                	}
                } else {
                	remoteTargetPath = targetPath + File.separator + srcFolderName;
                	client.makeDirectory(remoteTargetPath);
                }
            } catch(Exception e) {
            	throw new RuntimeException("Error: Couldn't process!!");
            }
            if (!client.changeWorkingDirectory(sourcePath)) {
                LOGGER.error("Source folder doesn't exist");
                throw new RuntimeException("Source folder doesn't exist!!");
            }
            if (!client.changeWorkingDirectory(targetPath)) {
                client.makeDirectory(targetPath);
            }
            FTPFile[] files = client.listFiles(sourcePath);
            for (FTPFile file : files) {
                String sourceFilePath = sourcePath + File.separator + file.getName();
                String destFilePath = remoteTargetPath + File.separator + file.getName();
                if (file.isDirectory()) {
                    moveFolder(client, sourceFilePath, destFilePath);
                } else {
                    client.rename(sourceFilePath, destFilePath);
                }
            }
            client.removeDirectory(sourcePath);
            return new FTPResponse<>("Folder moved successfully!!");
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
	   return Objects.hash(actionIfFolderExists, sourcePath, targetPath, createTargetFolder);
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    MoveFolderService other = (MoveFolderService) obj;
	    return Objects.equals(actionIfFolderExists, other.actionIfFolderExists)
	        && Objects.equals(sourcePath, other.sourcePath)
	        && Objects.equals(targetPath, other.targetPath)
	        && Objects.equals(createTargetFolder, other.createTargetFolder);
	}

}
