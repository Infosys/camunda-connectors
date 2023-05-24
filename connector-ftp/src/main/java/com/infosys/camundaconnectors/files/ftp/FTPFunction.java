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
  private final Gson gson;
  private final FTPServerClient fTPServerClient;
  public FTPFunction() {
    this(GsonSupplier.getGson(), new FTPServerClient());
  }
  public FTPFunction(Gson gson, FTPServerClient fTPServerClient) {
    this.gson = gson;
    this.fTPServerClient = fTPServerClient;
  }
  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
	LOGGER.info("Request verified successfully and all required secrets replaced"); 
    final var variables = outboundConnectorContext.getVariables();
    final var ftpRequest = gson.fromJson(variables, FTPRequest.class);
    outboundConnectorContext.validate(ftpRequest);
    outboundConnectorContext.replaceSecrets(ftpRequest);
    LOGGER.info("Request verified successfully and all required secrets replaced");
    return ftpRequest.invoke(fTPServerClient);
  }
}
