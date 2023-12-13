/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mysql.service;

import com.infosys.camundaconnectors.db.mysql.model.request.DatabaseConnection;
import com.infosys.camundaconnectors.db.mysql.model.request.MySQLRequestData;
import com.infosys.camundaconnectors.db.mysql.model.response.MySQLResponse;
import com.infosys.camundaconnectors.db.mysql.model.response.QueryResponse;
import com.infosys.camundaconnectors.db.mysql.utility.ConstructWhereClause;
import com.infosys.camundaconnectors.db.mysql.utility.DatabaseClient;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateDataService implements MySQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataService.class);
  @NotBlank private String databaseName;
  @NotBlank private String tableName;

  @NotEmpty(message = "updateMap can't be null or empty")
  private Map<String, Object> updateMap;

  private Map<String, Object> filters;
  private List<Map<String, String>> orderBy;
  private Integer limit;

  @Override
  public MySQLResponse invoke(DatabaseClient databaseClient,DatabaseConnection databaseConnection,String DatabaseName) throws SQLException {
	final Connection connection = databaseClient.getConnectionObject(databaseConnection, databaseName);
	QueryResponse<String> queryResponse;
    try {
      String updateQuery = updateRowsQuery(tableName, updateMap, filters, orderBy, limit);
      LOGGER.info("Update query: {}", updateQuery);
      int rowsUpdated;
      try (Statement st = connection.createStatement()) {
        connection.setAutoCommit(false);
        rowsUpdated = st.executeUpdate(updateQuery);
        connection.commit();
        queryResponse = new QueryResponse<>(rowsUpdated + " row(s) updated successfully");
        LOGGER.info("UpdateDataQueryStatus: {}", queryResponse.getResponse());
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

  private String updateRowsQuery(
      String tableName,
      Map<String, Object> updateMap,
      Map<String, Object> filters,
      List<Map<String, String>> orderBy,
      Integer limit) {
    String whereClause = ConstructWhereClause.getWhereClause(filters);
    if (whereClause == null || whereClause.isBlank() || filters == null || filters.isEmpty())
      LOGGER.debug("WHERE clause is empty, this will update all rows in the table");
    StringBuilder setClause = new StringBuilder();
    List<String> columns = new ArrayList<>(updateMap.keySet());
    for (String column : columns) {
      Object colValue = updateMap.get(column);
      if (colValue instanceof String) {
        setClause.append(column).append("=").append("'").append(colValue.toString()).append("', ");
      } else {
        setClause.append(column).append("=").append(colValue).append(", ");
      }
    }
    String setClauseStr = setClause.toString().strip();
    if (setClauseStr.length() > 1)
      setClauseStr = setClauseStr.substring(0, setClauseStr.length() - 1);
    String orderByClause = "";
    if (orderBy != null && !orderBy.isEmpty()) {
      String orderByStmt = generateSQLOrderByClause(orderBy);
      if (!orderByStmt.isBlank()) orderByClause = " ORDER BY " + orderByStmt;
    }
    String query =
        String.format(
            "UPDATE %s SET %s %s %s", tableName, setClauseStr, whereClause, orderByClause);
    if (limit != null && limit > 0) query += " LIMIT " + limit;
    return query;
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

  public Map<String, Object> getUpdateMap() {
    return updateMap;
  }

  public void setUpdateMap(Map<String, Object> updateMap) {
    this.updateMap = updateMap;
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

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }
}
