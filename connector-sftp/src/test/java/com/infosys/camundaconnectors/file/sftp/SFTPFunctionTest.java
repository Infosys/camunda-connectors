/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;

import com.infosys.camundaconnectors.file.sftp.model.request.Authentication;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import com.infosys.camundaconnectors.file.sftp.utility.SftpServerClient;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.PathComponents;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPEngine;
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
class SFTPFunctionTest extends BaseTest {
  @Mock private SFTPEngine sftpEngine;
  @Mock private RemoteFile file;
  @Mock private SSHClient client;
  @Mock private SftpServerClient sftpServerClient;
  @Mock private SFTPClient sftpClient;
  @Mock private FileAttributes attrs;

  private SFTPFunction sftpFunction;

  @BeforeEach
  void init() throws Exception {
    sftpFunction = new SFTPFunction(gson, sftpServerClient);
    Mockito.when(sftpServerClient.loginSftp(any(Authentication.class))).thenReturn(client);
    Mockito.when(client.newSFTPClient()).thenReturn(sftpClient);
    PathComponents path1 = new PathComponents("root", "c", "dummypath");
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileMode file = new FileMode(0100000);
    long dummyMTime = 1680259327;
    RemoteResourceInfo remoteFiles =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));
    List<RemoteResourceInfo> remoteResourceInfoMock = List.of(remoteFiles);
    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
  }

  @ParameterizedTest
  @MethodSource("com.infosys.camundaconnectors.file.sftp.BaseTest#executeListFilesTestCases")
  void execute_shouldListFiles(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = sftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    SFTPResponse<List<String>> queryResponse = (SFTPResponse<List<String>>) executeResponse;
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidListFilesTestCases")
  void invalid_listFiles(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> sftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeListFoldersTestCases")
  void execute_shouldListFolders(String input) throws Exception {
    PathComponents path1 = new PathComponents("root", "file_1", "dummypath");
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileMode file = new FileMode(0040000);
    long dummyMTime = 1680249379;
    RemoteResourceInfo remoteFiles =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));
    List<RemoteResourceInfo> remoteResourceInfoMock = List.of(remoteFiles);
    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = sftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    SFTPResponse<List<String>> queryResponse = (SFTPResponse<List<String>>) executeResponse;
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidListFoldersTestCases")
  void invalid_shouldListFolders(String input) throws Exception {
    PathComponents path1 = new PathComponents("root", "file_1", "dummypath");
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileMode file = new FileMode(0040000);
    long dummyMTime = 1680249379;
    RemoteResourceInfo remoteFiles =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));
    List<RemoteResourceInfo> remoteResourceInfoMock = List.of(remoteFiles);
    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> sftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeDeleteFileTestCases")
  void execute_shouldDeleteFile(String input) throws Exception {
    doNothing().when(sftpClient).rm(isA(String.class));
    sftpClient.rm("");
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = sftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) executeResponse;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThatItsValid(executeResponse, "deleted successfully");
  }

  @ParameterizedTest
  @MethodSource("invalidDeleteFileTestCases")
  void invalid_shouldDeleteFile(String input) throws Exception {
    doNothing().when(sftpClient).rm(isA(String.class));
    sftpClient.rm("");
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> sftpFunction.execute(context))
        // Then
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeDeleteFolderTestCases")
  void execute_shouldDeleteFolder(String input) throws Exception {
    doNothing().when(sftpClient).rmdir(isA(String.class));
    sftpClient.rmdir("");
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = sftpFunction.execute(context);
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) executeResponse;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThatItsValid(executeResponse, "deleted successfully");
  }

  @ParameterizedTest
  @MethodSource("invalidDeleteFolderTestCases")
  void invalid_shouldDeleteFolder(String input) throws Exception {
    doNothing().when(sftpClient).rm(isA(String.class));
    sftpClient.rm("");
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> sftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .matches("Please provide a valid folderPath");
  }

  @ParameterizedTest
  @MethodSource("executeCreateFolderTestCases")
  void execute_shouldCreateFolder(String input) throws Exception {
    doNothing().when(sftpClient).mkdir(isA(String.class));
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = sftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) executeResponse;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("created successfully");
  }

  @ParameterizedTest
  @MethodSource("invalidCreateFolderTestCases")
  void invalid_shouldCreateFolder(String input) throws Exception {
    doNothing().when(sftpClient).mkdir(isA(String.class));
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> sftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeMoveFileTestCases")
  void execute_shouldMoveFile(String input) throws Exception {
    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(attrs, attrs, null);
    doNothing().when(sftpClient).rename(any(String.class), any(String.class));
    doNothing().when(sftpClient).rm(any(String.class));
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = sftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) executeResponse;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File Moved Successfully");
  }

  @ParameterizedTest
  @MethodSource("invalidMoveFileTestCases")
  void invalid_shouldMoveFile(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> sftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeWriteFileTestCases")
  void execute_shouldWriteFile(String input) throws Exception {
    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(attrs);
    Mockito.when(sftpClient.getSFTPEngine()).thenReturn(sftpEngine);
    Mockito.when(sftpClient.getSFTPEngine().open(any(String.class), any(EnumSet.class)))
        .thenReturn(file);
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = sftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) executeResponse;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File written successfully");
  }

  @ParameterizedTest
  @MethodSource("invalidWriteFileTestCases")
  void invalid_writeFileTestCase(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> sftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  private void assertThatItsValid(Object executeResponse, String validateAgainst) {
    assertThat(executeResponse).isInstanceOf(Response.class);
    @SuppressWarnings("unchecked")
    SFTPResponse<String> response = (SFTPResponse<String>) executeResponse;
    assertThat(response.getResponse()).isNotNull();
    assertThat(response)
        .extracting("response")
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .contains(validateAgainst);
  }
}
