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
class UpdateDataServiceTest {
  private UpdateDataService service;
  @Mock private Connection connectionMock;
  @Mock private DatabaseClient databaseClient;
  @Mock private DatabaseConnection connection; 
  @Mock private Statement statementMock;
  @BeforeEach
  void init() throws SQLException {
    service = new UpdateDataService();
    service.setDatabaseName("XE");
    service.setTableName("testRio");
    service.setUpdateMap(Map.of("city", "Uri", "firstname", "Kilo-45"));
    service.setFilters(
        Map.of("filter", Map.of("colName", "personId", "operator", ">=", "value", 5)));
    service.setOrderBy(List.of());
    service.setLimit(2);
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class),any(String.class))).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
  }

  @DisplayName("Should not update table data as no row matched for given condition")
  @Test
  void shouldNotUpdateDataIfFilterNotMatchWhenExecute() throws SQLException {
    // given
    service.setUpdateMap(Map.of("personId", 4, "firstname", "44"));
    service.setFilters(
        Map.of("filter", Map.of("colName", "personId", "operator", ">", "value", 50)));
    service.setOrderBy(List.of(Map.of("sortOn", "personId", "order", "desc")));
    service.setLimit(10);
    when(statementMock.executeUpdate(anyString())).thenReturn(0);
    // When
    MySQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result, "0 row(s) updated successfully");
  }

  @DisplayName("Should update table data for row which match given condition")
  @Test
  void shouldUpdateDataIfFilterMatchWhenExecute() throws SQLException {
    // given
    service.setOrderBy(List.of(Map.of("sortOn", "personId", "order", "desc")));
    when(statementMock.executeUpdate(anyString())).thenReturn(1);
    // When
    MySQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result, "1 row(s) updated successfully");
  }

  @DisplayName("Should throw error as table name is invalid")
  @Test
  void shouldThrowErrorInvalidTable() throws SQLException {
    // given
    when(statementMock.executeUpdate(anyString()))
        .thenThrow(new SQLException("Table 'test7database.xia' doesn't exist"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Table 'test7database.xia' doesn't exist");
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  private void assertThatItsValid(MySQLResponse result, String validateAgainst) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).contains(validateAgainst);
  }
}
