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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateDocumentsToEnvelopeService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateEnvelopeService.class);
  @NotNull private String envelopeId;
  @NotNull private List<Map<String, String>> documents;
  DocuSignResponse<?> updateDocumentToEnvelope;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpRequest)
      throws IOException {
    if (envelopeId == null || envelopeId.equals(""))
      throw new RuntimeException("envelopeId can not be null");
    if (documents == null || documents.size() == 0)
      throw new RuntimeException("Documents can not be null");
    String basePath =
        "https://demo.docusign.net/restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/envelopes/"
            + envelopeId
            + "/documents";
    String payLoad = getJsonpayload();
    Map<String, Object> response = httpRequest.putRequest(basePath, payLoad, authentication);
    errorHandler.checkForErrorDetails(response);
    updateDocumentToEnvelope = new DocuSignResponse<Map<String, Object>>(response);
    LOGGER.info("Response", updateDocumentToEnvelope);
    return updateDocumentToEnvelope;
  }

  private String getJsonpayload() {
    JsonObject envelopeJson = new JsonObject();
    JsonArray documentsArray = new JsonArray();
    try {
      for (Map<String, String> documentField : documents) {

        File file = new File(documentField.get("documentPath").replace("\\", "/"));
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fileInputStream.read(data);
        String base64 = Base64.getEncoder().encodeToString(data);
        byte[] fileBytes = Files.readAllBytes(Paths.get(documentField.get("documentPath")));
        String base64String = Base64.getEncoder().encodeToString(fileBytes);
        String documentName = new File(documentField.get("documentPath")).getName();
        JsonObject documentJson = new JsonObject();
        documentJson.addProperty("documentBase64", base64String);
        documentJson.addProperty("name", documentName);
        documentJson.addProperty("documentId", documentField.get("documentId"));
        documentsArray.add(documentJson);
      }

      envelopeJson.add("documents", documentsArray);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }

    Gson gson = new Gson();
    String jsonPayLoad = gson.toJson(envelopeJson);
    return jsonPayLoad;
  }

  public String getEnvelopeId() {
    return envelopeId;
  }

  public void setEnvelopeId(String envelopeId) {
    this.envelopeId = envelopeId;
  }

  public List<Map<String, String>> getDocuments() {
    return documents;
  }

  public void setDocuments(List<Map<String, String>> documents) {
    this.documents = documents;
  }

  @Override
  public int hashCode() {
    return Objects.hash(documents, envelopeId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    UpdateDocumentsToEnvelopeService other = (UpdateDocumentsToEnvelopeService) obj;
    return Objects.equals(documents, other.documents)
        && Objects.equals(envelopeId, other.envelopeId);
  }

  @Override
  public String toString() {
    return "UpdateDocumentsToEnvelope [envelopeId="
        + envelopeId
        + ", documents="
        + documents
        + ", updateDocumentToEnvelope="
        + updateDocumentToEnvelope
        + "]";
  }
}
