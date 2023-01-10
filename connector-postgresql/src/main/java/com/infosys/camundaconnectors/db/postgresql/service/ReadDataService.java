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
import java.sql.*;
import java.util.*;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadDataService implements PostgreSQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReadDataService.class);
  @NotBlank private String databaseName;
  @NotBlank private String tableName;
  private List<String> columnNames;
  private Map<String, Object> filters;
  private List<Map<String, String>> orderBy;
  private Integer limit;

  @Override
  public PostgreSQLResponse invoke(Connection connection) throws SQLException {
    QueryResponse<List<Map<String, Object>>> queryResponse;
    try {
      String readDataQuery = selectRowsQuery(tableName, columnNames, filters, orderBy, limit);
      LOGGER.info("Read query: {}", readDataQuery);
      try (Statement st = connection.createStatement()) {
        ResultSet rs = st.executeQuery(readDataQuery);
        queryResponse = new QueryResponse<>(convertResultSetToList(rs));
        LOGGER.info("ReadDataQueryStatus: {}", queryResponse.getResponse());
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

  private String selectRowsQuery(
      String tableName,
      List<String> columnNames,
      Map<String, Object> filters,
      List<Map<String, String>> orderBy,
      Integer limit) {
    String whereClause = ConstructWhereClause.getWhereClause(filters);
    if (whereClause == null || whereClause.isBlank() || filters == null || filters.isEmpty())
      LOGGER.debug("WHERE clause is empty, this will return all rows in the table");
    String orderByClause = "";
    if (orderBy != null && !orderBy.isEmpty()) {
      String orderByStmt = generateSQLOrderByClause(orderBy);
      if (!orderByStmt.isBlank()) orderByClause = " ORDER BY " + orderByStmt;
    }
    String columnString = "*";
    if (columnNames != null && !columnNames.isEmpty())
      columnString = String.join(", ", columnNames);
    String query =
        String.format(
            "SELECT %s FROM %s %s %s", columnString, tableName, whereClause, orderByClause);
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

  public List<Map<String, Object>> convertResultSetToList(ResultSet rs) throws SQLException {
    ResultSetMetaData resultSetMetaData = rs.getMetaData();
    int columnSize = resultSetMetaData.getColumnCount();
    List<Map<String, Object>> rowsList = new ArrayList<>();
    while (rs.next()) {
      Map<String, Object> row = new HashMap<>(columnSize);
      for (int i = 1; i <= columnSize; ++i) {
        row.put(resultSetMetaData.getColumnName(i), rs.getObject(i));
      }
      rowsList.add(row);
    }

    return rowsList;
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

  public List<String> getColumnNames() {
    return columnNames;
  }

  public void setColumnNames(List<String> columnNames) {
    this.columnNames = columnNames;
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
    ReadDataService that = (ReadDataService) o;
    return Objects.equals(databaseName, that.databaseName)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(columnNames, that.columnNames)
        && Objects.equals(filters, that.filters)
        && Objects.equals(orderBy, that.orderBy)
        && Objects.equals(limit, that.limit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(databaseName, tableName, columnNames, filters, orderBy, limit);
  }

  @Override
  public String toString() {
    return "ReadDataService{"
        + "databaseName='"
        + databaseName
        + '\''
        + ", tableName='"
        + tableName
        + '\''
        + ", columnNames="
        + columnNames
        + ", filters="
        + filters
        + ", orderBy="
        + orderBy
        + ", limit="
        + limit
        + '}';
  }
}
