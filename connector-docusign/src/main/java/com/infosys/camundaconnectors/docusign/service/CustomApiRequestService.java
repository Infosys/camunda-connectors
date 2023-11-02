/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import com.google.gson.Gson;
import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.request.DocuSignRequestData;
import com.infosys.camundaconnectors.docusign.model.response.DocuSignResponse;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.ErrorHandler;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomApiRequestService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateUsersService.class);
  @NotNull private String url;
  @NotNull private String method;
  private Map<String, Object> queryParameter;
  private Map<String, Object> payload;
  DocuSignResponse<Map<String, Object>> response;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils service)
      throws IOException {
    if (url == null || url.equals("") || method == null || method.equals(""))
      throw new RuntimeException("url or method can not be empty");
    Map<String, Object> serviceResponse = null;
    try {
      URIBuilder uriBuilder = new URIBuilder(url);
      String jsonPayload = "";
      if (queryParameter != null && !queryParameter.equals("")) {
        for (Entry<String, Object> entry : queryParameter.entrySet()) {
          uriBuilder.addParameter((String) entry.getKey(), (String) entry.getValue());
        }
      }
      if (payload != null && !payload.equals("")) {
        Gson gson = new Gson();
        jsonPayload = gson.toJson(payload, Map.class);
      }
      String buildUrl = uriBuilder.build().toString();
      if (method.equals("get")) {
        serviceResponse = service.getRequest(buildUrl, authentication);
        response = new DocuSignResponse<Map<String, Object>>(serviceResponse);

      } else if (method.equals("post")) {
        serviceResponse = service.postRequest(buildUrl, jsonPayload, authentication);
        response = new DocuSignResponse<Map<String, Object>>(serviceResponse);

      } else if (method.equals("put")) {
        serviceResponse = service.putRequest(buildUrl, jsonPayload, authentication);
        response = new DocuSignResponse<Map<String, Object>>(serviceResponse);

      } else if (method.equals("delete")) {
        serviceResponse = service.deleteRequest(buildUrl, jsonPayload, authentication);
        response = new DocuSignResponse<Map<String, Object>>(serviceResponse);
      }
      errorHandler.checkForErrorDetails(serviceResponse);
    } catch (URISyntaxException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
    LOGGER.info("Response", response);
    return response;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Map<String, Object> getQueryParameter() {
    return queryParameter;
  }

  public void setQueryParameter(Map<String, Object> queryParameter) {
    this.queryParameter = queryParameter;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public void setPayload(Map<String, Object> payload) {
    this.payload = payload;
  }

  @Override
  public int hashCode() {
    return Objects.hash(method, payload, queryParameter, url);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CustomApiRequestService other = (CustomApiRequestService) obj;
    return Objects.equals(method, other.method)
        && Objects.equals(payload, other.payload)
        && Objects.equals(queryParameter, other.queryParameter)
        && Objects.equals(url, other.url);
  }

  @Override
  public String toString() {
    return "CustomApiRequestService [url="
        + url
        + ", method="
        + method
        + ", queryParameter="
        + queryParameter
        + ", payload="
        + payload
        + "]";
  }
}
