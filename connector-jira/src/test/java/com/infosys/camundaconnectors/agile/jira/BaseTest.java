package com.infosys.camundaconnectors.agile.jira;

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

  protected static OutboundConnectorContext context;

  protected interface ActualValue {

    String METHOD = "jira.get-sprint-details";
    String boardId = "Id";
    String sprintState = "future";
    String TOKEN = "TOKEN";

    interface Authentication {
      String URL = "instanceurl-address";
      String USERNAME = "Username";
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
    return loadTestCasesFromResourceFile(TestCasesPath.VALIDATE_FIELDS_REQUIRED_FAIL);
  }

  protected static Stream<String> executeCreateIssueTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_CREATE_ISSUE);
  }

  protected static Stream<String> invalidCreateIssueTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_CREATE_ISSUE);
  }

  protected static Stream<String> executeCreateIssueEpicTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_CREATE_ISSUE_EPIC);
  }

  protected static Stream<String> invalidCreateIssueEpicTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_CREATE_ISSUE_EPIC);
  }

  protected static Stream<String> executeUpdateIssueTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_UPDATE_ISSUE);
  }

  protected static Stream<String> invalidUpdateIssueTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_UPDATE_ISSUE);
  }

  protected static Stream<String> executeUpdateIssueEpicTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_UPDATE_ISSUE_EPIC);
  }

  protected static Stream<String> invalidUpdateIssueEpicTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_UPDATE_ISSUE_EPIC);
  }

  protected static Stream<String> executeGetIssueTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_GET_ISSUE);
  }

  protected static Stream<String> invalidGetIssueTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_GET_ISSUE);
  }

  protected static Stream<String> executeGetSprintDetailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_GET_SPRINT_DETAILS);
  }

  protected static Stream<String> invalidGetSprintDetailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_GET_SPRINT_DETAILS);
  }

  protected static Stream<String> executeGetBoardDetailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_GET_BOARD_DETAILS);
  }

  protected static Stream<String> invalidGetBoardDetailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_GET_BOARD_DETAILS);
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
