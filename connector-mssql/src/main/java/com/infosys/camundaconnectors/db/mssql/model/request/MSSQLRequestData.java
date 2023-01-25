/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.model.request;

import com.infosys.camundaconnectors.db.mssql.model.response.MSSQLResponse;
import java.sql.Connection;
import java.sql.SQLException;

public interface MSSQLRequestData {
  MSSQLResponse invoke(final Connection connection) throws SQLException;

  String getDatabaseName();
}
