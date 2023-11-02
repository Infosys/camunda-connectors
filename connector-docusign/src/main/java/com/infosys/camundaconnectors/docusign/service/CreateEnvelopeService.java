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
import com.infosys.camundaconnectors.docusign.utility.ErrorHandler;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateEnvelopeService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateEnvelopeService.class);

  @NotNull private String payloadType;
  private List<Map<String, Object>> recipients;
  private String emailSubject;
  private List<Map<String, String>> documents;
  private String status;
  private DocuSignResponse<?> createEnvelopeResponse;
  private Map<String, Object> payload;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpService)
      throws IOException {
    CloseableHttpClient httpClient = null;
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/envelopes";
    String jsonPayLoad = "";
    if (payloadType.equals("requiredFields")) jsonPayLoad = getJsonPayLoadFromRequiredFields();
    else jsonPayLoad = getJsonFromJsonString();
    Map<String, Object> response = httpService.postRequest(basePath, jsonPayLoad, authentication);
    errorHandler.checkForErrorDetails(response);
    createEnvelopeResponse = new DocuSignResponse<Map<String, Object>>(response);
    LOGGER.info("Response", createEnvelopeResponse);
    return createEnvelopeResponse;
  }

  String getJsonPayLoadFromRequiredFields() {
    if (recipients == null) throw new RuntimeException("Recipients can not be null");
    if (status == null) throw new RuntimeException("Status can not be null");
    if (documents == null) throw new RuntimeException("documents can not be null");
    if (emailSubject == null) throw new RuntimeException("emailSubject can not be null");
    JsonObject envelopeJson = new JsonObject();
    JsonArray documentsArray = new JsonArray();
    int documentIdCounter = 1;
    try {
      for (Map<String, String> documentField : documents) {
        File file = new File(documentField.get("documentPath"));
        file = new File(file.getAbsolutePath());
        if (!file.exists())
          throw new RuntimeException("Please enter the correct file name with correct extension");
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fileInputStream.read(data);
        byte[] fileBytes = Files.readAllBytes(Paths.get(documentField.get("documentPath")));
        String base64String = Base64.getEncoder().encodeToString(fileBytes);
        String documentName = new File(documentField.get("documentPath")).getName();
        if (documentName.split("\\.").length <= 1)
          throw new RuntimeException("Please enter the file name with its extension");
        String documentType = documentName.split("\\.")[1];
        if (documentType == null)
          throw new RuntimeException("Please enter the file name with its extension");
        JsonObject documentJson = new JsonObject();
        String string = new String(base64String);
        documentJson.addProperty("documentBase64", string);
        documentJson.addProperty("name", documentName);
        documentJson.addProperty("documentId", documentField.get("documentId"));
        documentJson.addProperty("fileExtension", documentType);
        documentsArray.add(documentJson);
      }
      envelopeJson.add("documents", documentsArray);
      // Json Object for email subject
      envelopeJson.addProperty("emailSubject", emailSubject);
      // Json Object for recipients
      JsonObject recipientsJson = new JsonObject();
      JsonArray signersArray = new JsonArray();
      JsonArray carbonCopyArray = new JsonArray();
      JsonArray certifiedDeliveryArray = new JsonArray();
      JsonArray inPersonSignerArray = new JsonArray();

      for (Map<String, Object> recipient : recipients) {

        String recipientType = (String) recipient.get("recipientType");
        String recipientEmail = (String) recipient.get("email");
        String recipientName = (String) recipient.get("name");
        String recipientId = "";
        if (recipient.containsKey("recipientId"))
          recipientId = (String) recipient.get("recipientId");
        else recipientId = uuidGenerator();

        if (recipientType.equals("signers")) {
          JsonObject signerJson = new JsonObject();
          JsonObject tabsJson = new JsonObject();
          JsonArray signHereTabsArray = new JsonArray();
          //          JsonObject signHereTabJson = new JsonObject();
          signerJson.addProperty("email", recipientEmail);
          signerJson.addProperty("name", recipientName);
          signerJson.addProperty("recipientId", recipientId);
          if (recipient.containsKey("tabs")) {
            List<Map<String, String>> tabsField = (List<Map<String, String>>) recipient.get("tabs");
            for (Map<String, String> tabField : tabsField) {
              JsonObject signHereTabJson = new JsonObject();
              signHereTabJson.addProperty("documentId", tabField.get("documentId"));
              signHereTabJson.addProperty("pageNumber", tabField.get("pageNumber"));
              signHereTabJson.addProperty("recipientId", recipientId);
              signHereTabJson.addProperty("tabType", tabField.get("tabType"));
              signHereTabJson.addProperty("xPosition", tabField.get("xPosition"));
              signHereTabJson.addProperty("yPosition", tabField.get("yPosition"));
              signHereTabsArray.add(signHereTabJson);
            }
            tabsJson.add("signHereTabs", signHereTabsArray);
            signerJson.add("tabs", tabsJson);
          }
          signersArray.add(signerJson);
        } else if (recipientType.equals("carboncopy")) {
          JsonObject carbonCopyJson = new JsonObject();
          carbonCopyJson.addProperty("email", recipientEmail);
          carbonCopyJson.addProperty("name", recipientName);
          carbonCopyJson.addProperty("recipientId", recipientId);
          carbonCopyArray.add(carbonCopyJson);

        } else if (recipientType.equals("certifieddelivery")) {
          JsonObject certifiedDeliveryJson = new JsonObject();
          certifiedDeliveryJson.addProperty("email", recipientEmail);
          certifiedDeliveryJson.addProperty("name", recipientName);
          certifiedDeliveryJson.addProperty("recipientId", recipientId);
          certifiedDeliveryArray.add(certifiedDeliveryJson);

        } else if (recipientType.equals("inpersonsigner")) {
          String hostName = (String) recipient.get("hostName");
          String hostEmail = (String) recipient.get("hostEmail");
          JsonObject inPersonSignerJson = new JsonObject();
          inPersonSignerJson.addProperty("name", recipientName);
          inPersonSignerJson.addProperty("hostEmail", hostEmail);
          inPersonSignerJson.addProperty("hostName", hostName);
          inPersonSignerJson.addProperty("recipientId", recipientId);
          inPersonSignerArray.add(inPersonSignerJson);
        }
      }

      recipientsJson.add("signers", signersArray);
      recipientsJson.add("carbonCopies", carbonCopyArray);
      recipientsJson.add("certifiedDeliveries", certifiedDeliveryArray);
      recipientsJson.add("inPersonSigners", inPersonSignerArray);
      envelopeJson.add("recipients", recipientsJson);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }
    envelopeJson.addProperty("status", status);
    Gson gson = new Gson();
    String jsonPayLoad = gson.toJson(envelopeJson);
    return jsonPayLoad;
  }

  private String getJsonFromJsonString() {
    Gson gson = new Gson();
    String gsonString = gson.toJson(payload, Map.class);
    String payLoad = payload.toString();
    return gsonString;
  }

  public String getPayloadType() {
    return payloadType;
  }

  public void setPayloadType(String payloadType) {
    this.payloadType = payloadType;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public void setPayload(Map<String, Object> payload) {
    this.payload = payload;
  }

  private String uuidGenerator() {
    return UUID.randomUUID().toString();
  }

  public List<Map<String, Object>> getRecipients() {
    return recipients;
  }

  public void setRecipients(List<Map<String, Object>> recipients) {
    this.recipients = recipients;
  }

  public String getEmailSubject() {
    return emailSubject;
  }

  public void setEmailSubject(String emailSubject) {
    this.emailSubject = emailSubject;
  }

  public List<Map<String, String>> getDocuments() {
    return documents;
  }

  public void setDocuments(List<Map<String, String>> documents) {
    this.documents = documents;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        createEnvelopeResponse, documents, emailSubject, payload, payloadType, recipients, status);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CreateEnvelopeService other = (CreateEnvelopeService) obj;
    return Objects.equals(createEnvelopeResponse, other.createEnvelopeResponse)
        && Objects.equals(documents, other.documents)
        && Objects.equals(emailSubject, other.emailSubject)
        && Objects.equals(payload, other.payload)
        && Objects.equals(payloadType, other.payloadType)
        && Objects.equals(recipients, other.recipients)
        && Objects.equals(status, other.status);
  }

  @Override
  public String toString() {
    return "CreateEnvelopeService [payloadType="
        + payloadType
        + ", recipients="
        + recipients
        + ", emailSubject="
        + emailSubject
        + ", documents="
        + documents
        + ", status="
        + status
        + ", createEnvelopeResponse="
        + createEnvelopeResponse
        + ", payload="
        + payload
        + "]";
  }
}
