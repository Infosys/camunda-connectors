/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.imap;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.email.imap.model.request.IMAPRequest;
import com.infosys.camundaconnectors.email.imap.utility.MailServerClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "IMAP",
    inputVariables = {"authentication", "operation", "data"},
    type = "com.infosys.camundaconnectors.email:imap:1")
public class IMAPFunction implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(IMAPFunction.class);
  private final Gson gson;
  private final MailServerClient mailServerClient;

  public IMAPFunction() {
    this(GsonSupplier.getGson(), new MailServerClient());
  }

  public IMAPFunction(Gson gson, MailServerClient mailServerClient) {
    this.gson = gson;
    this.mailServerClient = mailServerClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) {
    final var variables = outboundConnectorContext.getVariables();
    final var imapRequest = gson.fromJson(variables, IMAPRequest.class);
    outboundConnectorContext.validate(imapRequest);
    outboundConnectorContext.replaceSecrets(imapRequest);
    LOGGER.debug("Request verified successfully and all required secrets replaced");
    return imapRequest.invoke(mailServerClient);
  }
}
