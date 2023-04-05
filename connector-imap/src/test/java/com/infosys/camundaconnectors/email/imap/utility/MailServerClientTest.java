/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.email.imap.model.request.Authentication;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailServerClientTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private Authentication authentication;
  @Mock private MailServerClient mailServerClient;

  @DisplayName("Should return Folder object")
  @Test
  void shouldReturnFolder() throws MessagingException {
    // When
    when(authentication.getDomainName()).thenReturn("localhost.com");
    when(authentication.getUsername()).thenReturn("username");
    when(authentication.getPortNumber()).thenReturn("995");
    when(authentication.getFolderPath()).thenReturn(anyString());
    when(mailServerClient.getRecursiveFolder(store, anyString())).thenReturn(folder);
    when(mailServerClient.getFolder(store, authentication)).thenReturn(folder);
    // Then
    assertThat(mailServerClient.getFolder(store, authentication)).isInstanceOf(Folder.class);
  }

  @DisplayName("Should return Store object")
  @Test
  void shouldReturnStore() throws MessagingException {
    // When
    when(authentication.getDomainName()).thenReturn("localhost.com");
    when(authentication.getUsername()).thenReturn("username");
    when(authentication.getPortNumber()).thenReturn("995");
    when(authentication.getFolderPath()).thenReturn(anyString());
    when(mailServerClient.getRecursiveFolder(store, anyString())).thenReturn(folder);
    when(mailServerClient.getStore(authentication)).thenReturn(store);
    // Then
    assertThat(mailServerClient.getStore(authentication)).isInstanceOf(Store.class);
  }
}
