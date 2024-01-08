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

import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.model.response.Response;
import com.infosys.camundaconnectors.files.ftp.utility.FTPServerClient;
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
class DeleteFileServiceTest {

  @Mock private FTPClient ftpClient;
  @Mock private FTPServerClient ftpServerClient;
  private DeleteFileService service;

  @BeforeEach
  public void init() {
    service = new DeleteFileService();
    service.setFolderPath("/Documents/ftproot");
    service.setFileName("a.txt");
  }

  @DisplayName("Should Delete given file")
  @Test
  void validTestDeleteFile() throws Exception {
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] ftpFiles = {file1};
    file1.setType(0);
    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(ftpFiles);
    Mockito.when(ftpClient.isConnected()).thenReturn(true);
    Response result = service.invoke(ftpClient);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
    Mockito.verify(ftpClient, Mockito.times(1)).logout();
    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();

    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File Deleted Successfully");
  }

  @DisplayName("Should Throw an error")
  @Test
  void invalidTestDeleteFile() throws Exception {
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(false);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(false);
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
