/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.smtp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.email.smtp.model.SMTPResponse;
import com.infosys.camundaconnectors.email.smtp.model.request.Authentication;
import com.infosys.camundaconnectors.email.smtp.model.request.SMTPRequest;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class SendMailServiceTest extends BaseTest {
  @Mock private Session sessionMock;
  @Mock private Properties properties;
  @Mock private Transport transport;
  @Mock private SMTPSession smtpSessionMock;
  private SendMailService service;

  @BeforeEach
  public void init() throws NoSuchProviderException {
    service = new SendMailService();
    when(sessionMock.getProperties()).thenReturn(properties);
    when(sessionMock.getTransport(anyString())).thenReturn(transport);
  }

  @DisplayName("Should send mail form request")
  @ParameterizedTest(name = "#{index} - {0}")
  @MethodSource("executeSendMailTestCases")
  void shouldSendMailWhenExecuted(String input) throws NoSuchProviderException {
    // given
    when(smtpSessionMock.getSession(any(Authentication.class))).thenReturn(sessionMock);
    SMTPRequest request = gson.fromJson(input, SMTPRequest.class);
    SMTPResponse smtpResponse = service.execute(sessionMock, request);
    Mockito.verify(sessionMock, Mockito.times(1)).getProperties();
    Mockito.verify(sessionMock, Mockito.times(1)).getTransport(anyString());
    assertThat(smtpResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Mail sent successfully");
  }

  @DisplayName("Should throw error for send mail form request")
  @ParameterizedTest(name = "#{index} - {0}")
  @MethodSource("executeInvalidSendMailTestCases")
  void execute_shouldThrowErrorForSendMail(String input) {
    // Given
    when(smtpSessionMock.getSession(any(Authentication.class))).thenReturn(sessionMock);
    SMTPRequest request = gson.fromJson(input, SMTPRequest.class);
    assertThatThrownBy(() -> service.execute(sessionMock, request))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotBlank();
    Mockito.verify(sessionMock, Mockito.times(1)).getProperties();
  }
}
