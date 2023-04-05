/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.email.imap.model.request.Authentication;
import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import com.infosys.camundaconnectors.email.imap.service.MoveEmailService;
import com.infosys.camundaconnectors.email.imap.utility.MailServerClient;
import com.infosys.camundaconnectors.email.imap.utility.SearchCondition;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IMAPFunctionTest extends BaseTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private Message message;
  @Mock private SearchTerm searchTerm;
  @Mock private MailServerClient mailServerClient;
  @Mock private SearchCondition searchCondition;
  @InjectMocks @Spy private MoveEmailService moveEmailService;
  private IMAPFunction imapFunction;

  @BeforeEach
  void init() throws Exception {
    //		mimeMessageParserFunction.apply(mimeMessage);
    imapFunction = new IMAPFunction(gson, mailServerClient);
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
    Object executeResponse = imapFunction.execute(context);
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
    assertThatThrownBy(() -> imapFunction.execute(context))
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
    Object executeResponse = imapFunction.execute(context);
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
    assertThatThrownBy(() -> imapFunction.execute(context))
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
    Object executeResponse = imapFunction.execute(context);
    // Then
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    @SuppressWarnings("unchecked")
    IMAPResponse<List<Map<String, Object>>> queryResponse =
        (IMAPResponse<List<Map<String, Object>>>) executeResponse;
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeInvalidListEmailsTestCases")
  void execute_shouldThrowErrorInvalidListEmails(String input) throws MessagingException {
    Mockito.when(searchCondition.convertToSearchTerm(anyMap())).thenReturn(searchTerm);
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"12u-YI-K"});
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    assertThatThrownBy(() -> imapFunction.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotBlank();
  }

  @ParameterizedTest
  @MethodSource("executeMoveEmailTestCases")
  void execute_shouldMoveEmail(String input) throws Exception {
    Mockito.when(store.getDefaultFolder()).thenReturn(folder);
    Mockito.doReturn(folder)
        .when(moveEmailService)
        .setTargetFolder(any(Store.class), anyString(), anyBoolean());
    Mockito.when(folder.getFolder(anyString())).thenReturn(folder);
    Mockito.when(folder.exists()).thenReturn(true);
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    Object executeResponse = imapFunction.execute(context);
    // Then
    Mockito.verify(folder, Mockito.times(1)).search(any(MessageIDTerm.class));
    Mockito.verify(folder, Mockito.times(1)).close();
    Mockito.verify(store, Mockito.times(1)).close();
    assertThatItsValid(executeResponse, "Email moved successfully to folder:");
  }

  @ParameterizedTest
  @MethodSource("executeInvalidMoveEmailTestCases")
  void execute_shouldThrowErrorInvalidMoveEmailInputs(String input) throws MessagingException {
    if (input.contains("jio-2"))
      Mockito.when(folder.search(any(MessageIDTerm.class))).thenReturn(null);
    Mockito.when(store.getDefaultFolder()).thenReturn(folder);
    Mockito.doReturn(null)
        .when(moveEmailService)
        .setTargetFolder(any(Store.class), anyString(), anyBoolean());
    Mockito.when(folder.getFolder(anyString())).thenReturn(folder);
    if (input.contains("Yuna")) Mockito.when(folder.exists()).thenReturn(false);
    else Mockito.when(folder.exists()).thenReturn(true);
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    assertThatThrownBy(() -> imapFunction.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeInvalidReadEmailTestCases")
  void execute_shouldThrowErrorInvalidReadEmailInputs(String input) throws Exception {
    Mockito.when(folder.search(any(MessageIDTerm.class))).thenReturn(new Message[] {});
    //		Mockito.when(mimeMessageParser.parse()).thenReturn(mimeMessageParser);
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    assertThatThrownBy(() -> imapFunction.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeSearchEmailsTestCases")
  void execute_shouldSearchEmails(String input) throws Exception {
    // Given
    Mockito.when(message.getHeader(anyString())).thenReturn(new String[] {"12u-YI-K"});
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    Object executeResponse = imapFunction.execute(context);
    // Then
    @SuppressWarnings("unchecked")
    IMAPResponse<Map<String, Object>> queryResponse =
        (IMAPResponse<Map<String, Object>>) executeResponse;
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
    assertThatThrownBy(() -> imapFunction.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  private void assertThatItsValid(Object executeResponse, String validateAgainst) {
    assertThat(executeResponse).isInstanceOf(Response.class);
    @SuppressWarnings("unchecked")
    IMAPResponse<String> response = (IMAPResponse<String>) executeResponse;
    assertThat(response.getResponse()).isNotNull();
    assertThat(response)
        .extracting("response")
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .contains(validateAgainst);
  }
}
