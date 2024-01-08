/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.service;

import com.infosys.camundaconnectors.email.pop3.model.request.POP3RequestData;
import com.infosys.camundaconnectors.email.pop3.model.response.POP3Response;
import com.infosys.camundaconnectors.email.pop3.model.response.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.search.MessageIDTerm;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadEmailService implements POP3RequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DownloadEmailService.class);
  @NotBlank private String messageId;
  @NotBlank private String downloadFolderPath; // Folder path where eml file will be downloaded
  // Output
  POP3Response<?> downloadEmailResponse;

  @Override
  public Response invoke(Store store, Folder folder) {
    LOGGER.debug("DownloadEmailUsingPOP3 process started");
    downloadEmailResponse =
        new POP3Response<>("Failed to download email for messageId: " + messageId);
    try {
      File downloadPath = new File(downloadFolderPath);
      if (!downloadPath.exists()) downloadPath.mkdirs();
      if (!downloadPath.isDirectory())
        throw new RuntimeException("Is not a folder path: " + downloadFolderPath);
      LOGGER.debug("Fetching messages from INBOX folder.");
      if (messageId == null || messageId.isBlank())
        throw new RuntimeException("Please provide a valid message ID.");
      Message[] msgArr = folder.search(new MessageIDTerm(stripMessageId(messageId)));
      if (msgArr != null && msgArr.length > 0) {
        for (Message msg : msgArr) {
          String emlFilePath =
              (downloadPath + File.separator + removeSpecialCharacters(msg.getSubject()) + ".eml");
          writeEMLFile(emlFilePath, msg);
          downloadEmailResponse =
              new POP3Response<>("Email downloaded successfully at " + emlFilePath);
        }
      } else {
        LOGGER.error("No email found in the mailbox matching given message Id.");
        throw new RuntimeException("No email found in the mailbox matching given message Id.");
      }
      LOGGER.debug("DownloadEmailUsingPOP3 Process Completed");
    } catch (IOException e) {
      String errMsg = "DownloadFailed: Failed to download email, " + e.getLocalizedMessage();
      LOGGER.error(errMsg);
      throw new RuntimeException(errMsg);
    } catch (MessagingException e) {
      LOGGER.error(e.getLocalizedMessage());
      if (e.getClass().getName().contains("MailConnectException")) {
        throw new RuntimeException(
            "ConnectionError: Error Connecting to Mail Server. Please check hostName and portNumber. "
                + e.getLocalizedMessage());
      }
      throw new RuntimeException("MessagingException: " + e.getLocalizedMessage());
    } catch (RuntimeException e) {
      LOGGER.error(e.getLocalizedMessage());
      throw e;
    } finally {
      try {
        if (folder != null) folder.close();
        if (store != null) store.close();
      } catch (MessagingException | IllegalStateException ignored) {
      }
    }
    if (downloadEmailResponse != null)
      LOGGER.info("Download Email Status: {}", downloadEmailResponse.getResponse());
    return downloadEmailResponse;
  }

  /** Write email to a .eml file */
  private static void writeEMLFile(String emlFilePath, Message message)
      throws IOException, MessagingException {
    try (OutputStream out = new FileOutputStream(emlFilePath)) {
      message.writeTo(out);
    }
  }

  public String stripMessageId(String messageId) {
    if (messageId == null) return null;
    return messageId.trim().replaceAll("[<>]", "");
  }

  /** Remove special characters from email subject */
  public String removeSpecialCharacters(String subject) {
    if (subject == null) return null;
    return subject.trim().replaceAll("(\\W\\s)", " ");
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public void setDownloadFolderPath(String downloadFolderPath) {
    this.downloadFolderPath = downloadFolderPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DownloadEmailService that = (DownloadEmailService) o;
    return Objects.equals(messageId, that.messageId)
        && Objects.equals(downloadFolderPath, that.downloadFolderPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageId, downloadFolderPath);
  }

  @Override
  public String toString() {
    return "DownloadEmailService{"
        + "messageId='"
        + messageId
        + '\''
        + ", downloadFolderPath='"
        + downloadFolderPath
        + '\''
        + '}';
  }
}
