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
import java.util.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteDataService implements OracleDBRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDataService.class);
  @NotBlank private String databaseName;
  @NotBlank private String tableName;
  @NotEmpty private Map<String, Object> filters;
  private List<Map<String, String>> orderBy;
  private Integer limit;

  @Override
  public OracleDBResponse invoke(Connection connection) throws SQLException {
    QueryResponse<String> queryResponse;
    try {
      String deleteQuery = deleteRowsQuery(tableName, filters, orderBy, limit);
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
      Integer limit) {
    String whereClause = ConstructWhereClause.getWhereClause(filters);
    if (whereClause == null || whereClause.isBlank())
      LOGGER.debug("WHERE clause is empty, this will delete all row/s from the table");
    String orderByClause = "";
    if (orderBy != null && !orderBy.isEmpty()) {
      String orderByStmt = generateSQLOrderByClause(orderBy);
      if (!orderByStmt.isBlank()) orderByClause = " ORDER BY " + orderByStmt;
    }
    String deleteQuery = "DELETE FROM " + tableName + "\n";
    deleteQuery += " WHERE rowid IN ( SELECT ri FROM ( SELECT rowid as ri FROM " + tableName + "\n";
    if (whereClause != null && !whereClause.isBlank()) deleteQuery += whereClause + "\n";
    if (!orderByClause.isBlank()) deleteQuery += orderByClause + "\n";
    deleteQuery += " )";
    if (limit != null && limit > 0) {
      deleteQuery += " WHERE rownum <= " + limit;
    }
    deleteQuery += ")";
    return deleteQuery;
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
    DeleteDataService that = (DeleteDataService) o;
    return Objects.equals(databaseName, that.databaseName)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(filters, that.filters)
        && Objects.equals(orderBy, that.orderBy)
        && Objects.equals(limit, that.limit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(databaseName, tableName, filters, orderBy, limit);
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
        + ", limit="
        + limit
        + '}';
  }
}
