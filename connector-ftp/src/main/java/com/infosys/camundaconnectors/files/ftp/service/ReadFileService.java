/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequestData;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;

public class ReadFileService implements FTPRequestData {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadFileService.class);
	@NotBlank
	private String folderPath;
	@NotBlank
	private String fileName;

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	
	public String getFolderPath() {
		return this.folderPath;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	private boolean checkValidPath(String folderPath, FTPClient client) throws IOException {
		return client.changeWorkingDirectory(folderPath);
	}
	
	private boolean checkFileExists(String path, String filename, FTPClient client) throws IOException {
		FTPFile[] files = client.listFiles(path+File.separator+fileName);
		return files.length==1 && files[0].isFile();	
	}
	
	public FTPResponse<String> invoke(FTPClient client) {
		try {
			if(!checkValidPath(folderPath, client)) {
				throw new RuntimeException("Invalid folderPath!!");
			} else if(!checkFileExists(fileName, fileName, client)) {
				throw new RuntimeException("File doesn't exist!!");
			} else {
				client.setFileType(FTPClient.ASCII_FILE_TYPE);
		        client.changeWorkingDirectory(folderPath);
		        InputStreamReader inputStreamReader = new InputStreamReader(client.retrieveFileStream(fileName));
		        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		        String line = null;
		        String content = "";
		        while ((line = bufferedReader.readLine()) != null) {
		            content+=(line+"\n");
		        }
				byte[] contentInBytes = content.getBytes();
				if (contentInBytes.length > 32766) {
					throw new RuntimeException("Fetch overloaded!!");
				}
		        bufferedReader.close();
		        inputStreamReader.close();
		        client.completePendingCommand();
		        LOGGER.info("The content is : " + content);
		        return new FTPResponse<>(content);
			}
		} catch(Exception e) {
			LOGGER.error("Error : " + e.getMessage());
			throw new RuntimeException("Error: " + e.getMessage());
		} finally {
			try {
				if(client.isConnected()) {
					client.logout();
					client.disconnect();
				}
			} catch(Exception e) {
				LOGGER.error("Failed to disconnect from the FTP server!!");
			}
		}
	}

	@Override
	public FTPResponse<String> invoke(FTPClient client1, FTPClient client2) {
		return null;
	}
	
	@Override
	public int hashCode() {
	   return Objects.hash(folderPath, fileName);
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    ReadFileService other = (ReadFileService) obj;
	    return Objects.equals(folderPath, other.fileName);
	}

	
}
