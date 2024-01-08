/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;

import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.model.response.Response;
import java.util.Calendar;
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
class ListFilesServiceTest {

  @Mock private FTPClient ftpClient;
  @Mock private ListFilesService service;
  @Mock FTPFile file;
  @Mock Calendar cal;

  @BeforeEach
  public void init() {
    service = new ListFilesService();
    service.setFolderPath("C:/Users/curation.bot/Documents");
    service.setModifiedAfter("30/03/2023");
    service.setModifiedBefore("04/04/2023");
    service.setOutputType("filePaths");
    service.setSearchSubFolders("false");
    service.setSortBy("name");
  }

  @DisplayName("Should return file names when modified before and modified after is given")
  @Test
  void validTestListFilesWithModifiedBeforeAndModifiedAfter() throws Exception {
    service.setSearchSubFolders("true");
    service.setMaxDepth("90");
    service.setOutputType("fileDetails");
    service.setSortBy("name");
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023, 3, 2, 6, 19, 34);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    file1.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    file2.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    file3.setTimestamp(date);
    FTPFile[] ftpFiles = {file1, file2, file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName(
      "Should return file names when modified before and modified after is given and one directory is given")
  @Test
  void validTestListFilesWithModifiedBeforeAndModifiedAfterWithdirectory() throws Exception {
    service.setSearchSubFolders("true");
    service.setMaxDepth("90");
    service.setOutputType("fileDetails");
    service.setSortBy("name");
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023, 3, 2, 6, 19, 34);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    file1.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    file2.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    file3.setTimestamp(date);
    FTPFile[] ftpFiles = {file1, file2, file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should return filePath in ascending order of there file name")
  @Test
  void validTestListFilesInAscendingOrder() throws Exception {
    service.setSortBy("name");
    service.setMaxDepth("90");
    service.setModifiedBefore(null);
    service.setModifiedAfter(null);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023, 3, 2, 6, 19, 34);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    file1.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    file2.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    file3.setTimestamp(date);
    file1.setType(0);
    file2.setType(0);
    file3.setType(0);
    FTPFile[] ftpFiles = {file1, file2, file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should return filePath in ascending order of there modified time")
  @Test
  void validTestListFilesInAscendingOrderOfModifiedTime() throws Exception {
    service.setSortBy("modifiedDate");
    service.setMaxDepth("90");
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023, 3, 2, 6, 19, 34);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    file1.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    file2.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    file3.setTimestamp(date);
    file1.setType(0);
    file2.setType(0);
    file3.setType(0);
    FTPFile[] ftpFiles = {file1, file2, file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should return filePath in ascending order of their size")
  @Test
  void validTestListFilesInAscendingOrderOfAccessedTime() throws Exception {
    service.setSortBy("size");
    service.setMaxDepth("90");
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023, 3, 2, 6, 19, 34);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    file1.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    file2.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    file3.setTimestamp(date);
    file1.setType(0);
    file2.setType(0);
    file3.setType(0);
    FTPFile[] ftpFiles = {file1, file2, file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should return file names when only modified before is given")
  @Test
  void validTestListFilesWithModifiedBefore() throws Exception {
    service.setModifiedAfter(null);
    service.setMaxDepth("90");
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023, 3, 2, 6, 19, 34);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    file1.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    file2.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    file3.setTimestamp(date);
    file1.setType(0);
    file2.setType(0);
    file3.setType(0);
    FTPFile[] ftpFiles = {file1, file2, file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should return file names when only modified After is given")
  @Test
  void validTestListFilesWithModifiedAfter() throws Exception {
    service.setModifiedBefore(null);
    service.setMaxDepth("90");
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023, 3, 2, 6, 19, 34);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    file1.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    file2.setTimestamp(date);
    date.set(2023, 3, 3, 6, 19, 34);
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    file3.setTimestamp(date);
    file1.setType(0);
    file2.setType(0);
    file3.setType(0);
    FTPFile[] ftpFiles = {file1, file2, file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }
}
