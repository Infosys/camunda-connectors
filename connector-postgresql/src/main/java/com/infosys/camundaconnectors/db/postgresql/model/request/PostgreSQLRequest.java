/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.postgresql.model.request;

import com.infosys.camundaconnectors.db.postgresql.model.response.PostgreSQLResponse;
import com.infosys.camundaconnectors.db.postgresql.utility.DatabaseClient;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class PostgreSQLRequest<T extends PostgreSQLRequestData> {
  private DatabaseConnection databaseConnection;
  private String operation;
  private T data;

  public PostgreSQLResponse invoke(DatabaseClient databaseClient) throws SQLException {
    Connection connection;
    if (operation.equalsIgnoreCase("postgresql.create-database"))
      connection = databaseClient.getConnectionObject(databaseConnection, "template1");
    else
      connection = databaseClient.getConnectionObject(databaseConnection, data.getDatabaseName());
    return data.invoke(connection);
  }

  public DatabaseConnection getDatabaseConnection() {
    return databaseConnection;
  }

  public void setDatabaseConnection(DatabaseConnection databaseConnection) {
    this.databaseConnection = databaseConnection;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PostgreSQLRequest<?> that = (PostgreSQLRequest<?>) o;
    return Objects.equals(databaseConnection, that.databaseConnection)
        && Objects.equals(operation, that.operation)
        && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(databaseConnection, operation, data);
  }

  @Override
  public String toString() {
    return "PostgreSQLRequest{"
        + ", databaseConnection="
        + databaseConnection
        + ", operation='"
        + operation
        + '\''
        + ", data="
        + data
        + '}';
  }
}
