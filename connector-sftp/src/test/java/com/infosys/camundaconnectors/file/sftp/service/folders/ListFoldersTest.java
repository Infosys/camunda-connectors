/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.service.folders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;

import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.PathComponents;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
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
class ListFoldersTest {
  @Mock private SFTPClient sftpClient;
  @Mock private ListFoldersService service;

  @BeforeEach
  public void init() {
    service = new ListFoldersService();

    service.setFolderPath("C:/User/curation.bot/Documents");
    service.setModifiedAfter("31-3-2023 12:50:00");
    service.setModifiedBefore("31-3-2023 15:50:00");
    service.setOutputType("folderPaths");
    service.setMaxDepth("5");
    service.setMaxNumberOfFiles("100");
    service.setSearchSubFoldersAlso("False");
  }

  @DisplayName("Should return file names when modified before and modified after is given")
  @Test
  void validTestListFilesWithModifiedBeforeAndModifiedAfter() throws Exception {
    // input1
    PathComponents path1 = new PathComponents("root", "file_1", "dummypath");
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileMode file = new FileMode(0040000);
    long dummyMTime = 1680249379;
    RemoteResourceInfo remoteFiles =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));
    // input2
    PathComponents path2 = new PathComponents("root", "file_2", "dummypath");
    Map<String, String> m1 = new HashMap<String, String>();
    m.put("txt", "data");
    dummyMTime = 1680268225;
    file = new FileMode(0040000);
    RemoteResourceInfo remoteFiles2 =
        new RemoteResourceInfo(path2, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m1));
    List<RemoteResourceInfo> remoteResourceInfoMock = List.of(remoteFiles, remoteFiles2);
    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
    // When
    Response result = service.invoke(sftpClient);

    // Then
    @SuppressWarnings("unchecked")
    SFTPResponse<List<String>> queryResponse = (SFTPResponse<List<String>>) result;

    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @DisplayName(
      "Should return file names when modified before and modified after is given and one directory is given")
  @Test
  void validTestListFilesWithModifiedBeforeAndModifiedAfterWithdirectory() throws Exception {
    // when

    // given

    service.setOutputType("folderPaths");
    // input1
    PathComponents path1 = new PathComponents("root", "file_1", "dummypath");
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");

    FileMode file = new FileMode(0040000);
    long dummyMTime = 1680249379;
    RemoteResourceInfo remoteFiles =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));

    // input2

    PathComponents path2 = new PathComponents("root", "file_2", "dummypath");
    Map<String, String> m1 = new HashMap<String, String>();
    m.put("txt", "data");
    dummyMTime = 1680268225;
    file = new FileMode(0040000);
    RemoteResourceInfo remoteFiles2 =
        new RemoteResourceInfo(path2, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m1));
    // insert remoteResourceInfo into list

    RemoteResourceInfo remoteFiles3 =
        new RemoteResourceInfo(path2, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));
    // insert remoteResourceInfo into list
    List<RemoteResourceInfo> remoteResourceInfoMock =
        List.of(remoteFiles, remoteFiles2, remoteFiles3);

    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
    Mockito.when(sftpClient.size(any(String.class))).thenReturn((long) 2);
    // When
    Response result = service.invoke(sftpClient);

    // Then
    @SuppressWarnings("unchecked")
    SFTPResponse<List<String>> queryResponse = (SFTPResponse<List<String>>) result;

    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @DisplayName("Should return filePath in ascending order of there file name")
  @Test
  void validTestListFilesInAscendingOrder() throws Exception {
    // sort condition
    Map<String, String> sortBy = new TreeMap<>();
    sortBy.put("sortOn", "name");
    sortBy.put("order", "asc");
    service.setSortBy(sortBy);
    // input1
    PathComponents path1 = new PathComponents("root", "file_1", "dummypath");
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileMode file = new FileMode(0040000);
    long dummyMTime = 1680249379;
    RemoteResourceInfo remoteFiles =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));
    // input2
    PathComponents path2 = new PathComponents("root", "file_2", "dummypath");
    Map<String, String> m1 = new HashMap<String, String>();
    m.put("txt", "data");
    dummyMTime = 1680268225;
    file = new FileMode(0040000);
    RemoteResourceInfo remoteFiles2 =
        new RemoteResourceInfo(path2, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m1));
    RemoteResourceInfo remoteFiles3 =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));
    List<RemoteResourceInfo> remoteResourceInfoMock =
        List.of(remoteFiles, remoteFiles2, remoteFiles3);
    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
    Mockito.when(sftpClient.size(any(String.class))).thenReturn((long) 2);
    Response result = service.invoke(sftpClient);
    @SuppressWarnings("unchecked")
    SFTPResponse<List<String>> queryResponse = (SFTPResponse<List<String>>) result;

    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @DisplayName("Should return filePath in ascending order of there modified time")
  @Test
  void validTestListFilesInAscendingOrderOfModifiedTime() throws Exception {
    // input1
    PathComponents path1 = new PathComponents("root", "file_1", "dummypath");
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");

    FileMode file = new FileMode(0040000);
    long dummyMTime = 1680249379;
    RemoteResourceInfo remoteFiles =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));

    // input2
    m.put("txt", "data");
    dummyMTime = 1680268225;
    file = new FileMode(0040000);

    // insert remoteResourceInfo into list

    RemoteResourceInfo remoteFiles2 =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));

    RemoteResourceInfo remoteFiles3 =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));

    List<RemoteResourceInfo> remoteResourceInfoMock =
        List.of(remoteFiles, remoteFiles2, remoteFiles3);
    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
    Mockito.when(sftpClient.size(any(String.class))).thenReturn((long) 2);
    Response result = service.invoke(sftpClient);
    @SuppressWarnings("unchecked")
    SFTPResponse<List<String>> queryResponse = (SFTPResponse<List<String>>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @DisplayName("Should return file names when only modified before is given")
  @Test
  void validTestListFilesWithModifiedBefore() throws Exception {
    service.setModifiedAfter(null);
    PathComponents path1 = new PathComponents("root", "file_1", "dummypath");
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");
    FileMode file = new FileMode(0040000);
    long dummyMTime = 1680249379;
    RemoteResourceInfo remoteFiles =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));
    // input2
    PathComponents path2 = new PathComponents("root", "file_2", "dummypath");
    Map<String, String> m1 = new HashMap<String, String>();
    m.put("txt", "data");
    dummyMTime = 1680268225;
    file = new FileMode(0040000);
    RemoteResourceInfo remoteFiles2 =
        new RemoteResourceInfo(
            path2, new FileAttributes(0040000, 0, 0, 0, file, 0, dummyMTime, m1));
    List<RemoteResourceInfo> remoteResourceInfoMock = List.of(remoteFiles, remoteFiles2);
    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
    Response result = service.invoke(sftpClient);
    @SuppressWarnings("unchecked")
    SFTPResponse<List<String>> queryResponse = (SFTPResponse<List<String>>) result;

    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }

  @DisplayName("Should return file names when only modified After is given")
  @Test
  void validTestListFilesWithModifiedAfter() throws Exception {
    service.setModifiedBefore(null);
    PathComponents path1 = new PathComponents("root", "file_1", "dummypath");
    Map<String, String> m = new HashMap<String, String>();
    m.put("txt", "data");

    FileMode file = new FileMode(0040000);
    long dummyMTime = 1680249379;
    RemoteResourceInfo remoteFiles =
        new RemoteResourceInfo(path1, new FileAttributes(0, 0, 0, 0, file, 0, dummyMTime, m));
    PathComponents path2 = new PathComponents("root", "file_2", "dummypath");
    Map<String, String> m1 = new HashMap<String, String>();
    m.put("txt", "data");
    dummyMTime = 1680268225;
    file = new FileMode(0040000);
    RemoteResourceInfo remoteFiles2 =
        new RemoteResourceInfo(
            path2, new FileAttributes(0040000, 0, 0, 0, file, 0, dummyMTime, m1));
    List<RemoteResourceInfo> remoteResourceInfoMock = List.of(remoteFiles, remoteFiles2);
    Mockito.when(sftpClient.ls(any(String.class))).thenReturn(remoteResourceInfoMock);
    Response result = service.invoke(sftpClient);
    @SuppressWarnings("unchecked")
    SFTPResponse<List<String>> queryResponse = (SFTPResponse<List<String>>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(LIST).isNotEmpty();
  }
}
