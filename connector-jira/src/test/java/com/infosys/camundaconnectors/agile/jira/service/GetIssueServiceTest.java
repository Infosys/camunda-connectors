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
import java.util.ArrayList;
import java.util.List;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class GetIssueServiceTest {
  GetIssueService service;
  @Mock HttpResponse<JsonNode> resp;
  @Mock CustomHttpResponseTest httpResponse;
  Authentication auth;
  @Mock JsonNode jsonResp;

  @BeforeEach
  public void init() {
    service = new GetIssueService();
    service.setIssueKey("key");
    List<String> fields = new ArrayList<>();
    fields.add("field1");
    fields.add("field2");
    service.setRequiredFields(fields);
    service.setExcludeFields(fields);
    service.setExpandList(fields);
    auth = new Authentication();
    auth.setUrl("instanceurl-address");
    auth.setUsername("UserName");
    auth.setPassword("password");
  }

  @DisplayName("Should Get Issue Details with issueKey")
  @Test
  void validTestGetsprintWithNameAndType() throws Exception {
    GetIssueService serv = spy(service);
    String issueResp = "{\"fields\":{\"field_key\":\"field_value\"}}";
    JsonNode jsonResp = new JsonNode(issueResp);
    httpResponse = new CustomHttpResponseTest(jsonResp, 200, "OK");
    Mockito.doReturn(httpResponse)
        .when(serv)
        .get(any(String.class), any(String.class), any(String.class));
    Response result = serv.invoke(auth);
    @SuppressWarnings("unchecked")
    JIRAResponse<String> queryResponse = (JIRAResponse<String>) result;
    assertThat(queryResponse).extracting("response").asInstanceOf(STRING).contains("fields");
  }

  @DisplayName("Should throw an exception without issueKey")
  @Test
  void validTestGetsprintWithName() throws Exception {
    service.setIssueKey(null);
    GetIssueService serv = spy(service);
    assertThatThrownBy(() -> serv.invoke(auth))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
