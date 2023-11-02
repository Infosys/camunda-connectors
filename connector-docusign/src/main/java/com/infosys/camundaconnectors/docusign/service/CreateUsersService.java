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

public class CreateUsersService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateUsersService.class);
  @NotNull private List<Map<String, Object>> payload;
  DocuSignResponse<?> createUsersResponse;
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
    String jsonPayLoad = getJsonPayload();
    Map<String, Object> response = httpService.postRequest(basePath, jsonPayLoad, authentication);
    createUsersResponse = new DocuSignResponse<Map<String, Object>>(response);
    errorHandler.checkForErrorDetails(response);
    LOGGER.info("Response", createUsersResponse);
    return createUsersResponse;
  }

  private String getJsonPayload() {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    for (Map<String, Object> object : payload) {
      if (object.containsKey("userName") && object.containsKey("email")) {
        JsonObject userObject = gson.toJsonTree(object).getAsJsonObject();
        jsonArray.add(userObject);
      } else throw new RuntimeException("you must provide the userName and email properties");
    }
    jsonObject.add("newUsers", jsonArray);
    return jsonObject.toString();
  }

  public List<Map<String, Object>> getPayload() {
    return payload;
  }

  public void setPayload(List<Map<String, Object>> payload) {
    this.payload = payload;
  }

  @Override
  public int hashCode() {
    return Objects.hash(createUsersResponse, payload);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CreateUsersService other = (CreateUsersService) obj;
    return Objects.equals(createUsersResponse, other.createUsersResponse)
        && Objects.equals(payload, other.payload);
  }

  @Override
  public String toString() {
    return "CreateUsersService [payload="
        + payload
        + ", createUsersResponse="
        + createUsersResponse
        + "]";
  }
}
