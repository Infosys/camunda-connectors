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
public class UpdateUserTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private UpdateUserService service;

  @BeforeEach
  public void init() throws IOException {
    service = new UpdateUserService();
    service.setUserId("userId");
    service.setPayloadType("basicFields");
    service.setFirstName("abc");
    service.setMiddleName("def");
    service.setLastName("xyz");
    service.setJobTitle("jobTitle");
    service.setCompany("company");
    service.setLanguage("hindi");
    service.setGroups(List.of("groupId1", "groupId2"));
    service.setAddress1("Address");
    service.setCity("city");
    service.setCountryRegion("india");
    service.setPostalCode("postalCode");
    service.setPhone("9876543112");
  }

  @DisplayName("Update User With Payload Type As BasicFields With All fields")
  @Test
  void validUpdateUserWithPayloadTypeAsBasicFieldsWithAllFields() throws IOException {
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Update User With Payload Type As EntirePayload With All fields")
  @Test
  void validUpdateUserWithPayloadTypeAsEntirePayloadWithAllFields() throws IOException {
    service.setPayloadType("entirePayload");
    service.setPayload(Map.of("json", "json"));
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Update User With Payload Type As BasicFields With All fields And Invalid userId")
  @Test
  void inValidUpdateUserWithPayloadTypeAsBasicFieldsWithAllFieldsWithInvalidUserId()
      throws IOException {
    service.setUserId(null);
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Update User With Payload Type As BasicFields With All fields And Invalid userId")
  @Test
  void inValidUpdateUserWithPayloadTypeAsBasicFieldsWithAllFieldsWithInvalidPayload()
      throws IOException {
    service.setPayloadType("entirePayload");
    service.setPayload(null);
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
