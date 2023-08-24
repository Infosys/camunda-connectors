/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.agile.jira;

public interface TestCasesPath {
  String PATH = "src/test/resources/test-cases/";

  String REPLACE_SECRETS = PATH + "replace-secrets.json";
  String VALIDATE_FIELDS_REQUIRED_FAIL = PATH + "validate-fields-fail.json";
  String EXECUTE_CREATE_ISSUE = PATH + "valid-create-issue.json";
  String EXECUTE_CREATE_ISSUE_EPIC = PATH + "valid-create-issue-epic.json";
  String EXECUTE_UPDATE_ISSUE = PATH + "valid-update-issue.json";
  String EXECUTE_UPDATE_ISSUE_EPIC = PATH + "valid-update-issue-epic.json";
  String EXECUTE_GET_BOARD_DETAILS = PATH + "valid-get-board-details.json";
  String EXECUTE_GET_ISSUE = PATH + "valid-get-issue.json";
  String EXECUTE_GET_SPRINT_DETAILS = PATH + "valid-get-sprint-details.json";
  String INVALID_CREATE_ISSUE = PATH + "invalid-create-issue.json";
  String INVALID_CREATE_ISSUE_EPIC = PATH + "invalid-create-issue-epic.json";
  String INVALID_UPDATE_ISSUE = PATH + "invalid-update-issue.json";
  String INVALID_UPDATE_ISSUE_EPIC = PATH + "invalid-update-issue-epic.json";
  String INVALID_GET_BOARD_DETAILS = PATH + "invalid-get-board-details.json";
  String INVALID_GET_ISSUE = PATH + "invalid-get-issue.json";
  String INVALID_GET_SPRINT_DETAILS = PATH + "invalid-get-sprint-details.json";
}
