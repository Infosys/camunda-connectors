/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.files.ftp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import com.infosys.camundaconnectors.files.ftp.model.request.Authentication;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.utility.FTPServerClient;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
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
public class FTPFunctionTest extends BaseTest {
  
  @Mock private FTPServerClient ftpServerClient;
  @Mock private FTPFunction ftpFunction;
  @Mock private FTPClient ftpClient;
  @Mock private FTPFile file;
  
  String folderPath;
  @BeforeEach
  public void init() throws Exception {
    ftpFunction = new FTPFunction(gson, ftpServerClient);
    folderPath = "C:/Documents/ftproot";
    Mockito.when(ftpServerClient.loginFTP(any(Authentication.class))).thenReturn(ftpClient);
    FTPFile file1 = new FTPFile();
    file1.setName("a.txt");
    FTPFile file2 = new FTPFile();
    file2.setName("b.txt");
    FTPFile file3 = new FTPFile();
    file3.setName("c.txt");
    FTPFile[] ftpFiles = {file1, file2, file3};
    Mockito.when(ftpClient.listFiles(folderPath)).thenReturn(ftpFiles);
  }
  
  @ParameterizedTest
  @MethodSource("executeListFilesTestCases")
  void execute_shouldListFiles(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = ftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) executeResponse;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidListFilesTestCases")
  void invalid_listFiles(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> ftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeListFoldersTestCases")
  void execute_shouldListFolders(String input) throws Exception {
	String folderPath = "/Documents/ftproot";
	FTPFile folder1 = new FTPFile();
	folder1.setName("folderA");
	FTPFile folder2 = new FTPFile();
	folder2.setName("folderB");
	FTPFile folder3 = new FTPFile();
	folder3.setName("folderC");
	FTPFile[] ftpFolders = {folder1, folder2, folder3};
	Mockito.when(ftpClient.listFiles(folderPath)).thenReturn(ftpFolders);
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = ftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) executeResponse;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidListFoldersTestCases")
  void invalid_shouldListFolders(String input) throws Exception {
	FTPFile folder1 = new FTPFile();
	folder1.setName("folderA");
	FTPFile folder2 = new FTPFile();
	folder2.setName("folderB");
	FTPFile folder3 = new FTPFile();
	folder3.setName("folderC");
	FTPFile[] ftpFolders = {folder1, folder2, folder3};
	Mockito.when(ftpClient.listFiles(folderPath)).thenReturn(ftpFolders);
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> ftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeDeleteFileTestCases")
  void execute_shouldDeleteFile(String input) throws Exception {
	FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
	file3.setName("c.txt");
	FTPFile[] files = {file1};
	file1.setType(0);
	Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
	Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
	Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    ftpClient.deleteFile("");
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = ftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) executeResponse;
    assertThatItsValid(executeResponse, "File Deleted Successfully");
  }

  @ParameterizedTest
  @MethodSource("invalidDeleteFileTestCases")
  void invalid_shouldDeleteFile(String input) throws Exception {
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    ftpClient.deleteFile("");
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> ftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeDeleteFolderTestCases")
  void execute_shouldDeleteFolder(String input) throws Exception {
	Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
	Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
	FTPFile[] files = {};
	Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = ftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) executeResponse;
    assertThatItsValid(executeResponse, "Folder Deletion Successful!!");
  }

  @ParameterizedTest
  @MethodSource("invalidDeleteFolderTestCases")
  void invalid_shouldDeleteFolder(String input) throws Exception {
	Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    ftpClient.deleteFile("");
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> ftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .contains("must not be blank");
  }

  @ParameterizedTest
  @MethodSource("executeCreateFolderTestCases")
  void execute_shouldCreateFolder(String input) throws Exception {
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = ftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) executeResponse;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("New Folder Successfully created!!");
  }

  @ParameterizedTest
  @MethodSource("invalidCreateFolderTestCases")
  void invalid_shouldCreateFolder(String input) throws Exception {
    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> ftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeMoveFileTestCases")
  void execute_shouldMoveFile(String input) throws Exception {
    Mockito.when(ftpClient.rename(any(String.class), any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	FTPFile file2 = new FTPFile();
	file2.setName("b.txt");
	FTPFile file3 = new FTPFile();
	file3.setName("c.txt");
	file1.setType(0);
	FTPFile[] files = {file1};
	FTPFile[] ftpFiles = {file1, file2, file3};
	Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files, ftpFiles);
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = ftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) executeResponse;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("File moved successfully!!");
  }

  @ParameterizedTest
  @MethodSource("invalidMoveFileTestCases")
  void invalid_shouldMoveFile(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> ftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeWriteFileTestCases")
  void execute_shouldWriteFile(String input) throws Exception {
	
	FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	file1.setType(0);
	FTPFile[] files = {file1};
	Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
	OutputStream os =  new ByteArrayOutputStream();
    Mockito.doReturn(os).when(ftpClient).storeFileStream(any(String.class));
    Mockito.doReturn(os).when(ftpClient).appendFileStream(any(String.class));
    Mockito.when(file.isFile()).thenReturn(true);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    Mockito.when(ftpClient.completePendingCommand()).thenReturn(true);
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = ftpFunction.execute(context);
    @SuppressWarnings("unchecked")
    FTPResponse<String> queryResponse = (FTPResponse<String>) executeResponse;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Successfully written the content!!");
  }

  @ParameterizedTest
  @MethodSource("invalidWriteFileTestCases")
  void invalid_writeFileTestCase(String input) throws Exception {
	FTPFile file1 = new FTPFile();
	file1.setName("a.txt");
	file1.setType(0);
	FTPFile[] files = {file1};
	Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> ftpFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  private void assertThatItsValid(Object executeResponse, String validateAgainst) {
    assertThat(executeResponse).isInstanceOf(FTPResponse.class);
    @SuppressWarnings("unchecked")
    FTPResponse<String> response = (FTPResponse<String>) executeResponse;
    assertThat(response.getResponse()).isNotNull();
    assertThat(response)
        .extracting("response")
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .contains(validateAgainst);
  }
}
