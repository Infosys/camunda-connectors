/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.db.mssql.model.request.DatabaseConnection;
import com.infosys.camundaconnectors.db.mssql.model.response.MSSQLResponse;
import com.infosys.camundaconnectors.db.mssql.model.response.QueryResponse;
import com.infosys.camundaconnectors.db.mssql.utility.DatabaseClient;

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
    service.setDatabaseName("trial");
    service.setTableName("staff");
    service.setDropConstraintsList(List.of());
    service.setDropColumnsList(List.of());
    service.setConstraintName("");
    service.setModifyColumnsDetails(Map.of());
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
    service.setNewTableName("member");
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MSSQLResponse result = service.invoke(databaseClient,connection,"database");
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
    service.setNewColumnDetail(Map.of("oldColName", "member_name", "newColName", "memberName"));

    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MSSQLResponse result = service.invoke(databaseClient,connection,"database");
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
            Map.of("colName", "member_address", "dataType", "char(60)", "constraint", "NOT NULL"),
            Map.of("colName", "member_contact_number", "dataType", "int")));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MSSQLResponse result = service.invoke(databaseClient,connection,"database");
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
            Map.of("name", "unique", "sYmbol", "uq_member_id", "Definition", "member_id"),
            Map.of("Name", "Primary Key", "Symbol", "pk_member_id", "Definition", "member_id"),
            Map.of(
                "Name",
                "Foreign Key",
                "Symbol",
                "fk_member_id",
                "Definition",
                "(member_id) REFERENCES trialtable(PersonID)"),
            Map.of("Name", "DEFAULT", "Symbol", "Sys_date", "Definition", "GETDATE() FOR Sys_date"),
            Map.of("name", "CHECK", "symbol", "check_member_id", "Definition", "member_id>0")));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MSSQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should drop constraints from the table")
  @Test
  void shouldDropConstraintsWhenExecute() throws SQLException {
    // given
    service.setMethod("DROP_CONSTRAINT");
    service.setDropConstraintsList(
        List.of("pk_member_id", "fk_member_id", "uq_member_id", "check_member_id", "sys_date"));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MSSQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should drop columns from the table")
  @Test
  void shouldDropColumnsWhenExecute() throws SQLException {
    // given
    service.setMethod("DROP_COLUMN");
    service.setDropColumnsList(List.of("member_contact_number", "member_address"));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MSSQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should enable constraint")
  @Test
  void shouldEnableConstraintWhenExecute() throws SQLException {
    // given
    service.setMethod("ENABLE_CONSTRAINT");
    service.setConstraintName("fk_member_id");
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MSSQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should disable constraint")
  @Test
  void shouldDisableConstraintWhenExecute() throws SQLException {
    // given
    service.setMethod("DISABLE_CONSTRAINT");
    service.setConstraintName("fk_member_id");
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MSSQLResponse result = service.invoke(databaseClient,connection,"database");
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
    service.setMethod("Modify Column");
    service.setModifyColumnsDetails(
        Map.of(
            "colName", "member_contact_number", "dataType", "varchar(13)", "constraint", "NULL"));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    MSSQLResponse result = service.invoke(databaseClient,connection,"database");
    // Then
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(anyString());
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should throw table does not exists")
  @Test
  void shouldThrowErrorInvalidTable() throws SQLException {
    service.setMethod("renameTable");
    service.setNewTableName("testEmployee");
    // given
    when(statementMock.executeUpdate(anyString()))
        .thenThrow(new SQLException("table or view does not exist"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // then
        .isInstanceOf(SQLException.class)
        .hasMessageContaining("table or view does not exist");
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw invalid method type")
  @Test
  void shouldThrowErrorInvalidMethod() throws SQLException {
    String errMsg =
        "Method can only be - disableConstraint, enableConstraint, "
            + "modifyColumn, renameTable, renameColumn, "
            + "dropColumn, dropConstraint, addConstraint, addColumn";
    service.setMethod("Alter");
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(errMsg);
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error for missing input param - 'newTableName'")
  @Test
  void shouldThrowErrorMissingNewTableName() throws SQLException {
    service.setMethod("renameTable");
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("'newTableName' can not be null or blank");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error for missing input param - 'newColumnDetail'")
  @Test
  void shouldThrowErrorMissingNewColumnDetail() throws SQLException {
    service.setMethod("renameColumn");
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("'newColumnDetail' is invalid");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error for invalid parameter 'newTableName'")
  @Test
  void shouldThrowErrorInvalidNewColumnDetail() throws SQLException {
    service.setMethod("renameColumn");
    service.setNewColumnDetail(Map.of("oldColName", "troy"));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "'newColumnDetail' is invalid. Keys 'oldColName' and 'newColName' is required");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error for invalid parameter 'constraintDetails'")
  @Test
  void shouldThrowErrorInvalidConstraintDetailsEmptyMap() throws SQLException {
    service.setMethod("addConstraint");
    service.setConstraintDetails(List.of(Map.of()));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "Unable to create alter table - "
                + "add constraint query, Please check the input 'constraintDetails'");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error for invalid constraint type")
  @Test
  void shouldThrowErrorInvalidConstraintType() throws SQLException {
    service.setMethod("addConstraint");
    service.setConstraintDetails(
        List.of(Map.of("name", "col1", "symbol", "cons", "definition", "col1")));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "name should be - default, unique, primary key, foreign key or check");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error for invalid input, missing 'symbol' or constraint name")
  @Test
  void shouldThrowErrorInvalidConstraintDetailsMissingSymbolKey() throws SQLException {
    service.setMethod("addConstraint");
    service.setConstraintDetails(List.of(Map.of("name", "primaryKey", "definition", "col1")));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "'symbol' can not be null or blank. "
                + "Please provide constraint name. e.g. symbol: 'pk_id'");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error invalid constraint type")
  @Test
  void shouldThrowErrorInvalidConstraintDetailsInvalidConstraintName() throws SQLException {
    service.setMethod("addConstraint");
    service.setConstraintDetails(
        List.of(Map.of("name", "col1", "definition", "col1", "symbol", "ol")));
    // when
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"database"))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "name should be - default, unique, " + "primary key, foreign key or check");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  private void assertThatItsValid(MSSQLResponse result) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("executed successfully");
  }
}
