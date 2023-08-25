/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.model.request;

import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.utility.SftpServerClient;
import io.camunda.connector.api.annotation.Secret;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFTPRequest<T extends SFTPRequestData> {
  @NotNull @Valid @Secret private Authentication authentication;
  @NotBlank private String operation;
  @Valid @NotNull private T data;
  private static final Logger LOGGER = LoggerFactory.getLogger(SftpServerClient.class);

  public Response invoke(final SftpServerClient sftpServerClient) throws Exception {
    SSHClient client = sftpServerClient.loginSftp(authentication);
    SFTPClient sftpClient = client.newSFTPClient();
    LOGGER.info("Connected to the sftp server");
    return data.invoke(sftpClient);
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SFTPRequest<?> that = (SFTPRequest<?>) o;
    return Objects.equals(authentication, that.authentication)
        && Objects.equals(operation, that.operation)
        && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authentication, operation, data);
  }

  @Override
  public String toString() {
    return "SFTPRequest{"
        + "authentication=authentication"
        + ", operation='"
        + operation
        + '\''
        + ", data="
        + data
        + '}';
  }
}
