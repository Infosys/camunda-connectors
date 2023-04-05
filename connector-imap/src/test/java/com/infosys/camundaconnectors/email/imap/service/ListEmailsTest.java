/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;

import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import com.infosys.camundaconnectors.email.imap.utility.SearchCondition;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.mail.*;
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
class ListEmailsTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private Message message;
  @Mock private SearchTerm searchTerm;
  @Mock private SearchCondition searchCondition;
  @Mock private ListEmailsService service;

  @BeforeEach
  public void init() {
    service = new ListEmailsService();
    service.setFilters(Collections.singletonMap("filter", "Subject Like 'Test'"));
    service.setSortBy(Map.of("sortOn", "ReceivedTime", "order", "asc"));
    service.setMaxResults(100);
    service.setOutputType("messageIds");
  }

  @DisplayName("Should return header details for matching emails")
  @Test
  void validTestListEmailsForHeaderDetails() throws MessagingException {
    // given
    Mockito.when(searchCondition.convertToSearchTerm(anyMap())).thenReturn(searchTerm);
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"12u-YI-K"});
    // When
    Response result = service.invoke(store, folder);
    // Then
    @SuppressWarnings("unchecked")
    IMAPResponse<List<Map<String, Object>>> queryResponse =
        (IMAPResponse<List<Map<String, Object>>>) result;
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @DisplayName("Should return message ids for matching emails")
  @Test
  void validTestListEmailsForMessageIds() throws MessagingException {
    // given
    service.setOutputType("headerDetails");
    Mockito.when(searchCondition.convertToSearchTerm(anyMap())).thenReturn(searchTerm);
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"12u-YI-K"});
    // When
    Response result = service.invoke(store, folder);
    // Then
    @SuppressWarnings("unchecked")
    IMAPResponse<List<String>> queryResponse = (IMAPResponse<List<String>>) result;
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @DisplayName("Should return message ids limit to maxResults")
  @Test
  void validTestListEmailsForMaxResult() throws MessagingException {
    service.setMaxResults(0);
    // given
    Mockito.when(searchCondition.convertToSearchTerm(anyMap())).thenReturn(searchTerm);
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {});
    // When
    Response result = service.invoke(store, folder);
    // Then
    @SuppressWarnings("unchecked")
    IMAPResponse<List<String>> queryResponse = (IMAPResponse<List<String>>) result;
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isEmpty();
  }

  @DisplayName("Should throw error for invalid output type")
  @Test
  void invalidTestListEmailsForOutputType() throws MessagingException {
    service.setOutputType("email");
    // given
    Mockito.when(searchCondition.convertToSearchTerm(anyMap())).thenReturn(searchTerm);
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"12u-YI-K"});
    // When
    assertThatThrownBy(() -> service.invoke(store, folder))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("outputType should be messageIds or headerDetails");
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
  }
}
