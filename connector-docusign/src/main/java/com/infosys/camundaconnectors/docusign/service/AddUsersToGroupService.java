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

public class AddUsersToGroupService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(AddUsersToGroupService.class);
  @NotNull private String payloadType;
  private List<String> userIds;
  private Map<String, Object> payload;
  @NotNull private String groupId;
  DocuSignResponse<?> addUsersToGroupResponse;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpRequest)
      throws IOException {
    if (groupId == null || groupId.equals(""))
      throw new RuntimeException("GroupId can not be null");
    if (payloadType == null || payloadType.equals(""))
      throw new RuntimeException("payloadType can not be null");
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/groups/"
            + groupId
            + "/users";
    String payLoad = "";
    if (payloadType.equalsIgnoreCase("entirepayload")) payLoad = getJsonpayload();
    else payLoad = getJsonPayloadFromUserIds();
    Map<String, Object> response = httpRequest.putRequest(basePath, payLoad, authentication);
    errorHandler.checkForErrorDetails(response);
    addUsersToGroupResponse = new DocuSignResponse<Map<String, Object>>(response);
    LOGGER.info("Response", addUsersToGroupResponse);
    return addUsersToGroupResponse;
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

  private String getJsonpayload() {
    if (payload == null || payload.equals(""))
      throw new RuntimeException("Payload can not be null");
    Gson gson = new Gson();
    return gson.toJson(payload);
  }

  public String getPayloadType() {
    return payloadType;
  }

  public List<String> getUserIds() {
    return userIds;
  }

  public void setPayloadType(String payloadType) {
    this.payloadType = payloadType;
  }

  public void setUserIds(List<String> userIds) {
    this.userIds = userIds;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setPayload(Map<String, Object> payload) {
    this.payload = payload;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(addUsersToGroupResponse, groupId, payload, payloadType, userIds);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AddUsersToGroupService other = (AddUsersToGroupService) obj;
    return Objects.equals(addUsersToGroupResponse, other.addUsersToGroupResponse)
        && Objects.equals(groupId, other.groupId)
        && Objects.equals(payload, other.payload)
        && Objects.equals(payloadType, other.payloadType)
        && Objects.equals(userIds, other.userIds);
  }

  @Override
  public String toString() {
    return "AddUsersToGroupService [payloadType="
        + payloadType
        + ", userIds="
        + userIds
        + ", payload="
        + payload
        + ", groupId="
        + groupId
        + ", AddUsersToGroupResponse="
        + addUsersToGroupResponse
        + "]";
  }
}
