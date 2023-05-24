/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;
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
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.model.response.Response;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListFoldersServiceTest {
  @Mock private FTPClient ftpClient;
  @Mock private ListFoldersService service;

  @BeforeEach
  public void init() {
    service = new ListFoldersService();
    service.setFolderPath("C:/User/curation.bot/Documents");
    service.setModifiedAfter("31/03/2023");
    service.setModifiedBefore("04/04/2023");
    service.setOutputType("folderPaths");
    service.setMaxDepth("5");
    service.setMaxNumberOfFolders("100");
    service.setSearchSubFolders("false");
    service.setSortBy("name");
  }

  @DisplayName("Should return folder names when modified before and modified after is given")
  @Test
  void validTestListFilesWithModifiedBeforeAndModifiedAfter() throws Exception {
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023,3,2,6,19,34);
    FTPFile file1 = new FTPFile();
	file1.setName("folderA");
	file1.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file2 = new FTPFile();
	file2.setName("folderB");
	file2.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file3 = new FTPFile();
    file3.setName("folderC");
    file3.setTimestamp(date);
    file1.setType(1);
    file2.setType(1);
    file3.setType(1);
    FTPFile[] ftpFiles = {file1,file2,file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(ftpClient.rename(any(String.class),any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName(
      "Should return file names when modified before and modified after is given and one directory is given")
  @Test
  void validTestListFilesWithModifiedBeforeAndModifiedAfterWithdirectory() throws Exception {
    service.setOutputType("folderPaths");
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023,3,2,6,19,34);
    FTPFile file1 = new FTPFile();
	file1.setName("folderA");
	file1.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file2 = new FTPFile();
	file2.setName("folderB");
	file2.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file3 = new FTPFile();
    file3.setName("folderC");
    file3.setTimestamp(date);
    file1.setType(1);
    file2.setType(1);
    file3.setType(1);
    FTPFile[] ftpFiles = {file1,file2,file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(ftpClient.rename(any(String.class),any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should return filePath in ascending order of there file name")
  @Test
  void validTestListFilesInAscendingOrder() throws Exception {
    service.setSortBy("name");
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023,3,2,6,19,34);
    FTPFile file1 = new FTPFile();
	file1.setName("folderA");
	file1.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file2 = new FTPFile();
	file2.setName("folderB");
	file2.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file3 = new FTPFile();
    file3.setName("folderC");
    file3.setTimestamp(date);
    file1.setType(1);
    file2.setType(1);
    file3.setType(1);
    FTPFile[] ftpFiles = {file1,file2,file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(ftpClient.rename(any(String.class),any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should return filePath in ascending order of there modified time")
  @Test
  void validTestListFilesInAscendingOrderOfModifiedTime() throws Exception {
	service.setSortBy("modifiedDate");
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023,3,2,6,19,34);
    FTPFile file1 = new FTPFile();
	file1.setName("folderA");
	file1.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file2 = new FTPFile();
	file2.setName("folderB");
	file2.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file3 = new FTPFile();
    file3.setName("folderC");
    file3.setTimestamp(date);
    file1.setType(1);
    file2.setType(1);
    file3.setType(1);
    FTPFile[] ftpFiles = {file1,file2,file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(ftpClient.rename(any(String.class),any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should return file names when only modified before is given")
  @Test
  void validTestListFilesWithModifiedBefore() throws Exception {
    service.setModifiedAfter(null);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023,3,2,6,19,34);
    FTPFile file1 = new FTPFile();
	file1.setName("folderA");
	file1.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file2 = new FTPFile();
	file2.setName("folderB");
	file2.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file3 = new FTPFile();
    file3.setName("folderC");
    file3.setTimestamp(date);
    file1.setType(1);
    file2.setType(1);
    file3.setType(1);
    FTPFile[] ftpFiles = {file1,file2,file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(ftpClient.rename(any(String.class),any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @DisplayName("Should return file names when only modified After is given")
  @Test
  void validTestListFilesWithModifiedAfter() throws Exception {
    service.setModifiedBefore(null);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    Calendar date = Calendar.getInstance();
    date.set(2023,3,2,6,19,34);
    FTPFile file1 = new FTPFile();
	file1.setName("folderA");
	file1.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file2 = new FTPFile();
	file2.setName("folderB");
	file2.setTimestamp(date);
	date.set(2023, 3, 3, 6, 19, 34);
	FTPFile file3 = new FTPFile();
    file3.setName("folderC");
    file3.setTimestamp(date);
    file1.setType(1);
    file2.setType(1);
    file3.setType(1);
    FTPFile[] ftpFiles = {file1,file2,file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
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