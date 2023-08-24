package com.infosys.camundaconnectors.agile.jira;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.camundaconnectors.agile.jira.utility.JIRAServerClient;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.RequestBodyEntity;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JIRAFunctionTest extends BaseTest {

  @Mock JIRAServerClient jiraServerClient;
  @Mock Unirest uni;
  @Mock HttpRequestWithBody req;
  @Mock RequestBodyEntity reqBodyEntity;
  @Mock HttpResponse<JsonNode> resp;
  @Mock JsonNode json;
  @Mock JSONObject jsonObj;
  @Mock GetRequest getReq;
  @Mock JSONArray jsonArr;
  private JIRAFunction jiraFunction;
  ObjectMapper gson;

  @BeforeEach
  public void init() throws Exception {
    jiraFunction = new JIRAFunction(jiraServerClient);
  }

  @ParameterizedTest
  @MethodSource("invalidCreateIssueTestCases")
  void invalid_createIssue(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> jiraFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidCreateIssueEpicTestCases")
  void invalid_createIssueEpic(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> jiraFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidUpdateIssueTestCases")
  void invalid_updateIssue(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> jiraFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidUpdateIssueEpicTestCases")
  void invalid_updateIssueEpic(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> jiraFunction.execute(context)).isInstanceOf(RuntimeException.class);
  }

  @ParameterizedTest
  @MethodSource("invalidGetIssueTestCases")
  void invalid_getIssue(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> jiraFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidGetSprintDetailsTestCases")
  void invalid_getSprintDetails(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> jiraFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidGetBoardDetailsTestCases")
  void invalid_getBoardDetails(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> jiraFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
