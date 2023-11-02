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

public class SearchCustomFieldsInEnvelopeService implements DocuSignRequestData {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(SearchCustomFieldsInEnvelopeService.class);
  @NotNull private String envelopeId;
  DocuSignResponse<?> searchCustomeFieldsInEnvelopeResponse;
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
            + "/envelopes"
            + "/"
            + envelopeId
            + "/custom_fields";
    Map<String, Object> res = httpService.getRequest(basePath, authentication);
    errorHandler.checkForErrorDetails(res);
    searchCustomeFieldsInEnvelopeResponse = new DocuSignResponse<>(res);
    LOGGER.info("Response", searchCustomeFieldsInEnvelopeResponse);
    return searchCustomeFieldsInEnvelopeResponse;
  }

  public String getEnvelopeId() {
    return envelopeId;
  }

  public void setEnvelopeId(String envelopeId) {
    this.envelopeId = envelopeId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(envelopeId, searchCustomeFieldsInEnvelopeResponse);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SearchCustomFieldsInEnvelopeService other = (SearchCustomFieldsInEnvelopeService) obj;
    return Objects.equals(envelopeId, other.envelopeId)
        && Objects.equals(
            searchCustomeFieldsInEnvelopeResponse, other.searchCustomeFieldsInEnvelopeResponse);
  }

  @Override
  public String toString() {
    return "SeacrhCustomFieldsInEnvelopeService [envelopeId="
        + envelopeId
        + ", searchCustomeFieldsInEnvelopeResponse="
        + searchCustomeFieldsInEnvelopeResponse
        + "]";
  }
}
