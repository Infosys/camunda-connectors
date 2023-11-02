/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import java.io.File;
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
public class UpdateDocumentsToEnvelopeTest {
  @Mock Authentication authentication;
  @Mock HttpServiceUtils httpService;
  private UpdateDocumentsToEnvelopeService service;

  @BeforeEach
  public void init() throws IOException {
    service = new UpdateDocumentsToEnvelopeService();
    File document = new File("docusign_documents.pdf");
    service.setDocuments(
        List.of(
            Map.of("documentPath", document.getAbsoluteFile().toString()),
            Map.of("documentId", "1")));
    service.setEnvelopeId("123");
  }

  @DisplayName("Update document to envelope with envelope Id as Null")
  @Test
  void InvalidUpdateDocumentToEnvelopeWithEnvelopeIdAsNulll() throws IOException {
    service.setEnvelopeId(null);
    Mockito.when(
            httpService.putRequest(any(String.class), any(String.class), any(Authentication.class)))
        .thenReturn(Map.of("sendDraftEnvelope", "sendDraftEnvelope"));
    Mockito.when(authentication.getBaseUri()).thenReturn("uri");
    assertThatThrownBy(() -> service.invoke(authentication, httpService))
        .isInstanceOf(RuntimeException.class)
        .message()
        .isNotEmpty();
  }

  @DisplayName("Update document to envelope with Documents as Null")
  @Test
  void InvalidUpdateDocumentToEnvelopeWithDocumentsAsNull() throws IOException {
    service.setEnvelopeId(null);
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
