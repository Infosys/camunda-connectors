/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.postgresql.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
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
class AlterTableServiceTest {
  @Mock private Connection connectionMock;
  @Mock private DatabaseClient databaseClient;
  @Mock private DatabaseConnection connection; 
  @Mock private Statement statementMock;
  private AlterTableService service;

  @BeforeEach
  public void init() throws SQLException {
    service = new AlterTableService();
    service.setDatabaseName("test7database");
    service.setTableName("trialtable7");
    service.setDropEntityDetails(List.of(Map.of()));
    service.setModifyColumnsDetails(List.of());
    service.setMethod("");
    service.setColumnsDetails(List.of());
    service.setConstraintDetails(List.of(Map.of()));
    service.setNewTableName("");
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class),any(String.class))).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
    
  }

  @DisplayName("Should rename the table")
  @Test
  void shouldRenameTableWhenExecute() throws SQLException {
    // given
    service.setMethod("Rename Table");
    service.setNewTableName("testEmployee");
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should rename the column")
  @Test
  void shouldRenameColumnWhenExecute() throws SQLException {
    // given
    service.setMethod("Rename Column");
    service.setNewColumnDetail(Map.of("oldColName", "name", "newColName", "emp_name"));

    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  // add column
  @DisplayName("Should add columns to the table")
  @Test
  void shouldAddColumnsWhenExecute() throws SQLException {
    // given
    service.setMethod("ADD_COLUMN");
    service.setColumnsDetails(
        List.of(
            Map.of("colName", "adda", "dataType", "varchar(40)", "constraint", "NOT NULL"),
            Map.of("colName", "kit", "dataType", "int"),
            Map.of("colName", "dropthis", "dataType", "number")));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should add a constraint in the table")
  @Test
  void shouldAddConstraintsWhenExecute() throws SQLException {
    // given
    service.setMethod("ADD_CONSTRAINT");
    service.setConstraintDetails(
        List.of(
            Map.of("name", "Unique", "sYmbol", "UC_EmpNumber", "Definition", "empNumber"),
            Map.of("Name", "Primary Key", "Symbol", "PK_EmployeeID", "Definition", "empid"),
            Map.of(
                "Name", "Foreign Key",
                "Symbol", "FK_empID",
                "Definition", "(person_id) REFERENCES testemployee(id)"),
            Map.of("name", "CHECK", "symbol", "ch_id", "Definition", "empid>0")));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"dabasename");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should drop constraints from the table")
  @Test
  void shouldDropEntitiesWhenExecute() throws SQLException {
    // given
    service.setTableName("employee");
    service.setMethod("drop");
    // Constraint contains - check, index, pk ( tablename_pkey ), fk, constraint
    service.setDropEntityDetails(
        List.of(
            Map.of("EntityToDrop", "Constraint", "Name", "FK_empID"),
            Map.of("EntityToDrop", "Column", "Name", "dropthis"),
            Map.of("EntityToDrop", "Constraint", "Name", "ch_id"),
            Map.of("EntityToDrop", "Constraint", "Name", "UC_EmpNumber"),
            Map.of("EntityToDrop", "Constraint", "Name", "employee_pkey")));
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  // modify column
  @DisplayName("Should modify columns in the table")
  @Test
  void shouldModifyColumnsWhenExecute() throws SQLException {
    // given
    service.setTableName("employee");
    service.setMethod("Modify Column");
    service.setModifyColumnsDetails(
        List.of(
            Map.of(
                "colName",
                "seconds",
                "dataType",
                "timestamp with time zone",
                "expression",
                "timestamp with time zone 'epoch'"),
            Map.of("colName", "city", "dataType", "varchar(30)")));
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @Test
  void shouldThrowErrorInvalidTable() throws SQLException {
    service.setMethod("renameTable");
    service.setNewTableName("testEmployee");
    // given
    when(statementMock.executeUpdate(anyString()))
        .thenThrow(new SQLException("relation \"tableName\" does not exist"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(SQLException.class)
        .hasMessageContaining("relation \"tableName\" does not exist");
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorInvalidMethod() throws SQLException {
    String errMsg =
        "Method can only be - modifyColumn, RenameTable, drop, addCheck, "
            + "addConstraint, addColumn";
    service.setMethod("Alter");
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(errMsg);
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorInvalidNewTableName() throws SQLException {
    service.setMethod("renameTable");
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("'newTableName' can not be null or blank");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorMissingNewColumnDetail() throws SQLException {
    service.setMethod("renameColumn");
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("'newColumnDetail' is invalid");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorInvalidNewColumnDetail() throws SQLException {
    service.setMethod("renameColumn");
    service.setNewColumnDetail(Map.of("oldColName", "troy"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "'newColumnDetail' is invalid. " + "Keys 'oldColName' and 'newColName' is required");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorInvalidConstraintDetailsEmptyMap() throws SQLException {
    service.setMethod("addConstraint");
    service.setConstraintDetails(List.of(Map.of()));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid 'constraintDetails', can not contain empty object");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorInvalidConstraintDetailsMissingSymbolKey() throws SQLException {
    service.setMethod("addConstraint");
    service.setConstraintDetails(List.of(Map.of("name", "check", "definition", "col1>5")));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"demo"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("'symbol' - constraint name is required");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorInvalidConstraintDetailsInvalidConstraintName() throws SQLException {
    service.setMethod("addConstraint");
    service.setConstraintDetails(
        List.of(Map.of("name", "col1", "definition", "col1", "symbol", "ol")));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("name should be - unique, primary key, foreign key or check");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  private void assertThatItsValid(PostgreSQLResponse result) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Alter query executed successfully");
  }
}
