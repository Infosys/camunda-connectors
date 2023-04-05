/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.infosys.camundaconnectors.email.pop3.BaseTest;
import com.infosys.camundaconnectors.email.pop3.model.response.POP3Response;
import com.infosys.camundaconnectors.email.pop3.model.response.Response;
import com.infosys.camundaconnectors.email.pop3.service.DeleteEmailService;
import io.camunda.connector.impl.ConnectorInputException;
import javax.mail.Folder;
import javax.mail.Store;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class POP3RequestTest extends BaseTest {
  @ParameterizedTest()
  @MethodSource("replaceSecretsSuccessTestCases")
  @SuppressWarnings("unchecked")
  void replaceSecrets_shouldReplaceSecrets(String input) {
    // Given
    POP3Request<DeleteEmailService> requestData = gson.fromJson(input, POP3Request.class);
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

    assertThat(requestData.getOperation()).isEqualTo(ActualValue.METHOD);
    assertThat(requestData.getData().getMessageId()).isEqualTo(ActualValue.MESSAGE_ID);
  }

  @ParameterizedTest()
  @MethodSource("validateRequiredFieldsFailTestCases")
  @SuppressWarnings("unchecked")
  void validate_shouldThrowExceptionWhenLeastRequestFieldOneNotExist(String input) {
    POP3Request<DeleteEmailService> requestData = gson.fromJson(input, POP3Request.class);
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
    POP3Request<DeleteEmailService> requestData = new POP3Request<>();
    Authentication auth = new Authentication();
    auth.setHostname(ActualValue.Authentication.HOST);
    auth.setPortNumber(ActualValue.Authentication.PORT);
    auth.setUsername(ActualValue.Authentication.USERNAME);
    auth.setPassword(ActualValue.Authentication.PASSWORD);
    auth.setDomainName(ActualValue.Authentication.DOMAIN_NAME);
    auth.setKeyStorePath(ActualValue.Authentication.KEY_STORE_PATH);
    auth.setKeyStorePassword(ActualValue.Authentication.KEY_STORE_PASSWORD);
    requestData.setAuthentication(auth);
    requestData.setOperation(input);
    requestData.setData(
        new DeleteEmailService() {
          @Override
          public Response invoke(Store store, Folder folder) {
            return new POP3Response<>("");
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
