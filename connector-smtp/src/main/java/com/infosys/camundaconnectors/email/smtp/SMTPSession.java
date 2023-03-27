/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.smtp;

import com.infosys.camundaconnectors.email.smtp.model.request.Authentication;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import java.util.Properties;

public class SMTPSession {
  public Session getSession(Authentication authentication) {
    Properties properties = System.getProperties();
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");
    properties.put("mail.smtp.ssl.trust", authentication.getHostname());
    properties.put("mail.smtp.host", authentication.getHostname());
    properties.put("mail.smtp.port", authentication.getPort());
    return Session.getInstance(
        properties,
        new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(
                authentication.getUsername(), authentication.getPassword());
          }
        });
  }
}
