/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.email.pop3.model.request.Authentication;
import com.infosys.camundaconnectors.email.pop3.model.response.POP3Response;
import com.infosys.camundaconnectors.email.pop3.model.response.Response;
import com.infosys.camundaconnectors.email.pop3.utility.MailServerClient;
import com.infosys.camundaconnectors.email.pop3.utility.SearchCondition;
import java.util.List;
import java.util.Map;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class POP3FunctionTest extends BaseTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private Message message;
  @Mock private SearchTerm searchTerm;
  @Mock private MailServerClient mailServerClient;
  @Mock private SearchCondition searchCondition;
  private POP3Function pop3Function;

  @BeforeEach
  void init() throws Exception {
    //		mimeMessageParserFunction.apply(mimeMessage);
    pop3Function = new POP3Function(gson, mailServerClient);
    Mockito.when(mailServerClient.getStore(any(Authentication.class))).thenReturn(store);
    Mockito.when(mailServerClient.getFolder(any(Store.class), any(Authentication.class)))
        .thenReturn(folder);
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(folder.search(any(MessageIDTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(store.getDefaultFolder()).thenReturn(folder);
    Mockito.when(folder.getFolder(anyString())).thenReturn(folder);
    Mockito.when(folder.exists()).thenReturn(true);
  }

  @ParameterizedTest
  @MethodSource("executeDeleteEmailTestCases")
  void execute_shouldDeleteEmail(String input) throws Exception {
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {message});
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    Object executeResponse = pop3Function.execute(context);
    // Then
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThatItsValid(executeResponse, "deleted successfully");
  }

  @ParameterizedTest
  @MethodSource("executeInvalidDeleteEmailTestCases")
  void execute_shouldThrowErrorForDeleteEmail(String input) throws MessagingException {
    when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {});
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    assertThatThrownBy(() -> pop3Function.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .matches(
            "(.*(\\r\\n|\\r|\\n).*must not be blank)|"
                + "(No email found in the mailbox matching given message Id)");
  }

  @ParameterizedTest
  @MethodSource("executeDownloadEmailTestCases")
  void execute_shouldDownloadEmail(String input) throws Exception {
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    Object executeResponse = pop3Function.execute(context);
    // Then
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThatItsValid(executeResponse, "downloaded successfully");
  }

  @ParameterizedTest
  @MethodSource("executeInvalidDownloadEmailTestCases")
  void execute_shouldThrowErrorForDownloadEmail(String input) throws MessagingException {
    when(folder.search(any(MessageIDTerm.class))).thenReturn(null);
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    assertThatThrownBy(() -> pop3Function.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotBlank();
  }

  @ParameterizedTest
  @MethodSource("executeListEmailsTestCases")
  void execute_shouldListEmails(String input) throws Exception {
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"12u-YI-K"});
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    Object executeResponse = pop3Function.execute(context);
    // Then
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    @SuppressWarnings("unchecked")
    POP3Response<List<Map<String, Object>>> queryResponse =
        (POP3Response<List<Map<String, Object>>>) executeResponse;
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeInvalidListEmailsTestCases")
  void execute_shouldThrowErrorInvalidListEmails(String input) throws MessagingException {
    Mockito.when(searchCondition.convertToSearchTerm(anyMap())).thenReturn(searchTerm);
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"12u-YI-K"});
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    assertThatThrownBy(() -> pop3Function.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotBlank();
  }

  @ParameterizedTest
  @MethodSource("executeSearchEmailsTestCases")
  void execute_shouldSearchEmails(String input) throws Exception {
    // Given
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"12u-YI-K"});
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    Object executeResponse = pop3Function.execute(context);
    // Then
    @SuppressWarnings("unchecked")
    POP3Response<Map<String, Object>> queryResponse =
        (POP3Response<Map<String, Object>>) executeResponse;
    Mockito.verify(folder, Mockito.times(1)).search(any(SearchTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeInvalidSearchEmailsTestCases")
  void execute_shouldThrowErrorInvalidSearchEmailsInputs(String input) {
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    assertThatThrownBy(() -> pop3Function.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  private void assertThatItsValid(Object executeResponse, String validateAgainst) {
    assertThat(executeResponse).isInstanceOf(Response.class);
    @SuppressWarnings("unchecked")
    POP3Response<String> response = (POP3Response<String>) executeResponse;
    assertThat(response.getResponse()).isNotNull();
    assertThat(response)
        .extracting("response")
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .contains(validateAgainst);
  }
}
