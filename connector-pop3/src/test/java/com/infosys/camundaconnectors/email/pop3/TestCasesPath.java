/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3;

public interface TestCasesPath {
  String PATH = "src/test/resources/test-cases/";

  String REPLACE_SECRETS = PATH + "replace-secrets.json";
  String VALIDATE_REQUIRED_FIELDS_FAIL = PATH + "validate-fields-fail.json";
  String EXECUTE_DELETE_EMAIL = PATH + "valid-delete-email.json";
  String INVALID_DELETE_EMAIL = PATH + "invalid-delete-email.json";
  String EXECUTE_DOWNLOAD_EMAIL = PATH + "valid-download-email.json";
  String INVALID_DOWNLOAD_EMAIL = PATH + "invalid-download-email.json";
  String EXECUTE_LIST_EMAILS = PATH + "valid-list-emails.json";
  String INVALID_LIST_EMAILS = PATH + "invalid-list-emails.json";
  String EXECUTE_SEARCH_EMAILS = PATH + "valid-search-emails.json";
  String INVALID_SEARCH_EMAILS = PATH + "invalid-search-emails.json";
}
