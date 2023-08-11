/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.oracle.v2;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.db.oracle.GsonSupplier;
import com.infosys.camundaconnectors.db.oracle.model.request.OracleDBRequest;
import com.infosys.camundaconnectors.db.oracle.utility.DatabaseClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "Oracle v2",
    inputVariables = {"databaseConnection", "operation", "data"},
    type = "com.infosys.camundaconnectors.db:oracle:2")
public class OracleDBFunction implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(OracleDBFunction.class);
  private final Gson gson;
  private final DatabaseClient databaseClient;

  public OracleDBFunction() {
    this(GsonSupplier.getGson(), new DatabaseClient());
  }

  public OracleDBFunction(Gson gson, DatabaseClient databaseClient) {
    this.gson = gson;
    this.databaseClient = databaseClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    final var variables = outboundConnectorContext.getVariables();
    final var oracleDBRequest = gson.fromJson(variables, OracleDBRequest.class);
    outboundConnectorContext.replaceSecrets(oracleDBRequest);
    outboundConnectorContext.validate(oracleDBRequest);
    LOGGER.debug("Request verified successfully and all required secrets replaced");
    return oracleDBRequest.invoke(databaseClient);
  }
}
