/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.oracle;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.db.oracle.model.request.OracleDBRequest;
import com.infosys.camundaconnectors.db.oracle.utility.DatabaseClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "Oracle",
    inputVariables = {"databaseConnection", "operation", "data"},
    type = "com.infosys.camundaconnectors.db:oracle:1")
public class OracleDBFunction implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(OracleDBFunction.class);
  private final DatabaseClient databaseClient;

  public OracleDBFunction() {
    this(new DatabaseClient());
  }

  public OracleDBFunction(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    final var variables = outboundConnectorContext.bindVariables(OracleDBRequest.class);
    LOGGER.debug("Request verified successfully and all required secrets replaced");
    return variables.invoke(databaseClient);
  }
}
