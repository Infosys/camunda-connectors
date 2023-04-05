/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.email.pop3.model.request.POP3Request;
import com.infosys.camundaconnectors.email.pop3.utility.MailServerClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "POP3",
    inputVariables = {"authentication", "operation", "data"},
    type = "com.infosys.camundaconnectors.email:pop3:1")
public class POP3Function implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(POP3Function.class);
  private final Gson gson;
  private final MailServerClient mailServerClient;

  public POP3Function() {
    this(GsonSupplier.getGson(), new MailServerClient());
  }

  public POP3Function(Gson gson, MailServerClient mailServerClient) {
    this.gson = gson;
    this.mailServerClient = mailServerClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) {
    final var variables = outboundConnectorContext.getVariables();
    final var pop3Request = gson.fromJson(variables, POP3Request.class);
    outboundConnectorContext.validate(pop3Request);
    outboundConnectorContext.replaceSecrets(pop3Request);
    LOGGER.debug("Request verified successfully and all required secrets replaced");
    return pop3Request.invoke(mailServerClient);
  }
}
