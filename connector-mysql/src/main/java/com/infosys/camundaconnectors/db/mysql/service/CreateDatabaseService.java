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
import com.infosys.camundaconnectors.db.mysql.utility.DatabaseClient;

import java.sql.*;
import java.util.Objects;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateDatabaseService implements MySQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDatabaseService.class);
  @NotBlank private String databaseName;

  @Override
  public MySQLResponse invoke(DatabaseClient databaseClient,DatabaseConnection databaseConnection,String DatabaseName) throws SQLException {
	  final Connection connection = databaseClient.getConnectionObject(databaseConnection,"");
    QueryResponse<String> queryResponse;
    try (Statement st = connection.createStatement()) {
      String query = "CREATE DATABASE " + databaseName;
      st.executeUpdate(query);
      queryResponse = new QueryResponse<>("Database '" + databaseName + "' created successfully");
      LOGGER.info("CreateDatabaseQueryStatus: {}", queryResponse.getResponse());
    } catch (SQLException ex) {
      LOGGER.error(ex.getMessage());
      throw new RuntimeException(ex);
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

  @Override
  public String getDatabaseName() {
    return null;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateDatabaseService that = (CreateDatabaseService) o;
    return Objects.equals(databaseName, that.databaseName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(databaseName);
  }

  @Override
  public String toString() {
    return "CreateDatabaseService{" + "databaseName='" + databaseName + '\'' + '}';
  }
}
