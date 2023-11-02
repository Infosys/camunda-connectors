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
public class CreateUsersTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private CreateUsersService service;

  @BeforeEach
  public void init() throws IOException {
    service = new CreateUsersService();
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("userName", "abc");
    payload.put("email", "abc@gmail.com");
    service.setPayload(List.of(payload));
  }

  @DisplayName("Create Users ")
  @Test
  void validCreateUsers() throws IOException {
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Create Users without userName ")
  @Test
  void inValidCreateUsersWithoutUsername() throws IOException {
    Map<String, Object> payload = new HashMap<String, Object>();

    payload.put("email", "abc@gmail.com");
    service.setPayload(List.of(payload));
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Create Users without email ")
  @Test
  void inValidCreateUsersWithoutEmail() throws IOException {
    Map<String, Object> payload = new HashMap<String, Object>();

    payload.put("userName", "abc");
    service.setPayload(List.of(payload));
    Mockito.when(
            httpService.postRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("createUsers", "createUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
