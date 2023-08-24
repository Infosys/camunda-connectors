package com.infosys.camundaconnectors.agile.jira.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;

import com.infosys.camundaconnectors.agile.jira.model.request.Authentication;
import com.infosys.camundaconnectors.agile.jira.model.response.JIRAResponse;
import com.infosys.camundaconnectors.agile.jira.model.response.Response;
import com.infosys.camundaconnectors.agile.jira.utility.CustomHttpResponseTest;
import java.util.ArrayList;
import java.util.List;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class GetBoardDetailsServiceTest {

  GetBoardDetailsService service;
  @Mock HttpResponse<JsonNode> resp;
  @Mock CustomHttpResponseTest httpResponse;
  Authentication auth;
  @Mock JsonNode jsonResp;
  GetRequest gReq;

  @BeforeEach
  public void init() {
    service = new GetBoardDetailsService();
    service.setBoardName("name");
    List<String> boardType = new ArrayList<>();
    boardType.add("type");
    service.setBoardTypes(boardType);
    auth = new Authentication();
    auth.setUrl("instanceurl-address");
    auth.setUsername("UserName");
    auth.setPassword("password");
  }

  @DisplayName("Should Get Board Details with name and type")
  @Test
  void validTestGetBoardWithNameAndType() throws Exception {
    GetBoardDetailsService serv = spy(service);
    String boardResp = "{\"values\":[{\"name\":\"boardName\", \"type\":\"boardType\"}]}";
    JsonNode jsonResp = new JsonNode(boardResp);
    httpResponse = new CustomHttpResponseTest(jsonResp, 200, "OK");
    Mockito.doReturn(httpResponse)
        .when(serv)
        .get(any(String.class), any(String.class), any(String.class));

    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).contains("values");
  }

  @DisplayName("Should Get Board Details with name")
  @Test
  void validTestGetBoardWithName() throws Exception {
    service.setBoardTypes(null);
    GetBoardDetailsService serv = spy(service);
    String boardResp = "{\"values\":[{\"name\":\"boardName\"}]}";
    JsonNode jsonResp = new JsonNode(boardResp);
    httpResponse = new CustomHttpResponseTest(jsonResp, 200, "OK");
    Mockito.doReturn(httpResponse)
        .when(serv)
        .get(any(String.class), any(String.class), any(String.class));

    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).contains("values");
  }

  @DisplayName("Should Get Board Details with type")
  @Test
  void validTestGetBoardWithType() throws Exception {
    service.setBoardName(null);
    GetBoardDetailsService serv = spy(service);
    String boardResp = "{\"values\":[{\"name\":\"boardName\"}]}";
    JsonNode jsonResp = new JsonNode(boardResp);
    httpResponse = new CustomHttpResponseTest(jsonResp, 200, "OK");
    Mockito.doReturn(httpResponse)
        .when(serv)
        .get(any(String.class), any(String.class), any(String.class));
    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).contains("values");
  }

  @DisplayName("Should throw an exception without name and type")
  @Test
  void invalidTestGetBoard() throws Exception {
    service.setBoardName(null);
    service.setBoardTypes(null);
    GetBoardDetailsService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
