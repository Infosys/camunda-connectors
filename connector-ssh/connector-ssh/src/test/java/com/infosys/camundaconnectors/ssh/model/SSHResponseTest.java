/// *
// * Copyright (c) 2023 Infosys Ltd.
// * Use of this source code is governed by MIT license that can be found in the LICENSE file
// * or at https://opensource.org/licenses/MIT
// */

package com.infosys.camundaconnectors.ssh.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.infosys.camundaconnectors.ssh.model.response.SSHResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SSHResponseTest {
  private static final String STATUS = "output Commands";
  private static final List<String> commands = List.of("dir", "ipconfig");
  private static final String commandLine = "single";
  private static final String outputType = "statusCode";
  private static final List<Map<String, Object>> commandOutputRespnse =
      List.of(Map.of("Command 1", "output1"), Map.of("command 2", "output 2"));

  @DisplayName("Should return string query execution status")
  @Test
  void shouldReturnStringResponse() {
    // When
    SSHResponse<String> response = new SSHResponse<>(STATUS);
    // Then
    assertThat(response.getResponse()).isEqualTo(STATUS);
  }

  @DisplayName("Should return List of Map of file data as response")
  @Test
  void shouldReturnListFilesResponse() {
    // When
    SSHResponse<List<Map<String, Object>>> response = new SSHResponse<>(commandOutputRespnse);
    // Then
    assertThat(response.getResponse()).isEqualTo(commandOutputRespnse);
    assertThat(response.getResponse()).isNotEmpty();
    assertThat(response.getResponse()).isInstanceOf(List.class);
    assertThat(response.getResponse().get(0)).isNotEmpty();
    assertThat(response.getResponse().get(0)).isInstanceOf(Map.class);
  }
}
