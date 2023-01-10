/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mysql.utility;

import com.infosys.camundaconnectors.db.mysql.model.request.DatabaseConnection;
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
        "jdbc:mysql://"
            + databaseConnection.getHost().strip()
            + ":"
            + databaseConnection.getPort().strip();
    if (databaseName != null && !databaseName.isBlank()) dbURL += "/" + databaseName;
    return connectToDatabase(databaseConnection, dbURL);
  }

  private Connection connectToDatabase(DatabaseConnection databaseConnection, String dbURL) {
    Connection conn;
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      conn =
          DriverManager.getConnection(
              dbURL, databaseConnection.getUsername(), databaseConnection.getPassword());
    } catch (ClassNotFoundException clsEx) {
      LOGGER.error("Unable to load MySQL Driver: {}", clsEx.getMessage());
      throw new RuntimeException("Unable to load MySQL Driver: " + clsEx.getMessage(), clsEx);
    } catch (SQLException sqlException) {
      LOGGER.error("SQLException: {}", sqlException.getMessage());
      if (sqlException.getMessage().contains("Access denied"))
        throw new RuntimeException(
            "AuthenticationError: Invalid username or password " + sqlException.getMessage(),
            sqlException);
      else if (sqlException.getMessage().contains("Unknown database"))
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
