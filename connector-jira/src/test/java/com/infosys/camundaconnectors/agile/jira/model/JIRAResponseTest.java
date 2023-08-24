/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.agile.jira.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.infosys.camundaconnectors.agile.jira.model.response.JIRAResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JIRAResponseTest {
  private static final String STATUS = "Issue Successfully Created!!";

  @DisplayName("Should return string query execution status")
  @Test
  void shouldReturnStringResponse() {
    JIRAResponse<String> response = new JIRAResponse<>(STATUS);
    assertThat(response.getResponse()).isEqualTo(STATUS);
  }
}
