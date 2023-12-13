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
  @Mock private DatabaseClient databaseClient;
  @Mock private DatabaseConnection connection; 
  @Mock private Statement statementMock;
  private CreateDatabaseService service;

  @BeforeEach
  public void init() throws SQLException {
    service = new CreateDatabaseService();
    service.setDatabaseName("test7database");
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class),any(String.class))).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
  }

  @DisplayName("Should create database")
  @Test
  void shouldCreateDatabaseWhenExecute() throws SQLException {
    // given
    when(statementMock.executeUpdate(anyString())).thenReturn(1);
    // When
    PostgreSQLResponse result = service.invoke(databaseClient,connection,"databaseName");
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
    assertThatThrownBy(() -> service.invoke(databaseClient,connection,"databaseName"))
        // Then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("database \"alpha74081\" already exists");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }
}
