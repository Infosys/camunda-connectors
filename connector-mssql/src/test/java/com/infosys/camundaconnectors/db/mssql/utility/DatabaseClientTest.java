/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.db.mssql.model.request.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseClientTest {
  private final DatabaseConnection connection = new DatabaseConnection();
  private String databaseName;
  @Mock private DatabaseClient databaseClient;
  @Mock private Connection connectionMock;

  @BeforeEach
  void init() {
    connection.setHost("HOSTNAME");
    connection.setPort("1433");
    connection.setUsername("USERNAME");
    connection.setPassword("PASSWORD");
    databaseName = "xe";
  }

  @DisplayName("Should create connection Object for database")
  @Test
  void shouldCreateConnectionObject() throws SQLException {
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class), any(String.class)))
        .thenReturn(connectionMock);
    when(connectionMock.isValid(anyInt())).thenReturn(true);
    // when
    Connection conn = databaseClient.getConnectionObject(connection, databaseName);
    // then
    assertThat(conn.isValid(1)).isTrue();
    conn.close();
  }

  @DisplayName("Should throw error as port is invalid")
  @Test
  void shouldThrowErrorConnectionAsPortIsInvalid() {
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class), any(String.class)))
        .thenThrow(new RuntimeException("Invalid number format for port number"));
    // given
    connection.setPort("iota");
    // when
    assertThatThrownBy(() -> databaseClient.getConnectionObject(connection, databaseName))
        // then
        .hasMessageContaining("Invalid number format for port number");
  }

  @DisplayName("Should throw error as hostname is invalid")
  @Test
  void shouldThrowErrorConnection() {
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class), any(String.class)))
        .thenThrow(new RuntimeException("Connection could not establish the connection"));
    // given
    connection.setHost("localhost");
    // when
    assertThatThrownBy(() -> databaseClient.getConnectionObject(connection, databaseName))
        // then
        .hasMessageContaining("could not establish the connection");
  }

  @DisplayName("Should throw error as password is invalid")
  @Test
  void shouldThrowErrorInvalidCredential() {
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class), any(String.class)))
        .thenThrow(new RuntimeException("invalid username/password"));
    // given
    connection.setUsername("bot");
    // when
    assertThatThrownBy(() -> databaseClient.getConnectionObject(connection, databaseName))
        // then
        .hasMessageContaining("invalid username/password");
  }

  @DisplayName("Should throw error as password is invalid")
  @Test
  void shouldThrowErrorInvalidCredential2() {
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class), any(String.class)))
        .thenThrow(new RuntimeException("invalid username/password"));
    // given
    connection.setPassword("bot");
    // when
    assertThatThrownBy(() -> databaseClient.getConnectionObject(connection, databaseName))
        // then
        .hasMessageContaining("invalid username/password");
  }

  @DisplayName("Should throw error as database name is invalid")
  @Test
  void shouldThrowErrorInvalidDB() {
    when(databaseClient.getConnectionObject(any(DatabaseConnection.class), any(String.class)))
        .thenThrow(new RuntimeException("InvalidDatabase: Database doesn't exist"));
    // given
    databaseName = "Alpha74087";
    // when
    assertThatThrownBy(() -> databaseClient.getConnectionObject(connection, databaseName))
        // then
        .hasMessageContaining("InvalidDatabase: Database doesn't exist");
  }
}
