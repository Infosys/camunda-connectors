/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.smtp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.email.smtp.model.SMTPResponse;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SMTPFunctionTest extends BaseTest {
  private static final String STATUS = "Mail sent successfully";
  @Mock private Session sessionMock;
  @Mock private Properties properties;
  @Mock private Transport transport;
  @Mock private SendMailService service;
  private SMTPFunction smtpFunction;

  @BeforeEach
  void init() {
    smtpFunction = new SMTPFunction(service);
  }

  @DisplayName("Should execute connector and return success result")
  @ParameterizedTest(name = "#{index} - {0}")
  @MethodSource("smtpFunctionValidTestCases")
  void execute_shouldExecuteAndReturnResultWhenGiveContext(String input) {
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    SMTPRequest request = gson.fromJson(input, SMTPRequest.class);
    when(service.execute(any(Session.class), any(SMTPRequest.class)))
        .thenReturn(new SMTPResponse(STATUS));
    // When
    Object execute = smtpFunction.execute(context);
    // Then
    assertThat(execute).isInstanceOf(SMTPResponse.class);
    assertThat(((SMTPResponse) execute).getResponse()).isEqualTo(STATUS);
  }

  @DisplayName("Should throw error for invalid input")
  @ParameterizedTest(name = "#{index} - {0}")
  @MethodSource("smtpFunctionInvalidTestCases")
  void execute_shouldThrowErrorForSendMail(String input) throws NoSuchProviderException {
    // Given
    context = getContextBuilderWithSecrets().variables(input).build();
    // When
    assertThatThrownBy(() -> smtpFunction.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotBlank();
  }
}
