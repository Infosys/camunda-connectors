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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListGroupUsersService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ListGroupUsersService.class);
  private String count;
  private String groupId;
  DocuSignResponse<?> listGroupUsersResponse;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpService)
      throws IOException {
    if (groupId == null || groupId.equals("")) throw new RuntimeException("GroupId cannot be null");
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/groups/"
            + groupId
            + "/users";
    String url = buildUri(basePath);
    Map<String, Object> res = httpService.getRequest(url, authentication);
    errorHandler.checkForErrorDetails(res);
    listGroupUsersResponse = new DocuSignResponse<Map<String, Object>>(res);
    LOGGER.info("Response", listGroupUsersResponse);
    return listGroupUsersResponse;
  }

  public boolean isNullStr(String str) {
    return str == null || str.equals("") || str.equals("None");
  }

  private String buildUri(String basePath) {
    String baseUrl = "";
    try {
      URIBuilder uri = new URIBuilder(basePath);
      if (!isNullStr(count)) uri.setParameter("count", count);
      baseUrl = uri.build().toString();

    } catch (URISyntaxException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
    return baseUrl;
  }

  public String getCount() {
    return count;
  }

  public void setCount(String count) {
    this.count = count;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, groupId, listGroupUsersResponse);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ListGroupUsersService other = (ListGroupUsersService) obj;
    return Objects.equals(count, other.count)
        && Objects.equals(groupId, other.groupId)
        && Objects.equals(listGroupUsersResponse, other.listGroupUsersResponse);
  }

  @Override
  public String toString() {
    return "ListGroupUsersService [count="
        + count
        + ", groupId="
        + groupId
        + ", listGroupUsersResponse="
        + listGroupUsersResponse
        + "]";
  }
}
