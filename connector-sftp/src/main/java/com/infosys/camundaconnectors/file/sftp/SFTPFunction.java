/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequest;
import com.infosys.camundaconnectors.file.sftp.utility.SftpServerClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "SFTP",
    inputVariables = {"authentication", "operation", "data"},
    type = "com.infosys.camundaconnectors.files:sftp:1")
public class SFTPFunction implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(SFTPFunction.class);
  private final Gson gson;
  private final SftpServerClient sftpServerClient;

  public SFTPFunction() {
    this(GsonSupplier.getGson(), new SftpServerClient());
  }

  public SFTPFunction(Gson gson, SftpServerClient sftpServerClient) {
    this.gson = gson;
    this.sftpServerClient = sftpServerClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    final var variables = outboundConnectorContext.getVariables();
    final var sftpRequest = gson.fromJson(variables, SFTPRequest.class);
    outboundConnectorContext.validate(sftpRequest);
    outboundConnectorContext.replaceSecrets(sftpRequest);
    LOGGER.debug("Request verified successfully and all required secrets replaced");
    Object res = sftpRequest.invoke(sftpServerClient);
    sftpServerClient.logoutSftp();
    return res;
  }
}
