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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.HttpResponseException;
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
public class AddRecipientsToEnvelopeTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private AddRecipientsToEnvelopeService service;

  @BeforeEach
  public void init() throws IOException {
    service = new AddRecipientsToEnvelopeService();
    service.setEnvelopeId("envelopeId");
    service.setPayloadType("requiredRecipients");
    service.setSigners(
        List.of(
            Map.of("email", "abc@gmail.com"), Map.of("name", "abc"), Map.of("recipientId", "1")));
    service.setCarbonCopies(
        List.of(
            Map.of("email", "deg@gmail.com"), Map.of("name", "deg"), Map.of("recipientId", "2")));
    service.setInPersonSigners(
        List.of(
            Map.of("email", "ghi@gmail.com"), Map.of("name", "ghi"), Map.of("recipientId", "3")));
    service.setCertifiedDelivery(
        List.of(
            Map.of("email", "jkl@gmail.com"), Map.of("name", "jkl"), Map.of("recipientId", "4")));
    service.setResend_envelope("true");
  }

  @DisplayName("Add Recipients to Envelope with payloadType recipientDetails and all the fields")
  @Test
  void validAddRecipientsToEnvelopeWithPayloadTypeRecipientDetailsAndAllTheFields()
      throws IOException {
    Map<String, Object> dummypayload = Map.of("Response", (Object) "ResponseString");
    //    Mockito.when()
    //        .thenReturn(dummypayload);
    Mockito.doReturn(dummypayload)
        .when(httpService)
        .postRequest("Url", "payload", new Authentication());
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Add Recipients to Envelope with payloadType recipientDetails and CC")
  @Test
  void validAddRecipientsToEnvelopeWithPayloadTypeRecipientDetailsAndCC() throws IOException {
    service.setSigners(null);
    service.setCertifiedDelivery(null);
    service.setInPersonSigners(null);
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(dummypayload);
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Add Recipients to Envelope with payloadType recipientDetails and CertifiedDelivery")
  @Test
  void validAddRecipientsToEnvelopeWithPayloadTypeRecipientDetailsAndCertifiedDelivery()
      throws IOException {
    service.setCarbonCopies(null);
    service.setSigners(null);
    service.setInPersonSigners(null);
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(dummypayload);
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Add Recipients to Envelope with payloadType recipientDetails and InPersonSigners")
  @Test
  void validAddRecipientsToEnvelopeWithPayloadTypeRecipientDetailsAndInPersonSigners()
      throws IOException {
    service.setCarbonCopies(null);
    service.setCertifiedDelivery(null);
    service.setSigners(null);
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(dummypayload);
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Add Recipients to Envelope with payloadType recipientDetails and Signers")
  @Test
  void validAddRecipientsToEnvelopeWithPayloadTypeRecipientDetailsAndSigners() throws IOException {
    service.setCarbonCopies(null);
    service.setCertifiedDelivery(null);
    service.setInPersonSigners(null);
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(dummypayload);
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Add Recipients to Envelope with payloadType entirePayload")
  @Test
  void validAddRecipientsToEnvelopeWithPayloadTypeEntirePayload() throws IOException {
    service.setCarbonCopies(null);
    service.setCertifiedDelivery(null);
    service.setInPersonSigners(null);
    service.setPayloadType("entirePayload");
    service.setJsonPayload(
        Map.of("signers", List.of(Map.of("name", "abc"), Map.of("emailId", "abc@gmail.com"))));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(dummypayload);
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName(
      "Add Recipients to Envelope with payloadType entirePayload and contains errorDetails")
  @Test
  void invalidAddRecipientsToEnvelopeWithPayloadTypeEntirePayloadwithErrorDetails()
      throws IOException {
    service.setCarbonCopies(null);
    service.setCertifiedDelivery(null);
    service.setInPersonSigners(null);
    service.setPayloadType("entirePayload");
    JsonObject jsonObject = new JsonObject();
    JsonObject Signersobject = new JsonObject();
    JsonObject errorDetails = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    Signersobject.addProperty("name", "avc");
    Signersobject.addProperty("email", "avc8111@gmail.com");
    errorDetails.addProperty("errorCode", "400");
    errorDetails.addProperty("message", "Some error occured");
    Signersobject.add("errorDetails", errorDetails);
    jsonArray.add(Signersobject);
    jsonObject.add("signers", jsonArray);
    Gson gson = new Gson();
    service.setJsonPayload(gson.fromJson(gson.toJson(jsonObject), Map.class));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(gson.fromJson(gson.toJson(jsonObject), Map.class));
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(HttpResponseException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName(
      "Add Recipients to Envelope with payloadType entirePayload and contains errorDetails")
  @Test
  void validAddRecipientsToEnvelopeWithPayloadTypeEntirePayloadwithErrorDetails()
      throws IOException {
    service.setCarbonCopies(null);
    service.setCertifiedDelivery(null);
    service.setInPersonSigners(null);
    service.setPayloadType("entirePayload");
    JsonObject jsonObject = new JsonObject();
    JsonObject Signersobject = new JsonObject();
    JsonObject errorDetails = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    Signersobject.addProperty("name", "avc");
    Signersobject.addProperty("email", "avc8111@gmail.com");
    errorDetails.addProperty("errorCode", "400");
    errorDetails.addProperty("message", "Some error occured");
    //    Signersobject.add("errorDetails", errorDetails);
    jsonArray.add(Signersobject);
    jsonObject.addProperty("demoField", "demoField");
    jsonObject.add("signers", jsonArray);
    Gson gson = new Gson();
    service.setJsonPayload(gson.fromJson(gson.toJson(jsonObject), Map.class));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(gson.fromJson(gson.toJson(jsonObject), Map.class));
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }
}
