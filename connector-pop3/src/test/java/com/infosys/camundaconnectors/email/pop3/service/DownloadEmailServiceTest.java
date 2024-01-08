/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.email.pop3.model.response.POP3Response;
import com.infosys.camundaconnectors.email.pop3.model.response.Response;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.search.SearchTerm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DownloadEmailServiceTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private Message message;
  @Mock private DownloadEmailService service;

  @BeforeEach
  public void init() {
    service = new DownloadEmailService();
    service.setMessageId("messageID-345");
    service.setDownloadFolderPath("D:/Dump");
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
