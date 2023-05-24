/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.service.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;

import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.SFTPClient;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
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
class CopyFileTest {
  @Mock private SFTPClient sftpClient;
  @Mock private CopyFileService service;

  @BeforeEach
  public void init() throws IOException {
    service = new CopyFileService();
    service.setSourceFilePath("root/file1.txt");
    service.setActionIfFileExists("Rename");
    service.setCreateNewFolderIfNotExists("True");
    service.setTargetDirectory("c:/users/Downloads");
  }

  @DisplayName("Should Copy File")
  @Test
  void validTestCopyFile() throws Exception {
    String content = "";
    InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    OutputStream os = new ByteArrayOutputStream();

    FileMode fileMode = new FileMode(0100000);

    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");

    FileAttributes file = new FileAttributes(0, 2, 0, 0, fileMode, 0, 0, m);

    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(file, file, null, null);

    doNothing().when(sftpClient).mkdir(any(String.class));

    CopyFileService readFileServiceSpy = Mockito.spy(service);

    Mockito.doReturn(is).when(readFileServiceSpy).readFile(any(SFTPClient.class), any(Path.class));
    Mockito.doReturn(os)
        .when(readFileServiceSpy)
        .writeFile(any(SFTPClient.class), any(Path.class), any(Path.class));

    Response result = readFileServiceSpy.invoke(sftpClient);

    // Then
    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    Mockito.verify(sftpClient, Mockito.times(3)).stat(any(String.class));
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File Copied Successfully");
  }

  @DisplayName("Should throw an exception source file does not exists")
  @Test
  void invalidTestMoveFile() throws Exception {

    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(null);
    doNothing().when(sftpClient).rename(any(String.class), any(String.class));

    assertThatThrownBy(() -> service.invoke(sftpClient))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Source file does not exists");

    Mockito.verify(sftpClient, Mockito.times(1)).stat(any(String.class));

    Mockito.verify(sftpClient, Mockito.times(1)).close();
  }

  @DisplayName("Should rename file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWithRenameWhenSourceFileIsPresentInTarget() throws Exception {
    String content = "";
    InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    OutputStream os = new ByteArrayOutputStream();
    FileMode fileMode = new FileMode(0100000);
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileAttributes file = new FileAttributes(0, 2, 0, 0, fileMode, 0, 0, m);

    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(file, file, file, null);
    doNothing().when(sftpClient).mkdir(any(String.class));

    CopyFileService readFileServiceSpy = Mockito.spy(service);

    Mockito.doReturn(is).when(readFileServiceSpy).readFile(any(SFTPClient.class), any(Path.class));
    Mockito.doReturn(os)
        .when(readFileServiceSpy)
        .writeFile(any(SFTPClient.class), any(Path.class), any(Path.class));

    Response result = readFileServiceSpy.invoke(sftpClient);

    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    Mockito.verify(sftpClient, Mockito.times(4)).stat(any(String.class));
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File renamed and copied Successfully with new name as");
  }

  @DisplayName("Should replace file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWhenSourceFileIsPresentInTargetAndReplaceFile() throws Exception {
    service.setActionIfFileExists("replace");
    String content = "";
    InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    OutputStream os = new ByteArrayOutputStream();
    FileMode fileMode = new FileMode(0100000);
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileAttributes file = new FileAttributes(0, 2, 0, 0, fileMode, 0, 0, m);

    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(file, file, file, null);
    doNothing().when(sftpClient).mkdir(any(String.class));

    CopyFileService readFileServiceSpy = Mockito.spy(service);

    Mockito.doReturn(is).when(readFileServiceSpy).readFile(any(SFTPClient.class), any(Path.class));
    Mockito.doReturn(os)
        .when(readFileServiceSpy)
        .writeFile(any(SFTPClient.class), any(Path.class), any(Path.class));

    Response result = readFileServiceSpy.invoke(sftpClient);

    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    Mockito.verify(sftpClient, Mockito.times(3)).stat(any(String.class));
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File replaced successfully");
  }

  @DisplayName("Should skip file when source file is already present in a target folder")
  @Test
  void validTestMoveFileWhenSourceFileIsPresentInTargetAndSkipFile() throws Exception {
    service.setActionIfFileExists("skip");
    String content = "";
    InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    OutputStream os = new ByteArrayOutputStream();
    FileMode fileMode = new FileMode(0100000);
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileAttributes file = new FileAttributes(0, 2, 0, 0, fileMode, 0, 0, m);

    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(file, file, file, null);
    doNothing().when(sftpClient).mkdir(any(String.class));

    CopyFileService readFileServiceSpy = Mockito.spy(service);

    Mockito.doReturn(is).when(readFileServiceSpy).readFile(any(SFTPClient.class), any(Path.class));
    Mockito.doReturn(os)
        .when(readFileServiceSpy)
        .writeFile(any(SFTPClient.class), any(Path.class), any(Path.class));

    Response result = readFileServiceSpy.invoke(sftpClient);

    @SuppressWarnings("unchecked")
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    Mockito.verify(sftpClient, Mockito.times(3)).stat(any(String.class));
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File skipped successfully");
  }
}
