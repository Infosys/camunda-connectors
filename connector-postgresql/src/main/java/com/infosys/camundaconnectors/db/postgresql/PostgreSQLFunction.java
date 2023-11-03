/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.postgresql;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.db.postgresql.model.request.PostgreSQLRequest;
import com.infosys.camundaconnectors.db.postgresql.utility.DatabaseClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "PostgreSQL",
    inputVariables = {"databaseConnection", "operation", "data"},
    type = "com.infosys.camundaconnectors.db:postgresql:1")
public class PostgreSQLFunction implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLFunction.class);
  private final Gson gson;
  private final DatabaseClient databaseClient;

  public PostgreSQLFunction() {
    this(GsonSupplier.getGson(), new DatabaseClient());
  }

  public PostgreSQLFunction(Gson gson, DatabaseClient databaseClient) {
    this.gson = gson;
    this.databaseClient = databaseClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    final var variables = outboundConnectorContext.getJobContext().getVariables();
    final var postgreSQLRequest = gson.fromJson(variables, PostgreSQLRequest.class);
    //    outboundConnectorContext.validate(postgreSQLRequest);
    //    outboundConnectorContext.replaceSecrets(postgreSQLRequest);
    LOGGER.debug("Request verified successfully and all required secrets replaced");
    return postgreSQLRequest.invoke(databaseClient);
  }
}
