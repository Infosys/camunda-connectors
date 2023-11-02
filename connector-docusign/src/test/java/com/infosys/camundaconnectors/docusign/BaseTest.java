//
//  Copyright (c) 2023 Infosys Ltd.
//  Use of this source code is governed by MIT license that can be found in the LICENSE file
//  or at https://opensource.org/licenses/MIT
//
package com.infosys.camundaconnectors.docusign;

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

@SuppressWarnings("unused")
public abstract class BaseTest {
  //  protected static final Gson gson = GsonSupplier.getGson();
  protected static OutboundConnectorContext context;

  protected interface ActualValue {

    String METHOD = "docusign.getEnvelope";
    String envelopeId = "123";
    String TOKEN = "token";

    interface Authentication {
      String accountId = "cdsf-234d-234asd";
      String accessToken = "accessToken";
      String baseUri = "baseUri";
    }
  }

  protected interface SecretsConstant {
    String TOKEN = "TOKEN";
  }

  protected static OutboundConnectorContextBuilder getContextBuilderWithSecrets() {
    return OutboundConnectorContextBuilder.create()
        .secret(SecretsConstant.TOKEN, ActualValue.TOKEN);
  }

  protected static Stream<String> executeAddEnvelopeCustomFieldsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_ADD_ENVELOPE_CUSTOM_FIELDS);
  }

  protected static Stream<String> invalidAddEnvelopeCustomFieldsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_ADD_ENVELOPE_CUSTOM_FIELDS);
  }

  protected static Stream<String> executeGetEnvelopeTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_GET_ENVELOPE);
  }

  protected static Stream<String> invalidexecuteGetEnvelopeTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_GET_ENVELOPE);
  }

  protected static Stream<String> executeAddRecipientToEnvelopeTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_ADD_RECIPIENT_TO_ENVELOPE);
  }

  protected static Stream<String> invalidAddRecipientToEnvelopeTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_ADD_RECIPIENT_TO_ENVELOPE);
  }

  protected static Stream<String> executeUsersToGroupTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_ADD_USERS_TO_GROUP);
  }

  protected static Stream<String> invalidUsersToGroupTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_ADD_USERS_TO_GROUP);
  }

  protected static Stream<String> executeCreateEnvelopeTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_CREATE_ENVELOPE);
  }

  protected static Stream<String> invalidCreateEnvelopeTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_CREATE_ENVELOPE);
  }

  protected static Stream<String> executeCreateUsersTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_CREATE_USERS);
  }

  protected static Stream<String> invalidCreateUsersTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_CREATE_USERS);
  }

  protected static Stream<String> executeCustomApiRequestTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_CUSTOM_API_REQUEST);
  }

  protected static Stream<String> invalidCustomApiRequestTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_CUSTOM_API_REQUEST);
  }

  protected static Stream<String> executeDeleteUsersTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_DELETE_USERS_FROM_ACCOUNT);
  }

  protected static Stream<String> invalidDeleteUsersTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_DELETE_USERS_FROM_ACCOUNT);
  }

  protected static Stream<String> executeDeleteUsersFromGroupsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALID_DELETE_USERS_FROM_GROUPS);
  }

  protected static Stream<String> invalidDeleteUsersFromGroupsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_DELETE_USERS_FROM_GROUPS);
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
