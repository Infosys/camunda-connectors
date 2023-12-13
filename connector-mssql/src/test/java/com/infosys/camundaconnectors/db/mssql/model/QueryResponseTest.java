/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.infosys.camundaconnectors.db.mssql.model.response.QueryResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class QueryResponseTest {
  private static final String STATUS = "query executed successfully";
  private static final List<Map<String, Object>> ROWS_DATA =
      List.of(
          Map.of("colOne", 11, "colTwo", "rowValue12"),
          Map.of("colOne", "rowValue21", "colTwo", true));

  @DisplayName("Should return string query execution status")
  @Test
  public void shouldReturnStringResponse() {
    // When
    QueryResponse<String> response = new QueryResponse<>(STATUS);
    // Then
    assertThat(response.getResponse()).isEqualTo(STATUS);
  }

  @DisplayName("Should return List of Map of row data query execution response")
  @Test
  public void shouldReturnReadDataResponse_Rows() {
    // When
    QueryResponse<List<Map<String, Object>>> response = new QueryResponse<>(ROWS_DATA);
    // Then
    assertThat(response.getResponse()).isEqualTo(ROWS_DATA);
    assertThat(response.getResponse()).isNotEmpty();
    assertThat(response.getResponse()).isInstanceOf(List.class);
    assertThat(response.getResponse().get(0)).isNotEmpty();
    assertThat(response.getResponse().get(0)).isInstanceOf(Map.class);
  }
}
