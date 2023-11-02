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
public class DeleteUsersFromGroupTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private DeleteUsersFromGroupService service;

  @BeforeEach
  public void init() throws IOException {
    service = new DeleteUsersFromGroupService();
    service.setGroupId("group_id");
  }

  @DisplayName("Delete user according to userIds")
  @Test
  void validDeleteUsersFromGroupAccordingToUsersId() throws IOException {
    service.setDeleteBy("byUserIds");
    service.setUserIds(List.of("userId1", "userId2"));
    Mockito.when(
            httpService.deleteRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("deleteUsers", "deleteUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Delete user according to userIds")
  @Test
  void inValidDeleteUsersFromGroupAccordingToUsersId() throws IOException {
    service.setDeleteBy("byUserIds");
    service.setUserIds(null);
    Mockito.when(
            httpService.deleteRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("deleteUsers", "deleteUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Delete user according to payload")
  @Test
  void validDeleteUsersFromGroupAccordingToOtherFields() throws IOException {
    service.setDeleteBy("otherFields");
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("userId", "123424");
    payload.put("userName", "abc");
    service.setPayload(List.of(payload));
    Mockito.when(
            httpService.deleteRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("deleteUsers", "deleteUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("inValid delete users from groups with payload as null")
  @Test
  void inValidDeleteUsersFromGroupAccordingToOtherFields() throws IOException {
    service.setDeleteBy("otherFields");
    service.setPayload(null);
    Mockito.when(
            httpService.deleteRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("deleteUsers", "deleteUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("inValid delete users from groups with groupId as null")
  @Test
  void inValidDeleteUsersFromGroupWithNullGroupId() throws IOException {
    service.setGroupId(null);

    Mockito.when(
            httpService.deleteRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("deleteUsers", "deleteUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
