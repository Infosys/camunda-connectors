/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.service.folders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;

import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;
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
class CreateFolderTest {
  @Mock private SFTPClient sftpClient;
  @Mock private CreateFolderService service;
  @Mock private FileAttributes attrs;

  @BeforeEach
  public void init() {
    service = new CreateFolderService();
    service.setFolderPath("C:/User/curation.bot/Documents");
    service.setNewFolderName("Demo");
    service.setActionIfFileExists("rename");
  }

  @DisplayName("Should create given folder")
  @Test
  void validTestCreateFolder() throws Exception {
    doNothing().when(sftpClient).mkdir(isA(String.class));
    Response result = service.invoke(sftpClient);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("created successfully");
  }

  @DisplayName("Should throw an exception when given output type is null")
  @Test
  void invalidTestCreateFolder() throws Exception {
    service.setActionIfFileExists("");
    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(attrs);
    doNothing().when(sftpClient).mkdir(isA(String.class));
    assertThatThrownBy(() -> service.invoke(sftpClient))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Incorrect actionIfFileExists. It should be rename,replace or skip");
  }

  @DisplayName("Should replace given folder, if given folder is already exists")
  @Test
  void validTestCreateFolderReplace() throws Exception {
    service.setActionIfFileExists("replace");
    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(attrs);
    doNothing().when(sftpClient).mkdir(isA(String.class));
    Response result = service.invoke(sftpClient);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Folder created successfully");
  }

  @DisplayName("Should rename given folder, if given folder is already exists")
  @Test
  void validTestCreateFolderRename() throws Exception {
    service.setActionIfFileExists("rename");
    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(attrs, null);
    doNothing().when(sftpClient).mkdir(isA(String.class));
    Response result = service.invoke(sftpClient);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Folder is created with name");
  }

  @DisplayName("Should skip this operation")
  @Test
  void validTestCreateFolderSkip() throws Exception {
    service.setActionIfFileExists("skip");
    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(attrs);
    doNothing().when(sftpClient).mkdir(isA(String.class));
    Response result = service.invoke(sftpClient);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Folder skipped successfully");
  }
}
