/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.docusign;

import com.infosys.camundaconnectors.docusign.model.request.DocuSignRequest;
import com.infosys.camundaconnectors.docusign.service.AddEnvelopeCustomFieldsService;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "DocuSign",
    inputVariables = {"authentication", "operation", "data"},
    type = "com.infosys.camundaconnectors.docusign:docusign:1")
public class DocuSignFunction implements OutboundConnectorFunction {
  HttpServiceUtils service;
  private static final Logger LOGGER =
      LoggerFactory.getLogger(AddEnvelopeCustomFieldsService.class);

  public DocuSignFunction() {
    this(new HttpServiceUtils());
  }

  public DocuSignFunction(HttpServiceUtils service) {
    this.service = service;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    var request = outboundConnectorContext.bindVariables(DocuSignRequest.class);
    LOGGER.info("Request verified successfully and all required secrets replaced");
    Object result = request.invoke(service);
    return result;
  }
}
