/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.docusign.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.service.AddEnvelopeCustomFieldsService;
import com.infosys.camundaconnectors.docusign.service.AddRecipientsToEnvelopeService;
import com.infosys.camundaconnectors.docusign.service.AddUsersToGroupService;
import com.infosys.camundaconnectors.docusign.service.CreateEnvelopeService;
import com.infosys.camundaconnectors.docusign.service.CreateUsersService;
import com.infosys.camundaconnectors.docusign.service.CustomApiRequestService;
import com.infosys.camundaconnectors.docusign.service.DeleteUsersFromAccountService;
import com.infosys.camundaconnectors.docusign.service.DeleteUsersFromGroupService;
import com.infosys.camundaconnectors.docusign.service.DownloadDocumentsFromEnvelopeService;
import com.infosys.camundaconnectors.docusign.service.GetEnvelopeService;
import com.infosys.camundaconnectors.docusign.service.GetUserService;
import com.infosys.camundaconnectors.docusign.service.ListGroupUsersService;
import com.infosys.camundaconnectors.docusign.service.ListGroupsService;
import com.infosys.camundaconnectors.docusign.service.ListUsersService;
import com.infosys.camundaconnectors.docusign.service.SearchCustomFieldsInEnvelopeService;
import com.infosys.camundaconnectors.docusign.service.SearchEnvelopesService;
import com.infosys.camundaconnectors.docusign.service.SendDraftEnvelopesService;
import com.infosys.camundaconnectors.docusign.service.UpdateDocumentsToEnvelopeService;
import com.infosys.camundaconnectors.docusign.service.UpdateEnvelopeCustomFieldsService;
import com.infosys.camundaconnectors.docusign.service.UpdateUserService;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class DocuSignRequest<T extends DocuSignRequestData> {
  @NotNull @Valid private Authentication authentication;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
      property = "operation")
  @JsonSubTypes(
      value = {
        @JsonSubTypes.Type(value = CreateEnvelopeService.class, name = "docusign.createEnvelope"),
        @JsonSubTypes.Type(value = GetEnvelopeService.class, name = "docusign.getEnvelope"),
        @JsonSubTypes.Type(
            value = UpdateDocumentsToEnvelopeService.class,
            name = "docusign.uploadDocumentsToEnvelope"),
        @JsonSubTypes.Type(
            value = SendDraftEnvelopesService.class,
            name = "docusign.sendDraftEnvelope"),
        @JsonSubTypes.Type(value = SearchEnvelopesService.class, name = "docusign.searchEnvelopes"),
        @JsonSubTypes.Type(
            value = AddRecipientsToEnvelopeService.class,
            name = "docusign.addRecipientsToEnvelope"),
        @JsonSubTypes.Type(
            value = AddEnvelopeCustomFieldsService.class,
            name = "docusign.addEnvelopeCustomeFields"),
        @JsonSubTypes.Type(
            value = UpdateEnvelopeCustomFieldsService.class,
            name = "docusign.updateEnvelopeCustomeFields"),
        @JsonSubTypes.Type(
            value = SearchCustomFieldsInEnvelopeService.class,
            name = "docusign.searchEnvelopeInCustomeFields"),
        @JsonSubTypes.Type(
            value = DownloadDocumentsFromEnvelopeService.class,
            name = "docusign.downloadDocumentsFromEnvelope"),
        @JsonSubTypes.Type(value = CreateUsersService.class, name = "docusign.createUsers"),
        @JsonSubTypes.Type(value = AddUsersToGroupService.class, name = "docusign.addUsersToGroup"),
        @JsonSubTypes.Type(
            value = DeleteUsersFromAccountService.class,
            name = "docusign.deleteUsers"),
        @JsonSubTypes.Type(value = GetUserService.class, name = "docusign.getUser"),
        @JsonSubTypes.Type(value = ListUsersService.class, name = "docusign.listUsers"),
        @JsonSubTypes.Type(value = UpdateUserService.class, name = "docusign.updateUser"),
        @JsonSubTypes.Type(value = ListGroupsService.class, name = "docusign.listGroups"),
        @JsonSubTypes.Type(value = ListGroupUsersService.class, name = "docusign.listGroupUsers"),
        @JsonSubTypes.Type(
            value = DeleteUsersFromGroupService.class,
            name = "docusign.deleteUsersFromGroup"),
        @JsonSubTypes.Type(
            value = CustomApiRequestService.class,
            name = "docusign.customApiRequest")
      })
  @Valid
  @NotNull
  private T data;

  public Response invoke(HttpServiceUtils service) throws Exception {
    return data.invoke(authentication, service);
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
    return "DocuSignRequest [authentication=" + authentication + ", data=" + data + "]";
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
    DocuSignRequest other = (DocuSignRequest) obj;
    return Objects.equals(authentication, other.authentication) && Objects.equals(data, other.data);
  }
}
