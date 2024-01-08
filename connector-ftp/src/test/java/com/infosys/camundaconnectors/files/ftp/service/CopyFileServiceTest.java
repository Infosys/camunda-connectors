/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.model.response.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CopyFileServiceTest {

  @Mock CopyFileService service;
  @Mock FTPClient srcClient;
  @Mock FTPClient destClient;
  @Mock FTPFile file;

  @BeforeEach
  public void init() throws IOException {
    service = new CopyFileService();
    service.setSourceFolderPath("/Documents/ftproot");
    service.setSourceFileName("b.txt");
    service.setTargetFolderPath("/Documents/ftproot/Docs");
    service.setActionIfFileExists("rename");
    service.setCreateTargetFolder("false");
  }

  @DisplayName("Should Copy File")
  @Test
  void validTestCopyFile() throws Exception {
    String content = "";
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] ftpFiles = {file1, file2, file3};
    FTPFile[] files = {file1};
    file1.setType(0);
    Mockito.when(srcClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(destClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(file.isFile()).thenReturn(true);
    InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    OutputStream os = new ByteArrayOutputStream();
    Mockito.when(srcClient.makeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(srcClient.retrieveFileStream(any(String.class))).thenReturn(is);
    Mockito.when(destClient.storeFileStream(any(String.class))).thenReturn(os);
    Mockito.when(srcClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(destClient.changeWorkingDirectory(any(String.class)))
        .thenReturn(true, false, true);
    Mockito.when(srcClient.completePendingCommand()).thenReturn(true);
    Mockito.when(destClient.completePendingCommand()).thenReturn(true);
    Mockito.when(srcClient.isConnected()).thenReturn(true);
    Mockito.when(destClient.isConnected()).thenReturn(true);
    Response result = service.invoke(srcClient, destClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(srcClient, Mockito.times(1)).logout();
    Mockito.verify(srcClient, Mockito.times(1)).disconnect();
    Mockito.verify(destClient, Mockito.times(1)).logout();
    Mockito.verify(destClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File copied successfully.");
  }

  @DisplayName("Should throw an exception source file does not exists")
  @Test
  void invalidTestCopyFile() throws Exception {
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] files = {file1, file2, file3};
    Mockito.when(srcClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(destClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(srcClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(destClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(file.isFile()).thenReturn(false);
    assertThatThrownBy(() -> service.invoke(srcClient, destClient))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Source File doesn't exist!!");
  }

  @DisplayName("Should rename file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWithRenameWhenSourceFileIsPresentInTarget() throws Exception {
    String content = "";
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] ftpFiles = {file1, file2, file3};
    FTPFile[] files = {file1};
    file1.setType(0);
    Mockito.when(srcClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(destClient.listFiles(any(String.class))).thenReturn(files, ftpFiles);
    Mockito.when(file.isFile()).thenReturn(true);
    InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    OutputStream os = new ByteArrayOutputStream();
    Mockito.when(srcClient.makeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(srcClient.retrieveFileStream(any(String.class))).thenReturn(is);
    Mockito.when(destClient.storeFileStream(any(String.class))).thenReturn(os);
    Mockito.when(srcClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(destClient.changeWorkingDirectory(any(String.class)))
        .thenReturn(true, false, true, true);
    Mockito.when(srcClient.completePendingCommand()).thenReturn(true);
    Mockito.when(destClient.completePendingCommand()).thenReturn(true);
    Mockito.when(srcClient.isConnected()).thenReturn(true);
    Mockito.when(destClient.isConnected()).thenReturn(true);
    Response result = service.invoke(srcClient, destClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(srcClient, Mockito.times(1)).logout();
    Mockito.verify(srcClient, Mockito.times(1)).disconnect();
    Mockito.verify(destClient, Mockito.times(1)).logout();
    Mockito.verify(destClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File copied successfully.");
  }

  @DisplayName("Should replace file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWhenSourceFileIsPresentInTargetAndReplaceFile() throws Exception {
    service.setActionIfFileExists("replace");
    String content = "";
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] files = {file1};
    file1.setType(0);
    Mockito.when(srcClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(destClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(file.isFile()).thenReturn(true);
    InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    OutputStream os = new ByteArrayOutputStream();
    Mockito.when(srcClient.makeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(srcClient.retrieveFileStream(any(String.class))).thenReturn(is);
    Mockito.when(destClient.storeFileStream(any(String.class))).thenReturn(os);
    Mockito.when(srcClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(destClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(srcClient.completePendingCommand()).thenReturn(true);
    Mockito.when(destClient.completePendingCommand()).thenReturn(true);
    Mockito.when(srcClient.isConnected()).thenReturn(true);
    Mockito.when(destClient.isConnected()).thenReturn(true);
    Response result = service.invoke(srcClient, destClient);
    Mockito.verify(srcClient, Mockito.times(1)).logout();
    Mockito.verify(srcClient, Mockito.times(1)).disconnect();
    Mockito.verify(destClient, Mockito.times(1)).logout();
    Mockito.verify(destClient, Mockito.times(1)).disconnect();
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File copied successfully.");
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
    Mockito.when(srcClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(destClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(file.isFile()).thenReturn(true);
    Mockito.when(srcClient.makeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(srcClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(destClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(srcClient.isConnected()).thenReturn(true);
    Mockito.when(destClient.isConnected()).thenReturn(true);
    Response result = service.invoke(srcClient, destClient);
    Mockito.verify(srcClient, Mockito.times(1)).logout();
    Mockito.verify(srcClient, Mockito.times(1)).disconnect();
    Mockito.verify(destClient, Mockito.times(1)).logout();
    Mockito.verify(destClient, Mockito.times(1)).disconnect();
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Operation skipped!!");
  }
}
