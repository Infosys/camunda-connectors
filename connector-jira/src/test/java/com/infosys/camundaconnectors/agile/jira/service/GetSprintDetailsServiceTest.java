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
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class GetSprintDetailsServiceTest {

  GetSprintDetailsService service;
  @Mock HttpResponse<JsonNode> resp;
  @Mock CustomHttpResponseTest httpResponse;
  Authentication auth;
  @Mock JsonNode jsonResp;

  @BeforeEach
  public void init() {
    service = new GetSprintDetailsService();
    service.setBoardId("132");
    service.setSprintState("future");
    auth = new Authentication();
    auth.setUrl("instanceurl-address");
    auth.setUsername("UserName");
    auth.setPassword("password");
  }

  @DisplayName("Should Get Sprint Details with boardId and sprintState")
  @Test
  void validTestGetsprintWithNameAndType() throws Exception {
    GetSprintDetailsService serv = spy(service);
    String sprintResp = "{\"values\":[{\"id\":\"sprintId\", \"state\":\"sprintState\"}]}";
    JsonNode jsonResp = new JsonNode(sprintResp);
    httpResponse = new CustomHttpResponseTest(jsonResp, 200, "OK");
    Mockito.doReturn(httpResponse)
        .when(serv)
        .get(any(String.class), any(String.class), any(String.class));
    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).contains("values");
  }

  @DisplayName("Should throw an exception without boardId")
  @Test
  void validTestGetsprintWithName() throws Exception {
    service.setBoardId(null);
    GetSprintDetailsService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Should throw an exception without sprintState")
  @Test
  void validTestGetsprintWithType() throws Exception {
    service.setSprintState(null);
    GetSprintDetailsService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Should throw an exception without boardId and sprintState")
  @Test
  void invalidTestGetsprint() throws Exception {
    service.setBoardId(null);
    service.setSprintState(null);
    GetSprintDetailsService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
