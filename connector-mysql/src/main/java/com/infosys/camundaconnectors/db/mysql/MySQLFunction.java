/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mysql;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.db.mysql.model.request.MySQLRequest;
import com.infosys.camundaconnectors.db.mysql.utility.DatabaseClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "MySQL",
    inputVariables = {"databaseConnection", "operation", "data"},
    type = "com.infosys.camundaconnectors.db:mysql:1")
public class MySQLFunction implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(MySQLFunction.class);
  private final Gson gson;
  private final DatabaseClient databaseClient;

  public MySQLFunction() {
    this(GsonSupplier.getGson(), new DatabaseClient());
  }

  public MySQLFunction(Gson gson, DatabaseClient databaseClient) {
    this.gson = gson;
    this.databaseClient = databaseClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    final var variables = outboundConnectorContext.getVariables();
    final var mySQLRequest = gson.fromJson(variables, MySQLRequest.class);
    outboundConnectorContext.validate(mySQLRequest);
    outboundConnectorContext.replaceSecrets(mySQLRequest);
    LOGGER.debug("Request verified successfully and all required secrets replaced");
    return mySQLRequest.invoke(databaseClient);
  }
}
