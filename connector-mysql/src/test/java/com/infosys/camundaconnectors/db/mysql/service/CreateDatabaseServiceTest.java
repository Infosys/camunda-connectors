/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.mysql.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.db.mysql.model.response.MySQLResponse;
import com.infosys.camundaconnectors.db.mysql.model.response.QueryResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
class CreateDatabaseServiceTest {
  @Mock private Connection connectionMock;
  @Mock private Statement statementMock;
  private CreateDatabaseService service;

  @BeforeEach
  public void init() throws SQLException {
    service = new CreateDatabaseService();
    service.setDatabaseName("test5database");
    when(connectionMock.createStatement()).thenReturn(statementMock);
  }

  @DisplayName("Should create database")
  @Test
  void shouldCreateDatabaseWhenExecute() throws SQLException {
    // given
    when(statementMock.executeUpdate(anyString())).thenReturn(1);
    // When
    MySQLResponse result = service.invoke(connectionMock);
    // Then
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    Mockito.verify(statementMock, Mockito.times(1)).executeUpdate(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("created successfully");
  }

  @DisplayName("Should throw error as database already exists")
  @Test
  void shouldThrowErrorDatabaseAlreadyExists() throws SQLException {
    // given
    when(statementMock.executeUpdate(anyString()))
        .thenThrow(new RuntimeException("database \"alpha74081\" " + "already exists"));
    // When
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("database \"alpha74081\" already exists");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }
}
