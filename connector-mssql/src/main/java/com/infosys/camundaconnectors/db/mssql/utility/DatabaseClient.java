/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.utility;

import com.infosys.camundaconnectors.db.mssql.model.request.DatabaseConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Creates {@link java.sql.Connection} Object for database */
public class DatabaseClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseClient.class);

  public DatabaseClient() {}

  public Connection getConnectionObject(
      DatabaseConnection databaseConnection, String databaseName) {
    String dbURL =
        "jdbc:sqlserver://"
            + databaseConnection.getHost().strip()
            + ":"
            + databaseConnection.getPort().strip();
    if (databaseName != null && !databaseName.isBlank()) dbURL += ";databaseName=" + databaseName;
    return connectToDatabase(databaseConnection, dbURL);
  }

  private Connection connectToDatabase(DatabaseConnection databaseConnection, String dbURL) {
    Connection conn;
    try {
      conn =
          DriverManager.getConnection(
              dbURL, databaseConnection.getUsername(), databaseConnection.getPassword());
    } catch (SQLException sqlException) {
      LOGGER.error("SQLException: {}", sqlException.getMessage());
      if (sqlException.getMessage().contains("Login failed for user"))
        throw new RuntimeException(
            "AuthenticationError: Invalid username or password " + sqlException.getMessage(),
            sqlException);
      else if (sqlException.getMessage().contains("Cannot open database"))
        throw new RuntimeException(
            "InvalidDatabase: Database doesn't exist " + sqlException.getMessage(), sqlException);
      throw new RuntimeException(
          "ConnectionError: Unable to connect to DB " + sqlException.getMessage(), sqlException);
    }
    if (conn == null) {
      LOGGER.error("ConnectionError: Unable to connect to Database");
      throw new RuntimeException("ConnectionError: Unable to connect to Database");
    } else LOGGER.debug("Connected successfully to the database");
    return conn;
  }
}
