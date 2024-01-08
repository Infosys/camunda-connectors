/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3;

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
  private final MailServerClient mailServerClient;

  public POP3Function() {
    this(new MailServerClient());
  }

  public POP3Function(MailServerClient mailServerClient) {
    this.mailServerClient = mailServerClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) {
    LOGGER.info("Request verified successfully and all required secrets replaced");
    final var variables = outboundConnectorContext.bindVariables(POP3Request.class);
    LOGGER.info("Request verified successfully and all required secrets replaced");
    return variables.invoke(mailServerClient);
  }
}
