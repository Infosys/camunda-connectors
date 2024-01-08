/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequest;
import com.infosys.camundaconnectors.files.ftp.utility.FTPServerClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "ftp-connector",
    inputVariables = {"authentication", "operation", "data"},
    type = "com.infosys.camundaconnectors.files:ftp:1")
public class FTPFunction implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(FTPFunction.class);
  private final FTPServerClient fTPServerClient;

  public FTPFunction() {
    this(new FTPServerClient());
  }

  public FTPFunction(FTPServerClient fTPServerClient) {
    this.fTPServerClient = fTPServerClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    LOGGER.info("Request verified successfully and all required secrets replaced");
    final var variables = outboundConnectorContext.bindVariables(FTPRequest.class);
    LOGGER.info("Request verified successfully and all required secrets replaced");
    return variables.invoke(fTPServerClient);
  }
}
