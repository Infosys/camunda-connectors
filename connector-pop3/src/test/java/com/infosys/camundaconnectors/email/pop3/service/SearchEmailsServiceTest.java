/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.infosys.camundaconnectors.email.pop3.model.response.POP3Response;
import com.infosys.camundaconnectors.email.pop3.model.response.Response;
import java.util.Map;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchEmailsServiceTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private Message message;
  @Mock private SearchEmailsService service;

  @BeforeEach
  public void init() {
    service = new SearchEmailsService();
    service.setSearchField("subject");
    service.setSearchContent("Archive");
    service.setSearchType(null);
    service.setOutputType(null);
  }

  @DisplayName("Should search for email but no email should match")
  @Test
  void validTestSearchEmailZeroMatch() throws Exception {
    // given
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {});
    // When
    Response result = service.invoke(store, folder);
    // Then
    @SuppressWarnings("unchecked")
    POP3Response<Map<String, Object>> queryResponse = (POP3Response<Map<String, Object>>) result;
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isEmpty();
  }

  @DisplayName("Should search for email message id")
  @Test
  void validTestSearchEmailForMessageId() throws Exception {
    // given
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"OP-90"});
    // When
    Response result = service.invoke(store, folder);
    // Then
    @SuppressWarnings("unchecked")
    POP3Response<Map<String, Object>> queryResponse = (POP3Response<Map<String, Object>>) result;
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @DisplayName("Should search for email headerDetails")
  @Test
  void validTestSearchEmailForHeaderDetails() throws Exception {
    service.setOutputType("headerDetails");
    // given
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"OP-90"});
    // When
    Response result = service.invoke(store, folder);
    // Then
    @SuppressWarnings("unchecked")
    POP3Response<Map<String, Object>> queryResponse = (POP3Response<Map<String, Object>>) result;
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  // Invalid Search Field
  @DisplayName("Should throw error for invalid search field")
  @Test
  void shouldThrowErrorInvalidSearchField() throws MessagingException {
    // given
    service.setSearchField("field");
    // When
    assertThatThrownBy(() -> service.invoke(store, folder))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "searchField should be one of the following: subject, sender, from, "
                + "body, content, attachment, or all (to search in subject/from/body)");
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
  }
  // Invalid OutputType

}
