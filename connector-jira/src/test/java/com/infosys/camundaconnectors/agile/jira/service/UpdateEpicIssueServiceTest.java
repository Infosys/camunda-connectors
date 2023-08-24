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
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class UpdateEpicIssueServiceTest {

  UpdateEpicIssueService service;
  Authentication auth;
  CustomHttpResponseTest httpResponse;
  @Mock JSONObject jsonObj;
  @Mock JSONArray jsonArr;
  @Mock File file;

  @BeforeEach
  public void init() {
    service = new UpdateEpicIssueService();
    service.setIssueKey("key");
    service.setStatus("To Do");
    service.setSummary("summary");
    service.setReporterAccID("reporterId");
    auth = new Authentication();
    auth.setUrl("instanceurl-address");
    auth.setUsername("Username");
    auth.setPassword("password");
  }

  @DisplayName("Should Update Epic Issue")
  @Test
  void validTestUpdateEpicIssue() throws Exception {
    UpdateEpicIssueService serv = spy(service);
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

  @DisplayName("Should add attachment while Updating an Epic issue")
  @Test
  void validTestAddAttachmentsUpdateEpicIssue() throws Exception {
    String PATH = "src/test/resources/test-cases/Sample.txt";
    List<String> attachments = new ArrayList<>();
    attachments.add(PATH);
    service.setAttachments(attachments);
    UpdateEpicIssueService serv = spy(service);
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
    Mockito.doReturn(httpResponse)
        .when(serv)
        .put(any(String.class), any(String.class), any(String.class), any(String.class));
    Mockito.doNothing()
        .when(serv)
        .addAttachment(
            any(File.class), any(HttpResponse.class), any(String.class), any(Authentication.class));
    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Successfully Updated");
  }

  @DisplayName("Should throw an exception without issueKey")
  @Test
  void invalidTestCreateIssueWithNoProjectKey() throws Exception {
    service.setIssueKey(null);
    UpdateEpicIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
