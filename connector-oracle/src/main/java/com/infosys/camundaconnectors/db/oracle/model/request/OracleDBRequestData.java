/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.oracle.model.request;

import com.infosys.camundaconnectors.db.oracle.model.response.OracleDBResponse;
import java.sql.Connection;
import java.sql.SQLException;

public interface OracleDBRequestData {
  OracleDBResponse invoke(final Connection connection) throws SQLException;

  String getDatabaseName();
}
