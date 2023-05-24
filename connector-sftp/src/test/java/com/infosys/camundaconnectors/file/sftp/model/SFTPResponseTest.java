/// *
// * Copyright (c) 2023 Infosys Ltd.
// * Use of this source code is governed by MIT license that can be found in the LICENSE file
// * or at https://opensource.org/licenses/MIT
// */

package com.infosys.camundaconnectors.file.sftp.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SFTPResponseTest {
  private static final String STATUS = "File deleted Successfully";
  private static final List<String> FOLDER_PATHS =
      List.of("C:\\Users\\Documents", "C:\\Users\\Documents\\Demo1");
  private static final List<String> FILE_NAME = List.of("Example.txt", "Example2.txt");
  private static final List<Map<String, Object>> FILE_DETAILS =
      List.of(Map.of("File_Name", "C:\\Users\\Documents"), Map.of("Modified Date", "12-02-2023"));
  private static final Map<String, Object> FILES =
      Map.of("File_Name", "C:\\Users\\Documents", "Modified Date", "12-02-2023");

  @DisplayName("Should return string query execution status")
  @Test
  void shouldReturnStringResponse() {
    // When
    SFTPResponse<String> response = new SFTPResponse<>(STATUS);
    // Then
    assertThat(response.getResponse()).isEqualTo(STATUS);
  }

  @DisplayName("Should return list of string query execution status")
  @Test
  void shouldReturnListOfStringResponse() {
    // When
    SFTPResponse<List<String>> response = new SFTPResponse<>(FOLDER_PATHS);
    // Then
    assertThat(response.getResponse()).isEqualTo(FOLDER_PATHS);
  }

  @DisplayName("Should return map query execution status")
  @Test
  void shouldReturnMapResponse() {
    // When
    SFTPResponse<Map<String, Object>> response = new SFTPResponse<>(FILES);
    // Then
    assertThat(response.getResponse()).isEqualTo(FILES);
  }

  @DisplayName("Should return List of Map of file data as response")
  @Test
  void shouldReturnListFilesResponse() {
    // When
    SFTPResponse<List<Map<String, Object>>> response = new SFTPResponse<>(FILE_DETAILS);
    // Then
    assertThat(response.getResponse()).isEqualTo(FILE_DETAILS);
    assertThat(response.getResponse()).isNotEmpty();
    assertThat(response.getResponse()).isInstanceOf(List.class);
    assertThat(response.getResponse().get(0)).isNotEmpty();
    assertThat(response.getResponse().get(0)).isInstanceOf(Map.class);
  }
}
