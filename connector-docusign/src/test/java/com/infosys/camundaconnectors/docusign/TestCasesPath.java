/// *
// * Copyright (c) 2023 Infosys Ltd.
// * Use of this source code is governed by MIT license that can be found in the LICENSE file
// * or at https://opensource.org/licenses/MIT
// */

package com.infosys.camundaconnectors.docusign;

public interface TestCasesPath {
  String PATH = "src/test/resources/test-cases/";
  String REPLACE_SECRETS = PATH + "replace-secrets.json";
  String VALIDATE_REQUIRED_FIELDS_FAIL = PATH + "validate-fields-fail.json";
  String VALID_GET_ENVELOPE = PATH + "valid-get-envelope.json";
  String INVALID_GET_ENVELOPE = PATH + "invalid-get-envelope.json";
  String VALID_ADD_ENVELOPE_CUSTOM_FIELDS = PATH + "valid-add-envelope-custom-fields.json";
  String INVALID_ADD_ENVELOPE_CUSTOM_FIELDS = PATH + "invalid-add-envelope-custom-fields.json";
  String VALID_ADD_RECIPIENT_TO_ENVELOPE = PATH + "valid-add-recipients-to-envelope.json";
  String INVALID_ADD_RECIPIENT_TO_ENVELOPE = PATH + "invalid-add-recipients-to-envelope.json";
  String VALID_ADD_USERS_TO_GROUP = PATH + "valid-add-users-to-group.json";
  String INVALID_ADD_USERS_TO_GROUP = PATH + "invalid-add-users-to-group.json";
  String VALID_CREATE_ENVELOPE = PATH + "valid-create-envelope.json";
  String INVALID_CREATE_ENVELOPE = PATH + "invalid-create-envelope.json";
  String VALID_CREATE_USERS = PATH + "valid-create-users.json";
  String INVALID_CREATE_USERS = PATH + "invalid-create-users.json";
  String VALID_CUSTOM_API_REQUEST = PATH + "valid-custom-api-request.json";
  String INVALID_CUSTOM_API_REQUEST = PATH + "invalid-custom-api-request.json";
  String VALID_DELETE_USERS_FROM_ACCOUNT = PATH + "valid-delete-users-from-account.json";
  String INVALID_DELETE_USERS_FROM_ACCOUNT = PATH + "invalid-delete-users-from-account.json";
  String VALID_DELETE_USERS_FROM_GROUPS = PATH + "valid-delete-users-from-groups.json";
  String INVALID_DELETE_USERS_FROM_GROUPS = PATH + "invalid-delete-users-from-groups.json";
}
