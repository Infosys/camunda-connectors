/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.smtp;

public interface TestCasesPath {
  String PATH = "src/test/resources/";

  String REPLACE_SECRETS = PATH + "replace-secrets.json";
  String VALIDATE_REQUIRED_FIELDS_FAIL = PATH + "validate-fields-fail.json";
  String VALID_SEND_MAIL = PATH + "valid-send-mail.json";
  String INVALID_SEND_MAIL = PATH + "invalid-send-mail.json";
  String VALID_AUTHENTICATION = PATH + "valid-authentication.json";
  String INVALID_AUTHENTICATION = PATH + "invalid-authentication.json";
  String VALID_SMTP_FUNCTION = PATH + "valid-send-mail.json";
  String INVALID_SMTP_FUNCTION = PATH + "validate-fields-fail.json";
}
