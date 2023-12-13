/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.service;

import com.infosys.camundaconnectors.db.mssql.model.request.DatabaseConnection;
import com.infosys.camundaconnectors.db.mssql.model.request.MSSQLRequestData;
import com.infosys.camundaconnectors.db.mssql.model.response.MSSQLResponse;
import com.infosys.camundaconnectors.db.mssql.model.response.QueryResponse;
import com.infosys.camundaconnectors.db.mssql.utility.ConstructWhereClause;
import com.infosys.camundaconnectors.db.mssql.utility.DatabaseClient;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteDataService implements MSSQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDataService.class);
  @NotBlank private String databaseName;
  @NotBlank private String tableName;
  @NotEmpty private Map<String, Object> filters;
  private List<Map<String, String>> orderBy;
  private Integer top;

  @Override
  public MSSQLResponse invoke(DatabaseClient databaseClient,DatabaseConnection databaseConnection,String DatabaseName) throws SQLException {
	  final Connection connection = databaseClient.getConnectionObject(databaseConnection, databaseName);
	  QueryResponse<String> queryResponse;
    try {
      String deleteQuery = deleteRowsQuery(tableName, filters, orderBy, top);
      LOGGER.info("Delete query: {}", deleteQuery);
      int rowsDeleted;
      try (Statement st = connection.createStatement()) {
        connection.setAutoCommit(false);
        rowsDeleted = st.executeUpdate(deleteQuery);
        connection.commit();
        queryResponse = new QueryResponse<>(rowsDeleted + " row(s) deleted successfully");
        LOGGER.info("DeleteDataQueryStatus: {}", queryResponse.getResponse());
      }
    } catch (SQLException sqlException) {
      LOGGER.error(sqlException.getMessage());
      if (sqlException.getMessage().contains("Invalid object name"))
        throw new RuntimeException("Table '" + tableName + "' doesn't exist", sqlException);
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

  private String deleteRowsQuery(
      String tableName,
      Map<String, Object> filters,
      List<Map<String, String>> orderBy,
      Integer top) {
    String whereClause = ConstructWhereClause.getWhereClause(filters);
    if (whereClause == null || whereClause.isBlank())
      LOGGER.debug("WHERE clause is empty, this will delete all row/s from the table");
    String orderByClause = "";
    if (orderBy != null && !orderBy.isEmpty()) {
      String orderByStmt = generateSQLOrderByClause(orderBy);
      if (!orderByStmt.isBlank()) orderByClause = " ORDER BY " + orderByStmt;
    }
    String topStr = "";
    if (top != null && top > 0) topStr = "TOP " + top;
    return String.format(
        "DELETE T FROM ( SELECT %s * FROM %s%s%s) AS T",
        topStr, tableName, whereClause, orderByClause);
  }

  private String generateSQLOrderByClause(List<Map<String, String>> sortBy) {
    List<String> partialOrderBy = new ArrayList<>();
    for (Map<String, String> sortField : sortBy) {
      String fieldName = sortField.get("sortOn");
      String order = sortField.getOrDefault("order", "asc");
      if (fieldName != null && !fieldName.isBlank()) {
        if (order.toLowerCase().matches("d|desc|descending")) {
          partialOrderBy.add(fieldName + " DESC");
        } else partialOrderBy.add(fieldName + " ASC");
      }
    }
    return String.join(", ", partialOrderBy);
  }

  @Override
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

  public List<Map<String, String>> getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(List<Map<String, String>> orderBy) {
    this.orderBy = orderBy;
  }

  public Integer getTop() {
    return top;
  }

  public void setTop(Integer top) {
    this.top = top;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DeleteDataService that = (DeleteDataService) o;
    return Objects.equals(databaseName, that.databaseName)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(filters, that.filters)
        && Objects.equals(orderBy, that.orderBy)
        && Objects.equals(top, that.top);
  }

  @Override
  public int hashCode() {
    return Objects.hash(databaseName, tableName, filters, orderBy, top);
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
        + ", orderBy="
        + orderBy
        + ", top="
        + top
        + '}';
  }
}
