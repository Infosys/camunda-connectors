package com.infosys.camundaconnectors.agile.jira.service;

import com.infosys.camundaconnectors.agile.jira.model.request.Authentication;
import com.infosys.camundaconnectors.agile.jira.model.request.JIRARequestData;
import com.infosys.camundaconnectors.agile.jira.model.response.JIRAResponse;
import com.infosys.camundaconnectors.agile.jira.model.response.Response;
import java.util.List;
import java.util.Objects;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetBoardDetailsService implements JIRARequestData {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetBoardDetailsService.class);

  private String boardName;

  private List<String> boardTypes;

  public String getTypeArgs(List<String> boardTypeList) {
    String typeArg = "";
    for (String type : boardTypeList) {
      typeArg += (type + ",");
    }
    return typeArg.substring(0, typeArg.length() - 1);
  }

  public HttpResponse<JsonNode> get(String url, String uname, String pwd) {

    HttpResponse<JsonNode> resp =
        Unirest.get(url).basicAuth(uname, pwd).header("Accept", "application/json").asJson();
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

      String instanceUrl = auth.getUrl();
      String uname = auth.getUsername();
      String pwd = auth.getPassword();

      if (instanceUrl == null
          || instanceUrl.equals("")
          || instanceUrl.length() == 0
          || instanceUrl.equalsIgnoreCase("NA"))
        throw new RuntimeException("InvalidJiraEntry : Provide Jira instance Url!!");

      if (uname == null || uname.isEmpty() || uname.isBlank())
        throw new RuntimeException("InvalidJiraEntry : Provide a valid username!!");

      if (pwd == null || pwd.isEmpty() || pwd.isBlank())
        throw new RuntimeException("InvalidJiraEntry : Provide a valid password!!");

      boolean isBoardNameEmpty = boardName == null || boardName.isEmpty() || boardName.isBlank();
      boolean isBoardTypeEmpty = boardTypes == null || boardTypes.size() == 0;

      if (isBoardNameEmpty && isBoardTypeEmpty) {
        throw new RuntimeException(
            "Atleast Either of the boardName or boardType should be not null!!");
      }
      String url = instanceUrl + "/rest/agile/1.0/board";
      if (!isBoardNameEmpty) {
        url += ("?name=" + boardName);
        if (!isBoardTypeEmpty) {
          String typeArg = getTypeArgs(boardTypes);
          url += ("&?type=" + typeArg);
        }
      } else {
        String typeArg = getTypeArgs(boardTypes);
        url += ("?type=" + typeArg);
      }
      HttpResponse<JsonNode> resp = get(url, uname, pwd);
      String jsonResp;
      if (resp.getStatus() == 400) throw new RuntimeException("Invalid request!!");
      else if (resp.getStatus() == 401)
        throw new RuntimeException("AuthenticationError: Invalid username or password!!");
      else if (resp.getStatus() == 403)
        throw new RuntimeException("User does not a have valid license!!");
      else if (resp.getStatus() == 404)
        throw new RuntimeException("User does not have permission to view it!!");
      else if (resp.getStatus() != 200)
        throw new RuntimeException(
            "ConnectionError: Error Connecting to Jira Server. HTTP response status code "
                + resp.getStatus());
      jsonResp = resp.getBody().toString();
      if (jsonResp != null || !jsonResp.isEmpty() || !jsonResp.isBlank()) {
        LOGGER.info("Retrieval Successful!! Board Details are :\n" + jsonResp);
        JIRAResponse<String> jiraResponse = new JIRAResponse<String>(jsonResp);
        checkResponseSize(jiraResponse);
        return jiraResponse;
      } else {
        throw new RuntimeException("Board does not exist!!");
      }
    } catch (Exception e) {
      LOGGER.error("Error:" + e.getMessage());
      throw new RuntimeException("Error:" + e.getMessage());
    }
  }

  public String getBoardName() {
    return boardName;
  }

  public void setBoardName(String boardName) {
    this.boardName = boardName;
  }

  public List<String> getBoardTypes() {
    return boardTypes;
  }

  public void setBoardTypes(List<String> boardTypes) {
    this.boardTypes = boardTypes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(boardName, boardTypes);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GetBoardDetailsService other = (GetBoardDetailsService) obj;
    return Objects.equals(boardName, other.boardName)
        && Objects.equals(boardTypes, other.boardTypes);
  }

  @Override
  public String toString() {
    return "GetBoardDetailsService [boardName=" + boardName + ", boardTypes=" + boardTypes + "]";
  }
}
