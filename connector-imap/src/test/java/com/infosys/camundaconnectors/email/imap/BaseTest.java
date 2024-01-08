/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public abstract class BaseTest {

  protected interface ActualValue {
    String TOKEN = "TOKEN_KEY";
    String METHOD = "imap.delete-email";
    String MESSAGE_ID = "XE456OKL";
    String DOMAIN_NAME = "localhost.com";
    String KEY_STORE_PATH = "D:/keystore";
    String KEY_STORE_PASSWORD = "123";
    String FOLDER_PATH = "D:/Email";

    interface Authentication {
      String HOST = "HOSTNAME";
      String PORT = "995";
      String USERNAME = "ALPHA";
      String PASSWORD = "secrets.TOKEN";
      String DOMAIN_NAME = "localhost.com";
      String FOLDER_PATH = "Inbox";
      String KEY_STORE_PATH = "D:/keystore";
      String KEY_STORE_PASSWORD = "123";
    }
  }

  protected interface SecretsConstant {
    String TOKEN = "TOKEN";
  }

  protected static Stream<String> replaceSecretsSuccessTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.REPLACE_SECRETS);
  }

  protected static Stream<String> validateRequiredFieldsFailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALIDATE_REQUIRED_FIELDS_FAIL);
  }

  protected static Stream<String> executeDeleteEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_DELETE_EMAIL);
  }

  protected static Stream<String> executeInvalidDeleteEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_DELETE_EMAIL);
  }

  protected static Stream<String> executeDownloadEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_DOWNLOAD_EMAIL);
  }

  protected static Stream<String> executeInvalidDownloadEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_DOWNLOAD_EMAIL);
  }

  protected static Stream<String> executeListEmailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_LIST_EMAILS);
  }

  protected static Stream<String> executeInvalidListEmailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_LIST_EMAILS);
  }

  protected static Stream<String> executeMoveEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_MOVE_EMAIL);
  }

  protected static Stream<String> executeInvalidMoveEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_MOVE_EMAIL);
  }

  protected static Stream<String> executeReadEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_READ_EMAIL);
  }

  protected static Stream<String> executeInvalidReadEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_READ_EMAIL);
  }

  protected static Stream<String> executeSearchEmailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_SEARCH_EMAILS);
  }

  protected static Stream<String> executeInvalidSearchEmailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_SEARCH_EMAILS);
  }

  @SuppressWarnings("unchecked")
  protected static Stream<String> loadTestCasesFromResourceFile(final String fileWithTestCasesUri)
      throws IOException {
    final String cases = readString(new File(fileWithTestCasesUri).toPath(), UTF_8);
    final Gson testingGson = new Gson();
    var array = testingGson.fromJson(cases, ArrayList.class);
    return array.stream().map(testingGson::toJson).map(Arguments::of);
  }
}
