/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.smtp;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.email.smtp.model.SMTPResponse;
import com.infosys.camundaconnectors.email.smtp.model.request.SMTPRequest;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import jakarta.mail.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "SMTP",
    inputVariables = {
      "authentication",
      "smtpEmailMailBoxName",
      "smtpEmailToRecipients",
      "smtpEmailCcRecipients",
      "smtpEmailBccRecipients",
      "smtpEmailSubject",
      "smtpEmailContentType",
      "smtpEmailContent",
      "smtpEmailAttachments",
      "smtpEmailImportance",
      "smtpEmailReadReceipt",
      "smtpEmailFollowUp",
      "smtpEmailDirectReplyTo",
      "smtpEmailSensitivity"
    },
    type = "com.infosys.camundaconnectors.email:smtp:1")
public class SMTPFunction implements OutboundConnectorFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(SMTPFunction.class);

  private final SendMailService service;

  public SMTPFunction() {
    this(new SendMailService());
  }

  public SMTPFunction(final SendMailService service) {
    this.service = service;
  }

  @Override
  public Object execute(final OutboundConnectorContext context) {
    //final SMTPRequest request = new Gson().fromJson(context.getVariables(), SMTPRequest.class);
    //context.validate(request);
    //LOGGER.debug("Request verified successfully and all required secrets replaced");
    //return executeConnector(request);
	final var smtpRequest = context.bindVariables(SMTPRequest.class);
	LOGGER.info("Request verified successfully and all required secrets replaced");
	return executeConnector(smtpRequest);
  }

  private SMTPResponse executeConnector(final SMTPRequest request) {
    LOGGER.debug("Executing connector with request {}", request);
    Session session = new SMTPSession().getSession(request.getAuthentication());
    return service.execute(session, request);
  }
}
