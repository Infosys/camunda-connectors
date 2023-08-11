/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.oracle.utility;

import com.infosys.camundaconnectors.db.oracle.model.request.DatabaseConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Creates {@link Connection} Object for database */
public class DatabaseClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseClient.class);

  public DatabaseClient() {}

  public Connection getConnectionObject(DatabaseConnection databaseConnection, String databaseName)
      throws SQLException {
    String dbURL =
        "jdbc:oracle:thin:@"
            + databaseConnection.getHost().strip()
            + ":"
            + databaseConnection.getPort().strip();
    if (databaseName != null && !databaseName.isBlank()) dbURL += databaseConnection.getConnectionTypeSeparator() + databaseName;
    return connectToDatabase(databaseConnection, dbURL);
  }

  private Connection connectToDatabase(DatabaseConnection databaseConnection, String dbURL)
      throws SQLException {
    Connection conn;
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver");
      conn =
          DriverManager.getConnection(
              dbURL, databaseConnection.getUsername(), databaseConnection.getPassword());
    } catch (ClassNotFoundException clsEx) {
      LOGGER.error("Unable to load oracle Driver: {}", clsEx.getMessage());
      throw new RuntimeException("Unable to load oracle Driver: " + clsEx.getMessage(), clsEx);
    } catch (SQLException sqlException) {
      LOGGER.error("SQLException: {}", sqlException.getMessage());
      if (sqlException.getMessage().contains("ORA-12505"))
        throw new RuntimeException("InvalidDatabase: Database doesn't exist", sqlException);
      throw sqlException;
    }
    try {
      if (conn == null || !conn.isValid(1)) {
        LOGGER.error("ConnectionError: Unable to connect to Database");
        throw new RuntimeException("ConnectionError: Unable to connect to Database");
      } else LOGGER.debug("Connected successfully to the database");
    } catch (SQLException e) {
      LOGGER.error("ConnectionError: Unable to connect to Database");
      throw new RuntimeException("ConnectionError: Unable to connect to Database");
    }
    return conn;
  }
}
