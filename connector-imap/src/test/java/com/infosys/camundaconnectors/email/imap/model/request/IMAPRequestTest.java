/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.infosys.camundaconnectors.email.imap.BaseTest;
import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import com.infosys.camundaconnectors.email.imap.service.DeleteEmailService;
import io.camunda.connector.impl.ConnectorInputException;
import javax.mail.Folder;
import javax.mail.Store;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class IMAPRequestTest extends BaseTest {

  @ParameterizedTest()
  @MethodSource("replaceSecretsSuccessTestCases")
  @SuppressWarnings("unchecked")
  void replaceSecrets_shouldReplaceSecrets(String input) {
    // Given
    IMAPRequest<DeleteEmailService> requestData = gson.fromJson(input, IMAPRequest.class);
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
    assertThat(requestData.getAuthentication().getDomainName())
        .isEqualTo(ActualValue.Authentication.DOMAIN_NAME);
    assertThat(requestData.getAuthentication().getKeyStorePath())
        .isEqualTo(ActualValue.Authentication.KEY_STORE_PATH);
    assertThat(requestData.getAuthentication().getKeyStorePassword())
        .isEqualTo(ActualValue.Authentication.KEY_STORE_PASSWORD);
    assertThat(requestData.getAuthentication().getFolderPath())
        .isEqualTo(ActualValue.Authentication.FOLDER_PATH);

    assertThat(requestData.getOperation()).isEqualTo(ActualValue.METHOD);
    assertThat(requestData.getData().getMessageId()).isEqualTo(ActualValue.MESSAGE_ID);
  }

  @ParameterizedTest()
  @MethodSource("validateRequiredFieldsFailTestCases")
  @SuppressWarnings("unchecked")
  void validate_shouldThrowExceptionWhenLeastRequestFieldOneNotExist(String input) {
    IMAPRequest<DeleteEmailService> requestData = gson.fromJson(input, IMAPRequest.class);
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
    IMAPRequest<DeleteEmailService> requestData = new IMAPRequest<>();
    Authentication auth = new Authentication();
    auth.setHostname(ActualValue.Authentication.HOST);
    auth.setPortNumber(ActualValue.Authentication.PORT);
    auth.setUsername(ActualValue.Authentication.USERNAME);
    auth.setPassword(ActualValue.Authentication.PASSWORD);
    auth.setDomainName(ActualValue.DOMAIN_NAME);
    auth.setKeyStorePath(ActualValue.KEY_STORE_PATH);
    auth.setKeyStorePassword(ActualValue.KEY_STORE_PASSWORD);
    auth.setFolderPath(ActualValue.FOLDER_PATH);
    requestData.setAuthentication(auth);
    requestData.setOperation(input);
    requestData.setData(
        new DeleteEmailService() {
          @Override
          public Response invoke(Store store, Folder folder) {
            return new IMAPResponse<>("");
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
