/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.oracle.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.db.oracle.model.response.OracleDBResponse;
import com.infosys.camundaconnectors.db.oracle.model.response.QueryResponse;
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
  @Mock private Statement statementMock;
  private AlterTableService service;

  @BeforeEach
  public void init() throws SQLException {
    service = new AlterTableService();
    service.setDatabaseName("XE");
    service.setTableName("testRio");
    service.setDropConstraintsList(List.of());
    service.setDropColumnsList(List.of());
    service.setConstraintName("");
    service.setModifyColumnsDetails(List.of());
    service.setMethod("");
    service.setColumnsDetails(List.of());
    service.setConstraintDetails(List.of(Map.of()));
    service.setNewTableName("");
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
    OracleDBResponse result = service.invoke(connectionMock);
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
    OracleDBResponse result = service.invoke(connectionMock);
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
    OracleDBResponse result = service.invoke(connectionMock);
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
                "Name",
                "Foreign Key",
                "Symbol",
                "FK_empID",
                "Definition",
                "(person_id) REFERENCES testemployee(id)"),
            Map.of("name", "CHECK", "symbol", "ch_id", "Definition", "empid>0")));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
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
    // Column, check, index, pk, fk, constraint
    service.setDropConstraintsList(List.of("FK_empID", "ch_id", "UC_EmpNumber", "Primary Key"));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
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
    // Column, check, index, pk, fk, constraint
    service.setDropColumnsList(List.of("column1", "column2"));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
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
    service.setConstraintName("ch_id");
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
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
    service.setConstraintName("ch_id");
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
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
        List.of(
            Map.of("colName", "empnumber", "dataType", "varchar(20)", "constraint", "NOT NULL"),
            Map.of("colName", "city", "dataType", "varchar(20)")));
    when(statementMock.executeUpdate(any(String.class))).thenReturn(0);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
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
        .thenThrow(new SQLException("table or view does not exist"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(SQLException.class)
        .hasMessageContaining("table or view does not exist");
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorInvalidMethod() throws SQLException {
    String errMsg =
        "Method can only be - enableConstraint, "
            + "disableConstraint, modifyColumn, renameTable, RenameColumn, "
            + "dropColumn, dropConstraint, addConstraint, addColumn";
    service.setMethod("Alter");
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(errMsg);
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorMissingNewTableName() throws SQLException {
    service.setMethod("renameTable");
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("'newTableName' can not be null or blank");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorMissingNewColumnDetail() throws SQLException {
    service.setMethod("renameColumn");
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
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
    assertThatThrownBy(() -> service.invoke(connectionMock))
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
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid 'constraintDetails', can not contain empty object");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorInvalidConstraintDetailsMissingSymbolKey() throws SQLException {
    service.setMethod("addConstraint");
    service.setConstraintDetails(List.of(Map.of("name", "col1", "definition", "col1")));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "'symbol' can not be null or blank. "
                + "Please provide constraint name. e.g. symbol: 'pk_id'");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @Test
  void shouldThrowErrorInvalidConstraintDetailsInvalidConstraintName() throws SQLException {
    service.setMethod("addConstraint");
    service.setConstraintDetails(
        List.of(Map.of("name", "col1", "definition", "col1", "symbol", "ol")));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("name should be - unique, primary key, foreign key or check");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  private void assertThatItsValid(OracleDBResponse result) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Alter query executed successfully");
  }
}
