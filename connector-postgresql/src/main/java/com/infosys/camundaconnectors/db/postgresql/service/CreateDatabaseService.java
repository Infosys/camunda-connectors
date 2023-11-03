/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.postgresql.service;

import com.infosys.camundaconnectors.db.postgresql.model.request.PostgreSQLRequestData;
import com.infosys.camundaconnectors.db.postgresql.model.response.PostgreSQLResponse;
import com.infosys.camundaconnectors.db.postgresql.model.response.QueryResponse;
import java.sql.*;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateDatabaseService implements PostgreSQLRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDatabaseService.class);
  private String databaseName;

  @Override
  public PostgreSQLResponse invoke(Connection connection) throws SQLException {
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
    return databaseName;
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
