/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.imap.utility;

import com.infosys.camundaconnectors.email.imap.model.request.Authentication;
import java.util.Properties;
import javax.mail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailServerClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(MailServerClient.class);

  public Store getStore(final Authentication authentication) {
    String hostname = authentication.getHostname();
    String portNumber = authentication.getPortNumber();
    LOGGER.debug("Email Server URL - {}:{}", hostname, portNumber);
    /* Setting Properties for IMAP */
    Properties props = System.getProperties();
    props.setProperty("mail.imap.host", hostname);
    props.setProperty("mail.imap.port", String.valueOf(portNumber));
    props.setProperty("mail.imap.ssl.enable", "true");

    String keyStorePath = authentication.getKeyStorePath();
    String keyStorePassword = authentication.getKeyStorePassword();

    if (keyStorePath != null && !keyStorePath.isBlank()) {
      System.getProperties().setProperty("javax.net.ssl.trustStore", keyStorePath);
      if (keyStorePassword == null || keyStorePassword.isBlank()) {
        LOGGER.error("Please provide a valid password for email server's keystore");
        throw new RuntimeException("Please provide a valid password for email server's keystore");
      }
      System.getProperties().setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);
    }

    /* Create the session and get the store for mail. */
    Session session = Session.getInstance(System.getProperties());
    Store store = null;
    try {
      store = session.getStore("imap");
    } catch (NoSuchProviderException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return store;
  }

  public Folder getFolder(Store store, Authentication authentication) {
    Folder folder;
    /* Server Details */
    String domainName = authentication.getDomainName();
    String username = authentication.getUsername();
    String folderPath = authentication.getFolderPath();
    if (!domainName.startsWith("@")) domainName = "@" + domainName;
    if (!username.endsWith(domainName)) username += domainName;
    try {
      LOGGER.info("Connecting to mail server...");
      store.connect(
          authentication.getHostname(),
          Integer.parseInt(authentication.getPortNumber()),
          username,
          authentication.getPassword());
      LOGGER.info("Connected to mail server");
      if (folderPath == null || folderPath.isBlank()) {
        LOGGER.error("Please provide a valid folderPath");
        throw new RuntimeException("Please provide a valid folderPath");
      }
      folder = getRecursiveFolder(store, folderPath);
      if (folder == null) {
        LOGGER.error("Unable to find folderPath");
        throw new RuntimeException("Unable to find folderPath");
      }
      folder.open(Folder.READ_WRITE);
    } catch (FolderNotFoundException ee) {
      LOGGER.error(ee.getLocalizedMessage());
      throw new RuntimeException("FolderNotFound: " + ee.getLocalizedMessage());
    } catch (AuthenticationFailedException e) {
      LOGGER.error(e.getLocalizedMessage());
      throw new RuntimeException(
          "AuthenticationFailed : Please check userName, password or mail domain. "
              + e.getLocalizedMessage());
    } catch (MessagingException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return folder;
  }

  public Folder getRecursiveFolder(Store store, String folderPath) throws MessagingException {
    Folder targetFolder;
    if (folderPath == null) return null;
    String[] folderParts;
    if (folderPath.contains("\\.")) folderParts = folderPath.split("\\.");
    else folderParts = folderPath.split("/");
    targetFolder = store.getDefaultFolder();
    for (String part : folderParts) {
      targetFolder = targetFolder.getFolder(part);
    }
    return targetFolder;
  }
}
