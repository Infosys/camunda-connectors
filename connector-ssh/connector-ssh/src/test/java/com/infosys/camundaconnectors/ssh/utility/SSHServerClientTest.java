/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.ssh.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.ssh.model.request.Authentication;
import net.schmizz.sshj.SSHClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SSHServerClientTest {
  @Mock private SSHClient client;
  @Mock private Authentication authentication;
  @Mock private SshServerClient sshServerClient;

  @DisplayName("Should return Client object")
  @Test
  void shouldReturnClient() throws Exception {
    when(authentication.getUsername()).thenReturn("curation.bot");
    when(authentication.getPortNumber()).thenReturn("22");
    when(sshServerClient.loginSSH(authentication)).thenReturn(client);
    assertThat(sshServerClient.loginSSH(authentication)).isInstanceOf(SSHClient.class);
  }
}
