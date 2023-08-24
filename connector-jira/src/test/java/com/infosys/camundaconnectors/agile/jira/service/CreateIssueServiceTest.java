/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CreateIssueServiceTest {

  CreateIssueService service;
  @Mock Authentication auth;
  CustomHttpResponseTest httpResponse;
  @Mock JSONObject jsonObj;
  @Mock JSONArray jsonArr;

  @BeforeEach
  public void init() {
    service = new CreateIssueService();
    service.setProjectKey("key");
    service.setIssueType("Story");
    service.setStatus("To Do");
    service.setSummary("summary");
    service.setReporterAccID("reporterId");
    auth = new Authentication();
    auth.setUrl("instanceurl-address");
    auth.setUsername("Username");
    auth.setPassword("password");
  }

  @DisplayName("Should Create Issue when issueType is Story")
  @Test
  void validTestCreateIssueWithTypeStory() throws Exception {
    CreateIssueService serv = spy(service);
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
        .contains("Issue created successfully");
  }

  @DisplayName("Should Create Issue when issueType is Task")
  @Test
  void validTestCreateIssueWithTypeTask() throws Exception {
    service.setIssueType("Task");
    CreateIssueService serv = spy(service);
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
        .contains("Issue created successfully");
  }

  @DisplayName("Should Create Issue when issueType is Bug")
  @Test
  void validTestCreateIssueWithTypeBug() throws Exception {
    service.setIssueType("Bug");
    CreateIssueService serv = spy(service);
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
        .contains("Issue created successfully");
  }

  @DisplayName("Should throw an exception without projectKey")
  @Test
  void invalidTestCreateIssueWithNoProjectKey() throws Exception {
    service.setProjectKey(null);
    CreateIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Should throw an exception without issueType")
  @Test
  void invalidTestCreateIssueWithNoIssueType() throws Exception {
    service.setIssueType(null);
    CreateIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Should throw an exception without summary")
  @Test
  void invalidTestCreateIssueWithNoSummary() throws Exception {
    service.setSummary(null);
    CreateIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Should throw an exception without status")
  @Test
  void invalidTestCreateIssueWithNoStatus() throws Exception {
    service.setStatus(null);
    CreateIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Should throw an exception without reporterAccId")
  @Test
  void invalidTestCreateIssueWithNoReporterAccId() throws Exception {
    service.setReporterAccID(null);
    CreateIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
