/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import com.google.gson.Gson;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUsersFromGroupService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteUsersFromAccountService.class);
  @NotNull private String groupId;
  @NotNull private String deleteBy;
  private List<String> userIds;
  private List<Map<String, Object>> payload;
  DocuSignResponse<?> deleteUsersFromGroupResponse;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpService)
      throws IOException {
    if (groupId == null || groupId.equals(""))
      throw new RuntimeException("groupId can not be null");
    if (deleteBy == null || deleteBy.equals(""))
      throw new RuntimeException("deleteBy can not be null");
    if (deleteBy.equalsIgnoreCase("byusersids") && (userIds == null || userIds.size() == 0))
      throw new RuntimeException("userIds can not be null or empty");
    if (deleteBy.equalsIgnoreCase("otherfields") && (payload == null || payload.size() == 0))
      throw new RuntimeException("userIds can not be null or empty");
    if (deleteBy.equalsIgnoreCase("byusersids") && (payload == null || payload.size() == 0))
      throw new RuntimeException("userIds can not be null or empty");
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/groups/"
            + groupId
            + "/users";
    String payload = "";
    if (deleteBy.equalsIgnoreCase("byUserIds")) payload = getJsonPayloadFromUserIds();
    else payload = getJsonPayloadFromOtherFields();
    Map<String, Object> response = httpService.deleteRequest(basePath, payload, authentication);
    errorHandler.checkForErrorDetails(response);
    deleteUsersFromGroupResponse = new DocuSignResponse<Map<String, Object>>(response);
    LOGGER.info("Response", deleteUsersFromGroupResponse);
    return deleteUsersFromGroupResponse;
  }

  private String getJsonPayloadFromOtherFields() {
    JsonObject usersObject = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    Gson gson = new Gson();
    for (Map<String, Object> userDetail : payload) {
      JsonObject userObject = gson.toJsonTree(userDetail).getAsJsonObject();
      jsonArray.add(userObject);
    }
    usersObject.add("users", jsonArray);
    return gson.toJson(usersObject);
  }

  private String getJsonPayloadFromUserIds() {
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

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getDeleteBy() {
    return deleteBy;
  }

  public void setDeleteBy(String deleteBy) {
    this.deleteBy = deleteBy;
  }

  public List<String> getUserIds() {
    return userIds;
  }

  public void setUserIds(List<String> userIds) {
    this.userIds = userIds;
  }

  public List<Map<String, Object>> getPayload() {
    return payload;
  }

  public void setPayload(List<Map<String, Object>> payload) {
    this.payload = payload;
  }

  @Override
  public int hashCode() {
    return Objects.hash(deleteBy, deleteUsersFromGroupResponse, groupId, payload, userIds);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DeleteUsersFromGroupService other = (DeleteUsersFromGroupService) obj;
    return Objects.equals(deleteBy, other.deleteBy)
        && Objects.equals(deleteUsersFromGroupResponse, other.deleteUsersFromGroupResponse)
        && Objects.equals(groupId, other.groupId)
        && Objects.equals(payload, other.payload)
        && Objects.equals(userIds, other.userIds);
  }

  @Override
  public String toString() {
    return "DeleteUsersFromGroupService [groupId="
        + groupId
        + ", deleteBy="
        + deleteBy
        + ", userIds="
        + userIds
        + ", payload="
        + payload
        + ", deleteUsersFromGroupResponse="
        + deleteUsersFromGroupResponse
        + "]";
  }
}
