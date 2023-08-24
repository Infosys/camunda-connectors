package com.infosys.camundaconnectors.agile.jira.service;

import com.infosys.camundaconnectors.agile.jira.model.request.Authentication;
import com.infosys.camundaconnectors.agile.jira.model.request.JIRARequestData;
import com.infosys.camundaconnectors.agile.jira.model.response.JIRAResponse;
import com.infosys.camundaconnectors.agile.jira.model.response.Response;
import jakarta.validation.constraints.NotBlank;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateIssueService implements JIRARequestData {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateIssueService.class);

  @NotBlank private String issueKey;

  private String issueType;

  private String status;

  private String summary;

  private String description;

  private String assigneeAccID;

  private List<String> labels;

  private String sprint;

  private String sprintBoardId;

  private String storyPointEstimate;

  private String reporterAccID;

  private List<String> attachments;

  private String linkedIssuesRelation;

  private List<String> linkedIssues;

  private String flagged;

  public boolean isNullStr(String str) {
    return str == null || str.equals("") || str.equals("NA");
  }

  public boolean isNullList(List<String> list) {
    return (list == null || list.size() == 0);
  }

  public void deleteAttachment(
      File f, HttpResponse<JsonNode> attachmentsResponse, String issueKey, Authentication auth) {
    String url = auth.getUrl();
    String username = auth.getUsername();
    String password = auth.getPassword();
    JSONObject obj =
        (JSONObject) new JSONObject(attachmentsResponse.getBody().toString()).get("fields");
    JSONArray attachmentsList = obj.getJSONArray("attachment");
    boolean isDeleted = false;
    for (int i = 0; i < attachmentsList.length(); i++) {
      JSONObject attObj = new JSONObject(attachmentsList.get(i).toString());
      String filename = attObj.getString("filename");
      String currFilename = f.getName();
      if (currFilename.equals(filename)) {
        String fileId = attObj.getString("id");
        HttpResponse<JsonNode> deleteResponse =
            Unirest.delete(url + "/rest/api/latest/attachment/" + fileId)
                .basicAuth(username, password)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .asJson();
        if (isErrorExists(deleteResponse)) {
          String err = getErrorMessage(deleteResponse);
          throw new RuntimeException(err);
        }
        isDeleted = true;
        break;
      }
    }
    if (!isDeleted) {
      throw new RuntimeException("File not found to delete!!");
    }
  }

  public void addAttachment(
      File f, HttpResponse<JsonNode> attachmentsResponse, String issueKey, Authentication auth)
      throws Exception {
    String url = auth.getUrl();
    String username = auth.getUsername();
    String password = auth.getPassword();
    JSONObject obj =
        (JSONObject) new JSONObject(attachmentsResponse.getBody().toString()).get("fields");
    JSONArray attachmentsList = obj.getJSONArray("attachment");
    boolean isFileExist = false;
    for (int i = 0; i < attachmentsList.length(); i++) {
      JSONObject attObj = new JSONObject(attachmentsList.get(i).toString());
      String filename = attObj.getString("filename");
      String currFilename = f.getName();
      if (currFilename.equals(filename)) {
        isFileExist = true;
        break;
      }
    }
    if (isFileExist) {
      throw new RuntimeException("File with same name already exists!!");
    } else {
      InputStream in = new FileInputStream(f);
      HttpResponse<JsonNode> addResponse =
          Unirest.post(url + "/rest/api/latest/issue/" + issueKey + "/attachments")
              .basicAuth(username, password)
              .header("Accept", "application/json")
              .header("X-Atlassian-Token", "no-check")
              .field("file", in, f.getName())
              .asJson();
      if (isErrorExists(addResponse)) {
        String err = getErrorMessage(addResponse);
        throw new RuntimeException(err);
      }
    }
  }

  public String getIssueLinkID(
      String otherLinkType, String issueLinkTypeName, String otherIssueKey, Authentication auth) {
    String url = auth.getUrl();
    String username = auth.getUsername();
    String password = auth.getPassword();
    HttpResponse<JsonNode> resp =
        Unirest.get(url + "/rest/api/latest/issue/" + issueKey + "?fields=issuelinks")
            .basicAuth(username, password)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .asJson();
    JSONObject obj = (JSONObject) new JSONObject(resp.getBody().toString());
    obj = obj.getJSONObject("fields");
    JSONArray linksArr = obj.getJSONArray("issuelinks");
    String linkId = "-1";
    for (int i = 0; i < linksArr.length(); i++) {
      JSONObject jsonObj = linksArr.getJSONObject(i);
      String currLinkTypeName = jsonObj.getJSONObject("type").getString("name");
      String otherKey = jsonObj.getJSONObject(otherLinkType).getString("key");
      if (currLinkTypeName.equals(issueLinkTypeName) && otherIssueKey.equals(otherKey)) {
        linkId = jsonObj.getString("id");
        break;
      }
    }
    return linkId;
  }

  public String getIssueLink(String otherIssueKey, String otherLinkType) {
    String linkDirection = "->";
    if (otherLinkType.equals("outward")) linkDirection = "<-";
    String issueLink = "(" + issueKey + " " + linkDirection + " " + otherIssueKey + ")";
    return issueLink;
  }

  public void deleteIssueLink(
      String otherLinkType, String issueLinkTypeName, String otherIssueKey, Authentication auth) {
    String url = auth.getUrl();
    String username = auth.getUsername();
    String password = auth.getPassword();
    String issueLink = getIssueLink(otherIssueKey, otherLinkType);
    String linkId = getIssueLinkID(otherLinkType, issueLinkTypeName, otherIssueKey, auth);
    if (linkId.equals("-1"))
      throw new RuntimeException(
          "Link " + issueLink + "not found to delete!! Linking process stopped!!");
    else {
      HttpResponse<JsonNode> delResponse =
          Unirest.delete(url + "/rest/api/latest/issueLink/" + linkId)
              .basicAuth(username, password)
              .header("Accept", "application/json")
              .header("Content-Type", "application/json")
              .asJson();
      if (isErrorExists(delResponse)) {
        String err = getErrorMessage(delResponse);
        throw new RuntimeException(err);
      }
    }
  }

  public String getSprintID(String currSprintName, String boardId, Authentication auth) {
    String url = auth.getUrl();
    String username = auth.getUsername();
    String password = auth.getPassword();
    HttpResponse<JsonNode> sprints =
        Unirest.get(url + "/rest/agile/latest/board/" + boardId + "/sprint")
            .basicAuth(username, password)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .asJson();
    if (!isErrorExists(sprints)) {
      JSONObject sprintsObj = new JSONObject(sprints.getBody().toString());
      JSONArray sprintList = sprintsObj.getJSONArray("values");
      if (sprintList != null && sprintList.length() > 0) {
        for (int j = 0; j < sprintList.length(); j++) {
          String sprintName = sprintList.getJSONObject(j).getString("name");
          if (currSprintName.equals(sprintName)) {
            String sprintId = sprintList.getJSONObject(j).get("id").toString();
            return sprintId;
          }
        }
      }
    }
    return "-1";
  }

  public String getErrorMessage(HttpResponse<JsonNode> response) {
    JSONArray errorMessages =
        new JSONObject(response.getBody().toString()).getJSONArray("errorMessages");
    JSONObject errors = new JSONObject(response.getBody().toString()).getJSONObject("errors");
    errorMessages.put(errors.toString());
    String errMsg = "";
    for (int i = 0; i < errorMessages.length(); i++) {
      String err = errorMessages.getString(i);
      errMsg += (err + ";");
    }
    return errMsg;
  }

  public boolean isLabelExist(String labelname, JSONArray labels) {
    boolean isExist = false;
    for (int i = 0; i < labels.length(); i++) {
      if (labels.getString(i).equals(labelname)) {
        isExist = true;
        break;
      }
    }
    return isExist;
  }

  public boolean isErrorExists(HttpResponse<JsonNode> response) {
    try {
      JSONObject jsonObj = new JSONObject(response.getBody().toString());
      if (jsonObj != null) {
        boolean hasError = jsonObj.has("errorMessages");
        return hasError;
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  public HttpResponse<JsonNode> put(String url, String name, String pwd, String payload)
      throws Exception {
    HttpResponse<JsonNode> resp =
        Unirest.put(url)
            .basicAuth(name, pwd)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .body(payload)
            .asJson();
    if (isErrorExists(resp)) {
      String errMsg = getErrorMessage(resp);
      throw new RuntimeException(errMsg);
    }
    return resp;
  }

  public HttpResponse<JsonNode> get(String url, String name, String pwd) {
    HttpResponse<JsonNode> resp =
        Unirest.get(url)
            .basicAuth(name, pwd)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .asJson();
    if (isErrorExists(resp)) {
      String errMsg = getErrorMessage(resp);
      throw new RuntimeException(errMsg);
    }
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

  public HttpResponse<JsonNode> post(String url, String name, String pwd, String payload)
      throws Exception {
    HttpResponse<JsonNode> resp =
        Unirest.post(url)
            .basicAuth(name, pwd)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .body(payload)
            .asJson();
    if (isErrorExists(resp)) {
      String errMsg = getErrorMessage(resp);
      throw new RuntimeException(errMsg);
    }
    return resp;
  }

  @Override
  public Response invoke(Authentication auth) {
    try {
      String url = auth.getUrl();
      String username = auth.getUsername();
      String password = auth.getPassword();
      String errMsg = "";
      while (url.endsWith("/")) {
        url = url.substring(0, url.length() - 1);
      }
      String payload = "{ \"fields\": {\"issuetype\": {\"name\" : \"__ISSUETYPE__\"}";
      if (isNullStr(issueKey)) {
        throw new RuntimeException("Issue Key is mandatory!!");
      }
      if (isNullStr(issueType)) throw new RuntimeException("issueType should be non-empty!!");
      payload = payload.replaceAll("__ISSUETYPE__", issueType);
      if (!isNullStr(summary)) {
        while (summary.endsWith(",")) {
          summary = summary.substring(0, summary.length() - 1);
        }
        payload = payload + (", \"summary\" : \"" + summary + "\"");
      }
      if (!isNullStr(description)) {
        while (description.endsWith(",")) {
          description = description.substring(0, description.length() - 1);
        }
        payload = payload + (", \"description\" : \"" + description + "\"");
      }
      if (!isNullList(labels)) {
        HttpResponse<JsonNode> resp =
            get(url + "/rest/api/latest/issue/" + issueKey + "?fields=labels", username, password);
        JSONObject obj = (JSONObject) new JSONObject(resp.getBody().toString()).get("fields");
        JSONArray labelsList = obj.getJSONArray("labels");
        JSONArray currLabelsList = new JSONArray(labels);
        for (int i = 0; i < currLabelsList.length(); i++) {
          boolean deleteLabel = false;
          String label = currLabelsList.getString(i);
          if (label.startsWith("-")) deleteLabel = true;
          if (deleteLabel) {
            label = label.substring(1, label.length());
            boolean isExist = isLabelExist(label, labelsList);
            if (!isExist) throw new RuntimeException("Label (" + label + ") not found to delete!!");
            else {
              for (int x = 0; x < labelsList.length(); x++) {
                String labelname = labelsList.getString(x);
                if (label.equals(labelname)) {
                  labelsList.remove(x);
                  break;
                }
              }
            }
          } else {
            boolean isExist = isLabelExist(label, labelsList);
            if (isExist) throw new RuntimeException("Label (" + label + ") already exists!!");
            else {
              labelsList.put(label);
            }
          }
        }
        String labellist = labelsList.toString();
        payload = payload + (",\"labels\": " + labellist);
      }
      HashMap<String, String> nameIdMap = new HashMap<>();
      HttpResponse<JsonNode> fieldResponse =
          get(url + "/rest/api/latest/field", username, password);
      int cntFlag = 0;
      JSONArray fields = new JSONArray(fieldResponse.getBody().toString());
      for (int i = 0; i < fields.length(); i++) {
        if (cntFlag == 4) break;
        JSONObject obj = fields.getJSONObject(i);
        String name = obj.getString("name");
        if (name.equals("Sprint") && !nameIdMap.containsKey("Sprint")) {
          String id = obj.getString("id");
          nameIdMap.put("Sprint", id);
          cntFlag++;
        }
        if (name.equals("Story point estimate") && !nameIdMap.containsKey("Story point estimate")) {
          String id = obj.getString("id");
          nameIdMap.put("Story point estimate", id);
          cntFlag++;
        }
        if (name.equals("Flagged") && !nameIdMap.containsKey("Flagged")) {
          String id = obj.getString("id");
          nameIdMap.put("Flagged", id);
          cntFlag++;
        }
      }
      String fieldId = "";
      if (!isNullStr(flagged) && flagged.equals("true")) {
        fieldId = nameIdMap.get("Flagged");
        payload += (",\"" + fieldId + "\": " + "[{\"value\": \"Impediment\"}]");
      }
      if (!isNullStr(assigneeAccID)) {
        payload += (",\"assignee\" : {\"accountId\":" + "\"" + assigneeAccID + "\"" + "}");
      }
      if (!isNullStr(reporterAccID)) {
        payload += (",\"reporter\": {\"accountId\":" + "\"" + reporterAccID + "\"" + "}");
      }
      payload += "}}";
      HttpResponse<JsonNode> resp =
          put(url + "/rest/api/latest/issue/" + issueKey, username, password, payload + "}");
      String issueResponse = resp.getBody().toString();
      JSONObject jsonIssue = new JSONObject(issueResponse);
      if (!isNullStr(sprint) && !isNullStr(sprintBoardId)) {
        fieldId = nameIdMap.get("Sprint");
        String sprintID = getSprintID(sprint, sprintBoardId, auth);
        if (sprintID.equals("-1")) throw new RuntimeException("Invalid Sprint!!");
        String issuePayload = "{\"issues\": [\"" + issueKey + "\"]}";
        HttpResponse<JsonNode> sprintResp =
            post(
                url + "/rest/agile/1.0/sprint/" + sprintID + "/issue",
                username,
                password,
                issuePayload);
      }
      if (!isNullStr(storyPointEstimate)) {
        String spe = "";
        double dspe;
        try {
          dspe = Double.parseDouble(storyPointEstimate);
          spe = Double.toString(dspe);
        } catch (Exception e) {
          throw new RuntimeException("Story point estimate must be a numeric value!!");
        }
        fieldId = nameIdMap.get("Story point estimate");
        String spePayload = "{ \"fields\": { \"" + fieldId + "\":" + dspe + "}}";
        HttpResponse<JsonNode> speResp =
            put(url + "/rest/api/latest/issue/" + issueKey, username, password, spePayload);
      }
      if (!isNullStr(linkedIssuesRelation) && !isNullList(linkedIssues)) {
        String issueLinksPayload = "";
        HttpResponse<JsonNode> issueLinkData =
            get(url + "/rest/api/latest/issueLinkType", username, password);
        String presentLinkType = "";
        String issueLinkTypeName = "";
        String otherLinkType = "";
        boolean linkFound = false;
        JSONArray issueLinkTypes =
            new JSONObject(issueLinkData.getBody().toString()).getJSONArray("issueLinkTypes");
        for (int i = 0; i < issueLinkTypes.length(); i++) {
          JSONObject issueLinkjson = issueLinkTypes.getJSONObject(i);
          String inward = issueLinkjson.getString("inward");
          String outward = issueLinkjson.getString("outward");
          String name = issueLinkjson.getString("name");
          if (inward.equals(linkedIssuesRelation)) {
            issueLinkTypeName = name;
            presentLinkType = "inwardIssue";
            otherLinkType = "outwardIssue";
            linkFound = true;
            break;
          } else if (outward.equals(linkedIssuesRelation)) {
            issueLinkTypeName = name;
            presentLinkType = "inwardIssue";
            otherLinkType = "outwardIssue";
            linkFound = true;
            break;
          }
        }
        if (!linkFound) {
          throw new RuntimeException("Invalid issue link!!");
        } else {
          for (String key : linkedIssues) {
            boolean isDelete = false;
            if (key.startsWith("-")) {
              isDelete = true;
            }
            if (isDelete) {
              key = key.substring(1, key.length());
              deleteIssueLink(otherLinkType, issueLinkTypeName, key, auth);
            } else {
              String linkId = getIssueLinkID(otherLinkType, issueLinkTypeName, key, auth);
              if (!linkId.equals("-1")) {
                String issueLink = getIssueLink(key, otherLinkType);
                throw new RuntimeException(
                    "Issue link "
                        + issueLink
                        + " already exists for type ("
                        + issueLinkTypeName
                        + "). Unable to add!! Linking process stopped!!");
              }
              issueLinksPayload =
                  ("{\"type\" : {\"name\": \""
                      + issueLinkTypeName
                      + "\"}, \""
                      + presentLinkType
                      + "\": {\"key\":\""
                      + issueKey
                      + "\"}, \""
                      + otherLinkType
                      + "\": {\"key\":\""
                      + key
                      + "\"}}");
              HttpResponse<JsonNode> linkResponse =
                  post(url + "/rest/api/latest/issueLink", username, password, issueLinksPayload);
            }
          }
        }
      }
      if (!isNullList(attachments)) {
        try {
          HttpResponse<JsonNode> attachmentResponse =
              get(
                  url + "/rest/api/latest/issue/" + issueKey + "?fields=attachment",
                  username,
                  password);
          HashSet<String> filePaths = new HashSet<>();
          Queue<String> q = new LinkedList<>();
          for (String filePath : attachments) {
            q.add(filePath);
          }
          boolean deleteFlag = false;
          while (!q.isEmpty()) {
            int n = q.size();
            for (int i = 0; i < n; i++) {
              String fp = q.poll();
              if (fp.startsWith("-")) {
                deleteFlag = true;
                fp = fp.substring(1, fp.length());
              }
              File file = new File(fp);
              if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                  String path = fp + File.separator + f.getName();
                  q.add(path);
                }
              } else {
                if (deleteFlag) {
                  deleteAttachment(file, attachmentResponse, issueKey, auth);
                  deleteFlag = false;
                } else {
                  long maxFileSize = 10 * 1024 * 1024;
                  long currFileSize = file.length();
                  String filePath = file.getPath();
                  if (currFileSize == 0) throw new RuntimeException("Invalid Path!! - " + filePath);
                  if (currFileSize >= maxFileSize)
                    throw new RuntimeException(filePath + " - File size exceeded the limit(10MB)");
                  if (!filePaths.contains(filePath)) {
                    addAttachment(file, attachmentResponse, issueKey, auth);
                    filePaths.add(filePath);
                  }
                }
              }
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e.getMessage());
        }
      }
      if (isNullStr(status)) {
        HttpResponse<JsonNode> transitionResp =
            get(url + "/rest/api/latest/issue/" + issueKey + "/transitions", username, password);
        int statusCode = transitionResp.getStatus();
        String id = "11";
        JSONArray transitions =
            new JSONObject(transitionResp.getBody().toString()).getJSONArray("transitions");
        boolean found = false;
        for (int i = 0; i < transitions.length(); i++) {
          JSONObject transition = transitions.getJSONObject(i);
          String statusId = transition.getString("id");
          String statusName = transition.getString("name");
          if (statusName.equals(status)) {
            id = statusId;
            found = true;
            break;
          }
        }
        if (!found) {
          throw new RuntimeException("Invalid Status!!");
        }
        String statusPayload = "{\"transition\": { \"id\": \"" + id + "\"}}";
        HttpResponse<JsonNode> statusResponse =
            post(
                url + "/rest/api/latest/issue/" + issueKey + "/transitions",
                username,
                password,
                statusPayload);
      }
      LOGGER.info("Issue " + issueKey + "Successfully Updated!!");
      JIRAResponse<String> jiraResponse =
          new JIRAResponse<String>("Issue " + issueKey + " - Successfully Updated!!");
      checkResponseSize(jiraResponse);
      return jiraResponse;
    } catch (Exception e) {
      LOGGER.error("Error : " + e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
  }

  public String getIssueKey() {
    return issueKey;
  }

  public void setIssueKey(String issueKey) {
    this.issueKey = issueKey;
  }

  public String getIssueType() {
    return issueType;
  }

  public void setIssueType(String issueType) {
    this.issueType = issueType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAssigneeAccID() {
    return assigneeAccID;
  }

  public void setAssigneeAccID(String assigneeAccID) {
    this.assigneeAccID = assigneeAccID;
  }

  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public String getSprint() {
    return sprint;
  }

  public void setSprint(String sprint) {
    this.sprint = sprint;
  }

  public String getSprintBoardId() {
    return sprintBoardId;
  }

  public void setSprintBoardId(String sprintBoardId) {
    this.sprintBoardId = sprintBoardId;
  }

  public String getStoryPointEstimate() {
    return storyPointEstimate;
  }

  public void setStoryPointEstimate(String storyPointEstimate) {
    this.storyPointEstimate = storyPointEstimate;
  }

  public String getReporterAccID() {
    return reporterAccID;
  }

  public void setReporterAccID(String reporterAccID) {
    this.reporterAccID = reporterAccID;
  }

  public List<String> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<String> attachments) {
    this.attachments = attachments;
  }

  public String getLinkedIssuesRelation() {
    return linkedIssuesRelation;
  }

  public void setLinkedIssuesRelation(String linkedIssuesRelation) {
    this.linkedIssuesRelation = linkedIssuesRelation;
  }

  public List<String> getLinkedIssues() {
    return linkedIssues;
  }

  public void setLinkedIssues(List<String> linkedIssues) {
    this.linkedIssues = linkedIssues;
  }

  public String getFlagged() {
    return flagged;
  }

  public void setFlagged(String flagged) {
    this.flagged = flagged;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        assigneeAccID,
        attachments,
        description,
        flagged,
        issueKey,
        issueType,
        labels,
        linkedIssues,
        linkedIssuesRelation,
        reporterAccID,
        sprint,
        sprintBoardId,
        status,
        storyPointEstimate,
        summary);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    UpdateIssueService other = (UpdateIssueService) obj;
    return Objects.equals(assigneeAccID, other.assigneeAccID)
        && Objects.equals(attachments, other.attachments)
        && Objects.equals(description, other.description)
        && Objects.equals(flagged, other.flagged)
        && Objects.equals(issueKey, other.issueKey)
        && Objects.equals(issueType, other.issueType)
        && Objects.equals(labels, other.labels)
        && Objects.equals(linkedIssues, other.linkedIssues)
        && Objects.equals(linkedIssuesRelation, other.linkedIssuesRelation)
        && Objects.equals(reporterAccID, other.reporterAccID)
        && Objects.equals(sprint, other.sprint)
        && Objects.equals(sprintBoardId, other.sprintBoardId)
        && Objects.equals(status, other.status)
        && Objects.equals(storyPointEstimate, other.storyPointEstimate)
        && Objects.equals(summary, other.summary);
  }

  @Override
  public String toString() {
    return "UpdateIssueService [issueKey="
        + issueKey
        + ", issueType="
        + issueType
        + ", status="
        + status
        + ", summary="
        + summary
        + ", description="
        + description
        + ", assigneeAccID="
        + assigneeAccID
        + ", labels="
        + labels
        + ", sprint="
        + sprint
        + ", sprintBoardId="
        + sprintBoardId
        + ", storyPointEstimate="
        + storyPointEstimate
        + ", reporterAccID="
        + reporterAccID
        + ", attachments="
        + attachments
        + ", linkedIssuesRelation="
        + linkedIssuesRelation
        + ", linkedIssues="
        + linkedIssues
        + ", flagged="
        + flagged
        + "]";
  }
}
