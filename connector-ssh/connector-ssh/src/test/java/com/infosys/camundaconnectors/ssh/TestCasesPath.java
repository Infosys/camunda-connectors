/// *
// * Copyright (c) 2023 Infosys Ltd.
// * Use of this source code is governed by MIT license that can be found in the LICENSE file
// * or at https://opensource.org/licenses/MIT
// */

package com.infosys.camundaconnectors.ssh;

public interface TestCasesPath {
  String PATH = "src/test/resources/test-cases/";
  String REPLACE_SECRETS = PATH + "replace-secrets.json";
  String VALIDATE_REQUIRED_FIELDS_FAIL = PATH + "validate-fields-fail.json";
  String EXECUTE_COMMANDS = PATH + "valid-execute-commands.json";
  String INVALID_EXECUTE_COMMANDS = PATH + "invalid-execute-commands.json";
}
