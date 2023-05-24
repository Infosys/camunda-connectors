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
import static org.mockito.Mockito.doNothing;

import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import com.infosys.camundaconnectors.file.sftp.utility.SftpServerClient;
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
class DeleteFileTest {

  @Mock private SFTPClient sftpClient;
  @Mock private SftpServerClient sftpServerClient;
  @Mock private DeleteFileService service;

  @BeforeEach
  public void init() {
    service = new DeleteFileService();
    service.setFolderPath("C:/User/curation.bot/Documents");
    service.setFileName("Demo.txt");
  }

  @DisplayName("Should Delete given file")
  @Test
  void validTestDeleteFile() throws Exception {
    doNothing().when(sftpClient).rm(isA(String.class));
    sftpClient.rm("");

    Response result = service.invoke(sftpClient);

    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("deleted successfully");
  }

  @DisplayName("Should Throw an error")
  @Test
  void invalidTestDeleteFile() throws Exception {
    service.setFolderPath(null);
    doNothing().when(sftpClient).rm(isA(String.class));
    sftpClient.rm("");
    assertThatThrownBy(() -> service.invoke(sftpClient))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Please provide a valid folderPath");
  }
}
