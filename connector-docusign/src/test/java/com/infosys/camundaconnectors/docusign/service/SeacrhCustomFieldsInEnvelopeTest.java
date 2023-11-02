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
public class SeacrhCustomFieldsInEnvelopeTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private SearchCustomFieldsInEnvelopeService service;

  @BeforeEach
  public void init() throws IOException {
    service = new SearchCustomFieldsInEnvelopeService();
    service.setEnvelopeId("envelopeId");
  }

  @DisplayName("Search Custom Fields in Envelope")
  @Test
  void validSearchCustomFieldsInEnvelope() throws IOException {
    Mockito.when(httpService.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("customEnvelope", "customEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Search Custom Fields in Envelope")
  @Test
  void InvalidSearchCustomFieldsInEnvelope() throws IOException {
    service.setEnvelopeId(null);
    Mockito.when(httpService.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("customEnvelope", "customEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Search Custom Fields in Envelope with errorDetails")
  @Test
  void InvalidSearchCustomFieldsInEnvelopeWithErrorDetails() throws IOException {
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
    service.setEnvelopeId(null);
    Mockito.when(httpService.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(gson.fromJson(gson.toJson(jsonObject), Map.class));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
