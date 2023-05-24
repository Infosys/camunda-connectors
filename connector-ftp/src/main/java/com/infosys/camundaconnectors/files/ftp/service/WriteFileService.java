/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequestData;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;

public class WriteFileService implements FTPRequestData {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WriteFileService.class);
	
	@NotBlank
	private String folderPath;
	@NotBlank
	private String fileName;
	@NotBlank
	private String content;
	private String appendContent;
	
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
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public void setAppendContent(String appendContent) {
		this.appendContent = content;
	}
	
	public String getAppendContent() {
		return this.appendContent;
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
			} else if(!checkFileExists(folderPath, fileName, client)) {
				throw new RuntimeException("File doesn't exist!!");
			} else if(content.equals("") || content==null){
				throw new RuntimeException("Provide some content!!");
			} else {
				client.setFileType(FTPClient.ASCII_FILE_TYPE);
		        client.changeWorkingDirectory(folderPath);
		        OutputStream os;
				if (contentInBytes.length > 32766) {
					throw new RuntimeException("Fetch overloaded!!");
				}
		        if(appendContent.equals("true")) 
		        	os = client.appendFileStream(fileName);
		        else 
		        	os = client.storeFileStream(fileName);
	            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os);
	            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
	            bufferedWriter.append(content);
	            bufferedWriter.flush();
	            bufferedWriter.close();
	            outputStreamWriter.close();
		        client.completePendingCommand();
		        LOGGER.info("Successfully written the content!!");
		        return new FTPResponse<>("Successfully written the content!!");
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
      return Objects.hash(content, folderPath, fileName, appendContent);
    }

	@Override
	public boolean equals(Object obj) {
	  if (this == obj) return true;
	  if (obj == null) return false;
	  if (getClass() != obj.getClass()) return false;
	  WriteFileService other = (WriteFileService) obj;
	  return Objects.equals(content, other.content) && Objects.equals(folderPath, other.folderPath) && Objects.equals(appendContent, other.appendContent);
	}
	
}
