/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.oracle.service;

import com.infosys.camundaconnectors.db.oracle.model.request.OracleDBRequestData;
import com.infosys.camundaconnectors.db.oracle.model.response.OracleDBResponse;
import com.infosys.camundaconnectors.db.oracle.model.response.QueryResponse;
import com.infosys.camundaconnectors.db.oracle.utility.ConstructWhereClause;
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

public class UpdateDataService implements OracleDBRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataService.class);
  @NotBlank private String databaseName;
  @NotBlank private String tableName;

  @NotEmpty(message = "updateMap can't be null or empty")
  private Map<String, Object> updateMap;

  private Map<String, Object> filters;
  private List<Map<String, String>> orderBy;
  private Integer limit;

  @Override
  public OracleDBResponse invoke(Connection connection) throws SQLException {
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
    if (whereClause == null || whereClause.isBlank())
      LOGGER.debug("WHERE clause is empty, this will update all rows in the table");
    StringBuilder setClause = new StringBuilder();
    List<String> columns = new ArrayList<>(updateMap.keySet());
    for (String column : columns) {
      Object colValue = updateMap.get(column);
      setClause.append(column).append("=");
      if (colValue instanceof String) {
        setClause.append("'").append(colValue).append("', ");
      } else {
        setClause.append(colValue).append(", ");
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
    String updateQuery = "UPDATE " + tableName + "\n";
    updateQuery += " SET " + setClauseStr + "\n";
    updateQuery += " WHERE rowid IN ( SELECT ri FROM ( SELECT rowid as ri FROM " + tableName + "\n";
    if (whereClause != null && !whereClause.isBlank()) updateQuery += whereClause + "\n";
    if (!orderByClause.isBlank()) updateQuery += orderByClause + "\n";
    updateQuery += " )";
    if (limit != null && limit > 0) {
      updateQuery += " WHERE rownum <= " + limit;
    }
    updateQuery += ")";
    return updateQuery;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UpdateDataService that = (UpdateDataService) o;
    return Objects.equals(databaseName, that.databaseName)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(updateMap, that.updateMap)
        && Objects.equals(filters, that.filters)
        && Objects.equals(orderBy, that.orderBy)
        && Objects.equals(limit, that.limit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(databaseName, tableName, updateMap, filters, orderBy, limit);
  }

  @Override
  public String toString() {
    return "UpdateDataService{"
        + "databaseName='"
        + databaseName
        + '\''
        + ", tableName='"
        + tableName
        + '\''
        + ", updateMap="
        + updateMap
        + ", filters="
        + filters
        + ", orderBy="
        + orderBy
        + ", limit="
        + limit
        + '}';
  }
}
