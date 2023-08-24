package com.infosys.camundaconnectors.agile.jira.service;

import com.infosys.camundaconnectors.agile.jira.model.request.Authentication;
import com.infosys.camundaconnectors.agile.jira.model.request.JIRARequestData;
import com.infosys.camundaconnectors.agile.jira.model.response.JIRAResponse;
import com.infosys.camundaconnectors.agile.jira.model.response.Response;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetSprintDetailsService implements JIRARequestData {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetSprintDetailsService.class);

  @NotBlank private String boardId;

  @NotBlank private String sprintState;

  public HttpResponse<JsonNode> get(String url, String name, String pwd) {
    HttpResponse<JsonNode> resp =
        Unirest.get(url).basicAuth(name, pwd).header("Accept", "application/json").asJson();
    return resp;
  }

  public void checkResponseSize(JIRAResponse<String> response) throws Exception {
    byte[] responseInByte = response.toString().getBytes();
    long responseSize = responseInByte.length;
    if (responseSize > 32766) {
      LOGGER.error("Response size is greater than 32766 bytes");
      throw new RuntimeException("Response size is greater than 32766");
    }
  }

  @Override
  public Response invoke(Authentication auth) {
    try {
      if (auth.getUrl() == null || auth.getUrl().isEmpty() || auth.getUrl().isBlank())
        throw new RuntimeException("InvalidServerEntry : provide jira url");
      if (auth.getUsername() == null
          || auth.getUsername().isEmpty()
          || auth.getUsername().isBlank())
        throw new RuntimeException("InvalidServerEntry : provide a valid username");
      if (auth.getPassword() == null
          || auth.getPassword().isEmpty()
          || auth.getPassword().isBlank())
        throw new RuntimeException("InvalidServerEntry : provide a valid password");
      try {
        int opt = Integer.parseInt(boardId);
      } catch (Exception e) {
        if (e.getMessage().equalsIgnoreCase("For input string: \"" + boardId + "\""))
          throw new RuntimeException("Invalid Input!!");
      }
      if (sprintState.equalsIgnoreCase("NA")) sprintState = "";
      sprintState = sprintState.toLowerCase();
      if (sprintState.equalsIgnoreCase("all")) sprintState = "";
      String url =
          auth.getUrl() + "/rest/agile/1.0/board/" + boardId + "/sprint?state=" + sprintState;
      HttpResponse<JsonNode> resp = get(url, auth.getUsername(), auth.getPassword());
      JSONObject json;
      if (resp.getStatus() == 400) throw new RuntimeException("InvalidInput : sprintState");
      else if (resp.getStatus() == 401)
        throw new RuntimeException("Authentication Failed : invalid username or password");
      else if (resp.getStatus() == 403)
        throw new RuntimeException("AuthenticationFailed : user does not a have valid license");
      else if (resp.getStatus() == 404)
        throw new RuntimeException(
            "UnableToFetchDetails : board does not exist or the user does not have permission to view it");
      else if (resp.getStatus() != 200)
        throw new RuntimeException(
            "ConnectionError: Error Connecting to Jira Server. HTTP response status code "
                + resp.getStatus());
      String jsonResp = resp.getBody().getObject().toString();
      LOGGER.info("Success!! SprintDetails --> " + jsonResp);
      JIRAResponse<String> jiraResponse = new JIRAResponse<String>(jsonResp);
      checkResponseSize(jiraResponse);
      return jiraResponse;
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
  }

  public String getBoardId() {
    return boardId;
  }

  public void setBoardId(String boardId) {
    this.boardId = boardId;
  }

  public String getSprintState() {
    return sprintState;
  }

  public void setSprintState(String sprintState) {
    this.sprintState = sprintState;
  }

  @Override
  public int hashCode() {
    return Objects.hash(boardId, sprintState);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GetSprintDetailsService other = (GetSprintDetailsService) obj;
    return Objects.equals(boardId, other.boardId) && Objects.equals(sprintState, other.sprintState);
  }

  @Override
  public String toString() {
    return "GetSprintDetailsService [boardId=" + boardId + ", sprintState=" + sprintState + "]";
  }
}
