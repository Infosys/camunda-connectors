/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.oracle.model.request;

import com.infosys.camundaconnectors.db.oracle.model.response.OracleDBResponse;
import com.infosys.camundaconnectors.db.oracle.utility.DatabaseClient;
import io.camunda.connector.api.annotation.Secret;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class OracleDBRequest<T extends OracleDBRequestData> {
  @NotNull @Valid @Secret private DatabaseConnection databaseConnection;
  @NotBlank private String operation;
  @Valid @NotNull private T data;

  public OracleDBResponse invoke(DatabaseClient databaseClient) throws SQLException {
    Connection connection =
        databaseClient.getConnectionObject(databaseConnection, data.getDatabaseName());
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
    OracleDBRequest<?> that = (OracleDBRequest<?>) o;
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
    return "OracleDBRequest{"
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
