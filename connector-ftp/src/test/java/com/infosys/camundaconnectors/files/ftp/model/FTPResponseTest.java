/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FTPResponseTest {
  private static final String STATUS = "File deleted Successfully";
  private static final String FOLDER_PATHS =
      "[\"C:\\Users\\Documents\\Folder1\", \"C:\\Users\\Documents\\Demo1\"]";
  private static final String FILE_NAME =
      "[\"C:\\Users\\Documents\\a.txt\", \"C:\\Users\\Documents\\b.txt\"]";

  @DisplayName("Should return string query execution status")
  @Test
  void shouldReturnStringResponse() {
    FTPResponse<String> response = new FTPResponse<>(STATUS);
    assertThat(response.getResponse()).isEqualTo(STATUS);
  }

  @DisplayName("Should return list of string query execution status")
  @Test
  void shouldReturnListOfStringAsStringResponse() {
    FTPResponse<String> response = new FTPResponse<>(FOLDER_PATHS);
    assertThat(response.getResponse()).isEqualTo(FOLDER_PATHS);
  }

  @DisplayName("Should return map query execution status")
  @Test
  void shouldReturnMapAsStringResponse() {
    FTPResponse<String> response = new FTPResponse<>(FILE_NAME);
    assertThat(response.getResponse()).isEqualTo(FILE_NAME);
  }
}
