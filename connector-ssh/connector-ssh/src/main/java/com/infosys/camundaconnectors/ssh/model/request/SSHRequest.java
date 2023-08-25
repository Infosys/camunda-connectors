/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.ssh.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.ssh.model.response.Response;
import com.infosys.camundaconnectors.ssh.service.ExecuteCommandService;
import com.infosys.camundaconnectors.ssh.utility.SshServerClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import net.schmizz.sshj.SSHClient;

public class SSHRequest<T extends SSHRequestData> {

  @NotNull @Valid private Authentication authentication;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
      property = "operation")
  @JsonSubTypes(
      value = {
        @JsonSubTypes.Type(value = ExecuteCommandService.class, name = "ssh.executeCommand")
      })
  @Valid
  @NotNull
  private T data;

  public Response invoke(final SshServerClient sshServerClient) throws Exception {
    SSHClient sshClient = sshServerClient.loginSSH(authentication);
    return data.invoke(sshClient, authentication.getOperatingSystem());
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "SSHRequest [authentication=" + authentication + ", data=" + data + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(authentication, data);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SSHRequest other = (SSHRequest) obj;
    return Objects.equals(authentication, other.authentication) && Objects.equals(data, other.data);
  }
}
