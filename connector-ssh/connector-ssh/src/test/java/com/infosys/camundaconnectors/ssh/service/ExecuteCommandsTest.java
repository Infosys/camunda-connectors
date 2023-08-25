package com.infosys.camundaconnectors.ssh.service;

/// *
// * Copyright (c) 2023 Infosys Ltd.
// * Use of this source code is governed by MIT license that can be found in the LICENSE file
// * or at https://opensource.org/licenses/MIT
// */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import com.infosys.camundaconnectors.ssh.model.response.Response;
import com.infosys.camundaconnectors.ssh.model.response.SSHResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExecuteCommandsTest {

  @Mock private SSHClient client;
  @Mock private Session session;
  @Mock private Command cmd;
  //  @Mock private InputStream inputStream;
  private ExecuteCommandService service;

  @BeforeEach
  public void init() throws IOException {
    service = new ExecuteCommandService();
    List<String> commands = new ArrayList<String>();
    commands.add("ls");
    commands.add("free");
    service.setCommands(commands);
    service.setCommandLine("single");
    service.setOutputType("statusCode");
  }

  @DisplayName("Execute Command with single command line and statusCode as a output")
  @Test
  void validTestExecuteCommandsWithSingleCommandLineAndStatusCodeAsOutput() throws Exception {
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    Mockito.when(cmd.getExitStatus()).thenReturn(0);
    doNothing().when(session).close();
    Response result = service.invoke(client, "linux");
    SSHResponse<String> queryResponse = (SSHResponse<String>) result;
    Mockito.verify(client, Mockito.times(1)).close();
    Mockito.verify(session, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").isInstanceOf(String.class).isNotNull();
  }

  @DisplayName("Execute Command with single command line and commandOutput as a output")
  @Test
  void validTestExecuteCommandsWithSingleCommandLineAndCommandoutputAsOutput() throws Exception {
    service.setOutputType("commandOutput");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut").when(ser).readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(0);
    doNothing().when(session).close();
    Response result = ser.invoke(client, "linux");
    SSHResponse<String> queryResponse = (SSHResponse<String>) result;
    Mockito.verify(client, Mockito.times(1)).close();
    Mockito.verify(session, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").isInstanceOf(String.class).isNotNull();
  }

  @DisplayName("Should throw an exception when errorCode is 1")
  @Test
  void InvalidTestCaseThrowAnExceptionWhenErrorCodeIs1() throws Exception {
    service.setOutputType("commandOutput");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut").when(ser).readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(1);
    doNothing().when(session).close();
    assertThatThrownBy(() -> ser.invoke(client, "linux"))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
    Mockito.verify(client, Mockito.times(1)).close();
  }

  @DisplayName("Execute Command with Multiple command line and statusCode as a output")
  @Test
  void validTestExecuteCommandsWithMultipleCommandLineAndStatusCodeAsOutput() throws Exception {
    service.setCommandLine("multiple");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut", "CommandOutput2")
        .when(ser)
        .readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(0);
    doNothing().when(session).close();
    Response result = ser.invoke(client, "linux");
    SSHResponse<String> queryResponse = (SSHResponse<String>) result;
    Mockito.verify(client, Mockito.times(1)).close();
    Mockito.verify(session, Mockito.times(2)).close();
    assertThat(queryResponse).extracting("response").isInstanceOf(List.class).isNotNull();
  }

  @DisplayName("Execute Command with Multiple command line and commandOutput as a output")
  @Test
  void validTestExecuteCommandsWithMultipleCommandLineAndCommandoutputAsOutput() throws Exception {
    service.setCommandLine("multiple");
    service.setOutputType("commandOutput");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut", "CommandOutput2")
        .when(ser)
        .readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(0);
    doNothing().when(session).close();
    Response result = ser.invoke(client, "linux");
    SSHResponse<String> queryResponse = (SSHResponse<String>) result;
    Mockito.verify(client, Mockito.times(1)).close();
    Mockito.verify(session, Mockito.times(2)).close();
    assertThat(queryResponse).extracting("response").isInstanceOf(List.class).isNotNull();
  }

  @DisplayName("Should throw an exception when errorCode is 1 and commandLine is multiple")
  @Test
  void InvalidTestCaseThrowAnExceptionWhenErrorCodeIs1AndCommandlineIsMultiple() throws Exception {
    service.setOutputType("commandOutput");
    service.setCommandLine("multiple");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut").when(ser).readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(0, 1);
    doNothing().when(session).close();
    assertThatThrownBy(() -> ser.invoke(client, "linux"))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
    Mockito.verify(client, Mockito.times(1)).close();
  }

  @DisplayName("Execute Command with single command line and statusCode as a output on windows")
  @Test
  void validTestExecuteCommandsWithSingleCommandLineAndStatusCodeAsOutputOnWindows()
      throws Exception {
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    Mockito.when(cmd.getExitStatus()).thenReturn(0);
    doNothing().when(session).close();
    Response result = service.invoke(client, "windows");
    SSHResponse<String> queryResponse = (SSHResponse<String>) result;
    Mockito.verify(client, Mockito.times(1)).close();
    Mockito.verify(session, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").isInstanceOf(String.class).isNotNull();
  }

  @DisplayName("Execute Command with single command line and commandOutput as a output on windows")
  @Test
  void validTestExecuteCommandsWithSingleCommandLineAndCommandoutputAsOutputOnWindows()
      throws Exception {
    service.setOutputType("commandOutput");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut").when(ser).readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(0);
    doNothing().when(session).close();
    Response result = ser.invoke(client, "windows");
    SSHResponse<String> queryResponse = (SSHResponse<String>) result;
    Mockito.verify(client, Mockito.times(1)).close();
    Mockito.verify(session, Mockito.times(1)).close();
    assertThat(queryResponse).extracting("response").isInstanceOf(String.class).isNotNull();
  }

  @DisplayName("Should throw an exception when errorCode is 1 on windows")
  @Test
  void InvalidTestCaseThrowAnExceptionWhenErrorCodeIs1OnWindows() throws Exception {
    service.setOutputType("commandOutput");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut").when(ser).readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(1);
    doNothing().when(session).close();
    assertThatThrownBy(() -> ser.invoke(client, "windows"))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
    Mockito.verify(client, Mockito.times(1)).close();
  }

  @DisplayName("Execute Command with Multiple command line and statusCode as a output on windows")
  @Test
  void validTestExecuteCommandsWithMultipleCommandLineAndStatusCodeAsOutputOnWindows()
      throws Exception {
    service.setCommandLine("multiple");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut", "CommandOutput2")
        .when(ser)
        .readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(0);
    doNothing().when(session).close();
    Response result = ser.invoke(client, "windows");
    SSHResponse<String> queryResponse = (SSHResponse<String>) result;
    Mockito.verify(client, Mockito.times(1)).close();
    Mockito.verify(session, Mockito.times(2)).close();

    assertThat(queryResponse).extracting("response").isInstanceOf(List.class).isNotNull();
  }

  @DisplayName(
      "Execute Command with Multiple command line and commandOutput as a output on windows")
  @Test
  void validTestExecuteCommandsWithMultipleCommandLineAndCommandoutputAsOutputWindows()
      throws Exception {
    service.setCommandLine("multiple");
    service.setOutputType("commandOutput");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut", "CommandOutput2")
        .when(ser)
        .readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(0);
    doNothing().when(session).close();
    Response result = ser.invoke(client, "windows");
    SSHResponse<String> queryResponse = (SSHResponse<String>) result;
    Mockito.verify(client, Mockito.times(1)).close();
    Mockito.verify(session, Mockito.times(2)).close();
    assertThat(queryResponse).extracting("response").isInstanceOf(List.class).isNotNull();
  }

  @DisplayName(
      "Should throw an exception when errorCode is 1 and commandLine is multiple on windows")
  @Test
  void InvalidTestCaseThrowAnExceptionWhenErrorCodeIs1AndCommandlineIsMultipleWindows()
      throws Exception {
    service.setOutputType("commandOutput");
    service.setCommandLine("multiple");
    ExecuteCommandService ser = spy(service);
    Mockito.when(client.startSession()).thenReturn(session);
    Mockito.when(session.exec(any(String.class))).thenReturn(cmd);
    doNothing().when(cmd).join();
    String content = "Hello, world";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    Mockito.when(cmd.getInputStream()).thenReturn(inputStream);
    Mockito.doReturn("CommandOutPut").when(ser).readInputString(any(InputStream.class));
    Mockito.when(cmd.getExitStatus()).thenReturn(0, 1);
    doNothing().when(session).close();
    assertThatThrownBy(() -> ser.invoke(client, "windows"))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
    Mockito.verify(client, Mockito.times(1)).close();
  }

  @DisplayName("Invalid commandLine")
  @Test
  void InvalidCommandLine() throws Exception {
    service.setOutputType("commandOutput");
    service.setCommandLine("multilevel");
    ExecuteCommandService ser = spy(service);
    assertThatThrownBy(() -> ser.invoke(client, "windows"))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
    Mockito.verify(client, Mockito.times(1)).close();
  }

  @DisplayName("Invalid outputType")
  @Test
  void InvalidOutputType() throws Exception {
    service.setOutputType("outputCommand");
    ExecuteCommandService ser = spy(service);
    assertThatThrownBy(() -> ser.invoke(client, "windows"))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
    Mockito.verify(client, Mockito.times(1)).close();
  }

  @DisplayName("Invalid operatingSystem")
  @Test
  void InvalidOperatingSystem() throws Exception {
    ExecuteCommandService ser = spy(service);
    assertThatThrownBy(() -> ser.invoke(client, "unix"))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
    Mockito.verify(client, Mockito.times(1)).close();
  }

  @DisplayName("Invalid operatingSystem Blank")
  @Test
  void InvalidOperatingSystemBlank() throws Exception {
    ExecuteCommandService ser = spy(service);
    assertThatThrownBy(() -> ser.invoke(client, ""))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
    Mockito.verify(client, Mockito.times(1)).close();
  }
}
