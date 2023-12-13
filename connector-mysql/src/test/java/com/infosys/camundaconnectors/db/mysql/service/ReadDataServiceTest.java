/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mysql.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.db.mysql.model.request.DatabaseConnection;
import com.infosys.camundaconnectors.db.mysql.model.response.MySQLResponse;
import com.infosys.camundaconnectors.db.mysql.model.response.QueryResponse;
import com.infosys.camundaconnectors.db.mysql.utility.DatabaseClient;

import java.sql.*;
import java.util.HashMap;
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
class ReadDataServiceTest {
	  @Mock private DatabaseClient databaseClient;
	  @Mock private DatabaseConnection connection; 

  @Mock private Connection connectionMock;
  @Mock private Statement statementMock;
  @Mock private ResultSet resultSetMock;
  @Mock private ResultSetMetaData resultSetMetaData;
  private ReadDataService service;

  @BeforeEach
  void init() throws SQLException {
    service = new ReadDataService();
    service.setDatabaseName("XE");
    service.setTableName("testRio");
    service.setFilters(
        Map.of("filter", Map.of("colName", "personid", "operator", "=", "value", 3)));
    service.setColumnNames(List.of("FirstName"));
    service.setOrderBy(List.of(Map.of("sortOn", "personid", "order", "descending")));
    service.setLimit(1000);
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class),any(String.class))).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
    when(resultSetMock.getMetaData()).thenReturn(resultSetMetaData);
  }

  @DisplayName("Should throw error as filter is invalid")
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

  @DisplayName("Should throw error as filter key is invalid")
  @Test
  void shouldThrowErrorInvalidFilter() {
    // given
    service.setFilters(Map.of("kia", "op"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "Invalid filter: Map can have keys - filter, logicalOperator and filterList");
  }

  @DisplayName("Should throw error as table name is invalid")
  @Test
  void shouldThrowErrorInvalidTable() throws SQLException {
    // given
    when(statementMock.executeQuery(anyString()))
        .thenThrow(new SQLException("Table 'test7database.xia' doesn't exist"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Table 'test7database.xia' doesn't exist");
    Mockito.verify(statementMock, Mockito.times(1)).executeQuery(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error as column name is invalid")
  @Test
  void shouldThrowErrorInvalidColumnName() throws SQLException {
    // given
    service.setFilters(
        Map.of("filter", Map.of("colName", "orderNumber", "operator", "=", "value", 10100)));
    when(statementMock.executeQuery(anyString()))
        .thenThrow(new SQLException("Unknown column \"ORDERNUMBER\""));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Unknown column \"ORDERNUMBER\"");
    Mockito.verify(statementMock, Mockito.times(1)).executeQuery(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error as column list is invalid")
  @Test
  void shouldThrowErrorInvalidColumnList() throws SQLException {
    // given
    service.setColumnNames(List.of("orderId"));
    when(statementMock.executeQuery(anyString()))
        .thenThrow(new SQLException("Unknown column \"ORDERID\""));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Unknown column \"ORDERID\"");
    Mockito.verify(statementMock, Mockito.times(1)).executeQuery(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should read table data for simple filter")
  @Test
  void shouldReadDataWhenExecute() throws SQLException {
    when(statementMock.executeQuery(anyString())).thenReturn(resultSetMock);
    when(resultSetMock.next()).thenReturn(true).thenReturn(false);
    when(resultSetMetaData.getColumnCount()).thenReturn(1);
    when(resultSetMetaData.getColumnName(1)).thenReturn("something");
    when(resultSetMock.getObject(1)).thenReturn(576);
    // When
    MySQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    assertThatItsValid(result);
  }

  @DisplayName("Should read table data for filter - like")
  @Test
  void shouldReadDataWhenExecute2() throws SQLException {
    // given
    service.setFilters(
        Map.of("filter", Map.of("colName", "firstname", "operator", "like", "value", "A%")));
    service.setColumnNames(List.of("personid", "firstname"));
    when(statementMock.executeQuery(anyString())).thenReturn(resultSetMock);
    when(resultSetMock.next()).thenReturn(true).thenReturn(false);
    when(resultSetMetaData.getColumnCount()).thenReturn(2);
    when(resultSetMetaData.getColumnName(1)).thenReturn("something");
    when(resultSetMock.getObject(1)).thenReturn(576);
    when(resultSetMetaData.getColumnName(2)).thenReturn("somewhere");
    when(resultSetMock.getObject(2)).thenReturn(true);
    // When
    MySQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    assertThatItsValid(result);
  }

  @DisplayName("Should read table data for complex filter")
  @Test
  void shouldReadDataWhenExecute3() throws SQLException {
    // given
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("colName", "city");
    filterMap.put("operator", "is");
    filterMap.put("value", null);
    service.setFilters(
        Map.of(
            "logicalOperator",
            "OR",
            "filterList",
            List.of(
                Map.of("filter", Map.of("colName", "lastname", "operator", "like", "value", "A%")),
                Map.of("filter", filterMap))));
    service.setColumnNames(List.of("personid", "firstName", "LastName"));
    service.setOrderBy(List.of(Map.of("sortOn", "FirstName", "order", "ascending")));
    service.setLimit(1000);
    when(statementMock.executeQuery(anyString())).thenReturn(resultSetMock);
    when(statementMock.executeQuery(anyString())).thenReturn(resultSetMock);
    when(resultSetMock.next()).thenReturn(true).thenReturn(false);
    when(resultSetMetaData.getColumnCount()).thenReturn(3);
    when(resultSetMetaData.getColumnName(1)).thenReturn("id");
    when(resultSetMock.getObject(1)).thenReturn(101);
    when(resultSetMetaData.getColumnName(2)).thenReturn("name");
    when(resultSetMock.getObject(2)).thenReturn("Alex");
    when(resultSetMetaData.getColumnName(3)).thenReturn("alive");
    when(resultSetMock.getObject(3)).thenReturn(true);
    // When
    MySQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    assertThatItsValid(result);
  }

  private void assertThatItsValid(MySQLResponse result) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<List<Map<String, Object>>> queryResponse =
        (QueryResponse<List<Map<String, Object>>>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }
}
