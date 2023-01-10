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
import java.util.*;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlterTableService implements PostgreSQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(AlterTableService.class);
  @NotBlank private String databaseName;
  @NotBlank private String tableName;
  @NotBlank private String method;
  private List<Map<String, String>> constraintDetails;
  private String newTableName;
  private Map<String, String> newColumnDetail;
  private List<Map<String, String>> columnsDetails;
  private List<Map<String, String>> modifyColumnsDetails;
  private List<Map<String, String>> dropEntityDetails;

  @Override
  public PostgreSQLResponse invoke(Connection connection) throws SQLException {
    QueryResponse<String> queryResponse;
    try {
      method = method.replaceAll("[_\\s]", "");
      String alterTableQuery = "";
      if (method.equalsIgnoreCase("renameTable")) {
        alterTableQuery = renameTableQuery(newTableName);
      } else if (method.equalsIgnoreCase("renameColumn")) {
        alterTableQuery = renameColumnQuery(newColumnDetail);
      } else if (method.equalsIgnoreCase("modifyColumn")) {
        alterTableQuery = modifyColumnQuery(modifyColumnsDetails);
      } else if (method.equalsIgnoreCase("drop")) {
        alterTableQuery = dropQuery(dropEntityDetails);
      } else if (method.equalsIgnoreCase("addConstraint")) {
        alterTableQuery = addConstraintQuery(constraintDetails);
      } else if (method.equalsIgnoreCase("addColumn")) {
        alterTableQuery = addColumnsQuery(columnsDetails);
      } else {
        throw new RuntimeException(
            "Method can only be - modifyColumn, RenameTable, drop, addCheck, "
                + "addConstraint, addColumn");
      }
      if (alterTableQuery.isBlank())
        throw new RuntimeException("Failed " + "to create alter query");
      else {
        LOGGER.info("Alter Query: {}", alterTableQuery);
        try (Statement st = connection.createStatement()) {
          connection.setAutoCommit(false);
          st.executeUpdate(alterTableQuery);
          connection.commit();
          queryResponse = new QueryResponse<>("Alter query executed successfully");
          LOGGER.info("AlterTableQueryStatus: {}", queryResponse.getResponse());
        }
      }
    } catch (SQLException | RuntimeException exception) {
      LOGGER.error(exception.getMessage());
      throw exception;
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

  private String renameTableQuery(String newTableName) {
    if (newTableName == null || newTableName.isEmpty())
      throw new RuntimeException("'newTableName' can not be null or blank");
    return "ALTER TABLE " + tableName + " RENAME TO " + newTableName + " ;";
  }

  private String renameColumnQuery(Map<String, String> newColumnDetail) {
    if (newColumnDetail == null || newColumnDetail.isEmpty())
      throw new RuntimeException("'newColumnDetail' is invalid");
    Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    map.putAll(newColumnDetail);
    String oldColumnName = map.get("oldColName");
    String newColumnName = map.get("newColName");
    if (oldColumnName == null
        || oldColumnName.isBlank()
        || newColumnName == null
        || newColumnName.isBlank())
      throw new RuntimeException(
          "'newColumnDetail' is invalid. Keys 'oldColName' and 'newColName' is required");
    return "ALTER TABLE "
        + tableName
        + " RENAME COLUMN "
        + oldColumnName
        + " TO "
        + newColumnName
        + ";";
  }

  private String addColumnsQuery(List<Map<String, String>> columnsDetails) {
    if (columnsDetails == null || columnsDetails.isEmpty())
      throw new RuntimeException("'columnsDetails' can not be null or empty");
    String query = "ALTER TABLE " + tableName;
    StringBuilder addColStatement = new StringBuilder();
    for (int i = 0; i < columnsDetails.size(); i++) {
      Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      map.putAll(columnsDetails.get(i));
      String colName = map.get("colName");
      String dataType = map.get("dataType");
      String constraint = map.get("constraint");
      if (colName == null || colName.isBlank() || dataType == null || dataType.isBlank())
        throw new RuntimeException(
            "Failed to add column colName and dataType required - "
                + columnsDetails.get(i).toString());
      if (i != 0) addColStatement.append(",");
      addColStatement.append(" ADD COLUMN  ").append(colName).append(" ").append(dataType);
      if (constraint != null && !constraint.isBlank())
        addColStatement.append(" ").append(constraint);
    }
    if (addColStatement.toString().isBlank())
      throw new RuntimeException(
          "Unable to create alter table - add column query, Please check"
              + " the input - 'columnDetails'");
    query += addColStatement + ";";
    return query;
  }

  private String addConstraintQuery(List<Map<String, String>> constraintDetails) {
    if (constraintDetails == null || constraintDetails.isEmpty())
      throw new RuntimeException("'constraintDetails' can not be null or empty");
    String query = "ALTER TABLE " + tableName;
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
        String symbol = map.getOrDefault("symbol", "");
        String definition = map.get("definition");

        if (name.equalsIgnoreCase("UNIQUE")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' is required. e.g. col_name to apply unique "
                    + "constraint OR comma separated column name");
          if (symbol.isBlank())
            throw new RuntimeException("'symbol' - constraint name is required");
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement
              .append(" ADD CONSTRAINT ")
              .append(symbol)
              .append(" UNIQUE (")
              .append(definition)
              .append(")");
        } else if (name.replace(" ", "").equalsIgnoreCase("PRIMARYKEY")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' - column name required to make it primary key");
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement.append(" ADD PRIMARY KEY (").append(definition).append(")");
        } else if (name.replace(" ", "").equalsIgnoreCase("FOREIGNKEY")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' is "
                    + "required. e.g. (column_name) REFERENCE ref_table_name(Referencing column_name in "
                    + "ref_table_name)");
          if (symbol.isBlank())
            throw new RuntimeException("'symbol' - constraint name is required");
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement
              .append(" ADD CONSTRAINT ")
              .append(symbol)
              .append(" FOREIGN KEY ")
              .append(definition);
        } else if (name.equalsIgnoreCase("CHECK")) {
          if (definition == null || definition.isBlank())
            throw new RuntimeException(
                "'definition' - Check " + "Expression is required. e.g. id > 5");
          if (symbol.isBlank())
            throw new RuntimeException("'symbol' - constraint name is required");
          if (i != 0) addConstraintStatement.append(",");
          addConstraintStatement
              .append(" ADD CONSTRAINT ")
              .append(symbol)
              .append(" CHECK ( ")
              .append(definition)
              .append(" )");
        } else {
          throw new RuntimeException("name should be - unique, primary key, foreign key or check");
        }
      } else {
        throw new RuntimeException("Invalid 'constraintDetails', can not contain empty object");
      }
    }
    if (addConstraintStatement.toString().isBlank())
      throw new RuntimeException(
          "Unable to create alter table - add constraint query, Please check"
              + " the input - 'constraintDetails'");
    query += addConstraintStatement.toString();
    return query;
  }

  private String dropQuery(List<Map<String, String>> dropEntityDetails) {
    if (dropEntityDetails == null || dropEntityDetails.isEmpty())
      throw new RuntimeException("'dropEntityDetails' can not be null or empty");
    String queryToDrop = "ALTER TABLE " + tableName;
    StringBuilder dropStatement = new StringBuilder();
    // iterate over list of drop constraint map
    for (int i = 0; i < dropEntityDetails.size(); i++) {
      // check map is not null or empty
      if (dropEntityDetails.get(i) != null && !dropEntityDetails.get(i).isEmpty()) {
        // create case-insensitive map
        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(dropEntityDetails.get(i));
        String entityToDrop = map.getOrDefault("EntityToDrop", "");
        String nameIfPresent = map.getOrDefault("name", "");
        // Entity type
        if (entityToDrop.replaceAll("\\s+", "").equalsIgnoreCase("COLUMN")) {
          if (nameIfPresent.isBlank())
            throw new RuntimeException("name can not be null for dropping " + "column constraint");
          if (i != 0) dropStatement.append(", ");
          dropStatement.append(" DROP COLUMN ").append(nameIfPresent);
        } else if (entityToDrop.replaceAll("\\s+", "").equalsIgnoreCase("DEFAULT")) {
          if (i != 0) dropStatement.append(",");
          dropStatement.append(" DROP DEFAULT");
        } else if (entityToDrop.replaceAll("\\s+", "").equalsIgnoreCase("EXPRESSION")) {
          if (i != 0) dropStatement.append(",");
          dropStatement.append(" DROP EXPRESSION");
        } else if (entityToDrop.replaceAll("\\s+", "").equalsIgnoreCase("IDENTITY")) {
          if (i != 0) dropStatement.append(",");
          dropStatement.append(" DROP IDENTITY");
        } else if (entityToDrop.replaceAll("\\s+", "").equalsIgnoreCase("CONSTRAINT")) {
          if (nameIfPresent.isBlank())
            throw new RuntimeException("name can not be null for dropping " + "column constraint");
          if (i != 0) dropStatement.append(",");
          dropStatement.append(" DROP CONSTRAINT ").append(nameIfPresent);
        } else {
          throw new RuntimeException(
              "'entityToDrop' should be Column, DEFAULT, EXPRESSION, IDENTITY, Constraint");
        }
      }
    }
    if (dropStatement.toString().isBlank())
      throw new RuntimeException(
          "Unable to create alter table - drop query, Please check"
              + " the input - 'dropEntityDetails'");
    queryToDrop += dropStatement.toString();
    return queryToDrop;
  }

  private String modifyColumnQuery(List<Map<String, String>> modifyColumnsDetails) {
    if (modifyColumnsDetails == null || modifyColumnsDetails.isEmpty())
      throw new RuntimeException("'modifyColumnsDetails' can not be null or empty");
    StringBuilder query = new StringBuilder("ALTER TABLE " + tableName);
    StringBuilder modQuery = new StringBuilder();
    for (int i = 0; i < modifyColumnsDetails.size(); ++i) {
      if (modifyColumnsDetails.get(i) == null || modifyColumnsDetails.get(i).isEmpty()) continue;
      Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      map.putAll(modifyColumnsDetails.get(i));
      String columnName = map.get("colName");
      String dataType = map.get("dataType");
      String expression = map.get("expression");
      if (columnName != null && !columnName.isBlank() && dataType != null && !dataType.isBlank()) {
        if (expression != null && !expression.isBlank()) {
          if (i != 0) {
            modQuery.append(",");
          }
          modQuery
              .append(" ALTER COLUMN ")
              .append(columnName)
              .append(" SET DATA TYPE ")
              .append(dataType)
              .append(" USING ")
              .append(expression);
        } else {
          if (i != 0) {
            modQuery.append(",");
          }
          modQuery.append(" ALTER COLUMN ").append(columnName).append(" TYPE ").append(dataType);
        }
      }
    }
    if (modQuery.toString().isBlank())
      throw new RuntimeException(
          "Unable to create alter table - alter column query, "
              + "Please"
              + " check the input - 'modifyColumnsDetails' list of map should have keys - colName, dataType, expression");
    query.append(modQuery);
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

  public List<Map<String, String>> getDropEntityDetails() {
    return dropEntityDetails;
  }

  public void setDropEntityDetails(List<Map<String, String>> dropEntityDetails) {
    this.dropEntityDetails = dropEntityDetails;
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
        && Objects.equals(dropEntityDetails, that.dropEntityDetails);
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
        dropEntityDetails);
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
        + ", dropEntityDetails="
        + dropEntityDetails
        + '}';
  }
}
