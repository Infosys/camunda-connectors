/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.service.folders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;

import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
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
class DeleteFolderTest {
  @Mock private SFTPClient sftpClient;
  @Mock private DeleteFolderService service;

  @BeforeEach
  public void init() {
    service = new DeleteFolderService();
    service.setFolderPath("C:/User/curation.bot/Documents");
  }

  @DisplayName("Should Delete given folder")
  @Test
  void validTestDeleteFolder() throws Exception {
    doNothing().when(sftpClient).rmdir(isA(String.class));
    sftpClient.rmdir("");
    Response result = service.invoke(sftpClient);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("deleted successfully");
  }

  @DisplayName("Should throw an exception")
  @Test
  void invalidTestDeleteFolder() throws Exception {
    service.setFolderPath("");
    assertThatThrownBy(() -> service.invoke(sftpClient))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
