/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.smtp.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SMTPResponseTest {
  private static final String STATUS = "Mail sent successfully";
  @Mock private SMTPResponse smtpResponse;

  @DisplayName("Should return string query execution status")
  @Test
  void shouldReturnStringResponse() {
    when(smtpResponse.getResponse()).thenReturn(STATUS);
    // When
    String actualValue = smtpResponse.getResponse();
    // Then
    assertThat(actualValue).isEqualTo(STATUS);
  }

  @DisplayName("Should return null as response")
  @Test
  void shouldReturnNullAsResponse() {
    when(smtpResponse.getResponse()).thenReturn(null);
    // When
    String actualValue = smtpResponse.getResponse();
    // Then
    assertNull(actualValue);
  }
}
