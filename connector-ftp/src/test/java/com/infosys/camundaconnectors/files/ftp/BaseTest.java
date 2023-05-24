/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;

import com.google.gson.Gson;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public abstract class BaseTest {
  protected static final Gson gson = GsonSupplier.getGson();
  protected static OutboundConnectorContext context;

  protected interface ActualValue {
	
    String METHOD = "ftp.delete-file";
    String folderPath = "C:/Users/Documents";
    String fileName = "a.txt";
    String TOKEN = "TOKEN";  

    interface Authentication {
      String HOST = "HostName";
      String PORT = "21";
      String USERNAME = "Username";
      String PASSWORD = "secrets.TOKEN";
    }
  }

  protected interface SecretsConstant {
    String TOKEN = "TOKEN";
  }
  

  protected static OutboundConnectorContextBuilder getContextBuilderWithSecrets() {
    return OutboundConnectorContextBuilder.create()
        .secret(SecretsConstant.TOKEN, "TOKEN");
  }

  protected static Stream<String> replaceSecretsSuccessTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.REPLACE_SECRETS);
  }

  protected static Stream<String> validateRequiredFieldsFailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALIDATE_FIELDS_REQUIRED_FAIL);
  }

  protected static Stream<String> executeListFilesTestCases() throws IOException {
	return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_LIST_FILES);
  }

  protected static Stream<String> invalidListFilesTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_LIST_FILES);
  }

  protected static Stream<String> executeListFoldersTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_LIST_FOLDERS);
  }

  protected static Stream<String> invalidListFoldersTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_LIST_FOLDERS);
  }

  protected static Stream<String> executeDeleteFileTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_DELETE_FILE);
  }

  protected static Stream<String> invalidDeleteFileTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_DELETE_FILE);
  }

  protected static Stream<String> executeDeleteFolderTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_DELETE_FOLDER);
  }

  protected static Stream<String> invalidDeleteFolderTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_DELETE_FOLDER);
  }

  protected static Stream<String> executeCreateFolderTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_CREATE_FOLDER);
  }

  protected static Stream<String> invalidCreateFolderTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_CREATE_FOLDER);
  }

  protected static Stream<String> executeMoveFileTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_MOVE_FILE);
  }

  protected static Stream<String> invalidMoveFileTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_MOVE_FILE);
  }

  protected static Stream<String> executeWriteFileTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_WRITE_FILE);
  }

  protected static Stream<String> invalidWriteFileTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_WRITE_FILE);
  }
  
  protected static Stream<String> executeCopyFileTestCases() throws IOException {
	return loadTestCasesFromResourceFile(TestCasesPath.VALID_COPY_FILE);  
  }
  
  @SuppressWarnings("unchecked")
  protected static Stream<String> loadTestCasesFromResourceFile(final String fileWithTestCasesUri) throws IOException {
    final String cases = readString(new File(fileWithTestCasesUri).toPath(), UTF_8);
    final Gson testingGson = new Gson();
    var array = testingGson.fromJson(cases, ArrayList.class);
    return array.stream().map(testingGson::toJson).map(Arguments::of);
  }
}
