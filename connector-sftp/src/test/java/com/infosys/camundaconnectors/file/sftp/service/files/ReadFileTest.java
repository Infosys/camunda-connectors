/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.service.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;

import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.SFTPClient;
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
class ReadFileTest {
  private ReadFileService service;
  @Mock private SFTPClient sftpClient;

  @BeforeEach
  public void init() throws IOException {
    service = new ReadFileService();
    service.setSourceFilePath("C:\\Users\\abc\\Exam.txt");
  }

  @DisplayName("Should read file")
  @Test
  void validTestReadFile() throws Exception {
    FileMode fileMode = new FileMode(0100000);
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileAttributes file = new FileAttributes(0, 2, 0, 0, fileMode, 0, 0, m);
    String content = "Hello, world";
    InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    ReadFileService readFileServiceSpy = Mockito.spy(service);
    Mockito.doReturn(is)
        .when(readFileServiceSpy)
        .readFile(any(SFTPClient.class), any(String.class));
    Mockito.when(sftpClient.stat(any(String.class))).thenReturn(file);
    Response result = readFileServiceSpy.invoke(sftpClient);
    SFTPResponse<String> queryResponse = (SFTPResponse<String>) result;
    Mockito.verify(sftpClient, Mockito.times(1)).close();
    Mockito.verify(sftpClient, Mockito.times(1)).stat(any(String.class));
    assertThat(queryResponse).extracting("response").isInstanceOf(String.class).isNotNull();
  }

  @DisplayName("invalid read file")
  @Test
  void invalidTestReadFile() throws Exception {
    service.setSourceFilePath("");
    ReadFileService readFileServiceSpy = Mockito.spy(service);
    assertThatThrownBy(() -> readFileServiceSpy.invoke(sftpClient))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
