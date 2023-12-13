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
import java.util.ArrayList;
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
class CreateTableServiceTest {
	@Mock private Connection connectionMock;
	  @Mock private DatabaseClient databaseClient;
	  @Mock private DatabaseConnection connection; 
	  @Mock private Statement statementMock;
  private CreateTableService service;

  @BeforeEach
  public void init() throws SQLException {
    service = new CreateTableService();
    service.setDatabaseName("test7database");
    service.setTableName("trialtable7");
    service.setColumnsList(
        List.of(
            Map.of(
                "colName",
                "personid",
                "DataType",
                "int",
                "Constraint",
                new ArrayList<>(List.of("AUTO_INCREMENT", "PRIMARY KEY"))),
            Map.of("colName", "lastname", "DataType", "char(50)"),
            Map.of("colName", "firstname", "DataType", "char(50)", "Constraint", "NOT NULL"),
            Map.of("colName", "address", "DataType", "char(50)"),
            Map.of("colName", "city", "DataType", "char(50)")));
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class),any(String.class))).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
  }

  @DisplayName("Should create table")
  @Test
  void shouldCreateTableWhenExecute() throws SQLException {
    // given
    when(statementMock.execute(anyString())).thenReturn(true);
    // When
    MySQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    Mockito.verify(statementMock, Mockito.times(1)).execute(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Table '" + service.getTableName() + "' created successfully");
  }

  @DisplayName("Should throw error as columnsList is not set")
  @Test
  void shouldThrowErrorEmptyColumnsList() throws SQLException {
    // given
    service.setColumnsList(List.of());
    when(statementMock.execute(anyString()))
        .thenThrow(
            new SQLException(
                "Invalid 'columnsList', It should be a list of maps for column, "
                    + "with keys: 'colName', 'dataType' and optional 'constraints'"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "Invalid 'columnsList', It should be a list of maps for column, "
                + "with keys: 'colName', 'dataType' and optional 'constraints'");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error as columnsList is invalid")
  @Test
  void shouldThrowErrorInvalidColumnsList() throws SQLException {
    // given
    service.setColumnsList(List.of(Map.of("name", "op")));
    when(statementMock.execute(anyString()))
        .thenThrow(new SQLException("colName or dataType can't be null or empty"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("colName or dataType can't be null or empty");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw table already exists")
  @Test
  void shouldThrowErrorAsTableAlreadyExists() throws SQLException {
    // given
    when(statementMock.execute(anyString()))
        .thenThrow(new SQLException("Table 'trialtable7' already exists"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(SQLException.class)
        .hasMessageContaining("Table 'trialtable7' already exists");
    Mockito.verify(statementMock, Mockito.times(1)).execute(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }
}
