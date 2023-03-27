/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.smtp.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.infosys.camundaconnectors.email.smtp.BaseTest;
import io.camunda.connector.impl.ConnectorInputException;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class SMTPRequestTest extends BaseTest {
  @ParameterizedTest(name = "#{index} - {0}")
  @MethodSource("replaceSecretsSuccessTestCases")
  void replaceSecrets_shouldReplaceSecrets(String input) {
    // Given
    SMTPRequest requestData = gson.fromJson(input, SMTPRequest.class);
    context = getContextBuilderWithSecrets().build();
    // When
    context.replaceSecrets(requestData);
    // Then
    assertThat(requestData.getAuthentication().getHostname())
        .isEqualTo(ActualValue.Authentication.HOST);
    assertThat(requestData.getAuthentication().getPort())
        .isEqualTo(ActualValue.Authentication.PORT);
    assertThat(requestData.getAuthentication().getUsername())
        .isEqualTo(ActualValue.Authentication.USERNAME);
    assertThat(requestData.getAuthentication().getPassword()).isEqualTo(ActualValue.TOKEN);
    assertThat(requestData.getSmtpEmailMailBoxName()).isEqualTo(ActualValue.MAILBOX_NAME);
    assertThat(requestData.getSmtpEmailSubject()).isEqualTo(ActualValue.SUBJECT);
    assertThat(requestData.getSmtpEmailContentType()).containsPattern("(?i)(text|html)");
    assertThat(requestData.getSmtpEmailContent()).isNotEmpty();
    assertThat(requestData.getSmtpEmailToRecipients()).isNotEmpty();
  }

  @ParameterizedTest(name = "#{index} - {0}")
  @MethodSource("validateRequiredFieldsFailTestCases")
  void validate_shouldThrowExceptionWhenLeastRequestFieldOneNotExist(String input) {
    SMTPRequest requestData = gson.fromJson(input, SMTPRequest.class);
    context = getContextBuilderWithSecrets().build();
    // When context validate request
    ConnectorInputException thrown =
        assertThrows(
            ConnectorInputException.class,
            () -> context.validate(requestData),
            // Then expect ConnectorInputException
            "ConnectorInputException was expected");
    assertThat(thrown.getMessage()).contains("Found constraints violated while validating input");
  }

  @ParameterizedTest(name = "#{index} - {0}")
  @ValueSource(strings = {"K"})
  void validate_shouldThrowExceptionAuthenticationInvalid(String input) {
    // Given
    SMTPRequest requestData = new SMTPRequest();
    requestData.setAuthentication(new Authentication());
    requestData.setSmtpEmailMailBoxName(input);
    requestData.setSmtpEmailToRecipients(List.of(input));
    requestData.setSmtpEmailSubject(input);
    requestData.setSmtpEmailContentType(input);
    requestData.setSmtpEmailContent(input);
    context = getContextBuilderWithSecrets().build();
    // When context validate request
    // Then expect ConnectorInputException
    assertThrows(
        ConnectorInputException.class,
        () -> context.validate(requestData),
        "ConnectorInputException was expected");
  }

  @ParameterizedTest(name = "#{index} - {0}")
  @ValueSource(strings = {"", "  ", "     "})
  void validate_shouldThrowExceptionWhenInputIsInvalid(String input) {
    // Given
    SMTPRequest requestData = new SMTPRequest();
    Authentication auth = new Authentication();
    auth.setHostname(ActualValue.Authentication.HOST);
    auth.setPort(ActualValue.Authentication.PORT);
    auth.setUsername(ActualValue.Authentication.USERNAME);
    auth.setPassword(ActualValue.Authentication.PASSWORD);
    requestData.setAuthentication(auth);
    requestData.setSmtpEmailMailBoxName(input);
    context = getContextBuilderWithSecrets().build();
    // When context validate request
    // Then expect ConnectorInputException
    assertThrows(
        ConnectorInputException.class,
        () -> context.validate(requestData),
        "ConnectorInputException was expected");
  }
}
