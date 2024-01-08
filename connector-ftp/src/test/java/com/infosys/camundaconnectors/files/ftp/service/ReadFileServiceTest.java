/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.model.response.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class ReadFileServiceTest {

  private ReadFileService service;
  @Mock FTPFile file;
  @Mock FTPClient ftpClient;
  InputStream in;

  @BeforeEach
  public void init() throws IOException {
    service = new ReadFileService();
    service.setFolderPath("C:/Users/xyz/Documents/ftproot");
    service.setFileName("a.txt");
  }

  @DisplayName("Should read file content")
  @Test
  void validTestReadFile() throws Exception {
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    in = new ByteArrayInputStream("test data".getBytes());
    Mockito.when(ftpClient.retrieveFileStream(any(String.class))).thenReturn(in);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] ftpFiles = {file1};
    file1.setType(0);
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(ftpClient.completePendingCommand()).thenReturn(true);
    Mockito.when(file.isFile()).thenReturn(true);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    assertThat(queryResponse).extracting("response").isInstanceOf(String.class).isNotNull();
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
  }

  @DisplayName("Should throw an exception when Source path is invalid")
  @Test
  void invalidTestReadFile() throws Exception {
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    in = new ByteArrayInputStream("test data".getBytes());
    Mockito.when(ftpClient.retrieveFileStream(any(String.class))).thenReturn(in);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] ftpFiles = {file1, file2, file3};
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    assertThatThrownBy(() -> service.invoke(ftpClient))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error");
  }
}
