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
public class ListGroupsTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private ListGroupsService service;

  @BeforeEach
  public void init() throws IOException {
    service = new ListGroupsService();
    service.setCount("5");
    service.setGroupType("AdminGroup");
    service.setIncludeUsercount("False");
    service.setSearchText("Inbox");
  }

  @DisplayName("List groups")
  @Test
  void validListGroups() throws IOException {
    Mockito.when(httpService.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("getUser", "getUser"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("List groups")
  @Test
  void validListGroupsWithErrorDetails() throws IOException {
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
    Mockito.when(httpService.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(gson.fromJson(gson.toJson(jsonObject), Map.class));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(HttpResponseException.class)
        .message()
        .isNotEmpty();
  }
}
