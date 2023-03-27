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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationTest extends BaseTest {
  @ParameterizedTest(name = "#{index} - {0}")
  @MethodSource("authenticationValidTestCases")
  void shouldValidateAuthenticationClassInput(String input) {
    // Given
    Authentication authentication = gson.fromJson(input, Authentication.class);
    context = getContextBuilderWithSecrets().build();
    // When
    context.replaceSecrets(authentication);
    // Then
    assertThat(authentication.getHostname()).matches("(HOSTNAME|IPAddress)");
    assertThat(authentication.getPort()).isEqualTo(ActualValue.Authentication.PORT);
    assertThat(authentication.getUsername()).isEqualTo(ActualValue.Authentication.USERNAME);
  }

  @ParameterizedTest(name = "#{index} - {0}")
  @MethodSource("authenticationInvalidTestCases")
  void validate_shouldThrowExceptionWhenLeastRequestFieldOneNotExist(String input) {
    Authentication authentication = gson.fromJson(input, Authentication.class);
    context = getContextBuilderWithSecrets().build();
    // When context validate request
    ConnectorInputException thrown =
        assertThrows(
            ConnectorInputException.class,
            () -> context.validate(authentication),
            // Then expect ConnectorInputException
            "ConnectorInputException was expected");
    assertThat(thrown.getMessage()).contains("Found constraints violated while validating input");
  }
}
