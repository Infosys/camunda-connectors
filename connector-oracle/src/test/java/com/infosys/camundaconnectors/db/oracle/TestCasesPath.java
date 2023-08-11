/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.oracle;

public interface TestCasesPath {
  String PATH = "src/test/resources/test-cases/";

  String REPLACE_SECRETS = PATH + "replace-secrets.json";
  String REPLACE_SECRETS_CONNECTION_TYPE_SERVICE_NAME = PATH + "replace-secrets-connection-type.json";
  String VALIDATE_REQUIRED_FIELDS_FAIL = PATH + "validate-fields-fail.json";
  String EXECUTE_CREATE_TABLE = PATH + "execute-create-table.json";
  String INVALID_CREATE_TABLE = PATH + "invalid-create-table.json";
  String EXECUTE_INSERT_DATA = PATH + "execute-insert-data.json";
  String INVALID_INSERT_DATA = PATH + "invalid-insert-data.json";
  String EXECUTE_DELETE_DATA = PATH + "execute-delete-data.json";
  String INVALID_DELETE_DATA = PATH + "invalid-delete-data.json";
  String EXECUTE_UPDATE_DATA = PATH + "execute-update-data.json";
  String INVALID_UPDATE_DATA = PATH + "invalid-update-data.json";
  String EXECUTE_READ_DATA = PATH + "execute-read-data.json";
  String INVALID_READ_DATA = PATH + "invalid-read-data.json";
  String EXECUTE_ALTER_TABLE = PATH + "execute-alter-table.json";
  String INVALID_ALTER_TABLE = PATH + "invalid-alter-table.json";
}
