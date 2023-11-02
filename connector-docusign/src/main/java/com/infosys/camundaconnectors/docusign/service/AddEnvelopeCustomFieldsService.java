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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.hc.client5.http.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddEnvelopeCustomFieldsService implements DocuSignRequestData {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(AddEnvelopeCustomFieldsService.class);
  @NotNull private String envelopeId;
  private List<Map<String, Object>> textFields;
  private List<Map<String, Object>> listFields;
  ErrorHandler errorHandler = new ErrorHandler();
  DocuSignResponse<?> addEnvelopeCustomFieldResponse;

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils service) {
    if (envelopeId == null || envelopeId.equals(""))
      throw new RuntimeException("EnvelopId can not be null");
    if ((textFields == null || textFields.size() == 0)
        && (listFields == null || listFields.size() == 0))
      throw new RuntimeException("Both textField and ListField can not be null");
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
    try {
      String jsonPayLoad = getPayload();
      Map<String, Object> response = service.postRequest(basePath, jsonPayLoad, authentication);
      errorHandler.checkForErrorDetails(response);
      addEnvelopeCustomFieldResponse = new DocuSignResponse<Map<String, Object>>(response);
    } catch (HttpResponseException | NumberFormatException e) {
      throw new RuntimeException(e.getMessage());
    }
    LOGGER.info("Response" + addEnvelopeCustomFieldResponse);
    return addEnvelopeCustomFieldResponse;
  }

  private String getPayload() {
    JsonObject jsonObject = new JsonObject();
    JsonArray textFieldsArray = new JsonArray();
    JsonArray listFieldsArray = new JsonArray();
    if (textFields != null && textFields.size() > 0) {
      for (Map<String, Object> textField : textFields) {
        JsonObject textFieldjson = new JsonObject();
        if (textField.containsKey("fieldId"))
          textFieldjson.addProperty("fieldId", (String) textField.get("fieldId"));
        if (textField.containsKey("name"))
          textFieldjson.addProperty("name", (String) textField.get("name"));
        if (textField.containsKey("required"))
          textFieldjson.addProperty("required", (String) textField.get("required"));
        if (textField.containsKey("show"))
          textFieldjson.addProperty("show", (String) textField.get("show"));
        if (textField.containsKey("value"))
          textFieldjson.addProperty("value", (String) textField.get("value"));
        textFieldsArray.add(textFieldjson);
      }
    }
    if (listFields != null && listFields.size() > 0) {
      for (Map<String, Object> listField : listFields) {
        JsonObject listfieldJson = new JsonObject();
        JsonArray listItemsArray = new JsonArray();
        if (listField.containsKey("fieldId"))
          listfieldJson.addProperty("fieldId", (String) listField.get("fieldId"));

        if (listField.containsKey("listitems")) {
          for (String items : (List<String>) listField.get("listitems")) {
            listItemsArray.add(items);
          }
          listfieldJson.add("listitems", listItemsArray);
        }
        if (listField.containsKey("name"))
          listfieldJson.addProperty("name", (String) listField.get("name"));
        if (listField.containsKey("required"))
          listfieldJson.addProperty("required", (String) listField.get("required"));
        if (listField.containsKey("show"))
          listfieldJson.addProperty("show", (String) listField.get("show"));
        if (listField.containsKey("value"))
          listfieldJson.addProperty("value", (String) listField.get("value"));
        listFieldsArray.add(listfieldJson);
      }
    }
    jsonObject.add("listCustomFields", listFieldsArray);
    jsonObject.add("textCustomFields", textFieldsArray);
    Gson gson = new Gson();
    String jsonPayLoad = gson.toJson(jsonObject);
    return jsonPayLoad;
  }

  public String getEnvelopeId() {
    return envelopeId;
  }

  public List<Map<String, Object>> getTextFields() {
    return textFields;
  }

  public List<Map<String, Object>> getListFields() {
    return listFields;
  }

  public void setEnvelopeId(String envelopeId) {
    this.envelopeId = envelopeId;
  }

  public void setTextFields(List<Map<String, Object>> textFields) {
    this.textFields = textFields;
  }

  public void setListFields(List<Map<String, Object>> listFields) {
    this.listFields = listFields;
  }

  @Override
  public int hashCode() {
    return Objects.hash(envelopeId, listFields, textFields, addEnvelopeCustomFieldResponse);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AddEnvelopeCustomFieldsService other = (AddEnvelopeCustomFieldsService) obj;
    return Objects.equals(envelopeId, other.envelopeId)
        && Objects.equals(listFields, other.listFields)
        && Objects.equals(textFields, other.textFields)
        && Objects.equals(addEnvelopeCustomFieldResponse, other.addEnvelopeCustomFieldResponse);
  }

  @Override
  public String toString() {
    return "UpdateEnvelopeCustomFieldsService [envelopeId="
        + envelopeId
        + ", textFields="
        + textFields
        + ", listFields="
        + listFields
        + ", updateEnvelopeCustomeFieldResponse="
        + addEnvelopeCustomFieldResponse
        + "]";
  }
}
