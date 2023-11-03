/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.postgresql.service;

import com.infosys.camundaconnectors.db.postgresql.model.request.PostgreSQLRequestData;
import com.infosys.camundaconnectors.db.postgresql.model.response.PostgreSQLResponse;
import com.infosys.camundaconnectors.db.postgresql.model.response.QueryResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertDataService implements PostgreSQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(InsertDataService.class);
  private String databaseName;
  private String tableName;
  private List<Map<String, Object>> dataToInsert;

  @Override
  public PostgreSQLResponse invoke(Connection connection) throws SQLException {
    QueryResponse<String> queryResponse;
    try {
      if (dataToInsert == null || dataToInsert.isEmpty()) {
        String errMsg = "dataToInsert can not be null or empty";
        LOGGER.error(errMsg);
        throw new RuntimeException(errMsg);
      }
      if (dataToInsert.get(0) == null || dataToInsert.get(0).isEmpty()) {
        String errMsg = "dataToInsert first element can not be null or empty";
        LOGGER.error(errMsg);
        throw new RuntimeException(errMsg);
      }
      List<String> columnNamesList = new ArrayList<>(dataToInsert.get(0).keySet());
      String insertParameterizedQuery =
          insertDataParameterizedQuery(tableName, dataToInsert, columnNamesList);
      LOGGER.info("Insert Parameterized Query: {}", insertParameterizedQuery);
      int rowsInserted =
          executeQuery(connection, insertParameterizedQuery, dataToInsert, columnNamesList);
      queryResponse = new QueryResponse<>(rowsInserted + " row(s) inserted successfully");
      LOGGER.info("InsertDataQueryStatus: {}", queryResponse.getResponse());
      return queryResponse;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    } finally {
      try {
        connection.close();
        LOGGER.debug("Connection closed");
      } catch (SQLException e) {
        LOGGER.warn("Error while closing the database connection");
      }
    }
  }

  private String insertDataParameterizedQuery(
      String tableName, List<Map<String, Object>> dataToInsert, List<String> columnNamesList) {
    try {
      // Get Columns Name from First Element
      String columnNames =
          columnNamesList.stream().map(String::valueOf).collect(Collectors.joining(", "));
      // Construct Parameterized Query
      StringBuilder insertQuery = new StringBuilder();
      insertQuery
          .append("INSERT INTO ")
          .append(tableName)
          .append(" (")
          .append(columnNames)
          .append(") VALUES ");
      for (int j = 0; j < dataToInsert.size(); ++j) {
        insertQuery.append("(");
        for (int i = 0; i < columnNamesList.size(); ++i) {
          if (i != columnNamesList.size() - 1) insertQuery.append("?, ");
          else insertQuery.append("?");
        }
        if (j != dataToInsert.size() - 1) insertQuery.append("), ");
        else insertQuery.append(")");
      }
      return insertQuery.toString();
    } catch (Exception ee) {
      LOGGER.error("InvalidDataToInsert: {}", ee.getMessage());
      throw new RuntimeException("Invalid dataToInsert: " + ee.getMessage());
    }
  }

  private int executeQuery(
      Connection connection,
      String insertQuery,
      List<Map<String, Object>> dataToInsert,
      List<String> columnNamesList)
      throws SQLException {
    // Create Prepared Statement and Populate the row data
    int rowsInserted = 0;
    try (PreparedStatement prepareStatement = connection.prepareStatement(insertQuery)) {
      connection.setAutoCommit(false);
      // Extract row data from maps
      int paramCount = 0;
      for (Map<String, Object> rowMap : dataToInsert) {
        for (String colName : columnNamesList) {
          prepareStatement.setObject(++paramCount, rowMap.getOrDefault(colName, null));
        }
      }
      rowsInserted = prepareStatement.executeUpdate();
      connection.commit();
    }
    return rowsInserted;
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

  public List<Map<String, Object>> getDataToInsert() {
    return dataToInsert;
  }

  public void setDataToInsert(List<Map<String, Object>> dataToInsert) {
    this.dataToInsert = dataToInsert;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InsertDataService that = (InsertDataService) o;
    return Objects.equals(databaseName, that.databaseName)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(dataToInsert, that.dataToInsert);
  }

  @Override
  public int hashCode() {
    return Objects.hash(databaseName, tableName, dataToInsert);
  }

  @Override
  public String toString() {
    return "InsertDataService{"
        + "databaseName='"
        + databaseName
        + '\''
        + ", tableName='"
        + tableName
        + '\''
        + ", dataToInsert="
        + dataToInsert
        + '}';
  }
}
