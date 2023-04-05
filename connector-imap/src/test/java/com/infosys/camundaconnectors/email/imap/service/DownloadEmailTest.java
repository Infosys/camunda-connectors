/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import javax.mail.*;
import javax.mail.search.SearchTerm;
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

  @DisplayName("Should download email")
  @Test
  void shouldDownloadEmailWhenExecute() throws MessagingException {
    // given
    when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {message});
    // When
    Response result = service.invoke(store, folder);
    // Then
    @SuppressWarnings("unchecked")
    IMAPResponse<String> queryResponse = (IMAPResponse<String>) result;
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("downloaded successfully");
  }

  @DisplayName("Should throw error - messageId not matched")
  @Test
  void shouldThrowErrorMessageIdNotMatched() throws MessagingException {
    // given
    when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {});
    // When
    assertThatThrownBy(() -> service.invoke(store, folder))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No email found in the mailbox matching given message Id");
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error - invalid messageId")
  @Test
  void shouldThrowErrorInvalidMessageId() throws MessagingException {
    // given
    service.setMessageId(null);
    // When
    assertThatThrownBy(() -> service.invoke(store, folder))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Please provide a valid message ID");
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
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
