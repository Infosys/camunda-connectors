/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.postgresql.utility;

import com.infosys.camundaconnectors.db.postgresql.model.request.DatabaseConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Creates {@link Connection} Object for database */
public class DatabaseClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseClient.class);

  public DatabaseClient() {}

  public Connection getConnectionObject(
      DatabaseConnection databaseConnection, String databaseName) {
    String dbURL =
        "jdbc:postgresql://"
            + databaseConnection.getHost().strip()
            + ":"
            + databaseConnection.getPort().strip()
            + "/";
    if (databaseName != null && !databaseName.isBlank()) dbURL += databaseName.toLowerCase();
    else dbURL += "template1";
    return connectToDatabase(databaseConnection, dbURL);
  }

  private Connection connectToDatabase(DatabaseConnection databaseConnection, String dbURL) {
    Connection conn;
    try {
      Class.forName("org.postgresql.Driver");
      Properties props = new Properties();
      props.setProperty("user", databaseConnection.getUsername());
      props.setProperty("password", databaseConnection.getPassword());
      props.setProperty("stringtype", "unspecified");
      conn = DriverManager.getConnection(dbURL, props);
    } catch (ClassNotFoundException clsEx) {
      LOGGER.error("Unable to load Postgres Driver: {}", clsEx.getMessage());
      throw new RuntimeException("Unable to load PostgreSQL Driver: " + clsEx.getMessage(), clsEx);
    } catch (SQLException sqlException) {
      LOGGER.error("SQLException: {}", sqlException.getMessage());
      if (sqlException.getMessage().contains("FATAL: password authentication"))
        throw new RuntimeException(
            "AuthenticationError: Invalid username or password", sqlException);
      else if (sqlException.getMessage().contains("FATAL: database"))
        throw new RuntimeException("InvalidDatabase: " + sqlException.getMessage(), sqlException);
      else if (sqlException.getMessage().contains("FATAL: role"))
        throw new RuntimeException("InvalidUsername: " + sqlException.getMessage(), sqlException);
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
