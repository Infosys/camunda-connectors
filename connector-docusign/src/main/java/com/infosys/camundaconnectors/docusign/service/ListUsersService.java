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

public class ListUsersService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ListUsersService.class);
  private String email;
  private String emailSubstring;
  private List<String> groupIds;
  private List<String> notGroupIds;
  private List<String> status;
  private String loginStatus;
  private String userNameSubstring;
  private String count;
  private String additionalInfo;
  DocuSignResponse<?> listUsersResponse;
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
    String url = buildUri(basePath);
    Map<String, Object> response = httpService.getRequest(url, authentication);
    errorHandler.checkForErrorDetails(response);
    listUsersResponse = new DocuSignResponse<Map<String, Object>>(response);
    LOGGER.info("Response", listUsersResponse);
    return listUsersResponse;
  }

  public boolean isNullStr(String str) {
    return str == null || str.equals("") || str.equals("None");
  }

  public boolean isNullList(List<String> list) {
    return list == null || list.size() == 0;
  }

  public String buildUri(String basePath) {
    String baseUrl = "";
    try {
      URIBuilder uri = new URIBuilder(basePath);

      if (!isNullList(groupIds)) {
        String allGroupIds = String.join(",", groupIds);
        uri.setParameter("group_id", allGroupIds);
      }

      if (!isNullList(notGroupIds)) {
        String allNotGroupIds = String.join(",", notGroupIds);
        uri.setParameter("not_group_id", allNotGroupIds);
      }
      if (!isNullList(status)) {
        String allStatus = String.join(",", status);
        uri.setParameter("status", allStatus);
      }

      if (!isNullStr(email)) uri.setParameter("email", email);
      if (!isNullStr(count)) uri.setParameter("count", count);
      if (!isNullStr(emailSubstring)) uri.setParameter("email_substring", emailSubstring);
      if (!isNullStr(loginStatus)) uri.setParameter("login_status", loginStatus);
      if (!isNullStr(userNameSubstring)) uri.setParameter("user_name_substring", userNameSubstring);
      if (!isNullStr(additionalInfo)) uri.setParameter("additional_info", additionalInfo);
      baseUrl = uri.build().toString();

    } catch (URISyntaxException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
    return baseUrl;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmailSubstring() {
    return emailSubstring;
  }

  public void setEmailSubstring(String emailSubstring) {
    this.emailSubstring = emailSubstring;
  }

  public List<String> getGroupIds() {
    return groupIds;
  }

  public void setGroupIds(List<String> groupIds) {
    this.groupIds = groupIds;
  }

  public List<String> getNotGroupIds() {
    return notGroupIds;
  }

  public void setNotGroupIds(List<String> notGroupIds) {
    this.notGroupIds = notGroupIds;
  }

  public List<String> getStatus() {
    return status;
  }

  public void setStatus(List<String> status) {
    this.status = status;
  }

  public String getLoginStatus() {
    return loginStatus;
  }

  public void setLoginStatus(String loginStatus) {
    this.loginStatus = loginStatus;
  }

  public String getUserNameSubstring() {
    return userNameSubstring;
  }

  public void setUserNameSubstring(String userNameSubstring) {
    this.userNameSubstring = userNameSubstring;
  }

  public String getCount() {
    return count;
  }

  public void setCount(String count) {
    this.count = count;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public DocuSignResponse<?> getListUsersResponse() {
    return listUsersResponse;
  }

  public void setListUsersResponse(DocuSignResponse<?> listUsersResponse) {
    this.listUsersResponse = listUsersResponse;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        additionalInfo,
        count,
        email,
        emailSubstring,
        groupIds,
        listUsersResponse,
        loginStatus,
        notGroupIds,
        status,
        userNameSubstring);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ListUsersService other = (ListUsersService) obj;
    return Objects.equals(additionalInfo, other.additionalInfo)
        && Objects.equals(count, other.count)
        && Objects.equals(email, other.email)
        && Objects.equals(emailSubstring, other.emailSubstring)
        && Objects.equals(groupIds, other.groupIds)
        && Objects.equals(listUsersResponse, other.listUsersResponse)
        && Objects.equals(loginStatus, other.loginStatus)
        && Objects.equals(notGroupIds, other.notGroupIds)
        && Objects.equals(status, other.status)
        && Objects.equals(userNameSubstring, other.userNameSubstring);
  }

  @Override
  public String toString() {
    return "ListUsersService [email="
        + email
        + ", emailSubstring="
        + emailSubstring
        + ", groupIds="
        + groupIds
        + ", notGroupIds="
        + notGroupIds
        + ", status="
        + status
        + ", loginStatus="
        + loginStatus
        + ", userNameSubstring="
        + userNameSubstring
        + ", count="
        + count
        + ", additionalInfo="
        + additionalInfo
        + ", listUsersResponse="
        + listUsersResponse
        + "]";
  }
}
