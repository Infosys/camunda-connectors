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
public class SendDraftEnvelopeTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private SendDraftEnvelopesService service;

  @BeforeEach
  public void init() throws IOException {
    service = new SendDraftEnvelopesService();
    service.setEnvelopeIds(List.of("envelopeId1", "envelopeId2"));
  }

  @DisplayName("Send Draft Envelope")
  @Test
  void validSendDraftEnvelope() throws IOException {
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Mockito.when(httpService.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("envelopes", List.of(Map.of("envelopeId", "envelopeId"))));
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));

    Response result = service.invoke(authentication, httpService);
    assertThat(result).extracting("response").isInstanceOf(List.class).isNotNull();
  }

  @DisplayName("Send Draft Envelope with EnvelopeId as null")
  @Test
  void inValidSendDraftEnvelopeWithEnvelopeIdAsNull() throws IOException {
    service.setEnvelopeIds(null);
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    Mockito.when(httpService.getRequest(any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("envelopes", List.of(Map.of("envelopeId", "envelopeId"))));
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));

    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }
}
