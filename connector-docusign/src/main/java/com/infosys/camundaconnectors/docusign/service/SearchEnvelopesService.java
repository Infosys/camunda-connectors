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
import java.util.Set;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchEnvelopesService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchEnvelopesService.class);
  private String count;
  private String email;
  private String user_name;
  private List<String> envelopeIds;
  private List<String> folder_types;
  private String user_id;
  private String from_date;
  private String to_date;
  private List<String> status;
  private String order_by;
  private String order;
  private String search_text;
  private List<String> include;
  private List<Map<String, Object>> extraFields;
  DocuSignResponse<Map<String, Object>> searchEnvelopeResponse;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpService)
      throws IOException {
    boolean flag = true;
    if (isNullList(envelopeIds) && isNullStr(from_date)) {
      if (!isNullListMap(extraFields)) {
        for (Map<String, Object> map : extraFields) {
          Set<String> setOfKeys = map.keySet();
          for (String key : setOfKeys) {
            if (key.equals("transaction_ids")) flag = false;
          }
        }
      }
      if (flag) throw new RuntimeException("EnvelopeId's and from_date cant be null at a time");
    }
    String basePath =
        "https://demo.docusign.net/restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/envelopes";
    String url = buildUri(basePath);
    Map<String, Object> response = httpService.getRequest(url, authentication);
    errorHandler.checkForErrorDetails(response);
    searchEnvelopeResponse = new DocuSignResponse<Map<String, Object>>(response);
    LOGGER.info("Response", searchEnvelopeResponse);
    return searchEnvelopeResponse;
  }

  public boolean isNullStr(String str) {
    return str == null || str.equals("") || str.equals("None");
  }

  public boolean isNullList(List<String> list) {
    return list == null || list.size() == 0;
  }

  public boolean isNullListMap(List<Map<String, Object>> listMap) {
    return listMap == null || listMap.size() == 0;
  }

  public String buildUri(String basePath) {
    String baseUrl = "";
    try {
      URIBuilder uri = new URIBuilder(basePath);

      if (!isNullList(envelopeIds)) {
        String allEnvelopeIds = String.join(",", envelopeIds);
        uri.setParameter("envelope_ids", allEnvelopeIds);
      }

      if (!isNullList(folder_types)) {
        String allFolderTypes = String.join(",", folder_types);
        uri.setParameter("folder_types", allFolderTypes);
      }

      if (!isNullList(status)) {
        String allStatus = String.join(",", status);
        uri.setParameter("status", allStatus);
      }

      if (!isNullStr(user_name) && !isNullStr(email)) {
        uri.setParameter("user_name", user_name);
        uri.setParameter("email", email);
      } else if (!isNullStr(user_name) || !isNullStr(email)) {
        if (!isNullStr(user_name))
          throw new RuntimeException(
              "Along with user_name, email query parameter is also neccessary");
        else if (!isNullStr(email))
          throw new RuntimeException(
              "Along with email, user_name query parameter is also neccessary");
      }

      if (!isNullStr(user_id)) uri.setParameter("user_id", user_id);
      if (!isNullStr(count)) uri.setParameter("count", count);
      if (!isNullStr(from_date)) uri.setParameter("from_date", from_date);
      if (!isNullStr(to_date)) uri.setParameter("to_date", to_date);
      if (!isNullStr(search_text)) uri.setParameter("search_text", search_text);
      if (!isNullStr(order)) uri.setParameter("order", order);
      if (!isNullStr(order_by)) uri.setParameter("order_by", order_by);

      if (!isNullList(include)) {
        String allIncludes = String.join(",", include);
        uri.setParameter("include", allIncludes);
      }
      if (!isNullListMap(extraFields)) {
        for (Map<String, Object> map : extraFields) {
          Set<String> setOfKeys = map.keySet();
          for (String key : setOfKeys) {
            uri.setParameter(key, (String) map.get(key));
          }
        }
      }
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

  public String getEmail() {
    return email;
  }

  public List<String> getStatus() {
    return status;
  }

  public void setStatus(List<String> status) {
    this.status = status;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUser_name() {
    return user_name;
  }

  public void setUser_name(String user_name) {
    this.user_name = user_name;
  }

  public List<String> getEnvelopeIds() {
    return envelopeIds;
  }

  public void setEnvelopeIds(List<String> envelopeIds) {
    this.envelopeIds = envelopeIds;
  }

  public List<String> getFolder_types() {
    return folder_types;
  }

  public void setFolder_types(List<String> folder_types) {
    this.folder_types = folder_types;
  }

  public String getUser_id() {
    return user_id;
  }

  public void setUser_id(String user_id) {
    this.user_id = user_id;
  }

  public String getFrom_date() {
    return from_date;
  }

  public void setFrom_date(String from_date) {
    this.from_date = from_date;
  }

  public String getTo_date() {
    return to_date;
  }

  public List<String> getInclude() {
    return include;
  }

  public void setInclude(List<String> include) {
    this.include = include;
  }

  public List<Map<String, Object>> getExtraFields() {
    return extraFields;
  }

  public void setExtraFields(List<Map<String, Object>> extraFields) {
    this.extraFields = extraFields;
  }

  public void setTo_date(String to_date) {
    this.to_date = to_date;
  }

  public String getOrder_by() {
    return order_by;
  }

  public void setOrder_by(String order_by) {
    this.order_by = order_by;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public String getSearch_text() {
    return search_text;
  }

  public void setSearch_text(String search_text) {
    this.search_text = search_text;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        count,
        email,
        envelopeIds,
        errorHandler,
        extraFields,
        folder_types,
        from_date,
        include,
        order,
        order_by,
        searchEnvelopeResponse,
        search_text,
        status,
        to_date,
        user_id,
        user_name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SearchEnvelopesService other = (SearchEnvelopesService) obj;
    return Objects.equals(count, other.count)
        && Objects.equals(email, other.email)
        && Objects.equals(envelopeIds, other.envelopeIds)
        && Objects.equals(errorHandler, other.errorHandler)
        && Objects.equals(extraFields, other.extraFields)
        && Objects.equals(folder_types, other.folder_types)
        && Objects.equals(from_date, other.from_date)
        && Objects.equals(include, other.include)
        && Objects.equals(order, other.order)
        && Objects.equals(order_by, other.order_by)
        && Objects.equals(searchEnvelopeResponse, other.searchEnvelopeResponse)
        && Objects.equals(search_text, other.search_text)
        && Objects.equals(status, other.status)
        && Objects.equals(to_date, other.to_date)
        && Objects.equals(user_id, other.user_id)
        && Objects.equals(user_name, other.user_name);
  }

  @Override
  public String toString() {
    return "SearchEnvelopesService [count="
        + count
        + ", email="
        + email
        + ", user_name="
        + user_name
        + ", envelopeIds="
        + envelopeIds
        + ", folder_types="
        + folder_types
        + ", user_id="
        + user_id
        + ", from_date="
        + from_date
        + ", to_date="
        + to_date
        + ", status="
        + status
        + ", order_by="
        + order_by
        + ", order="
        + order
        + ", search_text="
        + search_text
        + ", include="
        + include
        + ", extraFields="
        + extraFields
        + ", searchEnvelopeResponse="
        + searchEnvelopeResponse
        + ", errorHandler="
        + errorHandler
        + "]";
  }
}
