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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetEnvelopeService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(GetEnvelopeService.class);
  @NotNull private String envelopeId;
  private String advanced_update;
  private List<String> include;
  DocuSignResponse<Map<String, Object>> getEnvelopeResponse;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpService)
      throws IOException {
    if (envelopeId == null || envelopeId.equals(""))
      throw new RuntimeException("EnvelopeId can not be null");
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/envelopes/"
            + envelopeId;

    String includeFields = "";
    if (include != null && !include.isEmpty() && include.size() > 0) {
      int i = 0;
      for (String field : include) {
        includeFields += field;
        i++;
        if (i != include.size()) includeFields += ",";
      }
      basePath += "?include=";
      basePath += includeFields;
    }
    Map<String, Object> res = httpService.getRequest(basePath, authentication);
    getEnvelopeResponse = new DocuSignResponse<Map<String, Object>>(res);
    errorHandler.checkForErrorDetails(res);
    LOGGER.info("Response", getEnvelopeResponse);
    return getEnvelopeResponse;
  }

  public String getEnvelopeId() {
    return envelopeId;
  }

  public void setEnvelopeId(String envelopeId) {
    this.envelopeId = envelopeId;
  }

  public String getAdvanced_update() {
    return advanced_update;
  }

  public void setAdvanced_update(String advanced_update) {
    this.advanced_update = advanced_update;
  }

  public List<String> getInclude() {
    return include;
  }

  public void setInclude(List<String> include) {
    this.include = include;
  }

  @Override
  public int hashCode() {
    return Objects.hash(advanced_update, envelopeId, include);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GetEnvelopeService other = (GetEnvelopeService) obj;
    return Objects.equals(advanced_update, other.advanced_update)
        && Objects.equals(envelopeId, other.envelopeId)
        && Objects.equals(include, other.include);
  }

  @Override
  public String toString() {
    return "GetEnvelopeService [envelopeId="
        + envelopeId
        + ", advanced_update="
        + advanced_update
        + ", include="
        + include
        + "]";
  }
}
