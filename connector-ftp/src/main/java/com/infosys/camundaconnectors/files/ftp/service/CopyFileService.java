/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;
	
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequestData;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
	
public class CopyFileService implements FTPRequestData {	
	private static final Logger LOGGER = LoggerFactory.getLogger(CopyFileService.class);
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
		return client.changeWorkingDirectory(folderPath);
	}
	
	private boolean checkFileExists(String filePath, FTPClient client) throws IOException {
		FTPFile[] files = client.listFiles(filePath);
		return files.length==1 && files[0].isFile();
	}
	
	public FTPResponse<String> invoke(FTPClient srcClient, FTPClient destClient) {
	
        try {
	        if (!checkFolderExists(sourceFolderPath, srcClient)) {
	        	throw new RuntimeException("Source Folder doesn't exist!!");
	        } else if(!checkFileExists(sourceFolderPath+File.separator+sourceFileName, srcClient)) {
	        	throw new RuntimeException("Source File doesn't exist!!");
	        } else if(!checkFolderExists(targetFolderPath, destClient)) {
	        	LOGGER.info(""+createTargetFolder);
	        	if(createTargetFolder.equals("true")) {
	        		destClient.changeWorkingDirectory(File.separator);
	        		destClient.makeDirectory(targetFolderPath);
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
            srcClient.enterLocalPassiveMode();
            srcClient.setFileType(FTP.BINARY_FILE_TYPE);
            destClient.enterLocalPassiveMode();
            destClient.setFileType(FTP.BINARY_FILE_TYPE); 
            String remoteFilePath = sourceFolderPath + File.separator + sourceFileName;
            String remoteTargetPath;
            if(checkFileExists(targetFolderPath+File.separator+sourceFileName, destClient)) {
            	if(actionIfFileExists.equals("rename")) {
            		String fileName = sourceFileName.split("\\.")[0];
            		String extn = sourceFileName.split("\\.")[1];
            		int cnt = 1;
            		String remoteFileName = fileName+"("+Integer.toString(cnt)+")."+extn;
            		while(checkFileExists(targetFolderPath+File.separator+remoteFileName, destClient)) {
            			cnt++;
            			remoteFileName = fileName+"("+Integer.toString(cnt)+")."+extn;
            		}
                    remoteTargetPath = targetFolderPath + File.separator + remoteFileName;
            	} else if(actionIfFileExists.equals("replace")) {
            		remoteTargetPath = targetFolderPath + File.separator + sourceFileName;
            	} else {
            		LOGGER.info("Operation skipped!!");
            		return new FTPResponse<>("Operation skipped!!");            		
            	}
            } else {
            	remoteTargetPath = targetFolderPath + File.separator + sourceFileName;
            }
            InputStream inputStream = srcClient.retrieveFileStream(remoteFilePath);
            OutputStream outputStream = destClient.storeFileStream(remoteTargetPath);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            boolean startSuccess = srcClient.completePendingCommand();
            boolean endSuccess = destClient.completePendingCommand();
            if (startSuccess && endSuccess) {
            	LOGGER.info("File copied successfully.");
                return new FTPResponse<>("File copied successfully.");
            } else {
                throw new RuntimeException("Failed to copy file.");
            }
        } catch (Exception ex) {
        	LOGGER.error("Error: " + ex.getMessage());
            throw new RuntimeException("Error: " + ex.getMessage());
        } finally {
            try {
                if (srcClient.isConnected()) {
                    srcClient.logout();
                    srcClient.disconnect();
                    LOGGER.debug("Successfully disconnected from the server");
                }
                if (destClient.isConnected()) {
                    destClient.logout();
                    destClient.disconnect();
                    LOGGER.debug("Successfully disconnected from the server");
                }
            } catch (IOException ex) {
            	LOGGER.error("Failed to disconnect from the server!!");
            }
        }       
	}
	
	
	@Override
	public FTPResponse<String> invoke(FTPClient client) {
		return null;
	}
	
	@Override
	public int hashCode() {
	  return Objects.hash(
	        actionIfFileExists, createTargetFolder, sourceFolderPath, sourceFileName, targetFolderPath);
	}
	
    @Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    CopyFileService other = (CopyFileService) obj;
	    return Objects.equals(actionIfFileExists, other.actionIfFileExists)
	        && Objects.equals(createTargetFolder, other.createTargetFolder)
	        && Objects.equals(sourceFileName, other.sourceFileName)
	        && Objects.equals(sourceFolderPath, other.sourceFolderPath)
	        && Objects.equals(targetFolderPath, other.targetFolderPath);
	 }	
}
