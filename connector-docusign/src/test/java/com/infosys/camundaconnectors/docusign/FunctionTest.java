//  Copyright (c) 2023 Infosys Ltd.
//  Use of this source code is governed by MIT license that can be found in the LICENSE file
//  or at https://opensource.org/licenses/MIT

package com.infosys.camundaconnectors.docusign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.response.DocuSignResponse;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FunctionTest extends BaseTest {

  @Mock CloseableHttpClient httpClient;

  @Mock HttpGet httpGet;
  @Captor private ArgumentCaptor<HttpClientResponseHandler<ClassicHttpResponse>> callbackCaptor;
  @Mock HttpServiceUtils service;
  @Mock ClassicHttpResponse httpResponse;
  @Mock CloseableHttpResponse closeableResponse;
  private DocuSignFunction docuSignFunction;

  @BeforeEach
  void init() throws Exception {

    docuSignFunction = new DocuSignFunction(service);
    Mockito.when(service.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("response", "response"));
    Mockito.when(
            service.postRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("response", "response"));
    Mockito.when(
            service.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("response", "response"));
    Mockito.when(
            service.deleteRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("response", "response"));
  }

  @ParameterizedTest
  @MethodSource("executeGetEnvelopeTestCases")
  void valid_getEnvelope(String input) throws Exception {

    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = docuSignFunction.execute(context);
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response).extracting("response").isInstanceOf(Map.class);
  }

  @ParameterizedTest
  @MethodSource("invalidexecuteGetEnvelopeTestCases")
  void invalid_getEnvelope(String input) throws Exception {

    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> docuSignFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeAddEnvelopeCustomFieldsTestCases")
  void valid_AddEnvelopeCustomFields(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = docuSignFunction.execute(context);
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("invalidAddEnvelopeCustomFieldsTestCases")
  void invalid_AddEnvelopeCustomFields(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> docuSignFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeAddRecipientToEnvelopeTestCases")
  void valid_AddRecipientToEnvelope(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = docuSignFunction.execute(context);
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("invalidAddRecipientToEnvelopeTestCases")
  void invalid_AddRecipientToEnvelope(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> docuSignFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeUsersToGroupTestCases")
  void valid_UsersToGroup(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = docuSignFunction.execute(context);
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("invalidUsersToGroupTestCases")
  void invalid_UsersToGroup(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> docuSignFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeCreateEnvelopeTestCases")
  void valid_CreateEnvelope(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = docuSignFunction.execute(context);
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("invalidCreateEnvelopeTestCases")
  void invalid_CreateEnvelope(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> docuSignFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeCreateUsersTestCases")
  void valid_CreateUsers(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = docuSignFunction.execute(context);
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("invalidCreateUsersTestCases")
  void invalid_CreateUsers(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> docuSignFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeCustomApiRequestTestCases")
  void valid_CustomApiRequest(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = docuSignFunction.execute(context);
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("invalidCustomApiRequestTestCases")
  void invalid_CustomApiRequest(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> docuSignFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeDeleteUsersTestCases")
  void valid_DeleteUsers(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = docuSignFunction.execute(context);
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("invalidDeleteUsersTestCases")
  void invalid_DeleteUsers(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> docuSignFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("executeDeleteUsersFromGroupsTestCases")
  void valid_DeleteUsersFromGroups(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    Object executeResponse = docuSignFunction.execute(context);
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("invalidDeleteUsersFromGroupsTestCases")
  void invalid_DeleteUsersFromGroups(String input) throws Exception {
    context = getContextBuilderWithSecrets().variables(input).build();
    assertThatThrownBy(() -> docuSignFunction.execute(context))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  private void assertThatItsValid(Object executeResponse, String validateAgainst) {
    assertThat(executeResponse).isInstanceOf(Response.class);
    @SuppressWarnings("unchecked")
    DocuSignResponse<String> response = (DocuSignResponse<String>) executeResponse;
    assertThat(response.getResponse()).isNotNull();
    assertThat(response)
        .extracting("response")
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .contains(validateAgainst);
  }
}
