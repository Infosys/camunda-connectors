/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mysql.model.request;

import com.infosys.camundaconnectors.db.mysql.model.response.MySQLResponse;
import com.infosys.camundaconnectors.db.mysql.utility.DatabaseClient;

import java.sql.Connection;
import java.sql.SQLException;

public interface MySQLRequestData {
  MySQLResponse invoke(final DatabaseClient databaseClient,final DatabaseConnection databaseConnection, final String databaseName) throws SQLException;

  String getDatabaseName();
}
