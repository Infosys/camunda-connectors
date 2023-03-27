/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.smtp;

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

public class BaseTest {
  protected static final Gson gson = new Gson();
  protected static OutboundConnectorContext context;

  protected interface ActualValue {
    String TOKEN = "TOKEN_VALUE";
    String MAILBOX_NAME = "mailBoxName";
    String SUBJECT = "subject";

    interface Authentication {
      String HOST = "HOSTNAME";
      String PORT = "25";
      String USERNAME = "USER";
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

  protected static Stream<String> executeSendMailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_SEND_MAIL);
  }

  protected static Stream<String> executeInvalidSendMailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_SEND_MAIL);
  }

  protected static Stream<String> authenticationValidTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_AUTHENTICATION);
  }

  protected static Stream<String> authenticationInvalidTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_AUTHENTICATION);
  }

  protected static Stream<String> smtpFunctionValidTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_SMTP_FUNCTION);
  }

  protected static Stream<String> smtpFunctionInvalidTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_SMTP_FUNCTION);
  }

  @SuppressWarnings("unchecked")
  protected static Stream<String> loadTestCasesFromResourceFile(final String fileWithTestCasesUri)
      throws IOException {
    final String cases = readString(new File(fileWithTestCasesUri).toPath(), UTF_8);
    var array = gson.fromJson(cases, ArrayList.class);
    return array.stream().map(gson::toJson).map(Arguments::of);
  }
}
