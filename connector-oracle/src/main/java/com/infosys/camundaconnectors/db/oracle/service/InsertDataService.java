/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.oracle.service;

import com.infosys.camundaconnectors.db.oracle.model.request.OracleDBRequestData;
import com.infosys.camundaconnectors.db.oracle.model.response.OracleDBResponse;
import com.infosys.camundaconnectors.db.oracle.model.response.QueryResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertDataService implements OracleDBRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(InsertDataService.class);
  @NotBlank private String databaseName;
  @NotBlank private String tableName;
  @NotEmpty private List<Map<String, Object>> dataToInsert;

  @Override
  public OracleDBResponse invoke(Connection connection) throws SQLException {
    QueryResponse<String> queryResponse;
    try {
      if (dataToInsert.get(0) == null || dataToInsert.get(0).isEmpty()) {
        String errMsg = "Invalid dataToInsert, can not be empty or null";
        LOGGER.error(errMsg);
        throw new RuntimeException(errMsg);
      }
      List<LinkedHashMap<String, Object>> dataSetList =
          dataToInsert.stream().map(LinkedHashMap::new).collect(Collectors.toList());
      String insertParameterizedQuery = insertDataParameterizedQuery(tableName, dataSetList);
      LOGGER.info("Insert Parameterized Query: {}", insertParameterizedQuery);
      int rowsInserted = executeQuery(connection, insertParameterizedQuery, dataSetList);
      queryResponse = new QueryResponse<>(rowsInserted + " row(s) inserted successfully");
      LOGGER.info("InsertDataQueryStatus: {}", queryResponse.getResponse());
    } catch (SQLException e) {
      LOGGER.error("Unable to insert data into the table: {}", e.getMessage());
      throw new RuntimeException("Unable to insert data into the table: " + e.getMessage());
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

  private String insertDataParameterizedQuery(
      String tableName, List<LinkedHashMap<String, Object>> dataToInsert) {
    try {
      // Construct Parameterized Query
      StringBuilder insertQuery = new StringBuilder("INSERT ALL\n");
      for (Map<String, Object> insertData : dataToInsert) {
        // Get Columns Name from First Element
        List<String> columnNamesList = new ArrayList<>(insertData.keySet());
        String columnNames =
            columnNamesList.stream().map(String::valueOf).collect(Collectors.joining(", "));
        StringBuilder instQuery = new StringBuilder();
        for (int i = 0; i < columnNamesList.size(); ++i) {
          if (i != columnNamesList.size() - 1) instQuery.append("?, ");
          else instQuery.append("?");
        }
        insertQuery
            .append(" INTO ")
            .append(tableName)
            .append(" (")
            .append(columnNames)
            .append(") VALUES (")
            .append(instQuery)
            .append(") ");
      }
      insertQuery.append(" SELECT 1 FROM DUAL");
      return insertQuery.toString();
    } catch (Exception ee) {
      LOGGER.error("InvalidDataToInsert: {}", ee.getMessage());
      throw new RuntimeException("Invalid dataToInsert: " + ee.getMessage());
    }
  }

  private int executeQuery(
      Connection connection, String insertQuery, List<LinkedHashMap<String, Object>> dataSetList)
      throws SQLException {
    // Create Prepared Statement and Populate the row data
    int rowsInserted = 0;
    try (PreparedStatement prepareStatement = connection.prepareStatement(insertQuery)) {
      connection.setAutoCommit(false);
      // Extract row data from maps
      int paramCount = 0;
      for (Map<String, Object> rowMap : dataSetList) {
        List<String> columnNamesList = new ArrayList<>(rowMap.keySet());
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
