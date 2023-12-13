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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlterTableService implements OracleDBRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(AlterTableService.class);
  @NotBlank private String databaseName;
  @NotBlank private String tableName;
  @NotBlank private String method;
  private List<Map<String, String>> constraintDetails;
  private String newTableName;
  private Map<String, String> newColumnDetail;
  private List<Map<String, String>> columnsDetails;
  private List<Map<String, String>> modifyColumnsDetails;
  private List<String> dropConstraintsList;
  private List<String> dropColumnsList;
  private String constraintName;

  @Override
  public OracleDBResponse invoke(Connection connection) throws SQLException {
    QueryResponse<String> queryResponse;
    try {
      method = method.replaceAll("[_\\s]", "");
      String alterTableQuery = "";
      if (method.equalsIgnoreCase("disableConstraint")) {
        alterTableQuery = disableConstraintQuery(tableName, constraintName);
      } else if (method.equalsIgnoreCase("enableConstraint")) {
        alterTableQuery = enableConstraintQuery(tableName, constraintName);
      } else if (method.equalsIgnoreCase("dropColumn")) {
        alterTableQuery = dropColumnsListQuery(tableName, dropColumnsList);
      } else if (method.equalsIgnoreCase("renameTable")) {
        alterTableQuery = renameTableQuery(tableName, newTableName);
      } else if (method.equalsIgnoreCase("renameColumn")) {
        alterTableQuery = renameColumnQuery(tableName, newColumnDetail);
      } else if (method.equalsIgnoreCase("modifyColumn")) {
        alterTableQuery = modifyColumnQuery(tableName, modifyColumnsDetails);
      } else if (method.equalsIgnoreCase("dropConstraint")) {
        alterTableQuery = dropConstraintsQuery(tableName, dropConstraintsList);
      } else if (method.equalsIgnoreCase("addConstraint")) {
        alterTableQuery = addConstraintQuery(tableName, constraintDetails);
      } else if (method.equalsIgnoreCase("addColumn")) {
        alterTableQuery = addColumnsQuery(tableName, columnsDetails);
      } else {
        throw new RuntimeException(
            "Method can only be - enableConstraint, disableConstraint, "
                + "modifyColumn, renameTable, RenameColumn, dropColumn, dropConstraint, addConstraint, addColumn");
      }
      if (alterTableQuery.isBlank()) {
        throw new RuntimeException("Failed to create alter query");
      } else {
        LOGGER.info("Alter Query: {}", alterTableQuery);
        try (Statement st = connection.createStatement()) {
          connection.setAutoCommit(false);
          st.executeUpdate(alterTableQuery);
          connection.commit();
          queryResponse = new QueryResponse<>("Alter query executed successfully");
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
    return "ALTER TABLE " + tableName + " DISABLE CONSTRAINT " + constraintName;
  }

  private String enableConstraintQuery(String tableName, String constraintName) {
    if (constraintName == null || constraintName.isBlank())
      throw new RuntimeException("'constraintName' can not be null or blank");
    return "ALTER TABLE " + tableName + " ENABLE CONSTRAINT " + constraintName;
  }

  private String dropColumnsListQuery(String tableName, List<String> dropColumnsList) {
    if (dropColumnsList == null || dropColumnsList.isEmpty())
      throw new RuntimeException("'dropColumnsList' can not be null or empty");
    return "ALTER TABLE " + tableName + " Drop ( " + String.join(", ", dropColumnsList) + " )";
  }

  private String renameTableQuery(String tableName, String newTableName) {
    if (newTableName == null || newTableName.isBlank())
      throw new RuntimeException("'newTableName' can not be null or blank");
    return "ALTER TABLE " + tableName + " RENAME TO " + newTableName;
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
    return "ALTER TABLE " + tableName + " RENAME COLUMN " + oldColumnName + " TO " + newColumnName;
  }

  private String modifyColumnQuery(
      String tableName, List<Map<String, String>> modifyColumnsDetails) {
    if (modifyColumnsDetails == null || modifyColumnsDetails.isEmpty())
      throw new RuntimeException("'modifyColumnsDetails' can not be null or empty");
    String query = "ALTER TABLE " + tableName + " MODIFY ( ";
    StringBuilder modQuery = new StringBuilder();
    for (int i = 0; i < modifyColumnsDetails.size(); ++i) {
      if (modifyColumnsDetails.get(i) == null || modifyColumnsDetails.get(i).isEmpty()) continue;
      Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      map.putAll(modifyColumnsDetails.get(i));
      String columnName = map.get("colName");
      String dataType = map.get("dataType");
      String constraint = map.get("constraint");
      if (columnName != null && !columnName.isBlank()) {
        if (i == 0) {
          modQuery.append(columnName);
        } else modQuery.append(", ").append(columnName);
        if (dataType != null && !dataType.isBlank()) modQuery.append(" ").append(dataType);
        if (constraint != null && !constraint.isBlank()) modQuery.append(" ").append(constraint);
      }
    }
    if (modQuery.toString().isBlank())
      throw new RuntimeException(
          "Unable to create modify query, "
              + "Please check the input 'modifyColumnsDetails', it's a list of map with "
              + "keys - 'colName', 'dataType' and 'constraint'");
    query += modQuery + ")";
    return query;
  }

  private String dropConstraintsQuery(String tableName, List<String> dropConstraintsList) {
    if (dropConstraintsList == null || dropConstraintsList.isEmpty())
      throw new RuntimeException("'dropConstraintsList' can not be null or empty");
    StringBuilder queryToDrop = new StringBuilder("ALTER TABLE " + tableName);
    for (String constraint : dropConstraintsList) {
      if (constraint != null && !constraint.isBlank()) {
        if (constraint.replace(" ", "").equalsIgnoreCase("primaryKey"))
          queryToDrop.append(" Drop primary key ");
        else queryToDrop.append(" Drop Constraint ").append(constraint);
      }
    }
    return queryToDrop.toString();
  }

  private String addConstraintQuery(String tableName, List<Map<String, String>> constraintDetails) {
    if (constraintDetails == null || constraintDetails.isEmpty())
      throw new RuntimeException("'constraintDetails' can not be null or empty");
    StringBuilder query = new StringBuilder("ALTER TABLE " + tableName + " ADD ( ");
    StringBuilder addConstraintStatement = new StringBuilder();
    // Iterate over list of constraints map
    for (int i = 0; i < constraintDetails.size(); i++) {
      // check map is not null or empty
      if (constraintDetails.get(i) != null && !constraintDetails.get(i).isEmpty()) {
        // create case-insensitive map
        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(constraintDetails.get(i));
        // Constraint Type
        String name = map.getOrDefault("name", "");
        // Symbol for constraint definition
        String symbol = map.get("symbol");
        if (symbol == null || symbol.isBlank())
          throw new RuntimeException(
              "'symbol' can not be null or blank. Please provide constraint name. e"
                  + ".g. symbol: 'pk_id'");
        String definition = map.get("definition");
        if (name.equalsIgnoreCase("UNIQUE")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' is required. e.g. 'col_name' to apply unique " + "constraint");
          String uQuery = " CONSTRAINT " + symbol + " UNIQUE (" + definition + ")";
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement.append(uQuery);
        } else if (name.replace(" ", "").equalsIgnoreCase("PRIMARYKEY")) {
          String pkQuery = " CONSTRAINT " + symbol + " PRIMARY KEY ";
          if (definition != null && !definition.isBlank()) pkQuery += "(" + definition + ")";
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement.append(pkQuery);
        } else if (name.replace(" ", "").equalsIgnoreCase("FOREIGNKEY")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' is "
                    + "required. e.g. (column_name) REFERENCE ref_table_name(Referencing column_name in "
                    + "ref_table_name)");
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
        } else {
          throw new RuntimeException("name should be - unique, primary key, foreign key or check");
        }
      } else {
        throw new RuntimeException("Invalid 'constraintDetails', can not contain empty object");
      }
    }
    if (addConstraintStatement.toString().isBlank())
      throw new RuntimeException(
          "Failed to create add constraint query, Please provide valid"
              + " input - 'constraintDetails'. It should a list of Maps with keys - 'name', 'symbol' and 'definition'");
    addConstraintStatement.append(")");
    query.append(addConstraintStatement);
    return query.toString();
  }

  private String addColumnsQuery(String tableName, List<Map<String, String>> columnsDetails) {
    if (columnsDetails == null || columnsDetails.isEmpty())
      throw new RuntimeException("'columnsDetails' can not be null or empty");
    StringBuilder query = new StringBuilder("ALTER TABLE " + tableName + " ADD ( ");
    StringBuilder addColumnsStatement = new StringBuilder();
    for (int i = 0; i < columnsDetails.size(); i++) {
      if (columnsDetails.get(i) == null || columnsDetails.get(i).isEmpty()) continue;
      Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      map.putAll(columnsDetails.get(i));
      String colName = map.get("colName");
      String dataType = map.get("dataType");
      String constraint = map.get("constraint");
      if (colName == null || colName.isBlank() || dataType == null || dataType.isBlank())
        throw new RuntimeException(
            "Failed to add column. colName and dataType required - "
                + columnsDetails.get(i).toString());
      if (i == 0) addColumnsStatement.append(colName).append(" ").append(dataType);
      else addColumnsStatement.append(", ").append(colName).append(" ").append(dataType);
      if (constraint != null && !constraint.isBlank())
        addColumnsStatement.append(" ").append(constraint);
    }
    if (addColumnsStatement.toString().isBlank())
      throw new RuntimeException(
          "Failed to create add column query - Please provide valid 'columnsDetails'. "
              + "It should be a list of maps with keys - 'colName', 'dataType' and 'constraint'");
    query.append(addColumnsStatement).append(" )");
    return query.toString();
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

  public List<Map<String, String>> getConstraintDetails() {
    return constraintDetails;
  }

  public void setConstraintDetails(List<Map<String, String>> constraintDetails) {
    this.constraintDetails = constraintDetails;
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

  public List<Map<String, String>> getModifyColumnsDetails() {
    return modifyColumnsDetails;
  }

  public void setModifyColumnsDetails(List<Map<String, String>> modifyColumnsDetails) {
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
        && Objects.equals(constraintDetails, that.constraintDetails)
        && Objects.equals(newTableName, that.newTableName)
        && Objects.equals(newColumnDetail, that.newColumnDetail)
        && Objects.equals(columnsDetails, that.columnsDetails)
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
        constraintDetails,
        newTableName,
        newColumnDetail,
        columnsDetails,
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
        + ", constraintDetails="
        + constraintDetails
        + ", newTableName='"
        + newTableName
        + '\''
        + ", newColumnDetail="
        + newColumnDetail
        + ", columnsDetails="
        + columnsDetails
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
