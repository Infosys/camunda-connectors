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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateTableService implements PostgreSQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateTableService.class);
  private static final String CONSTRAINTS = "constraints";
  private String databaseName;
  private String tableName;

  private List<Map<String, Object>> columnsList;

  @Override
  public PostgreSQLResponse invoke(Connection connection) throws SQLException {
    QueryResponse<String> queryResponse;
    try {
      String columns = getColumns(columnsList);
      if (columns.isBlank()) {
        String errMSg =
            "Invalid 'columnsList', It should be a list of maps for "
                + "column, with keys: 'colName', 'dataType' and optional 'constraints'";
        LOGGER.error(errMSg);
        throw new RuntimeException(errMSg);
      }
      String queryString = "CREATE TABLE " + tableName + " (" + columns + ")";
      LOGGER.info("Create Table Query: {}", queryString);
      createTable(connection, queryString);
      queryResponse = new QueryResponse<>("Table '" + tableName + "' created successfully");
      LOGGER.info("CreateTableQueryStatus: {}", queryResponse.getResponse());
    } catch (SQLException sqlException) {
      LOGGER.error(sqlException.getMessage());
      throw sqlException;
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

  private void createTable(Connection conn, String createTableQuery) throws SQLException {
    try (Statement st = conn.createStatement()) {
      st.execute(createTableQuery);
    }
  }

  private String getColumns(List<Map<String, Object>> columnsList) {
    StringBuilder columns = new StringBuilder();
    boolean first = true;
    for (Map<String, Object> colMap : columnsList) {
      if (colMap != null && !colMap.isEmpty()) {
        Map<String, Object> columnDetails = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        columnDetails.putAll(colMap);
        String columnNameStr = columnDetails.getOrDefault("colName", "").toString();
        String dataTypeStr = columnDetails.getOrDefault("dataType", "").toString();
        if (columnNameStr.isBlank() || dataTypeStr.isBlank()) {
          String errMSg = "colName or dataType can't be null or empty";
          LOGGER.error(errMSg);
          throw new RuntimeException(errMSg);
        }
        if (columnDetails.get(CONSTRAINTS) != null) {
          StringBuilder constraints = new StringBuilder();
          if (columnDetails.get(CONSTRAINTS) instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> columnConstraintsList = (List<String>) columnDetails.get(CONSTRAINTS);
            for (String constraint : columnConstraintsList) {
              if (constraint != null && !constraint.isBlank())
                constraints.append(" ").append(constraint);
            }
          } else if (columnDetails.get(CONSTRAINTS) instanceof String) {
            constraints.append(" ").append(columnDetails.get(CONSTRAINTS));
          } else {
            String errMSg =
                "Invalid constraint type '"
                    + columnDetails.get(CONSTRAINTS)
                    + "' passed in column"
                    + columnNameStr;
            LOGGER.error(errMSg);
            throw new RuntimeException(errMSg);
          }
          if (first) {
            columns
                .append(columnNameStr)
                .append(" ")
                .append(dataTypeStr)
                .append(" ")
                .append(constraints);
            first = false;
          } else {
            columns
                .append(",")
                .append(columnNameStr)
                .append(" ")
                .append(dataTypeStr)
                .append(" ")
                .append(constraints);
          }
        } else {
          if (first) {
            columns.append(columnNameStr).append(" ").append(dataTypeStr);
            first = false;
          } else {
            columns.append(",").append(columnNameStr).append(" ").append(dataTypeStr);
          }
        }
      }
    }
    return columns.toString();
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

  public List<Map<String, Object>> getColumnsList() {
    return columnsList;
  }

  public void setColumnsList(List<Map<String, Object>> columnsList) {
    this.columnsList = columnsList;
  }
}
