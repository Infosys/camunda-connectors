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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListGroupsService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ListUsersService.class);
  private String count;
  private String groupType;
  private String includeUsercount;
  private String searchText;
  DocuSignResponse<?> listGroupsResponse;
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
            + "/groups";
    String url = buildUri(basePath);
    Map<String, Object> res = httpService.getRequest(url, authentication);
    errorHandler.checkForErrorDetails(res);
    listGroupsResponse = new DocuSignResponse<Map<String, Object>>(res);
    LOGGER.info("Response", listGroupsResponse);
    return listGroupsResponse;
  }

  public boolean isNullStr(String str) {
    return str == null || str.equals("") || str.equals("None");
  }

  public boolean isNullList(List<String> list) {
    return list == null || list.size() == 0;
  }

  private String buildUri(String basePath) {
    String baseUrl = "";
    try {
      URIBuilder uri = new URIBuilder(basePath);

      if (!isNullStr(count)) uri.setParameter("count", count);
      if (!isNullStr(groupType)) {
        if (groupType.equalsIgnoreCase("AdminGroup")
            || groupType.equalsIgnoreCase("CustomGroup")
            || groupType.equalsIgnoreCase("EveryoneGroup"))
          uri.setParameter("group_type", groupType);
        else
          throw new RuntimeException("grouptype should be AdminGroup,CustomGroup or EveryoneGroup");
      }
      if (!isNullStr(includeUsercount)) uri.setParameter("include_usercount", includeUsercount);
      if (!isNullStr(searchText)) uri.setParameter("search_text", searchText);
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

  public String getGroupType() {
    return groupType;
  }

  public void setGroupType(String groupType) {
    this.groupType = groupType;
  }

  public String getIncludeUsercount() {
    return includeUsercount;
  }

  public void setIncludeUsercount(String includeUsercount) {
    this.includeUsercount = includeUsercount;
  }

  public String getSearchText() {
    return searchText;
  }

  public void setSearchText(String searchText) {
    this.searchText = searchText;
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, groupType, includeUsercount, listGroupsResponse, searchText);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ListGroupsService other = (ListGroupsService) obj;
    return Objects.equals(count, other.count)
        && Objects.equals(groupType, other.groupType)
        && Objects.equals(includeUsercount, other.includeUsercount)
        && Objects.equals(listGroupsResponse, other.listGroupsResponse)
        && Objects.equals(searchText, other.searchText);
  }

  @Override
  public String toString() {
    return "ListGroupsService [count="
        + count
        + ", groupType="
        + groupType
        + ", includeUsercount="
        + includeUsercount
        + ", searchText="
        + searchText
        + ", listGroupsResponse="
        + listGroupsResponse
        + "]";
  }
}
