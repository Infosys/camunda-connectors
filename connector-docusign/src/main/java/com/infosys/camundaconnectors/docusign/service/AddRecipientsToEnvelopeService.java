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
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddRecipientsToEnvelopeService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateEnvelopeService.class);
  @NotNull private String envelopeId;
  private String resend_envelope;
  @NotNull private String payloadType;
  private List<Map<String, Object>> signers;
  private List<Map<String, Object>> carbonCopies;
  private List<Map<String, Object>> inPersonSigners;
  private List<Map<String, Object>> certifiedDelivery;
  private Map<String, Object> jsonPayload;
  private DocuSignResponse<?> addRecipientToEnvelopeResponse;

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils service)
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
            + "/recipients";

    try {
      URIBuilder uri = new URIBuilder(basePath);
      if (resend_envelope != null && resend_envelope.equals("true")) {
        uri.addParameter("resend_envelope", "true");
      }
      baseUri = uri.build().toString();
      String jsonPayLoad = "";
      if (payloadType.equals("requiredRecipients"))
        jsonPayLoad = getJsonPayLoadFromRequiredFields();
      else jsonPayLoad = getJsonFromJsonString();
      Map<String, Object> response = service.postRequest(baseUri, jsonPayLoad, authentication);
      checkForErrorDetails(response);
      addRecipientToEnvelopeResponse = new DocuSignResponse<Map<String, Object>>(response);
      LOGGER.info(addRecipientToEnvelopeResponse.toString());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e.getMessage());
    }
    LOGGER.info("Response", addRecipientToEnvelopeResponse);
    return addRecipientToEnvelopeResponse;
  }

  private void checkForErrorDetails(Map<String, Object> res)
      throws HttpResponseException, NumberFormatException {
    if (res.containsKey("errorDetails")) {
      Map<String, String> errorDetail = (Map<String, String>) res.get("errorDetails");
      throw new HttpResponseException(
          Integer.parseInt(errorDetail.get("errorCode")), errorDetail.get("message"));
    }
    for (Entry<String, Object> entry : res.entrySet()) {
      if (entry.getValue() instanceof List) {
        for (Map<String, Object> fields : (List<Map<String, Object>>) entry.getValue()) {
          if (fields.containsKey("errorDetails")) {
            Map<String, String> errorDetail = (Map<String, String>) fields.get("errorDetails");
            throw new HttpResponseException(
                Integer.parseInt(errorDetail.get("errorCode")), errorDetail.get("message"));
          }
        }
      }
    }
  }

  private String getJsonFromJsonString() {
    Gson gson = new Gson();
    String payload = gson.toJson(jsonPayload, Map.class);
    return payload;
  }

  private String getJsonPayLoadFromRequiredFields() {
    JsonObject envelopeJson = new JsonObject();
    JsonArray signersArray = new JsonArray();
    JsonArray carbonCopiesArray = new JsonArray();
    JsonArray inPersonSignersArray = new JsonArray();
    JsonArray certifiedDeliveryArray = new JsonArray();
    if (signers != null && signers.size() > 0) {
      for (Map<String, Object> signer : signers) {
        JsonObject signerJson = new JsonObject();
        signerJson.addProperty("email", (String) signer.get("email"));
        signerJson.addProperty("name", (String) signer.get("name"));
        signerJson.addProperty("recipientId", (String) signer.get("recipientId"));
        signersArray.add(signerJson);
      }
    }
    if (carbonCopies != null && carbonCopies.size() > 0) {
      for (Map<String, Object> carbonCopy : carbonCopies) {
        JsonObject carbonCopyJson = new JsonObject();
        carbonCopyJson.addProperty("email", (String) carbonCopy.get("email"));
        carbonCopyJson.addProperty("name", (String) carbonCopy.get("name"));
        carbonCopyJson.addProperty("recipientId", (String) carbonCopy.get("recipientId"));
        carbonCopiesArray.add(carbonCopyJson);
      }
    }
    if (inPersonSigners != null && inPersonSigners.size() > 0) {
      for (Map<String, Object> inPersonSigner : inPersonSigners) {
        JsonObject carbonCopyJson = new JsonObject();
        carbonCopyJson.addProperty("name", (String) inPersonSigner.get("name"));
        carbonCopyJson.addProperty("hostName", (String) inPersonSigner.get("hostName"));
        carbonCopyJson.addProperty("hostEmail", (String) inPersonSigner.get("hostEmail"));
        carbonCopyJson.addProperty("recipientId", (String) inPersonSigner.get("recipientId"));
        inPersonSignersArray.add(carbonCopyJson);
      }
    }
    if (certifiedDelivery != null && certifiedDelivery.size() > 0) {
      for (Map<String, Object> cd : certifiedDelivery) {
        JsonObject cdJson = new JsonObject();
        cdJson.addProperty("email", (String) cd.get("name"));
        cdJson.addProperty("name", (String) cd.get("email"));
        cdJson.addProperty("recipientId", (String) cd.get("recipientId"));
        certifiedDeliveryArray.add(cdJson);
      }
    }
    envelopeJson.add("signers", signersArray);
    envelopeJson.add("carbonCopies", carbonCopiesArray);
    envelopeJson.add("certifiedDeliveries", certifiedDeliveryArray);
    envelopeJson.add("inPersonSigners", inPersonSignersArray);
    Gson gson = new Gson();
    String jsonPayLoad = gson.toJson(envelopeJson);
    return jsonPayLoad;
  }

  public String getEnvelopeId() {
    return envelopeId;
  }

  public String getResend_envelope() {
    return resend_envelope;
  }

  public String getPayloadType() {
    return payloadType;
  }

  public List<Map<String, Object>> getSigners() {
    return signers;
  }

  public List<Map<String, Object>> getCarbonCopies() {
    return carbonCopies;
  }

  public List<Map<String, Object>> getInPersonSigners() {
    return inPersonSigners;
  }

  public List<Map<String, Object>> getCertifiedDelivery() {
    return certifiedDelivery;
  }

  public Map<String, Object> getJsonPayload() {
    return jsonPayload;
  }

  public void setEnvelopeId(String envelopeId) {
    this.envelopeId = envelopeId;
  }

  public void setResend_envelope(String resend_envelope) {
    this.resend_envelope = resend_envelope;
  }

  public void setPayloadType(String payloadType) {
    this.payloadType = payloadType;
  }

  public void setSigners(List<Map<String, Object>> signers) {
    this.signers = signers;
  }

  public void setCarbonCopies(List<Map<String, Object>> carbonCopies) {
    this.carbonCopies = carbonCopies;
  }

  public void setInPersonSigners(List<Map<String, Object>> inPersonSigners) {
    this.inPersonSigners = inPersonSigners;
  }

  public void setCertifiedDelivery(List<Map<String, Object>> certifiedDelivery) {
    this.certifiedDelivery = certifiedDelivery;
  }

  public void setJsonPayload(Map<String, Object> jsonPayload) {
    this.jsonPayload = jsonPayload;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        carbonCopies,
        certifiedDelivery,
        envelopeId,
        inPersonSigners,
        jsonPayload,
        payloadType,
        resend_envelope,
        signers);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AddRecipientsToEnvelopeService other = (AddRecipientsToEnvelopeService) obj;
    return Objects.equals(carbonCopies, other.carbonCopies)
        && Objects.equals(certifiedDelivery, other.certifiedDelivery)
        && Objects.equals(envelopeId, other.envelopeId)
        && Objects.equals(inPersonSigners, other.inPersonSigners)
        && Objects.equals(jsonPayload, other.jsonPayload)
        && Objects.equals(payloadType, other.payloadType)
        && Objects.equals(resend_envelope, other.resend_envelope)
        && Objects.equals(signers, other.signers);
  }

  @Override
  public String toString() {
    return "AddRecipientsToEnvelopeService [envelopeId="
        + envelopeId
        + ", resend_envelope="
        + resend_envelope
        + ", payloadType="
        + payloadType
        + ", signers="
        + signers
        + ", carbonCopies="
        + carbonCopies
        + ", inPersonSigners="
        + inPersonSigners
        + ", certifiedDelivery="
        + certifiedDelivery
        + ", jsonPayload="
        + jsonPayload
        + ", addRecipientToEnvelopeResponse="
        + addRecipientToEnvelopeResponse
        + "]";
  }
}
