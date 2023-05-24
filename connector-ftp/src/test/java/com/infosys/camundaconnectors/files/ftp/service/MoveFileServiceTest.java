/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;
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
class MoveFileServiceTest {

  @Mock private FTPClient ftpClient;
  @Mock private MoveFileService service;
  @Mock private FTPFile file;
  
  @BeforeEach
  public void init() {
    service = new MoveFileService();
    service.setSourceFolderPath("/Documents/ftproot");
    service.setSourceFileName("a.txt");
    service.setTargetFolderPath("/Documents/ftproot/MyFolder");
    service.setActionIfFileExists("rename");
    service.setCreateTargetFolder("false");
  }

  @DisplayName("Should move file")
  @Test
  void validTestMoveFile() throws Exception {
	FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] ftpFiles = {file1, file2, file3};
    FTPFile[] files = {file1};
    file1.setType(0);
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files, ftpFiles);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.rename(any(String.class), any(String.class))).thenReturn(true);
    Mockito.when(file.isFile()).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File moved successfully!!");
  }

  @DisplayName("Should throw an exception source file does not exists")
  @Test
  void invalidTestMoveFile() throws Exception {
    Mockito.when(ftpClient.rename(any(String.class), any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    assertThatThrownBy(() -> service.invoke(ftpClient))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error");
  }

  @DisplayName("Should rename file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWithRenameWhenSourceFileIsPresentInTarget() throws Exception {
	FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] ftpFiles = {file1, file2, file3};
    FTPFile[] files = {file1};
    file1.setType(0);
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files, files, ftpFiles);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.rename(any(String.class), any(String.class))).thenReturn(true);
    Mockito.when(file.isFile()).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
	    .contains("File moved successfully!!");
  }

  @DisplayName("Should replace file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWhenSourceFileIsPresentInTargetAndReplaceFile() throws Exception {
    service.setActionIfFileExists("replace");
	FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] files = {file1};
    file1.setType(0);
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.rename(any(String.class), any(String.class))).thenReturn(true);
    Mockito.when(file.isFile()).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
	    .contains("File moved successfully!!");
  }

  @DisplayName("Should skip file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWhenSourceFileIsPresentInTargetAndSkipFile() throws Exception {
    service.setActionIfFileExists("skip");
	FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] files = {file1};
    file1.setType(0);
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.rename(any(String.class), any(String.class))).thenReturn(true);
    Mockito.when(file.isFile()).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
	    .contains("Operation skipped!!");
    }
}