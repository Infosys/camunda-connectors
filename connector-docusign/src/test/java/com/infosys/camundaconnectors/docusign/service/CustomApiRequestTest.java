/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import java.io.IOException;
import java.util.HashMap;
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
public class CustomApiRequestTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private CustomApiRequestService service;

  @BeforeEach
  public void init() throws IOException {
    service = new CustomApiRequestService();
    service.setUrl("http://demourl.com/");
  }

  @DisplayName("Get Request with query Parameter")
  @Test
  void validGetRequestWithQueryParameter() throws IOException {
    service.setMethod("get");
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("userName", "abc");
    payload.put("email", "abc@gmail.com");
    service.setQueryParameter(payload);
    Mockito.when(httpService.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Get Request without query Parameter")
  @Test
  void validGetRequestWithoutQueryParameter() throws IOException {
    service.setMethod("get");
    Mockito.when(httpService.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Post Request with payload")
  @Test
  void validPostRequestWithPayload() throws IOException {
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("userName", "abc");
    payload.put("email", "abc@gmail.com");
    service.setPayload(payload);
    service.setMethod("post");
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Put Request with payload")
  @Test
  void validPutRequestWithPayload() throws IOException {
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("userName", "abc");
    payload.put("email", "abc@gmail.com");
    service.setPayload(payload);
    service.setMethod("put");
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Delete Request with queryParameter")
  @Test
  void validDeleteRequestWithQueryParameter() throws IOException {
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("userName", "abc");
    payload.put("email", "abc@gmail.com");
    service.setQueryParameter(payload);
    service.setMethod("delete");
    Mockito.when(
            httpService.deleteRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Delete Request with queryParameter and payload")
  @Test
  void validDeleteRequestWithQueryParameterWithPayload() throws IOException {
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("userName", "abc");
    payload.put("email", "abc@gmail.com");
    service.setQueryParameter(payload);
    Map<String, Object> payload2 = new HashMap<String, Object>();
    payload2.put("userName", "abc");
    payload2.put("email", "abc@gmail.com");
    service.setPayload(payload2);
    service.setMethod("delete");
    Mockito.when(
            httpService.deleteRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Custom Api request without url")
  @Test
  void inValidCustomApiRequest() throws IOException {
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("userName", "abc");
    payload.put("email", "abc@gmail.com");
    service.setQueryParameter(payload);
    Map<String, Object> payload2 = new HashMap<String, Object>();
    payload2.put("userName", "abc");
    payload2.put("email", "abc@gmail.com");
    service.setPayload(payload2);
    service.setMethod("");
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");

    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Custom Api request without url and method")
  @Test
  void inValidCustomApiRequestwihoutUrlAndMethod() throws IOException {
    service.setUrl(null);
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("userName", "abc");
    payload.put("email", "abc@gmail.com");
    service.setQueryParameter(payload);
    Map<String, Object> payload2 = new HashMap<String, Object>();
    payload2.put("userName", "abc");
    payload2.put("email", "abc@gmail.com");
    service.setPayload(payload2);
    service.setMethod("");
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");

    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
