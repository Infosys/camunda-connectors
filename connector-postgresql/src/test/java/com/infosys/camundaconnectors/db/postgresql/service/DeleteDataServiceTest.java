/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.postgresql.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.db.postgresql.model.request.DatabaseConnection;
import com.infosys.camundaconnectors.db.postgresql.model.response.PostgreSQLResponse;
import com.infosys.camundaconnectors.db.postgresql.model.response.QueryResponse;
import com.infosys.camundaconnectors.db.postgresql.utility.DatabaseClient;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeleteDataServiceTest {
	@Mock private Connection connectionMock;
	@Mock private DatabaseClient databaseClient;
	@Mock private DatabaseConnection connection; 
	@Mock private Statement statementMock;
  private DeleteDataService service;

  @BeforeEach
  void init() throws SQLException {
    service = new DeleteDataService();
    service.setDatabaseName("test7database");
    service.setTableName("trialtable7");
    service.setFilters(
        Map.of("filter", Map.of("colName", "firstname", "operator", "=", "value", "Xon")));
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class),any(String.class))).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);

  }

  @DisplayName("Should throw error as filter string is invalid")
  @Test
  void shouldThrowErrorInvalidFilterString() {
    // given
    service.setFilters(Map.of("filter", Map.of("colName", "uio")));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid filter");
  }

  @DisplayName("Should throw error as filter is invalid")
  @Test
  void shouldThrowErrorInvalidFilter() {
    // given
    service.setFilters(Map.of("kia", "op"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Map can have keys - filter, logicalOperator and filterList");
  }

  @DisplayName("Should throw error as table name is invalid")
  @Test
  void shouldThrowErrorInvalidTable() throws SQLException {
    // given
    when(statementMock.executeUpdate(anyString()))
        .thenThrow(new SQLException("relation \"tableName\" does not exist"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("relation \"tableName\" does not exist");
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error as column name is invalid")
  @Test
  void shouldThrowErrorInvalidColumnName() {
    // given
    service.setFilters(Map.of("filter", Map.of("colName", "", "operator", "=", "value", "Bob")));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Column Name can not be null or blank");
  }

  @DisplayName(
      "Should throw error as filter value is invalid type - is \"String\" but should be a \"Map\" with "
          + "keys - filter, logicalOperator and filterList")
  @Test
  void shouldThrowErrorInvalidFilterValueType() {
    // given
    service.setFilters(
        Map.of(
            "logicalOperator",
            "AND",
            "filterList",
            List.of(Map.of("filter", "firstname = 'Bob'"), Map.of("filter", "personId = 'io'"))));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "filter should be a map and can have keys - filter, logicalOperator and filterList");
  }

  @DisplayName("Should throw error as row name is invalid")
  @Test
  void shouldThrowErrorInvalidRow() throws SQLException {
    // given
    service.setFilters(
        Map.of(
            "logicalOperator",
            "AND",
            "filterList",
            List.of(
                Map.of("filter", Map.of("colName", "firstname", "operator", "=", "value", "Bob")),
                Map.of("filter", Map.of("colName", "personId", "operator", "=", "value", "io")))));
    when(statementMock.executeUpdate(anyString()))
        .thenThrow(new SQLException("'io' invalid number"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("invalid number");
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should delete the row in table which matched the given condition")
  @Test
  void shouldDeleteDataWhenExecute() throws SQLException {
    // given input
    when(statementMock.executeUpdate(anyString())).thenReturn(1);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should delete the row in table which matched the given filter - like")
  @Test
  void shouldDeleteDataWhenExecute2() throws SQLException {
    // given
    service.setFilters(
        Map.of("filter", Map.of("colName", "firstname", "operator", "like", "value", "%kio%")));
    when(statementMock.executeUpdate(anyString())).thenReturn(1);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should delete the row in table which matched the given complex filter")
  @Test
  void shouldDeleteDataWhenExecute3() throws SQLException {
    // given
    service.setFilters(
        Map.of(
            "logicalOperator",
            "AND",
            "filterList",
            List.of(
                Map.of("filter", Map.of("colName", "lastname", "operator", "=", "value", "Ben")),
                Map.of("filter", Map.of("colName", "personid", "operator", ">", "value", 1)))));
    when(statementMock.executeUpdate(anyString())).thenReturn(1);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  private void assertThatItsValid(PostgreSQLResponse result) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("deleted successfully");
  }
}
