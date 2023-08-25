/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.ssh;

import com.infosys.camundaconnectors.ssh.model.request.SSHRequest;
import com.infosys.camundaconnectors.ssh.utility.SshServerClient;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;

@OutboundConnector(
    name = "SSH",
    inputVariables = {"authentication", "operation", "data"},
    type = "com.infosys.camundaconnectors.ssh:ssh:1")
public class SSHFunction implements OutboundConnectorFunction {

  private final SshServerClient sshServerClient;

  public SSHFunction() {
    this(new SshServerClient());
  }

  public SSHFunction(final SshServerClient sshServerClient) {
    this.sshServerClient = sshServerClient;
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    var request = outboundConnectorContext.bindVariables(SSHRequest.class);
    Object response = request.invoke(sshServerClient);
    sshServerClient.logoutSSH();
    return response;
  }
}
