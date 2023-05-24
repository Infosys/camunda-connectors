/// *
// * Copyright (c) 2023 Infosys Ltd.
// * Use of this source code is governed by MIT license that can be found in the LICENSE file
// * or at https://opensource.org/licenses/MIT
// */

package com.infosys.camundaconnectors.file.sftp.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.infosys.camundaconnectors.file.sftp.BaseTest;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import com.infosys.camundaconnectors.file.sftp.service.files.DeleteFileService;
import io.camunda.connector.impl.ConnectorInputException;
import net.schmizz.sshj.sftp.SFTPClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class SFTPRequestTest extends BaseTest {

  @ParameterizedTest()
  @MethodSource("replaceSecretsSuccessTestCases")
  @SuppressWarnings("unchecked")
  void replaceSecrets_shouldReplaceSecrets(String input) {
    // Given
    SFTPRequest<DeleteFileService> requestData = gson.fromJson(input, SFTPRequest.class);
    context = getContextBuilderWithSecrets().build();
    // When
    context.replaceSecrets(requestData);
    // Then
    assertThat(requestData.getAuthentication().getHostname())
        .isEqualTo(ActualValue.Authentication.HOST);
    assertThat(requestData.getAuthentication().getPortNumber())
        .isEqualTo(ActualValue.Authentication.PORT);
    assertThat(requestData.getAuthentication().getUsername())
        .isEqualTo(ActualValue.Authentication.USERNAME);
    assertThat(requestData.getAuthentication().getPassword()).isEqualTo(ActualValue.TOKEN);
    assertThat(requestData.getAuthentication().getKnownHostsPath())
        .isEqualTo(ActualValue.Authentication.known_hostsPath);

    assertThat(requestData.getOperation()).isEqualTo(ActualValue.METHOD);
    assertThat(requestData.getData().getFileName()).isEqualTo(ActualValue.fileName);
    assertThat(requestData.getData().getFolderPath()).isEqualTo(ActualValue.folderPath);
  }

  @ParameterizedTest()
  @MethodSource("validateRequiredFieldsFailTestCases")
  @SuppressWarnings("unchecked")
  void validate_shouldThrowExceptionWhenLeastRequestFieldOneNotExist(String input) {
    SFTPRequest<DeleteFileService> requestData = gson.fromJson(input, SFTPRequest.class);
    context = getContextBuilderWithSecrets().build();
    // When context validate request
    // Then expect ConnectorInputException
    ConnectorInputException thrown =
        assertThrows(
            ConnectorInputException.class,
            () -> context.validate(requestData),
            "ConnectorInputException was expected");
    assertThat(thrown.getMessage()).contains("Found constraints violated while validating input");
  }

  @ParameterizedTest()
  @ValueSource(strings = {"", "  ", "     "})
  void validate_shouldThrowExceptionWhenMethodIsBlank(String input) {
    // Given
    SFTPRequest<DeleteFileService> requestData = new SFTPRequest<>();
    Authentication auth = new Authentication();
    auth.setHostname(ActualValue.Authentication.HOST);
    auth.setPortNumber(ActualValue.Authentication.PORT);
    auth.setUsername(ActualValue.Authentication.USERNAME);
    auth.setPassword(ActualValue.Authentication.PASSWORD);
    auth.setKnownHostsPath(ActualValue.Authentication.known_hostsPath);
    requestData.setAuthentication(auth);
    requestData.setOperation(input);
    requestData.setData(
        new DeleteFileService() {
          @Override
          public Response invoke(SFTPClient sftpClient) {
            return new SFTPResponse<>("");
          }
        });

    context = getContextBuilderWithSecrets().build();
    // When context validate request
    // Then expect ConnectorInputException
    assertThrows(
        ConnectorInputException.class,
        () -> context.validate(requestData),
        "ConnectorInputException was expected");
  }
}
