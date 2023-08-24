package com.infosys.camundaconnectors.agile.jira.service;

import com.infosys.camundaconnectors.agile.jira.model.request.Authentication;
import com.infosys.camundaconnectors.agile.jira.model.request.JIRARequestData;
import com.infosys.camundaconnectors.agile.jira.model.response.JIRAResponse;
import com.infosys.camundaconnectors.agile.jira.model.response.Response;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetIssueService implements JIRARequestData {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetIssueService.class);

  @NotBlank private String issueKey;

  private List<String> requiredFields;

  private List<String> excludeFields;

  private List<String> expandList;

  public HttpResponse<JsonNode> get(String url, String name, String pwd) {
    HttpResponse<JsonNode> response =
        Unirest.get(url)
            .basicAuth(name, pwd)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .asJson();
    return response;
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
      if (auth.getUrl() == null || auth.getUrl().isEmpty() || auth.getUrl().isBlank())
        throw new RuntimeException("Invalid input, please provide valid JIRA instance url.");
      if (auth.getUsername() == null
          || auth.getUsername().isEmpty()
          || auth.getUsername().isBlank()) {
        throw new RuntimeException(
            "Invalid input, please provide a valid username for JIRA access.");
      }
      if (auth.getPassword() == null
          || auth.getPassword().isEmpty()
          || auth.getPassword().isBlank()) {
        throw new RuntimeException("Invalid Input, please provid a valid password.");
      }
      if (requiredFields == null) requiredFields = new ArrayList<String>();
      if (excludeFields == null) excludeFields = new ArrayList<String>();

      if (!instanceUrl.endsWith("/")) instanceUrl += "/";
      instanceUrl += "rest/api/latest/issue/" + issueKey.strip();
      boolean isParam = false;
      if (requiredFields != null && !requiredFields.isEmpty()) {
        instanceUrl += "?fields=";
        isParam = true;
        for (String reqFields : requiredFields) {
          instanceUrl += (reqFields.strip() + ",");
        }
        if (instanceUrl != null
            && instanceUrl.length() > 0
            && instanceUrl.charAt(instanceUrl.length() - 1) == ',') {
          instanceUrl = instanceUrl.substring(0, instanceUrl.length() - 1);
        }
      }
      if (excludeFields != null && !excludeFields.isEmpty()) {
        if (isParam) {
          for (String exFields : excludeFields) {
            if (!exFields.startsWith("-")) exFields = "-" + exFields.strip();
            instanceUrl += ("," + exFields);
          }
        } else {
          instanceUrl += "?fields=*all,";
          isParam = true;
          for (String exFields : excludeFields) {
            if (!exFields.startsWith("-")) exFields = "-" + exFields.strip();
            instanceUrl += (exFields + ",");
          }
          if (instanceUrl != null
              && instanceUrl.length() > 0
              && instanceUrl.charAt(instanceUrl.length() - 1) == ',') {
            instanceUrl = instanceUrl.substring(0, instanceUrl.length() - 1);
          }
        }
      }
      if (expandList != null && expandList.size() != 0) {
        if (isParam) {
          instanceUrl += "&expand=";
          for (String exp : expandList) {
            instanceUrl += (exp.strip() + ",");
          }
          if (instanceUrl != null
              && instanceUrl.length() > 0
              && instanceUrl.charAt(instanceUrl.length() - 1) == ',') {
            instanceUrl = instanceUrl.substring(0, instanceUrl.length() - 1);
          }
        } else {
          instanceUrl += "?expand=";
          isParam = true;
          for (String exp : expandList) {
            instanceUrl += (exp.strip() + ",");
          }
          if (instanceUrl != null
              && instanceUrl.length() > 0
              && instanceUrl.charAt(instanceUrl.length() - 1) == ',') {
            instanceUrl = instanceUrl.substring(0, instanceUrl.length() - 1);
          }
        }
      }
      HttpResponse<JsonNode> response = get(instanceUrl, uname, pwd);
      String resp = "";
      String errMsg = "";
      JSONObject json = response.getBody().getObject();
      if (response.getStatus() == 404) {
        errMsg = json.get("errorMessages").toString();
        throw new RuntimeException(
            "Issue is not found or the user does not have permission to view it. ErrMsg: "
                + errMsg);
      } else if (response.getStatus() == 401) {
        errMsg = json.get("errorMessages").toString();
        throw new RuntimeException(
            "AuthenticationError: Invalid username or password. ErrMsg: " + errMsg);
      } else if (response.getStatus() != 200) {
        throw new RuntimeException(
            "ConnectionError: Error Connecting to Jira Server. HTTP response status code "
                + response.getStatus()
                + " &  ErrMsg: "
                + errMsg);
      }
      if (response.getStatus() == 200) {
        resp = response.getBody().getObject().toString();
        LOGGER.info("Successfully retrieved Jira issue details!!\n" + resp);
      }
      JIRAResponse<String> jiraResponse = new JIRAResponse<String>(resp);
      checkResponseSize(jiraResponse);
      return jiraResponse;
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
  }

  public String getIssueKey() {
    return issueKey;
  }

  public void setIssueKey(String issueKey) {
    this.issueKey = issueKey;
  }

  public List<String> getRequiredFields() {
    return requiredFields;
  }

  public void setRequiredFields(List<String> requiredFields) {
    this.requiredFields = requiredFields;
  }

  public List<String> getExcludeFields() {
    return excludeFields;
  }

  public void setExcludeFields(List<String> excludeFields) {
    this.excludeFields = excludeFields;
  }

  public List<String> getExpandList() {
    return expandList;
  }

  public void setExpandList(List<String> expandList) {
    this.expandList = expandList;
  }

  @Override
  public int hashCode() {
    return Objects.hash(excludeFields, expandList, issueKey, requiredFields);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GetIssueService other = (GetIssueService) obj;
    return Objects.equals(excludeFields, other.excludeFields)
        && Objects.equals(expandList, other.expandList)
        && Objects.equals(issueKey, other.issueKey)
        && Objects.equals(requiredFields, other.requiredFields);
  }

  @Override
  public String toString() {
    return "GetIssueService [issueKey="
        + issueKey
        + ", requiredFields="
        + requiredFields
        + ", excludeFields="
        + excludeFields
        + ", expandList="
        + expandList
        + "]";
  }
}
