/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.agile.jira;

import com.infosys.camundaconnectors.agile.jira.model.request.JIRARequest;
import com.infosys.camundaconnectors.agile.jira.utility.JIRAServerClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "jira-connector",
    inputVariables = {"authentication", "operation", "data"},
    type = "com.infosys.camundaconnectors.agile:jira:1")
public class JIRAFunction implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(JIRAFunction.class);
  private final JIRAServerClient jiraServerClient;

  public JIRAFunction() {
    this(new JIRAServerClient());
  }

  public JIRAFunction(JIRAServerClient jiraServerClient) {
    this.jiraServerClient = jiraServerClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    LOGGER.info("Request verified successfully and all required secrets replaced");
    final var variables = outboundConnectorContext.bindVariables(JIRARequest.class);
    LOGGER.info("Request verified successfully and all required secrets replaced");
    return variables.invoke(jiraServerClient);
  }
}
