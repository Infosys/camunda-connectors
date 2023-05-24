/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.model.response.Response;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WriteFileServiceTest {
	
	private WriteFileService service;
	@Mock FTPFile file;
	@Mock FTPClient ftpClient;
	
	@BeforeEach
	public void init() throws IOException {
		service = new WriteFileService();
		service.setFolderPath("C:/Users/xyz/Documents/ftproot");
		service.setFileName("a.txt");
		service.setContent("content");
		service.setAppendContent("false");
	}
	
	@DisplayName("Should Write file")
	@Test
	void validTestWriteFile() throws Exception {
	    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
		OutputStream os = new ByteArrayOutputStream();
		FTPFile file1 = new FTPFile();
        file1.setName("a.txt");
        file1.setType(0);
        FTPFile[] ftpFiles = {file1};
        Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
	    Mockito.when(ftpClient.storeFileStream(any(String.class))).thenReturn(os);
	    Mockito.when(ftpClient.appendFileStream(any(String.class))).thenReturn(os);
        Mockito.when(file.isFile()).thenReturn(true);
        Mockito.when(ftpClient.completePendingCommand()).thenReturn(true);
        Mockito.when(ftpClient.isConnected()).thenReturn(true);
	    Response result = service.invoke(ftpClient);
	    @SuppressWarnings("unchecked")
		FTPResponse<String> queryResponse = (FTPResponse<String>) result;
	    assertThat(queryResponse).extracting("response").isInstanceOf(String.class).isNotNull();
	    Mockito.verify(ftpClient, Mockito.times(1)).logout();
	    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();	
	}
	
    @DisplayName("Should throw an exception source file does not exists")
	@Test
	void invalidTestWriteFile() throws Exception {
        Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    	String folderPath = "/Documents/ftproot";
    	FTPFile file1 = new FTPFile();
        file1.setName("a.txt");
        FTPFile file2 = new FTPFile();
        file2.setName("b.txt");
        FTPFile file3 = new FTPFile();
        file3.setName("c.txt");
        FTPFile[] ftpFiles = {file1, file2, file3};
        Mockito.when(ftpClient.listFiles(folderPath)).thenReturn(ftpFiles);
        Mockito.when(ftpClient.isConnected()).thenReturn(true);
	    assertThatThrownBy(() -> service.invoke(ftpClient))
	    .isInstanceOf(RuntimeException.class)
	    .hasMessageContaining("Error");
	    Mockito.verify(ftpClient, Mockito.times(1)).logout();
	    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
	}

}
