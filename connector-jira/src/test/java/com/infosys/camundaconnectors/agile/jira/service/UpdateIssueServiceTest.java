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
import kong.unirest.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class UpdateIssueServiceTest {

  UpdateIssueService service;
  @Mock Authentication auth;
  CustomHttpResponseTest httpResponse;
  @Mock JSONObject jsonObj;
  @Mock JSONArray jsonArr;

  @BeforeEach
  public void init() {
    service = new UpdateIssueService();
    service.setIssueKey("key");
    service.setIssueType("Story");
    service.setStatus("To Do");
    service.setSummary("summary");
    service.setReporterAccID("reporterId");
    auth = new Authentication();
    auth.setUrl("instanceurl-address");
    auth.setUsername("Username");
    auth.setPassword("password");
  }

  @DisplayName("Should Update Issue when issueType is Story")
  @Test
  void validTestUpdateIssueWithTypeStory() throws Exception {
    UpdateIssueService serv = spy(service);
    String resp =
        "{\"key\": \"response_value\", \"transitions\":[{\"id\":\"statusId\", \"name\":\"statusName\"}]}";
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
    Mockito.doReturn(httpResponse)
        .when(serv)
        .put(any(String.class), any(String.class), any(String.class), any(String.class));
    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Successfully Updated");
  }

  @DisplayName("Should Update Issue when issueType is Task")
  @Test
  void validTestUpdateIssueWithTypeTask() throws Exception {
    service.setIssueType("Task");
    UpdateIssueService serv = spy(service);
    String resp =
        "{\"key\": \"response_value\", \"transitions\":[{\"id\":\"statusId\", \"name\":\"statusName\"}]}";
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
    Mockito.doReturn(httpResponse)
        .when(serv)
        .put(any(String.class), any(String.class), any(String.class), any(String.class));
    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Successfully Updated");
  }

  @DisplayName("Should Update Issue when issueType is Bug")
  @Test
  void validTestUpdateIssueWithTypeBug() throws Exception {
    service.setIssueType("Bug");
    UpdateIssueService serv = spy(service);
    String resp =
        "{\"key\": \"response_value\", \"transitions\":[{\"id\":\"statusId\", \"name\":\"statusName\"}]}";
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
    Mockito.doReturn(httpResponse)
        .when(serv)
        .put(any(String.class), any(String.class), any(String.class), any(String.class));
    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Successfully Updated");
  }

  @DisplayName("Should throw an exception without projectKey")
  @Test
  void invalidTestUpdateIssueWithNoProjectKey() throws Exception {
    service.setIssueKey(null);
    UpdateIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
