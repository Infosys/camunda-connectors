/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.postgresql.model.request;

import com.infosys.camundaconnectors.db.postgresql.model.response.PostgreSQLResponse;
import com.infosys.camundaconnectors.db.postgresql.utility.DatabaseClient;

import java.sql.SQLException;

public interface PostgreSQLRequestData {
  PostgreSQLResponse invoke(final DatabaseClient databaseClient,DatabaseConnection databaseConnection,String databaseName) throws SQLException;
  String getDatabaseName();
}
