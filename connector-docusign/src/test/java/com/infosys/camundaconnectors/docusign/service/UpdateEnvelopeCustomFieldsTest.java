/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateEnvelopeCustomFieldsTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private UpdateEnvelopeCustomFieldsService service;

  @BeforeEach
  public void init() throws IOException {
    service = new UpdateEnvelopeCustomFieldsService();
    service.setEnvelopeId("123");
    service.setListFields(
        List.of(Map.of("fieldId", "12"), Map.of("ListItems", List.of("item1", "item2"))));
  }

  @DisplayName("Update Envelope Custom field with listFields")
  @Test
  void validUpdateEnvelopeCustomFieldWithListFields() throws IOException {
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Update Envelope Custom field with textFields")
  @Test
  void validUpdateEnvelopeCustomFieldWithTextFields() throws IOException {
    service.setListFields(null);
    service.setTextFields(List.of(Map.of("fieldId", "12"), Map.of("value", "value")));
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Update Envelope Custom field with textFields and listFields")
  @Test
  void validUpdateEnvelopeCustomFieldWithTextFieldsAndListFields() throws IOException {

    service.setTextFields(List.of(Map.of("fieldId", "12"), Map.of("value", "value")));
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Update Envelope Custom field with EnvelopeId as Null")
  @Test
  void validUpdateEnvelopeCustomFieldWithEnvelopeIdAsNulll() throws IOException {
    service.setEnvelopeId(null);
    service.setTextFields(List.of(Map.of("fieldId", "12"), Map.of("value", "value")));
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
