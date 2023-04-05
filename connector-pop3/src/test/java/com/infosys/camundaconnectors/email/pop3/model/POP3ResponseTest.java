/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.infosys.camundaconnectors.email.pop3.model.response.POP3Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class POP3ResponseTest {
  private static final String STATUS = "Email deleted successfully";
  private static final List<String> MESSAGE_IDS =
      List.of("123361253267ghdgewjh", "j98due9dj9jednu93");
  private static final List<Map<String, Object>> HEADER_DETAILS =
      List.of(Map.of("Subject", "email subject"), Map.of("Received date", "12-02-2023"));
  private static final Map<String, Object> HEADER =
      Map.of("Subject", "email subject", "Received date", "12-02-2023");

  @DisplayName("Should return string query execution status")
  @Test
  void shouldReturnStringResponse() {
    // When
    POP3Response<String> response = new POP3Response<>(STATUS);
    // Then
    assertThat(response.getResponse()).isEqualTo(STATUS);
  }

  @DisplayName("Should return list of string query execution status")
  @Test
  void shouldReturnListOfStringResponse() {
    // When
    POP3Response<List<String>> response = new POP3Response<>(MESSAGE_IDS);
    // Then
    assertThat(response.getResponse()).isEqualTo(MESSAGE_IDS);
  }

  @DisplayName("Should return map query execution status")
  @Test
  void shouldReturnMapResponse() {
    // When
    POP3Response<Map<String, Object>> response = new POP3Response<>(HEADER);
    // Then
    assertThat(response.getResponse()).isEqualTo(HEADER);
  }

  @DisplayName("Should return List of Map of email data as response")
  @Test
  void shouldReturnSearchEmailsResponse() {
    // When
    POP3Response<List<Map<String, Object>>> response = new POP3Response<>(HEADER_DETAILS);
    // Then
    assertThat(response.getResponse()).isEqualTo(HEADER_DETAILS);
    assertThat(response.getResponse()).isNotEmpty();
    assertThat(response.getResponse()).isInstanceOf(List.class);
    assertThat(response.getResponse().get(0)).isNotEmpty();
    assertThat(response.getResponse().get(0)).isInstanceOf(Map.class);
  }
}
