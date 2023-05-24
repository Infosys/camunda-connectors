/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.infosys.camundaconnectors.files.ftp.BaseTest;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.service.DeleteFileService;
import io.camunda.connector.impl.ConnectorInputException;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class FTPRequestTest extends BaseTest {

	  @ParameterizedTest()
	  @MethodSource("replaceSecretsSuccessTestCases")
	  @SuppressWarnings("unchecked")
	  void replaceSecrets_shouldReplaceSecrets(String input) {
	    // Given
	    FTPRequest<DeleteFileService> requestData = gson.fromJson(input, FTPRequest.class);
	    context = getContextBuilderWithSecrets().build();
	    context.replaceSecrets(requestData);
	    assertThat(requestData.getAuthentication().getHost())
	        .isEqualTo(ActualValue.Authentication.HOST);
	    assertThat(requestData.getAuthentication().getPort())
	        .isEqualTo(ActualValue.Authentication.PORT);
	    assertThat(requestData.getAuthentication().getUsername())
	        .isEqualTo(ActualValue.Authentication.USERNAME);
	    assertThat(requestData.getAuthentication().getPassword()).isEqualTo(ActualValue.TOKEN);

	    assertThat(requestData.getOperation()).isEqualTo(ActualValue.METHOD);
	    assertThat(requestData.getData().getFileName()).isEqualTo(ActualValue.fileName);
	    assertThat(requestData.getData().getFolderPath()).isEqualTo(ActualValue.folderPath);
	  }

	  @ParameterizedTest()
	  @MethodSource("validateRequiredFieldsFailTestCases")
	  @SuppressWarnings("unchecked")
	  void validate_shouldThrowExceptionWhenLeastRequestFieldOneNotExist(String input) {
	    FTPRequest<DeleteFileService> requestData = gson.fromJson(input, FTPRequest.class);
	    context = getContextBuilderWithSecrets().build();
	    ConnectorInputException thrown = assertThrows(ConnectorInputException.class,
	            () -> context.validate(requestData),
	            "ConnectorInputException was expected");
	    assertThat(thrown.getMessage()).contains("Found constraints violated while validating input");
	  }

	  @ParameterizedTest()
	  @ValueSource(strings = {"", "  ", "     "})
	  void validate_shouldThrowExceptionWhenMethodIsBlank(String input) {
	    // Given
	    FTPRequest<DeleteFileService> requestData = new FTPRequest<>();
	    Authentication auth = new Authentication();
	    auth.setHost(ActualValue.Authentication.HOST);
	    auth.setPort(ActualValue.Authentication.PORT);
	    auth.setUsername(ActualValue.Authentication.USERNAME);
	    auth.setPassword(ActualValue.Authentication.PASSWORD);
	    requestData.setAuthentication(auth);
	    requestData.setOperation(input);
	    requestData.setData(
	        new DeleteFileService() {
	          @Override
	          public FTPResponse<String> invoke(FTPClient ftpClient) {
	            return new FTPResponse<>("");
	          }
	        });

	    context = getContextBuilderWithSecrets().build();
	    assertThrows(
	        ConnectorInputException.class,
	        () -> context.validate(requestData),
	        "ConnectorInputException was expected");
	 }
}
