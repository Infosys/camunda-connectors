/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import javax.mail.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DownloadEmailTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private Message message;
  @Mock private DownloadEmailService service;

  @BeforeEach
  public void init() {
    service = new DownloadEmailService();
    service.setMessageId("messageID-345");
    service.setDownloadFolderPath("D:/Email");
  }

  @DisplayName("Should throw error - invalid downloadPath")
  @Test
  void shouldThrowErrorInvalidDownloadPath() throws MessagingException {
    // given
    service.setDownloadFolderPath("");
    // When
    assertThatThrownBy(() -> service.invoke(store, folder))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Is not a folder path:");
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
  }
}
