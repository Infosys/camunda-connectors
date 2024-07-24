/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CreateEnvelopeTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private CreateEnvelopeService service;

  @BeforeEach
  public void init() throws IOException {
    service = new CreateEnvelopeService();
    service.setPayloadType("requiredFields");
    Gson gson = new Gson();
    JsonObject documentObject = new JsonObject();
    documentObject.addProperty(
        "documentPath",
        "./Contribution License Agreement.docx");
    documentObject.addProperty("documentId", "12");
    service.setDocuments(List.of(gson.fromJson(documentObject, Map.class)));
    service.setEmailSubject("Please sign the following documents");
    Map<String, Object> recipientDetails = new HashMap<String, Object>();
    recipientDetails.put("name", "abc");
    recipientDetails.put("email", "abc@gmail.com");
    recipientDetails.put("recipientType", "signers");
    recipientDetails.put("recipientId", "recipientId");
    Map<String, Object> tabs = new HashMap<String, Object>();
    tabs.put("yPosition", "100");
    tabs.put("xPosition", "100");
    tabs.put("tabType", "signature");
    tabs.put("pageNumber", "1");
    tabs.put("documentId", "12");
    recipientDetails.put("tabs", List.of(tabs));
    service.setRecipients(List.of(recipientDetails));
    service.setStatus("created");
  }

  @DisplayName("Create Envelope With payloadType as RequiredFields")
  @Test
  void validCreateEnvelopeWithPayloadTypeAsRequiredFields() throws IOException {
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createEnvelope", "createEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Create Envelope With PayloadType as requiredFields and Documents as NULL")
  @Test
  void invalidCreateEnvelopeWithPayloadTypeAsRequiredFieldsAndDocumentsAsNull() throws IOException {
    service.setDocuments(null);
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createEnvelope", "createEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Create Envelope With PayloadType as requiredFields and EmailSubject as NULL")
  @Test
  void invalidCreateEnvelopeWithPayloadTypeAsRequiredFieldsAndEmailSubjectAsNull()
      throws IOException {
    service.setEmailSubject(null);
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createEnvelope", "createEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Create Envelope With PayloadType as requiredFields and RecipientDetails as NULL")
  @Test
  void invalidCreateEnvelopeWithPayloadTypeAsRequiredFieldsAndEmailRecipientDetailsAsNull()
      throws IOException {
    service.setRecipients(null);
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createEnvelope", "createEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Create Envelope With PayloadType as requiredFields and Status as NULL")
  @Test
  void invalidCreateEnvelopeWithPayloadTypeAsRequiredFieldsAndStatusAsNull() throws IOException {
    service.setStatus(null);
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createEnvelope", "createEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName(
      "Create Envelope With PayloadType as requiredFields and documentPath without extension")
  @Test
  void invalidCreateEnvelopeWithPayloadTypeAsRequiredFieldsAnddocumentPathWithoutExtension()
      throws IOException {
    Gson gson = new Gson();
    JsonObject documentObject = new JsonObject();
    documentObject.addProperty(
        "documentPath",
        "./Contribution License Agreement");
    documentObject.addProperty("documentId", "12");
    service.setDocuments(List.of(gson.fromJson(documentObject, Map.class)));
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createEnvelope", "createEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Create Envelope With PayloadType as requiredFields and without RecipientId")
  @Test
  void validCreateEnvelopeWithPayloadTypeAsRequiredFieldsAndWithoutRecipientId()
      throws IOException {
    Map<String, Object> recipientDetails = new HashMap<String, Object>();
    recipientDetails.put("name", "abc");
    recipientDetails.put("email", "abc@gmail.com");
    recipientDetails.put("recipientType", "signers");
    service.setRecipients(List.of(recipientDetails));
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createEnvelope", "createEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }
}
