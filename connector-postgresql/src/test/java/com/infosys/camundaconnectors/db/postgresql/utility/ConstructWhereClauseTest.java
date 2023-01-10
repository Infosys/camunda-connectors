/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.postgresql.utility;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConstructWhereClauseTest {
  @DisplayName("Should create where clause with '='")
  @Test
  void shouldCreateWhereClauseWhenExecute() {
    // given
    Map<String, Object> context =
        Map.of("filter", Map.of("operator", "=", "colName", "id", "value", 23));
    // when
    var result = ConstructWhereClause.getWhereClause(context);
    // then
    assertThat(result).isInstanceOf(String.class).isEqualTo(" WHERE id = 23");
  }

  @DisplayName("Should create where clause with 'like'")
  @Test
  void shouldCreateWhereClauseForLikeWhenExecute() {
    // given
    Map<String, Object> context =
        Map.of("filter", Map.of("operator", "like", "colName", "id", "value", "%rio"));
    // when
    var result = ConstructWhereClause.getWhereClause(context);
    // then
    assertThat(result).isInstanceOf(String.class).isEqualTo(" WHERE id  LIKE  '%rio'");
  }

  @DisplayName("Should create where clause with 'startswith'")
  @Test
  void shouldCreateWhereClauseForStartsWithWhenExecute() {
    // given
    Map<String, Object> context =
        Map.of("filter", Map.of("operator", "startswith", "colName", "name", "value", "Iota"));
    // when
    var result = ConstructWhereClause.getWhereClause(context);
    // then
    assertThat(result).isInstanceOf(String.class).isEqualTo(" WHERE name  LIKE  'Iota%'");
  }

  @DisplayName("Should create where clause with 'endswith' and negated")
  @Test
  void shouldCreateWhereClauseForEndsWithWhenExecute() {
    // given
    Map<String, Object> context =
        Map.of(
            "filter",
            Map.of("operator", "endswith", "colName", "name", "value", "Iota"),
            "logicalOperator",
            "not");
    // when
    var result = ConstructWhereClause.getWhereClause(context);
    // then
    assertThat(result).isInstanceOf(String.class).isEqualTo(" WHERE NOT name  LIKE  '%Iota'");
  }

  @DisplayName("Should return empty string as where clause")
  @Test
  void shouldReturnBlankStringWhenExecute() {
    // given null
    // when
    var result = ConstructWhereClause.getWhereClause(null);
    // then
    assertThat(result).isInstanceOf(String.class).isBlank();
  }

  @DisplayName("Should throw error as 'filter' key is missing")
  @Test
  void shouldThrowErrorAsFilterKeyIsMissing() {
    // given
    Map<String, Object> context =
        Map.of(
            "filterList",
            List.of(Map.of("operator", "endswith", "colName", "name", "value", "Iota")),
            "logicalOperator",
            "and");
    // when
    assertThatThrownBy(() -> ConstructWhereClause.getWhereClause(context))
        // then
        .hasMessageContaining("Map should have key - filter");
  }

  @DisplayName("Should throw error as Logical operator is invalid")
  @Test
  void shouldThrowErrorAsFilterLogicalOperatorIsInvalid() {
    // given
    Map<String, Object> context =
        Map.of(
            "filterList",
            List.of(
                Map.of(
                    "filter", Map.of("operator", "endswith", "colName", "name", "value", "Iota"))),
            "logicalOperator",
            "xia");
    // when
    assertThatThrownBy(() -> ConstructWhereClause.getWhereClause(context))
        // then
        .hasMessageContaining("Invalid Logical operator type");
  }
}
