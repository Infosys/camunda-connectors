//  Copyright (c) 2023 Infosys Ltd.
//  Use of this source code is governed by MIT license that can be found in the LICENSE file
//  or at https://opensource.org/licenses/MIT

package com.infosys.camundaconnectors.ssh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.infosys.camundaconnectors.ssh.model.request.Authentication;
import com.infosys.camundaconnectors.ssh.model.response.Response;
import com.infosys.camundaconnectors.ssh.model.response.SSHResponse;
import com.infosys.camundaconnectors.ssh.service.ExecuteCommandService;
import com.infosys.camundaconnectors.ssh.utility.SshServerClient;
import java.util.ArrayList;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SSHFunctionTest extends BaseTest {
  @Mock private SSHClient client;
  @Mock private Session session;
  @Mock private Command cmd;
  @Mock private SshServerClient sshServerClient;
  @Mock private ExecuteCommandService service;
  private SSHFunction sshFunction;

  @BeforeEach
  void init() throws Exception {
    sshFunction = new SSHFunction(sshServerClient);
    Mockito.when(sshServerClient.loginSSH(any(Authentication.class))).thenReturn(client);
  }

  @ParameterizedTest
  @MethodSource("executeExecuteCommandsTestCases")
  void execute_shouldExecuteCommands(String input) throws Exception {
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    Mockito.when(cmd.getExitStatus()).thenReturn(0);
    doNothing().when(session).close();
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = sshFunction.execute(context);
    @SuppressWarnings("unchecked")
    SSHResponse<String> queryResponse = (SSHResponse<String>) executeResponse;
    Mockito.verify(client, Mockito.times(1)).close();
    if (input.contains("multiple")) Mockito.verify(session, Mockito.times(3)).close();
    else Mockito.verify(session, Mockito.times(1)).close();
    if (input.contains("multiple"))
      assertThat(queryResponse).extracting("response").isInstanceOf(ArrayList.class).isNotNull();
    else assertThat(queryResponse).extracting("response").isInstanceOf(String.class).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("invalidExecuteCommandsTestCases")
  void invalid_executeCommands(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> sshFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  private void assertThatItsValid(Object executeResponse, String validateAgainst) {
    assertThat(executeResponse).isInstanceOf(Response.class);
    @SuppressWarnings("unchecked")
    SSHResponse<String> response = (SSHResponse<String>) executeResponse;
    assertThat(response.getResponse()).isNotNull();
    assertThat(response)
        .extracting("response")
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .contains(validateAgainst);
  }
}
