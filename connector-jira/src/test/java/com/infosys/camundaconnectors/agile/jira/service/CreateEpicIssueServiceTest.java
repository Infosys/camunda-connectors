package com.infosys.camundaconnectors.agile.jira.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;

import com.infosys.camundaconnectors.agile.jira.model.request.Authentication;
import com.infosys.camundaconnectors.agile.jira.model.response.JIRAResponse;
import com.infosys.camundaconnectors.agile.jira.model.response.Response;
import com.infosys.camundaconnectors.agile.jira.utility.CustomHttpResponseTest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import kong.unirest.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
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
public class CreateEpicIssueServiceTest {

  CreateEpicIssueService service;
  Authentication auth;
  CustomHttpResponseTest httpResponse;
  @Mock JSONObject jsonObj;
  @Mock JSONArray jsonArr;
  @Mock File file;

  @BeforeEach
  public void init() {
    service = new CreateEpicIssueService();
    service.setProjectKey("key");
    service.setStatus("To Do");
    service.setSummary("summary");
    service.setReporterAccID("reporterId");
    auth = new Authentication();
    auth.setUrl("instanceurl-address");
    auth.setUsername("Username");
    auth.setPassword("password");
  }

  @DisplayName("Should Link Issues While Creating an Epic Issue")
  @Test
  void validTestLinkIssueCreateEpicIssue() throws Exception {
    List<String> issues = new ArrayList<>();
    issues.add("issue1");
    issues.add("issue2");
    service.setLinkedIssuesRelation("blocks");
    service.setLinkedIssues(issues);
    CreateEpicIssueService serv = spy(service);
    String resp =
        "{\"key\": \"response_value\", \"issueLinkTypes\": [{\"name\":\"Block\", \"inward\":\"is blocked by\", \"outward\": \"blocks\"}], \"transitions\":[{\"id\":\"statusId\", \"name\":\"To Do\"}]}";
    JsonNode jsonResp = new JsonNode(resp);
    httpResponse = new CustomHttpResponseTest(jsonResp, 200, "OK");
    String arrResp = "[{\"id\":\"statusId\", \"name\":\"statusName\"}]";
    JsonNode arrJsonResp = new JsonNode(arrResp);
    CustomHttpResponseTest httpArrResponse = new CustomHttpResponseTest(arrJsonResp, 200, "OK");
    Mockito.doReturn(httpArrResponse, httpResponse)
        .when(serv)
        .get(any(String.class), any(String.class), any(String.class));
    Mockito.doReturn(httpResponse)
        .when(serv)
        .post(any(String.class), any(String.class), any(String.class), any(String.class));
    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Issue Successfully Created");
  }

  @DisplayName("Should add attachment while Creating an Epic issue")
  @Test
  void validTestAddAttachmentsCreateEpicIssue() throws Exception {
    String PATH = "src/test/resources/test-cases/Sample.txt";
    List<String> attachments = new ArrayList<>();
    attachments.add(PATH);
    service.setAttachments(attachments);
    CreateEpicIssueService serv = spy(service);
    String resp =
        "{\"key\": \"response_value\", \"transitions\":[{\"id\":\"statusId\", \"name\":\"To Do\"}]}";
    JsonNode jsonResp = new JsonNode(resp);
    httpResponse = new CustomHttpResponseTest(jsonResp, 200, "OK");
    String arrResp = "[{\"id\":\"statusId\", \"name\":\"statusName\"}]";
    JsonNode arrJsonResp = new JsonNode(arrResp);
    CustomHttpResponseTest httpArrResponse = new CustomHttpResponseTest(arrJsonResp, 200, "OK");
    Mockito.doReturn(httpArrResponse, httpResponse)
        .when(serv)
        .get(any(String.class), any(String.class), any(String.class));
    Mockito.doReturn(httpResponse)
        .when(serv)
        .post(any(String.class), any(String.class), any(String.class), any(String.class));
    Mockito.doNothing()
        .when(serv)
        .addAttachments(any(File.class), any(String.class), any(Authentication.class));
    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Issue Successfully Created");
  }

  @DisplayName("Should throw an exception without projectKey")
  @Test
  void invalidTestCreateIssueWithNoProjectKey() throws Exception {
    service.setProjectKey(null);
    CreateEpicIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Should throw an exception without summary")
  @Test
  void invalidTestCreateIssueWithNoSummary() throws Exception {
    service.setSummary(null);
    CreateEpicIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Should throw an exception without status")
  @Test
  void invalidTestCreateIssueWithNoStatus() throws Exception {
    service.setStatus(null);
    CreateEpicIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Should throw an exception without reporterAccId")
  @Test
  void invalidTestCreateIssueWithNoReporterAccId() throws Exception {
    service.setReporterAccID(null);
    CreateEpicIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
