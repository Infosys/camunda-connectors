/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.request.DocuSignRequestData;
import com.infosys.camundaconnectors.docusign.model.response.DocuSignResponse;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.ErrorHandler;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(GetUserService.class);
  @NotNull private String userId;
  private DocuSignResponse<?> getUserResponse;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpService)
      throws IOException {
    if (userId == null || userId.equals("")) throw new RuntimeException("userId can not be null");
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/users/"
            + userId;
    Map<String, Object> res = httpService.getRequest(basePath, authentication);
    errorHandler.checkForErrorDetails(res);
    getUserResponse = new DocuSignResponse<Map<String, Object>>(res);
    LOGGER.info("Response", getUserResponse);
    return getUserResponse;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUserResponse, userId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GetUserService other = (GetUserService) obj;
    return Objects.equals(getUserResponse, other.getUserResponse)
        && Objects.equals(userId, other.userId);
  }

  @Override
  public String toString() {
    return "GetUserService [userId=" + userId + ", getUserResponse=" + getUserResponse + "]";
  }
}
