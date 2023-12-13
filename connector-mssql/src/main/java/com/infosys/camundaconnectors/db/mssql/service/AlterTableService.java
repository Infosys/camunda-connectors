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
import com.infosys.camundaconnectors.db.mssql.utility.DatabaseClient;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.constraints.*;

public class AlterTableService implements MSSQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(AlterTableService.class);
  @NotBlank private String databaseName;
  @NotBlank private String tableName;
  @NotBlank private String method;
  private String newTableName;
  private Map<String, String> newColumnDetail;
  private List<Map<String, String>> columnsDetails;
  private List<Map<String, String>> constraintDetails;
  private Map<String, String> modifyColumnsDetails;
  private List<String> dropConstraintsList;
  private List<String> dropColumnsList;
  private String constraintName;

  @Override
  public MSSQLResponse invoke(DatabaseClient databaseClient,DatabaseConnection databaseConnection,String DatabaseName) throws SQLException {
	  final Connection connection = databaseClient.getConnectionObject(databaseConnection, databaseName);
	  QueryResponse<String> queryResponse;
    try {
      String operation = method;
      method = method.replaceAll("[_\\s]", "");
      String alterTableQuery = "";
      if (method.equalsIgnoreCase("disableConstraint")) {
        alterTableQuery = disableConstraintQuery(tableName, constraintName);
      } else if (method.equalsIgnoreCase("enableConstraint")) {
        alterTableQuery = enableConstraintQuery(tableName, constraintName);
      } else if (method.equalsIgnoreCase("renameTable")) {
        alterTableQuery = renameTableQuery(tableName, newTableName);
      } else if (method.equalsIgnoreCase("renameColumn")) {
        alterTableQuery = renameColumnQuery(tableName, newColumnDetail);
      } else if (method.equalsIgnoreCase("addColumn")) {
        alterTableQuery = addColumnsQuery(tableName, columnsDetails);
      } else if (method.equalsIgnoreCase("addConstraint")) {
        alterTableQuery = addConstraintQuery(tableName, constraintDetails);
      } else if (method.equalsIgnoreCase("modifyColumn")) {
        alterTableQuery = modifyColumnQuery(tableName, modifyColumnsDetails);
      } else if (method.equalsIgnoreCase("dropColumn")) {
        alterTableQuery = dropColumnsListQuery(tableName, dropColumnsList);
      } else if (method.equalsIgnoreCase("dropConstraint")) {
        alterTableQuery = dropConstraintsQuery(tableName, dropConstraintsList);
      } else {
        throw new RuntimeException(
            "Method can only be - disableConstraint, enableConstraint, "
                + "modifyColumn, renameTable, renameColumn, dropColumn, "
                + "dropConstraint, addConstraint, addColumn");
      }
      if (alterTableQuery.isBlank()) {
        throw new RuntimeException("Failed to create alter query");
      } else {
        LOGGER.info("Alter Query: {}", alterTableQuery);
        try (Statement st = connection.createStatement()) {
          connection.setAutoCommit(false);
          st.executeUpdate(alterTableQuery);
          connection.commit();
          queryResponse =
              new QueryResponse<>("Alter operation - " + operation + " executed successfully");
          LOGGER.info("AlterTableQueryStatus: {}", queryResponse.getResponse());
        }
      }
    } catch (SQLException | RuntimeException re) {
      LOGGER.error(re.getMessage());
      throw re;
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

  private String disableConstraintQuery(String tableName, String constraintName) {
    if (constraintName == null || constraintName.isBlank())
      throw new RuntimeException("'constraintName' can not be null or blank");
    return "ALTER TABLE " + tableName + " NOCHECK CONSTRAINT " + constraintName;
  }

  private String enableConstraintQuery(String tableName, String constraintName) {
    if (constraintName == null || constraintName.isBlank())
      throw new RuntimeException("'constraintName' can not be null or blank");
    return "ALTER TABLE " + tableName + " WITH CHECK CHECK CONSTRAINT " + constraintName;
  }

  private String renameTableQuery(String tableName, String newTableName) {
    if (newTableName == null || newTableName.isBlank())
      throw new RuntimeException("'newTableName' can not be null or blank");
    return "sp_rename '" + tableName + "', '" + newTableName + "';";
  }

  private String renameColumnQuery(String tableName, Map<String, String> newColumnDetail) {
    if (newColumnDetail == null || newColumnDetail.isEmpty())
      throw new RuntimeException("'newColumnDetail' is invalid");
    Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    map.putAll(newColumnDetail);
    String oldColumnName = map.get("oldColName");
    String newColumnName = map.get("newColName");
    if (oldColumnName == null
        || oldColumnName.isBlank()
        || newColumnName == null
        || newColumnName.isBlank()) {
      throw new RuntimeException(
          "'newColumnDetail' is invalid. Keys 'oldColName' and 'newColName' is required");
    }
    return "sp_rename '"
        + tableName
        + "."
        + oldColumnName
        + "', '"
        + newColumnName
        + "', 'COLUMN';";
  }

  private String addColumnsQuery(String tableName, List<Map<String, String>> columnsDetails) {
    if (columnsDetails == null || columnsDetails.isEmpty())
      throw new RuntimeException("'columnsDetails' can not be null or empty");
    String query = "ALTER TABLE " + tableName + " ADD ";
    StringBuilder addColStatement = new StringBuilder();
    for (int i = 0; i < columnsDetails.size(); i++) {
      if (columnsDetails.get(i) == null || columnsDetails.get(i).isEmpty()) continue;
      Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      map.putAll(columnsDetails.get(i));
      String colName = map.get("colName");
      String dataType = map.get("dataType");
      String constraint = map.get("constraint");
      if (colName == null || colName.isBlank() || dataType == null || dataType.isBlank())
        throw new RuntimeException(
            "Failed to add column, colName and dataType required - "
                + columnsDetails.get(i).toString());
      if (i != 0) addColStatement.append(",");
      addColStatement.append(colName).append(" ").append(dataType);
      if (constraint != null && !constraint.isBlank())
        addColStatement.append(" ").append(constraint);
    }
    if (addColStatement.toString().isBlank())
      throw new RuntimeException(
          "Unable to create alter table - add column query, "
              + "Please check the input - 'columnDetails'");
    query += addColStatement.toString() + ";";
    return query;
  }

  private String addConstraintQuery(String tableName, List<Map<String, String>> constraintDetails) {
    if (constraintDetails == null || constraintDetails.isEmpty())
      throw new RuntimeException("'constraintDetails' can not be null or empty");
    String query = "ALTER TABLE " + tableName + " ADD";
    StringBuilder addConstraintStatement = new StringBuilder();
    // Iterate over list of constraints map
    for (int i = 0; i < constraintDetails.size(); i++) {
      // check map is not null or empty
      if (constraintDetails.get(i) != null && !constraintDetails.get(i).isEmpty()) {
        // create case-insensitive map
        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(constraintDetails.get(i));
        // Constraint Type
        String name = map.getOrDefault("name", "").replaceAll(" ", "");
        // Symbol for constraint definition
        String symbol = map.getOrDefault("symbol", "");
        if (symbol.isBlank())
          throw new RuntimeException(
              "'symbol' can not be null or blank. "
                  + "Please provide constraint name. e.g. symbol: 'pk_id'");
        String definition = map.get("definition");
        if (name.equalsIgnoreCase("UNIQUE")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' is required. e.g. col_name to apply unique " + "constraint");
          String uQuery = " CONSTRAINT " + symbol + " UNIQUE (" + definition + ")";
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement.append(uQuery);
        } else if (name.equalsIgnoreCase("PRIMARYKEY")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' is required. e.g. col_name to apply primary key constraint");
          String pkQuery = " CONSTRAINT " + symbol + " PRIMARY KEY (" + definition + ")";
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement.append(pkQuery);
        } else if (name.equalsIgnoreCase("FOREIGNKEY")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' is required. e.g. "
                    + "(child_column_names,...) REFERENCE ref_table_name"
                    + "(Referencing column_names,... in ref_table_name)");
          String fkQuery = " CONSTRAINT " + symbol + " FOREIGN KEY " + definition;
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement.append(fkQuery);
        } else if (name.equalsIgnoreCase("CHECK")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' - Check " + "Expression is required. e.g. id > 5");
          String checkQuery = " CONSTRAINT " + symbol + " CHECK ( " + definition + " )";
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement.append(checkQuery);
        } else if (name.equalsIgnoreCase("DEFAULT")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' for default constraint can not be null or blank. "
                    + "e.g. GETDATE() FOR Sys_date, i.e. defaultValue FOR columnName");
          String checkQuery = " CONSTRAINT " + symbol + " DEFAULT " + definition;
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement.append(checkQuery);
        } else {
          throw new RuntimeException(
              "name should be - default, unique, " + "primary key, foreign key or check");
        }
      }
    }
    if (addConstraintStatement.toString().isBlank())
      throw new RuntimeException(
          "Unable to create alter table - add constraint query, Please check"
              + " the input 'constraintDetails'");
    query += addConstraintStatement.toString() + ";";
    return query;
  }

  private String modifyColumnQuery(String tableName, Map<String, String> modifyColumnsDetails) {
    if (modifyColumnsDetails == null || modifyColumnsDetails.isEmpty())
      throw new RuntimeException("'modifyColumnsDetails' can not be null or empty");
    String query = "";
    StringBuilder modQuery = new StringBuilder();
    Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    map.putAll(modifyColumnsDetails);
    String columnName = map.get("colName");
    String dataType = map.get("dataType");
    String constraint = map.get("constraint");
    if (columnName != null && !columnName.isBlank() && dataType != null && !dataType.isBlank()) {
      modQuery
          .append("ALTER TABLE ")
          .append(tableName)
          .append(" ALTER COLUMN ")
          .append(columnName)
          .append(" ")
          .append(dataType);
      if (constraint != null && !constraint.isBlank()) modQuery.append(" ").append(constraint);
      modQuery.append(";");
    }
    if (modQuery.toString().isBlank())
      throw new RuntimeException(
          "Unable to create alter table - modify query, Please "
              + "check the input - 'modifyColumnsDetails' map should have "
              + "keys - colName, dataType, constraint");
    query += modQuery.toString();
    return query;
  }

  private String dropConstraintsQuery(String tableName, List<String> dropConstraintsList) {
    if (dropConstraintsList == null || dropConstraintsList.isEmpty())
      throw new RuntimeException("'dropConstraintsList' can not be null or empty");
    String queryToDrop =
        "ALTER TABLE "
            + tableName
            + " DROP CONSTRAINT "
            + String.join(", ", dropConstraintsList)
            + ";";
    if (queryToDrop.isBlank())
      throw new RuntimeException("Unable to create alter table - drop  constraints query");
    return queryToDrop;
  }

  private String dropColumnsListQuery(String tableName, List<String> dropColumnsList) {
    if (dropColumnsList == null || dropColumnsList.isEmpty())
      throw new RuntimeException("'dropColumnsList' can not be null or empty");
    return "ALTER TABLE " + tableName + " Drop COLUMN " + String.join(", ", dropColumnsList) + ";";
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

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getNewTableName() {
    return newTableName;
  }

  public void setNewTableName(String newTableName) {
    this.newTableName = newTableName;
  }

  public Map<String, String> getNewColumnDetail() {
    return newColumnDetail;
  }

  public void setNewColumnDetail(Map<String, String> newColumnDetail) {
    this.newColumnDetail = newColumnDetail;
  }

  public List<Map<String, String>> getColumnsDetails() {
    return columnsDetails;
  }

  public void setColumnsDetails(List<Map<String, String>> columnsDetails) {
    this.columnsDetails = columnsDetails;
  }

  public List<Map<String, String>> getConstraintDetails() {
    return constraintDetails;
  }

  public void setConstraintDetails(List<Map<String, String>> constraintDetails) {
    this.constraintDetails = constraintDetails;
  }

  public Map<String, String> getModifyColumnsDetails() {
    return modifyColumnsDetails;
  }

  public void setModifyColumnsDetails(Map<String, String> modifyColumnsDetails) {
    this.modifyColumnsDetails = modifyColumnsDetails;
  }

  public List<String> getDropConstraintsList() {
    return dropConstraintsList;
  }

  public void setDropConstraintsList(List<String> dropConstraintsList) {
    this.dropConstraintsList = dropConstraintsList;
  }

  public List<String> getDropColumnsList() {
    return dropColumnsList;
  }

  public void setDropColumnsList(List<String> dropColumnsList) {
    this.dropColumnsList = dropColumnsList;
  }

  public String getConstraintName() {
    return constraintName;
  }

  public void setConstraintName(String constraintName) {
    this.constraintName = constraintName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AlterTableService that = (AlterTableService) o;
    return Objects.equals(databaseName, that.databaseName)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(method, that.method)
        && Objects.equals(newTableName, that.newTableName)
        && Objects.equals(newColumnDetail, that.newColumnDetail)
        && Objects.equals(columnsDetails, that.columnsDetails)
        && Objects.equals(constraintDetails, that.constraintDetails)
        && Objects.equals(modifyColumnsDetails, that.modifyColumnsDetails)
        && Objects.equals(dropConstraintsList, that.dropConstraintsList)
        && Objects.equals(dropColumnsList, that.dropColumnsList)
        && Objects.equals(constraintName, that.constraintName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        databaseName,
        tableName,
        method,
        newTableName,
        newColumnDetail,
        columnsDetails,
        constraintDetails,
        modifyColumnsDetails,
        dropConstraintsList,
        dropColumnsList,
        constraintName);
  }

  @Override
  public String toString() {
    return "AlterTableService{"
        + "databaseName='"
        + databaseName
        + '\''
        + ", tableName='"
        + tableName
        + '\''
        + ", method='"
        + method
        + '\''
        + ", newTableName='"
        + newTableName
        + '\''
        + ", newColumnDetail="
        + newColumnDetail
        + ", columnsDetails="
        + columnsDetails
        + ", constraintDetails="
        + constraintDetails
        + ", modifyColumnsDetails="
        + modifyColumnsDetails
        + ", dropConstraintsList="
        + dropConstraintsList
        + ", dropColumnsList="
        + dropColumnsList
        + ", constraintName='"
        + constraintName
        + '\''
        + '}';
  }
}
