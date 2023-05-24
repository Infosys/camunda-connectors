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
import java.io.IOException;
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
class MoveFolderServiceTest {
  @Mock private FTPClient ftpClient;
  @Mock private MoveFolderService service;

  @BeforeEach
  public void init() throws IOException {
    service = new MoveFolderService();
    service.setSourcePath("/Documents/ftproot/demo1");
    service.setTargetPath("/Documents/ftproot/demo2");
    service.setActionIfFolderExists("rename");
    service.setCreateTargetFolder("false");
  }

  @DisplayName("Should move folder")
  @Test
  void validTestMoveFile() throws Exception {
	Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true,true,true,false,true,true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] files = {};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(ftpClient.rename(any(String.class),any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should throw an exception source file does not exists")
  @Test
  void invalidTestMoveFile() throws Exception {
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(false);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    assertThatThrownBy(() -> service.invoke(ftpClient))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error");
  }

  @DisplayName("Should rename file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWithRenameWhenSourceFileIsPresentInTarget() throws Exception {
	Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true,true,true,true,false,true,true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] files = {};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(ftpClient.rename(any(String.class),any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should replace file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWhenSourceFileIsPresentInTargetAndReplaceFile() throws Exception {
    service.setActionIfFolderExists("replace");
	Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] files = {};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(ftpClient.rename(any(String.class),any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should skip file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWhenSourceFileIsPresentInTargetAndSkipFile() throws Exception {
    service.setActionIfFolderExists("skip");
	Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] files = {};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(ftpClient.rename(any(String.class),any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }
}