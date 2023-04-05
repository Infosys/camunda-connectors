/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.imap.model.request;

import com.infosys.camundaconnectors.email.imap.model.response.Response;
import com.infosys.camundaconnectors.email.imap.utility.MailServerClient;
import io.camunda.connector.api.annotation.Secret;
import java.util.Objects;
import javax.mail.Folder;
import javax.mail.Store;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class IMAPRequest<T extends IMAPRequestData> {
  @NotNull @Valid @Secret private Authentication authentication;
  @NotBlank private String operation;
  @Valid @NotNull private T data;

  public Response invoke(final MailServerClient mailServerClient) {
    Store store = mailServerClient.getStore(authentication);
    Folder folder = mailServerClient.getFolder(store, authentication);
    return data.invoke(store, folder);
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
    IMAPRequest<?> that = (IMAPRequest<?>) o;
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
    return "IMAPRequest{"
        + "authentication=authentication"
        + ", operation='"
        + operation
        + '\''
        + ", data="
        + data
        + '}';
  }
}
