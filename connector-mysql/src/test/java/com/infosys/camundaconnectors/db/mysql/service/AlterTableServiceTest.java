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
    service.setTableName("trialtable");
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
    MySQLResponse result = service.invoke(databaseClient,connection,"database");
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
    MySQLResponse result = service.invoke(databaseClient,connection,"database");
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
    MySQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should add a constraint in the table")
  @Test
  void shouldAddConstraintsWhenExecute() throws SQLException {
    // given
    service.setTableName("employee");
    service.setMethod("ADD_CONSTRAINT");
    service.setConstraintDetails(
        List.of(
            Map.of("name", "Unique", "sYmbol", "UC_EmpNumber", "Definition", "empNumber"),
            Map.of("Name", "Primary Key", "Symbol", "PK_EmployeeID", "Definition", "empid"),
            Map.of(
                "Name",
                "Foreign Key",
                "Symbol",
                "FK_empID",
                "Definition",
                "(person_id) REFERENCES trialtable" + "(personid)"),
            Map.of("name", "CHECK", "Definition", "empid<=1000"),
            Map.of("name", "CHECK", "symbol", "ch_id", "Definition", "empid>0")));
    Mockito.when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MySQLResponse result = service.invoke(databaseClient,connection,"database");
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
    // Column, check, index, pk, fk, constraint
    service.setDropEntityDetails(
        List.of(
            Map.of("EntityToDrop", "Foreign  Key", "Name", "FK_empID"),
            Map.of("EntityToDrop", "index", "Name", "FK_empID"),
            Map.of("EntityToDrop", "Column", "Name", "dropthis"),
            Map.of("EntityToDrop", "check", "Name", "ch_id"),
            Map.of("EntityToDrop", "Constraint", "Name", "UC_EmpNumber"),
            Map.of("EntityToDrop", "Primary Key")));
    Mockito.when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MySQLResponse result = service.invoke(databaseClient,connection,"database");
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
            Map.of("colName", "empnumber", "dataType", "varchar(30)", "constraint", "NOT NULL"),
            Map.of("colName", "city", "dataType", "varchar(30)")));
    Mockito.when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MySQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should throw error for invalid 'modifyColumnsDetails'")
  @Test
  void shouldThrowErrorForInvalidModifyColumnsDetailsWhenExecute() throws SQLException {
    // given
    service.setMethod("modifyColumn");
    service.setTableName("xia");
    Mockito.when(statementMock.executeUpdate(any(String.class)))
        .thenThrow(new RuntimeException("'modifyColumnsDetails' " + "can not be null or empty"));
    // When
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("'modifyColumnsDetails' can not be null or empty");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error for empty 'modifyColumnsDetails'")
  @Test
  void shouldThrowErrorForInvalidModifyColumnsDetailsEmptyWhenExecute() throws SQLException {
    // given
    service.setMethod("modifyColumn");
    service.setTableName("xia");
    service.setModifyColumnsDetails(List.of(Map.of()));
    Mockito.when(statementMock.executeUpdate(any(String.class)))
        .thenThrow(
            new RuntimeException(
                "Unable to create alter "
                    + "table - modify query, Please check the input - 'modifyColumnsDetails' list of map should have keys -"
                    + " colName, dataType, constraint"));
    // When
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "Unable to create alter table - modify query, Please check the input - "
                + "'modifyColumnsDetails' list of map should have keys - colName, dataType, constraint");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error for table does not exists")
  @Test
  void shouldThrowErrorForInvalidModifyEmptyWhenExecute() throws SQLException {
    // given
    service.setMethod("modifyColumn");
    service.setTableName("xia");
    service.setModifyColumnsDetails(
        List.of(Map.of("colName", "address", "dataType", "varchar(50)", "constraint", "NOT NULL")));
    Mockito.when(statementMock.executeUpdate(any(String.class)))
        .thenThrow(new RuntimeException("Table 'test7database.xia'" + " doesn't exist"));
    // When
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Table 'test7database.xia' doesn't exist");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  private void assertThatItsValid(MySQLResponse result) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Alter query executed successfully");
  }
}
