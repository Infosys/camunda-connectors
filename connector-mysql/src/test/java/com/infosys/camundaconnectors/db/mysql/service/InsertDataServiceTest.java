/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mysql.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.db.mysql.model.response.MySQLResponse;
import com.infosys.camundaconnectors.db.mysql.model.response.QueryResponse;
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
public class InsertDataServiceTest {
  @Mock private Connection connectionMock;
  @Mock private PreparedStatement psMock;
  private InsertDataService service;

  @BeforeEach
  void init() throws SQLException {
    service = new InsertDataService();
    service.setDatabaseName("test55database");
    service.setTableName("trialtable");
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
                "St. " + "Peter Avenue",
                "City",
                "New York"),
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
                "New Jersey")));
    Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);
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
    Mockito.when(psMock.executeUpdate())
        .thenThrow(new SQLException("Incorrect integer value: 'op' for column 'personid'"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Incorrect integer value: 'op' for column 'personid'");
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error as table name is invalid")
  @Test
  void shouldThrowErrorInvalidTable() throws SQLException {
    // given
    Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);
    Mockito.when(psMock.executeUpdate())
        .thenThrow(new SQLException("Table 'test7database.xia' doesn't exist"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Table 'test7database.xia' doesn't exist");
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
    Mockito.when(psMock.executeUpdate()).thenThrow(new SQLException("Unknown column"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Unknown column");
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
    MySQLResponse result = service.invoke(connectionMock);
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
    MySQLResponse result = service.invoke(connectionMock);
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
    MySQLResponse result = service.invoke(connectionMock);
    // Then
    Mockito.verify(psMock, Mockito.times(1)).executeUpdate();
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThatItsValid(result);
  }

  private void assertThatItsValid(MySQLResponse result) {
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("inserted successfully");
  }
}
