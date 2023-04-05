/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import java.util.List;
import java.util.Map;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.search.MessageIDTerm;
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
class MoveEmailTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private Message message;
  @Mock private MoveEmailService service;

  @BeforeEach
  public void init() {
    service = new MoveEmailService();
    service.setMessageId("12-UI-78");
    service.setTargetFolderPath("Archive");
    service.setCreateTargetFolder(true);
  }

  @DisplayName("Should move email")
  @Test
  void validTestMoveEmail() throws MessagingException {
    // given
    MoveEmailService moveEmailService = Mockito.spy(service);
    Mockito.when(store.getDefaultFolder()).thenReturn(folder);
    Mockito.doReturn(folder)
        .when(moveEmailService)
        .setTargetFolder(any(Store.class), anyString(), anyBoolean());
    Mockito.when(folder.search(any(MessageIDTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(folder.getFolder(anyString())).thenReturn(folder);
    Mockito.when(folder.exists()).thenReturn(true);
    // When
    Response result = service.invoke(store, folder);
    // Then
    @SuppressWarnings("unchecked")
    IMAPResponse<List<Map<String, Object>>> queryResponse =
        (IMAPResponse<List<Map<String, Object>>>) result;
    Mockito.verify(folder, Mockito.times(1)).search(any(MessageIDTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .isEqualTo("Email moved successfully to folder: Archive");
  }

  @DisplayName("Should throw error for message ids")
  @Test
  void shouldThrowErrorInvalidMessageIds() throws MessagingException {
    // given
    Mockito.when(folder.search(any(MessageIDTerm.class))).thenReturn(null);
    // When
    assertThatThrownBy(() -> service.invoke(store, folder))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No matching message found for given message ID in the folder");
    Mockito.verify(folder, Mockito.times(1)).search(any(MessageIDTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error for invalid destination folder")
  @Test
  void shouldThrowErrorInvalidDestFolder() throws MessagingException {
    // given
    Mockito.when(folder.search(any(MessageIDTerm.class))).thenReturn(new Message[] {message});
    MoveEmailService moveEmailService = Mockito.spy(service);
    Mockito.when(store.getDefaultFolder()).thenReturn(folder);
    Mockito.doReturn(null)
        .when(moveEmailService)
        .setTargetFolder(any(Store.class), anyString(), anyBoolean());
    Mockito.when(folder.getFolder(anyString())).thenReturn(folder);
    Mockito.when(folder.exists()).thenReturn(false);
    // When
    assertThatThrownBy(() -> service.invoke(store, folder))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "FolderNotFound: The given folder path Archive doesn't exists and creation failed");
    Mockito.verify(folder, Mockito.times(1)).search(any(MessageIDTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
  }
}
