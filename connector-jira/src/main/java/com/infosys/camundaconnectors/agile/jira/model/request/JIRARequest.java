/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.agile.jira.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.agile.jira.model.response.Response;
import com.infosys.camundaconnectors.agile.jira.service.CreateEpicIssueService;
import com.infosys.camundaconnectors.agile.jira.service.CreateIssueService;
import com.infosys.camundaconnectors.agile.jira.service.GetBoardDetailsService;
import com.infosys.camundaconnectors.agile.jira.service.GetIssueService;
import com.infosys.camundaconnectors.agile.jira.service.GetSprintDetailsService;
import com.infosys.camundaconnectors.agile.jira.service.UpdateEpicIssueService;
import com.infosys.camundaconnectors.agile.jira.service.UpdateIssueService;
import com.infosys.camundaconnectors.agile.jira.utility.JIRAServerClient;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class JIRARequest<T extends JIRARequestData> {
  @NotNull private Authentication authentication;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
      property = "operation")
  @JsonSubTypes(
      value = {
        @JsonSubTypes.Type(value = CreateEpicIssueService.class, name = "jira.create-issue-epic"),
        @JsonSubTypes.Type(value = CreateIssueService.class, name = "jira.create-issue"),
        @JsonSubTypes.Type(value = UpdateEpicIssueService.class, name = "jira.update-issue-epic"),
        @JsonSubTypes.Type(value = UpdateIssueService.class, name = "jira.update-issue"),
        @JsonSubTypes.Type(value = GetIssueService.class, name = "jira.get-issue"),
        @JsonSubTypes.Type(value = GetBoardDetailsService.class, name = "jira.get-board-details"),
        @JsonSubTypes.Type(value = GetSprintDetailsService.class, name = "jira.get-sprint-detais")
      })
  private T data;

  public Response invoke(JIRAServerClient jiraServerClient) throws Exception {
    jiraServerClient.loginJira(authentication);
    return data.invoke(authentication);
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JIRARequest<?> that = (JIRARequest<?>) o;
    return Objects.equals(authentication, that.authentication) && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authentication, data);
  }

  @Override
  public String toString() {
    return "JIRARequest [authentication=" + authentication + ", data=" + data + "]";
  }
}
