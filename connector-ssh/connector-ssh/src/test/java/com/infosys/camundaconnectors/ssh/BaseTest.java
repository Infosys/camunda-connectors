//
//  Copyright (c) 2023 Infosys Ltd.
//  Use of this source code is governed by MIT license that can be found in the LICENSE file
//  or at https://opensource.org/licenses/MIT
//

package com.infosys.camundaconnectors.ssh;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;

import com.google.gson.Gson;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

@SuppressWarnings("unused")
public abstract class BaseTest {
  //  protected static final Gson gson = GsonSupplier.getGson();
  protected static OutboundConnectorContext context;

  protected interface ActualValue {

    String METHOD = "ssh.executeCommand";
    String[] commandsList = {"dir", "ipconfig", "hostname"};
    List<String> commands = new ArrayList<String>(Arrays.asList(commandsList));
    String commandLine = "multiple";
    String outputType = "statusCode";
    String TOKEN = "Password";

    interface Authentication {
      String HOST = "Hostname";
      String PORT = "22";
      String USERNAME = "Username";
      String PASSWORD = "secrets.TOKEN";
      String known_hostsPath = "pathOf/known_hosts";
      String operatingSystem = "windows";
    }
  }

  protected interface SecretsConstant {
    String TOKEN = "TOKEN";
  }

  protected static OutboundConnectorContextBuilder getContextBuilderWithSecrets() {
    return OutboundConnectorContextBuilder.create()
        .secret(SecretsConstant.TOKEN, ActualValue.TOKEN);
  }

  protected static Stream<String> replaceSecretsSuccessTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.REPLACE_SECRETS);
  }

  protected static Stream<String> validateRequiredFieldsFailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALIDATE_REQUIRED_FIELDS_FAIL);
  }

  protected static Stream<String> executeExecuteCommandsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_COMMANDS);
  }

  protected static Stream<String> invalidExecuteCommandsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_COMMANDS);
  }

  @SuppressWarnings("unchecked")
  protected static Stream<String> loadTestCasesFromResourceFile(final String fileWithTestCasesUri)
      throws IOException {
    final String cases = readString(new File(fileWithTestCasesUri).toPath(), UTF_8);
    final Gson testingGson = new Gson();
    var array = testingGson.fromJson(cases, ArrayList.class);
    return array.stream().map(testingGson::toJson).map(Arguments::of);
  }
}
