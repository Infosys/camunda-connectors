/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.postgresql.service;

import com.infosys.camundaconnectors.db.postgresql.model.request.PostgreSQLRequestData;
import com.infosys.camundaconnectors.db.postgresql.model.response.PostgreSQLResponse;
import com.infosys.camundaconnectors.db.postgresql.model.response.QueryResponse;
import com.infosys.camundaconnectors.db.postgresql.utility.ConstructWhereClause;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteDataService implements PostgreSQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDataService.class);
  private String databaseName;
  private String tableName;

  private Map<String, Object> filters;

  @Override
  public PostgreSQLResponse invoke(Connection connection) throws SQLException {
    QueryResponse<String> queryResponse;
    try {
      String deleteQuery = deleteRowsQuery(tableName, filters);
      LOGGER.info("Delete query: {}", deleteQuery);
      int rowsDeleted = 0;
      try (Statement st = connection.createStatement()) {
        connection.setAutoCommit(false);
        rowsDeleted = st.executeUpdate(deleteQuery);
        connection.commit();
        queryResponse = new QueryResponse<>(rowsDeleted + " row(s) deleted successfully");
        LOGGER.info("DeleteDataQueryStatus: {}", queryResponse.getResponse());
      }
    } catch (SQLException sqlException) {
      LOGGER.error(sqlException.getMessage());
      throw new RuntimeException(sqlException);
    } finally {
      try {
        connection.close();
        LOGGER.debug("Connection closed");
      } catch (SQLException e) {
        LOGGER.warn("Error while closing the database connection");
      }
    }
    return queryResponse;
  }

  private String deleteRowsQuery(String tableName, Map<String, Object> filters) {
    String whereClause = ConstructWhereClause.getWhereClause(filters);
    if (whereClause == null || whereClause.isBlank())
      LOGGER.debug("WHERE clause is empty, this will delete all row/s from the table");
    return String.format("DELETE FROM %s %s", tableName, whereClause);
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public Map<String, Object> getFilters() {
    return filters;
  }

  public void setFilters(Map<String, Object> filters) {
    this.filters = filters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DeleteDataService that = (DeleteDataService) o;
    return Objects.equals(databaseName, that.databaseName)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(filters, that.filters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(databaseName, tableName, filters);
  }

  @Override
  public String toString() {
    return "DeleteDataService{"
        + "databaseName='"
        + databaseName
        + '\''
        + ", tableName='"
        + tableName
        + '\''
        + ", filters="
        + filters
        + '}';
  }
}
