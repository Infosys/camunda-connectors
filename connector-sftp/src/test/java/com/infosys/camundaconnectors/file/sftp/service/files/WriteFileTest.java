/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.service.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;

import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import java.util.EnumSet;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPEngine;
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
class WriteFileTest {

  @Mock private SFTPClient sftpClient;
  @Mock private WriteFileService service;
  @Mock private FileAttributes attrs;
  @Mock private SFTPEngine sftpEngine;
  @Mock private RemoteFile file;

  @BeforeEach
  public void init() {
    service = new WriteFileService();
    service.setFilePath("C:\\user\\Documents\\Demo1\\Example.txt");
    service.setContent("Hello From Test file");
  }

  @DisplayName("Should write file")
  @Test
  void validTestWriteFile() throws Exception {
    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(attrs);
    Mockito.when(sftpClient.getSFTPEngine()).thenReturn(sftpEngine);
    Mockito.when(sftpClient.getSFTPEngine().open(any(String.class), any(EnumSet.class)))
        .thenReturn(file);
    Response result = service.invoke(sftpClient);
    // Then
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    Mockito.verify(sftpClient, Mockito.times(2)).stat(any(String.class));
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File written successfully");
  }

  @DisplayName("Should throw an exception source file does not exists")
  @Test
  void invalidTestMoveFile() throws Exception {

    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(null, null, attrs);
    Mockito.when(sftpClient.getSFTPEngine()).thenReturn(sftpEngine);
    Mockito.when(sftpClient.getSFTPEngine().open(any(String.class), any(EnumSet.class)))
        .thenReturn(file);
    assertThatThrownBy(() -> service.invoke(sftpClient))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Source file does not exists");

    Mockito.verify(sftpClient, Mockito.times(1)).close();
  }
}
