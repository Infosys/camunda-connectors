/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import java.util.Map;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.MessageIDTerm;
import org.apache.commons.mail.util.MimeMessageParser;
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
class ReadEmailTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private MimeMessage mimeMessage;
  @Mock private MimeMessageParser mimeMessageParser;
  @Mock private ReadEmailService service;

  @BeforeEach
  public void init() throws MessagingException {
    mimeMessage.setSubject("op");
    service = new ReadEmailService(mimeMessage -> mimeMessageParser);
    service.setMessageId("12-UI-78");
    service.setPostReadAction("Read");
    service.setReadLatestResponse(true);
    service.setOutputType(null);
  }

  @DisplayName("Should read email")
  @Test
  void validTestMoveEmail() throws Exception {
    // given
    Mockito.when(folder.search(any(MessageIDTerm.class))).thenReturn(new Message[] {mimeMessage});
    Mockito.when(mimeMessageParser.parse()).thenReturn(mimeMessageParser);
    // When
    Response result = service.invoke(store, folder);
    // Then
    @SuppressWarnings("unchecked")
    IMAPResponse<Map<String, Object>> queryResponse = (IMAPResponse<Map<String, Object>>) result;
    Mockito.verify(folder, Mockito.times(1)).search(any(MessageIDTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").asInstanceOf(MAP).isNotEmpty();
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

  @DisplayName("Should throw error for invalid output type")
  @Test
  void invalidTestListEmailsForOutputType() throws Exception {
    // given
    service.setOutputType("email");
    Mockito.when(folder.search(any(MessageIDTerm.class))).thenReturn(new Message[] {mimeMessage});
    Mockito.when(mimeMessageParser.parse()).thenReturn(mimeMessageParser);
    // When
    assertThatThrownBy(() -> service.invoke(store, folder))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("outputType should be header or detailedHeader");
    Mockito.verify(folder, Mockito.times(1)).search(any(MessageIDTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
  }
}
