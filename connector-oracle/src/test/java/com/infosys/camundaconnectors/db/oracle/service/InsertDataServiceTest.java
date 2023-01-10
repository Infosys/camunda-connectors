/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.oracle.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.db.oracle.model.response.OracleDBResponse;
import com.infosys.camundaconnectors.db.oracle.model.response.QueryResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
  @Mock private PreparedStatement psMock;
  private InsertDataService service;

  @BeforeEach
  void init() {
    service = new InsertDataService();
    service.setDatabaseName("XE");
    service.setTableName("testRio");
    service.setDataToInsert(
        List.of(
            Map.of(
                "personid",
                1,
                "lastname",
                "Acro",
                "firstname",
                "Alpha",
                "address",
                "St." + " Peter Avenue",
                "city",
                "New York"),
            Map.of(
                "personid",
                2,
                "address",
                "St. Louis Avenue",
                "city",
                "New Jersey",
                "lastname",
                "Ben",
                "firstname",
                "Bob")));
  }

  @DisplayName("Should throw error as dataToInsert is invalid")
  @Test
  void shouldThrowErrorInvalidDataToInsert() {
    // given
    service.setDataToInsert(List.of(Map.of()));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid dataToInsert, can not be empty or null");
  }

  @DisplayName("Should throw error as table data is invalid")
  @Test
  void shouldThrowErrorInvalidTableData() throws SQLException {
    // given
    service.setDataToInsert(List.of(Map.of("PersonID", "op")));
    Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);
    Mockito.when(psMock.executeUpdate()).thenThrow(new SQLException("'op' invalid number"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
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
    Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);
    Mockito.when(psMock.executeUpdate())
        .thenThrow(new SQLException("table or view does not exist"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("table or view does not exist");
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
                "PersonID",
                4,
                "LastName",
                "Acro",
                "FirstName",
                "Alpha",
                "Address",
                "St. Peter Avenue",
                "Tower",
                "New York")));
    Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);
    Mockito.when(psMock.executeUpdate())
        .thenThrow(new SQLException("\"TOWER\": invalid identifier"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("\"TOWER\": invalid identifier");
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should insert 2 rows in table")
  @Test
  void shouldInsertDataWhenExecute() throws SQLException {
    // given input
    Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);
    Mockito.when(psMock.executeUpdate()).thenReturn(2);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
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
                "Acro",
                "FirstName",
                "Alpha",
                "Address",
                "St. Peter Avenue")));

    Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);
    Mockito.when(psMock.executeUpdate()).thenReturn(1);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
    // Then
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  @DisplayName("Should insert row with minimum data in table")
  @Test
  void shouldInsertDataWhenExecute3() throws SQLException {
    // given
    service.setDataToInsert(
        List.of(
            Map.of("PersonID", 5, "firstname", "last"),
            Map.of("lastname", "Hobo", "PersonID", 7, "firstname", "oppo")));
    Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);
    Mockito.when(psMock.executeUpdate()).thenReturn(2);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
    // Then
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  private void assertThatItsValid(OracleDBResponse result) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("inserted successfully");
  }
}
