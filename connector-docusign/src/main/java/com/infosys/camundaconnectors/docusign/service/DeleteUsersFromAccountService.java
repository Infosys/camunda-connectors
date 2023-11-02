/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.request.DocuSignRequestData;
import com.infosys.camundaconnectors.docusign.model.response.DocuSignResponse;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.ErrorHandler;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUsersFromAccountService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteUsersFromAccountService.class);
  @NotNull private List<String> userIds;
  private String groups;
  private String permissionSet;
  private String signingGroupsEmail;
  DocuSignResponse<?> deleteUsersFromAccountResponse;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpService)
      throws IOException {
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/users";
    try {
      String queryParameter = "";
      URIBuilder uriBuilder = new URIBuilder(basePath);
      if (!isNullStr(groups)) queryParameter += "groups";
      if (!isNullStr(permissionSet))
        queryParameter +=
            (queryParameter.equalsIgnoreCase("") ? ("PermissionSet") : (",PermissionSet"));
      if (!isNullStr(signingGroupsEmail))
        queryParameter +=
            (queryParameter.equalsIgnoreCase("")
                ? ("signingGroupsEmail")
                : (",signingGroupsEmail"));
      if (!queryParameter.equals("")) uriBuilder.addParameter("delete", queryParameter);
      String payload = getJsonPayload();
      Map<String, Object> response =
          httpService.deleteRequest(uriBuilder.build().toString(), payload, authentication);
      errorHandler.checkForErrorDetails(response);
      deleteUsersFromAccountResponse = new DocuSignResponse<Map<String, Object>>(response);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e.getMessage());
    }
    LOGGER.info("Response", deleteUsersFromAccountResponse);
    return deleteUsersFromAccountResponse;
  }

  private String getJsonPayload() {
    if (userIds == null || userIds.size() == 0 || userIds.equals(""))
      throw new RuntimeException("UserIds cannot be null");
    JsonObject payloadObject = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    for (String id : userIds) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("userId", id);
      jsonArray.add(jsonObject);
    }
    payloadObject.add("users", jsonArray);
    return payloadObject.toString();
  }

  private boolean isNullStr(String str) {
    return str == null || str.equals("") || str.equalsIgnoreCase("false");
  }

  public List<String> getUserIds() {
    return userIds;
  }

  public void setUserIds(List<String> userIds) {
    this.userIds = userIds;
  }

  public String getGroups() {
    return groups;
  }

  public void setGroups(String groups) {
    this.groups = groups;
  }

  public String getPermissionSet() {
    return permissionSet;
  }

  public void setPermissionSet(String permissionSet) {
    this.permissionSet = permissionSet;
  }

  public String getSigningGroupsEmail() {
    return signingGroupsEmail;
  }

  public void setSigningGroupsEmail(String signingGroupsEmail) {
    this.signingGroupsEmail = signingGroupsEmail;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        deleteUsersFromAccountResponse, groups, permissionSet, signingGroupsEmail, userIds);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DeleteUsersFromAccountService other = (DeleteUsersFromAccountService) obj;
    return Objects.equals(deleteUsersFromAccountResponse, other.deleteUsersFromAccountResponse)
        && Objects.equals(groups, other.groups)
        && Objects.equals(permissionSet, other.permissionSet)
        && Objects.equals(signingGroupsEmail, other.signingGroupsEmail)
        && Objects.equals(userIds, other.userIds);
  }

  @Override
  public String toString() {
    return "DeleteUsersFromAccountService [userIds="
        + userIds
        + ", groups="
        + groups
        + ", permissionSet="
        + permissionSet
        + ", signingGroupsEmail="
        + signingGroupsEmail
        + ", deleteUsersFromAccountResponse="
        + deleteUsersFromAccountResponse
        + "]";
  }
}
