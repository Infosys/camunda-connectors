/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.postgresql;

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
  protected static OutboundConnectorContext context;

  protected interface ActualValue {
    String TOKEN = "TOKEN_KEY";
    String METHOD = "postgresql.create-table";
    String DATABASE_NAME = "XE";
    String TABLE_NAME = "testRio";

    interface DatabaseConnection {
      String HOST = "HOSTNAME";
      String PORT = "5432";
      String USERNAME = "ALPHA";
      String PASSWORD = "secrets.TOKEN";
    }
  }

  protected interface SecretsConstant {
    String TOKEN = "TOKEN";
  }

  protected static OutboundConnectorContextBuilder getContextBuilderWithSecrets() {
    return OutboundConnectorContextBuilder.create()
        .secret(SecretsConstant.TOKEN, ActualValue.TOKEN);
  }

  protected static Stream<String> replaceSecretsSuccessTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.REPLACE_SECRETS);
  }

  protected static Stream<String> validateRequiredFieldsFailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALIDATE_REQUIRED_FIELDS_FAIL);
  }

  protected static Stream<String> executeCreateDatabaseTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_CREATE_DATABASE);
  }

  protected static Stream<String> executeInvalidCreateDatabaseTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_CREATE_DATABASE);
  }

  protected static Stream<String> executeCreateTableTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_CREATE_TABLE);
  }

  protected static Stream<String> executeInvalidCreateTableTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_CREATE_TABLE);
  }

  protected static Stream<String> executeInsertDataTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_INSERT_DATA);
  }

  protected static Stream<String> executeInvalidInsertDataTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_INSERT_DATA);
  }

  protected static Stream<String> executeDeleteDataTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_DELETE_DATA);
  }

  protected static Stream<String> executeInvalidDeleteDataTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_DELETE_DATA);
  }

  protected static Stream<String> executeUpdateDataTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_UPDATE_DATA);
  }

  protected static Stream<String> executeInvalidUpdateDataTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_UPDATE_DATA);
  }

  protected static Stream<String> executeAlterTableTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_ALTER_TABLE);
  }

  protected static Stream<String> executeInvalidAlterTableTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_ALTER_TABLE);
  }

  protected static Stream<String> executeReadDataTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_READ_DATA);
  }

  protected static Stream<String> executeInvalidReadDataTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_READ_DATA);
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
