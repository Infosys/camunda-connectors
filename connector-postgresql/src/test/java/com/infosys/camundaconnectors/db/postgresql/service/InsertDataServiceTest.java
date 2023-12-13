/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.postgresql.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.db.postgresql.model.request.DatabaseConnection;
import com.infosys.camundaconnectors.db.postgresql.model.response.PostgreSQLResponse;
import com.infosys.camundaconnectors.db.postgresql.model.response.QueryResponse;
import com.infosys.camundaconnectors.db.postgresql.utility.DatabaseClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
class InsertDataServiceTest {
	@Mock private Connection connectionMock;
	  @Mock private DatabaseClient databaseClient;
	  @Mock private DatabaseConnection connection; 
	  @Mock private Statement statementMock;
	  @Mock private PreparedStatement psMock;
  private InsertDataService service;

  @BeforeEach
  void init() throws SQLException {
    service = new InsertDataService();
    service.setDatabaseName("test7database");
    service.setTableName("trialtable7");
    service.setDataToInsert(
        List.of(
            Map.of(
                "PersonID",
                1,
                "LastName",
                "Acro",
                "FirstName",
                "Alpha",
                "Address",
                "St." + " Peter Avenue",
                "City",
                "New York",
                "time",
                "2020-09-01T16:34:02"),
            Map.of(
                "PersonID",
                2,
                "LastName",
                "Ben",
                "FirstName",
                "Bob",
                "Address",
                "St. Louis Avenue",
                "City",
                "New Jersey",
                "time",
                "2022-09-01T16:34:02")));
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class),any(String.class))).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
    Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);
  }

  @DisplayName("Should throw error as dataToInsert is empty")
  @Test
  void shouldFailWhenValidate_MapEmpty() {
    // given
    service.setDataToInsert(List.of());
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("dataToInsert can not be null or empty");
  }

  @DisplayName("Should throw error as table data is invalid")
  @Test
  void shouldThrowErrorInvalidTableData() throws SQLException {
    // given
    service.setDataToInsert(List.of(Map.of("PersonID", "op")));
    Mockito.when(psMock.executeUpdate()).thenThrow(new SQLException("'op' invalid number"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("invalid number");
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error as table name is invalid")
  @Test
  void shouldThrowErrorInvalidTable() throws SQLException {
    // given
    Mockito.when(psMock.executeUpdate())
        .thenThrow(new SQLException("relation \"tableName\" does not exist"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("relation \"tableName\" does not exist");
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error as column name is invalid")
  @Test
  void shouldThrowErrorInvalidColumnName() throws SQLException {
    // given
    service.setDataToInsert(
        List.of(
            Map.of(
                "PersonID", 4,
                "LastName", "Acro",
                "FirstName", "Alpha",
                "Address", "St. Peter Avenue",
                "Tower", "New York")));
    Mockito.when(psMock.executeUpdate())
        .thenThrow(new SQLException("\"TOWER\": invalid identifier"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("\"TOWER\": invalid identifier");
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should insert 2 rows in table")
  @Test
  void shouldInsertDataWhenExecute() throws SQLException {
    // given
    Mockito.when(psMock.executeUpdate()).thenReturn(2);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should insert a row in table")
  @Test
  void shouldInsertDataWhenExecute2() throws SQLException {
    // given
    service.setDataToInsert(
        List.of(
            Map.of(
                "PersonID",
                3,
                "LastName",
                "Chaplin",
                "FirstName",
                "Charlie",
                "Address",
                "St. Johns Avenue")));
    Mockito.when(psMock.executeUpdate()).thenReturn(1);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should insert row with minimum data in table")
  @Test
  void shouldInsertDataWhenExecute3() throws SQLException {
    // given
    service.setDataToInsert(List.of(Map.of("PersonID", 5, "firstname", "last")));
    Mockito.when(psMock.executeUpdate()).thenReturn(2);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
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
        .contains("inserted successfully");
  }
}
