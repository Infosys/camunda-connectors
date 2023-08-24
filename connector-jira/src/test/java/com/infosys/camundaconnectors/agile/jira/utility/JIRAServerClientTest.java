/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.agile.jira.utility;

import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.agile.jira.model.request.Authentication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JIRAServerClientTest {
  @Mock private Authentication authentication;
  @Mock private JIRAServerClient jiraServerClient;

  void ShouldReturnClient() throws Exception {
    when(authentication.getUrl()).thenReturn("instanceurl-address");
    when(authentication.getUsername()).thenReturn("Username");
    when(authentication.getPassword()).thenReturn("TOKEN");
  }
}
