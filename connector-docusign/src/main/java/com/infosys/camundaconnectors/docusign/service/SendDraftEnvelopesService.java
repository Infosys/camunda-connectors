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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendDraftEnvelopesService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(SendDraftEnvelopesService.class);
  @NotNull List<String> envelopeIds;
  DocuSignResponse<?> sendDraftEnvelopesResponse;
  Map<String, Object> res;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpService)
      throws IOException {
    List<Map<String, Object>> putResponse = new ArrayList<Map<String, Object>>();

    for (String envelopeId : envelopeIds) {
      if (!checkEnvelopeInDraft(envelopeId, authentication, httpService)) {
        throw new RuntimeException("Envelope " + envelopeId + " Does not exists in Draft");
      }
      String baseUri = authentication.getBaseUri();
      String basePath =
          baseUri
              + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
              + "restapi/v2.1/accounts/"
              + authentication.getAccountId()
              + "/envelopes/"
              + envelopeId;
      String payLoad = "{\"status\": \"sent\"}";
      Map<String, Object> res = httpService.putRequest(basePath, payLoad, authentication);
      errorHandler.checkForErrorDetails(res);
      if (res != null) putResponse.add(res);
    }
    sendDraftEnvelopesResponse = new DocuSignResponse<List<Map<String, Object>>>(putResponse);
    LOGGER.info("Response", sendDraftEnvelopesResponse);
    return sendDraftEnvelopesResponse;
  }

  boolean checkEnvelopeInDraft(
      String envelopeId, Authentication authentication, HttpServiceUtils httpService) {
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/envelopes?"
            + "envelope_ids="
            + envelopeId
            + "&folder_types=draft";
    Map<String, Object> res = httpService.getRequest(basePath, authentication);
    List<Map<String, Object>> envelope = (List<Map<String, Object>>) res.get("envelopes");
    if (envelope != null) return true;

    return false;
  }

  public List<String> getEnvelopeIds() {
    return envelopeIds;
  }

  public void setEnvelopeIds(List<String> envelopeIds) {
    this.envelopeIds = envelopeIds;
  }

  @Override
  public int hashCode() {
    return Objects.hash(envelopeIds);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SendDraftEnvelopesService other = (SendDraftEnvelopesService) obj;
    return Objects.equals(envelopeIds, other.envelopeIds);
  }

  @Override
  public String toString() {
    return "SendDraftEnvelopesService [envelopeIds="
        + envelopeIds
        + ", sendDraftEnvelopesResponse="
        + sendDraftEnvelopesResponse
        + "]";
  }
}
