/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.infosys.camundaconnectors.db.mssql.BaseTest;
import com.infosys.camundaconnectors.db.mssql.model.response.QueryResponse;
import com.infosys.camundaconnectors.db.mssql.service.CreateTableService;
import io.camunda.connector.impl.ConnectorInputException;

import java.sql.Connection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class MSSQLRequestTest extends BaseTest {

	@ParameterizedTest()
	@MethodSource("replaceSecretsSuccessTestCases")
	@SuppressWarnings("unchecked")
	public void replaceSecrets_shouldReplaceSecrets(String input) {
		// Given
		MSSQLRequest<CreateTableService> requestData = gson.fromJson(input, MSSQLRequest.class);
		context = getContextBuilderWithSecrets().build();
		// When
		context.replaceSecrets(requestData);
		// Then
		assertThat(requestData.getDatabaseConnection().getHost())
				.isEqualTo(ActualValue.DatabaseConnection.HOST);
		assertThat(requestData.getDatabaseConnection().getPort())
				.isEqualTo(ActualValue.DatabaseConnection.PORT);
		assertThat(requestData.getDatabaseConnection().getUsername())
				.isEqualTo(ActualValue.DatabaseConnection.USERNAME);
		assertThat(requestData.getDatabaseConnection().getPassword()).isEqualTo(ActualValue.TOKEN);
		assertThat(requestData.getOperation()).isEqualTo(ActualValue.METHOD);
		assertThat(requestData.getData().getDatabaseName()).isEqualTo(ActualValue.DATABASE_NAME);
		assertThat(requestData.getData().getTableName()).isEqualTo(ActualValue.TABLE_NAME);
		assertThat(requestData.getData().getColumnsList()).isNotEmpty();
	}

	@ParameterizedTest()
	@MethodSource("validateRequiredFieldsFailTestCases")
	@SuppressWarnings("unchecked")
	void validate_shouldThrowExceptionWhenLeastRequestFieldOneNotExist(String input) {
		MSSQLRequest<CreateTableService> requestData = gson.fromJson(input, MSSQLRequest.class);
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
		MSSQLRequest<CreateTableService> requestData = new MSSQLRequest<>();
		DatabaseConnection databaseConnection = new DatabaseConnection();
		databaseConnection.setHost(ActualValue.DatabaseConnection.HOST);
		databaseConnection.setPort(ActualValue.DatabaseConnection.PORT);
		databaseConnection.setUsername(ActualValue.DatabaseConnection.USERNAME);
		databaseConnection.setPassword(ActualValue.DatabaseConnection.PASSWORD);
		requestData.setDatabaseConnection(databaseConnection);
		requestData.setOperation(input);
		requestData.setData(
				new CreateTableService() {
					@Override
					public QueryResponse<String> invoke(final Connection connection) {
						return new QueryResponse<>("");
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
