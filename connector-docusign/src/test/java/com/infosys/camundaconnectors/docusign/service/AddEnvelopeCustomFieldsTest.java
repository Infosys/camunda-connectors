/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.docusign.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
public class AddEnvelopeCustomFieldsTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private AddEnvelopeCustomFieldsService service;

  @BeforeEach
  public void init() throws IOException {
    service = new AddEnvelopeCustomFieldsService();
    service.setEnvelopeId("envelopeId");
  }

  @DisplayName("Add Envelope Custom field Service with Both List Field and Text Field")
  @Test
  void validAddEnvelopeCustomFieldServiceWithBothListFieldAndTextField() {
    service.setListFields(List.of(Map.of("fieldId", "1234")));
    service.setTextFields(List.of(Map.of("fieldId", "1234")));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(httpService.postRequest("url", "payload", authentication))
        .thenReturn(dummypayload);
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Add Envelope Custom field Service with List Field only")
  @Test
  void validAddEnvelopeCustomFieldServiceWithListFieldOnly() {
    service.setListFields(
        List.of(
            Map.of("fieldId", "1234"), Map.of("listitems", List.of("listitems1", "listItems2"))));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(httpService.postRequest("url", "payload", new Authentication()))
        .thenReturn(dummypayload);
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Add Envelope Custom field Service with Text Field only")
  @Test
  void validAddEnvelopeCustomFieldServiceWithTextFieldOnly() {
    service.setTextFields(List.of(Map.of("fieldId", "1234"), Map.of("value", "TextField")));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(httpService.postRequest("url", "payload", authentication))
        .thenReturn(dummypayload);
    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(Map.class).isNotNull();
  }

  @DisplayName("Add Envelope Custom field Service without EnvelopId")
  @Test
  void invalidAddEnvelopeCustomFieldServiceWithoutEnvelopId() {
    service.setEnvelopeId("");
    service.setTextFields(List.of(Map.of("fieldId", "1234"), Map.of("value", "TextField")));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(httpService.postRequest("url", "payload", new Authentication()))
        .thenReturn(dummypayload);
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Add Envelope Custom field Service without textFields and listfields")
  @Test
  void invalidAddEnvelopeCustomFieldServiceWithoutTextFieldsAndListFields() {

    service.setTextFields(null);
    service.setListFields(null);
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Map<String, Object> dummypayload = Map.of("Response", "ResponseString");
    Mockito.when(httpService.postRequest("url", "payload", new Authentication()))
        .thenReturn(dummypayload);
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
