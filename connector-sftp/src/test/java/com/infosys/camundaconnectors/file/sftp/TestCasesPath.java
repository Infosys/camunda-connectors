/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp;

public interface TestCasesPath {
  String PATH = "src/test/resources/test-cases/";

  String REPLACE_SECRETS = PATH + "replace-secrets.json";
  String VALIDATE_REQUIRED_FIELDS_FAIL = PATH + "validate-fields-fail.json";
  String EXECUTE_LIST_FILES = PATH + "valid-list-files.json";
  String EXECUTE_LIST_FOLDERS = PATH + "valid-list-folders.json";
  String INVALID_LIST_FILES = PATH + "invalid-list-files.json";
  String INVALID_LIST_FOLDERS = PATH + "invalid-list-folders.json";
  String EXECUTE_DELETE_FILE = PATH + "valid-delete-file.json";
  String INVALID_DELETE_FILE = PATH + "invalid-delete-file.json";
  String EXECUTE_DELETE_FOLDER = PATH + "valid-delete-folder.json";
  String INVALID_DELETE_FOLDER = PATH + "invalid-delete-folder.json";
  String EXECUTE_CREATE_FOLDER = PATH + "valid-create-folder.json";
  String INVALID_CREATE_FOLDER = PATH + "invalid-create-folder.json";
  String EXECUTE_MOVE_FILE = PATH + "valid-move-file.json";
  String INVALID_MOVE_FILE = PATH + "invalid-move-file.json";
  String EXECUTE_WRITE_FILE = PATH + "valid-write-file.json";
  String INVALID_WRITE_FILE = PATH + "invalid-write-file.json";
}
