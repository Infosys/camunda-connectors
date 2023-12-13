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
    service.setDatabaseName("test7database");
    service.setTableName("trialtable7");
    service.setUpdateMap(
        Map.of("personid", 12, "firstname", "pluto", "time", "2024-09-01T16:34:02"));
    service.setFilters(
        Map.of("filter", Map.of("colName", "personid", "operator", "=", "value", 5)));
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class),any(String.class))).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
  }

  @DisplayName("Should throw error as table name is invalid")
  @Test
  void shouldThrowErrorInvalidTable() throws SQLException {
    // given
    when(statementMock.executeUpdate(anyString()))
        .thenThrow(new SQLException("relation \"xia\" does not exist"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("relation \"xia\" does not exist");
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should not update table data as now row matched for given condition")
  @Test
  void shouldNotUpdateDataIfFilterNotMatchWhenExecute() throws SQLException {
    // given
    service.setUpdateMap(Map.of("personId", 4, "firstname", "44"));
    service.setFilters(
        Map.of("filter", Map.of("colName", "personId", "operator", ">", "value", 50)));
    when(statementMock.executeUpdate(anyString())).thenReturn(0);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result, "0 row(s) updated successfully");
  }

  @DisplayName("Should update table data for row which match given condition")
  @Test
  void shouldUpdateDataIfFilterMatchWhenExecute() throws SQLException {
    // given
    when(statementMock.executeUpdate(anyString())).thenReturn(1);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result, "1 row(s) updated successfully");
  }

  private void assertThatItsValid(PostgreSQLResponse result, String validateAgainst) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).contains(validateAgainst);
  }
}
