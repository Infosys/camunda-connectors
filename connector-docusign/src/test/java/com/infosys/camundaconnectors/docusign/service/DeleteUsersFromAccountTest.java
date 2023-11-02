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
public class DeleteUsersFromAccountTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private DeleteUsersFromAccountService service;

  @BeforeEach
  public void init() throws IOException {
    service = new DeleteUsersFromAccountService();
    service.setUserIds(List.of("userId1", "userId2"));
  }

  @DisplayName("Delete user from account")
  @Test
  void validDeleteUsersFromAccount() throws IOException {
    service.setPermissionSet("true");
    service.setGroups("true");
    service.setSigningGroupsEmail("true");
    Mockito.when(
            httpService.deleteRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("deleteUsers", "deleteUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Delete user from account Without extra parameter")
  @Test
  void validDeleteUsersFromAccountWithoutExtraparamater() throws IOException {

    Mockito.when(
            httpService.deleteRequest(
                any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("deleteUsers", "deleteUsers"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Delete user from account with Userids as null")
  @Test
  void inValidDeleteUsersFromAccountWithUserIdsAsNull() throws IOException {
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
}
