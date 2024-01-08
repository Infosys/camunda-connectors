/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.smtp.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.infosys.camundaconnectors.email.smtp.BaseTest;
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
    // Then
    assertThat(authentication.getHostname()).matches("(HOSTNAME|IPAddress)");
    assertThat(authentication.getPort()).isEqualTo(ActualValue.Authentication.PORT);
    assertThat(authentication.getUsername()).isEqualTo(ActualValue.Authentication.USERNAME);
  }
}
