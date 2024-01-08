/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.imap.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import com.infosys.camundaconnectors.email.imap.service.DeleteEmailService;
import com.infosys.camundaconnectors.email.imap.service.DownloadEmailService;
import com.infosys.camundaconnectors.email.imap.service.ListEmailsService;
import com.infosys.camundaconnectors.email.imap.service.MoveEmailService;
import com.infosys.camundaconnectors.email.imap.service.ReadEmailService;
import com.infosys.camundaconnectors.email.imap.service.SearchEmailsService;
import com.infosys.camundaconnectors.email.imap.utility.MailServerClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import javax.mail.Folder;
import javax.mail.Store;

public class IMAPRequest<T extends IMAPRequestData> {
  @NotNull @Valid private Authentication authentication;
  @NotBlank private String operation;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
      property = "operation")
  @JsonSubTypes(
      value = {
        @JsonSubTypes.Type(value = DeleteEmailService.class, name = "imap.delete-email"),
        @JsonSubTypes.Type(value = DownloadEmailService.class, name = "imap.download-email"),
        @JsonSubTypes.Type(value = ListEmailsService.class, name = "imap.list-emails"),
        @JsonSubTypes.Type(value = MoveEmailService.class, name = "imap.move-email"),
        @JsonSubTypes.Type(value = ReadEmailService.class, name = "imap.read-email"),
        @JsonSubTypes.Type(value = SearchEmailsService.class, name = "imap.search-emails")
      })
  @Valid
  @NotNull
  private T data;

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
