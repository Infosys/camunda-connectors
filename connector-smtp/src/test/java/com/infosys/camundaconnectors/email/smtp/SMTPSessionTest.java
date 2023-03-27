/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.smtp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.email.smtp.model.request.Authentication;
import jakarta.mail.Session;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SMTPSessionTest {
  private final Authentication authentication = new Authentication();
  @Mock private Session sessionMock;

  @Mock private SMTPSession smtpSessionMock;

  @BeforeEach
  void init() {
    authentication.setHostname("HOSTNAME");
    authentication.setPort("25");
    authentication.setUsername("USERNAME");
    authentication.setPassword("PASSWORD");
  }

  @DisplayName("Should create session Object for SMTP")
  @Test
  void shouldCreateSessionObject() {
    when(smtpSessionMock.getSession(any(Authentication.class))).thenReturn(sessionMock);
    Properties properties = System.getProperties();
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");
    properties.put("mail.smtp.ssl.trust", authentication.getHostname());
    properties.put("mail.smtp.host", authentication.getHostname());
    properties.put("mail.smtp.port", authentication.getPort());
    when(sessionMock.getProperties()).thenReturn(properties);
    // when
    Session sessionObject = smtpSessionMock.getSession(authentication);
    // then
    assertThat(sessionObject.getProperties().getProperty("mail.smtp.host"))
        .isEqualTo(authentication.getHostname());
    Mockito.verify(sessionObject, Mockito.times(1)).getProperties();
  }

  @DisplayName("Should throw error")
  @Test
  void shouldThrowError() {
    when(smtpSessionMock.getSession(any(Authentication.class)))
        .thenThrow(new RuntimeException("Invalid"));
    // when
    assertThatThrownBy(() -> smtpSessionMock.getSession(authentication))
        // then
        .hasMessageContaining("Invalid");
    Mockito.verify(smtpSessionMock, Mockito.times(1)).getSession(any(Authentication.class));
  }
}
