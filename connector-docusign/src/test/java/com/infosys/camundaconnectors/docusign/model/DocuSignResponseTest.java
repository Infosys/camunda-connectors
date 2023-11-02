package com.infosys.camundaconnectors.docusign.model;

/// *
// * Copyright (c) 2023 Infosys Ltd.
// * Use of this source code is governed by MIT license that can be found in the LICENSE file
// * or at https://opensource.org/licenses/MIT
// */

import static org.assertj.core.api.Assertions.assertThat;

import com.infosys.camundaconnectors.docusign.model.response.DocuSignResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DocuSignResponseTest {
  private static final String STATUS = "output Commands";
  private static final List<Map<String, Object>> commandOutputRespnse =
      List.of(Map.of("Command 1", "output1"), Map.of("command 2", "output 2"));

  @DisplayName("Should return string query execution status")
  @Test
  void shouldReturnStringResponse() {
    // When
    DocuSignResponse<String> response = new DocuSignResponse<>(STATUS);
    // Then
    assertThat(response.getResponse()).isEqualTo(STATUS);
  }

  @DisplayName("Should return List of Map of file data as response")
  @Test
  void shouldReturnListFilesResponse() {
    // When
    DocuSignResponse<List<Map<String, Object>>> response =
        new DocuSignResponse<>(commandOutputRespnse);
    // Then
    assertThat(response.getResponse()).isEqualTo(commandOutputRespnse);
    assertThat(response.getResponse()).isNotEmpty();
    assertThat(response.getResponse()).isInstanceOf(List.class);
    assertThat(response.getResponse().get(0)).isNotEmpty();
    assertThat(response.getResponse().get(0)).isInstanceOf(Map.class);
  }
}
