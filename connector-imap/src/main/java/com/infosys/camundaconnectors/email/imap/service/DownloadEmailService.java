/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.service;

import com.infosys.camundaconnectors.email.imap.model.request.IMAPRequestData;
import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import jakarta.validation.constraints.NotBlank;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import javax.mail.*;
import javax.mail.search.MessageIDTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadEmailService implements IMAPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DownloadEmailService.class);
  @NotBlank private String messageId;
  @NotBlank private String downloadFolderPath; // Folder path where eml file will be downloaded
  // Output
  IMAPResponse<?> downloadEmailResponse;

  @Override
  public Response invoke(Store store, Folder folder) {
    LOGGER.debug("DownloadEmailUsingIMAP process started");
    downloadEmailResponse =
        new IMAPResponse<>("Failed to download email for messageId: " + messageId);
    try {
      File downloadPath = new File(downloadFolderPath);
      if (!downloadPath.exists()) downloadPath.mkdirs();
      if (!downloadPath.isDirectory())
        throw new RuntimeException("Is not a folder path: " + downloadFolderPath);
      LOGGER.debug("Fetching messages from  folder.");
      if (messageId == null || messageId.isBlank())
        throw new RuntimeException("Please provide a valid message ID.");
      Message[] msgArr = folder.search(new MessageIDTerm(stripMessageId(messageId)));
      if (msgArr != null && msgArr.length > 0) {
        for (Message msg : msgArr) {
          String emlFilePath =
              (downloadPath + File.separator + removeSpecialCharacters(msg.getSubject()) + ".eml");
          writeEMLFile(emlFilePath, msg);
          downloadEmailResponse =
              new IMAPResponse<>("Email downloaded successfully at " + emlFilePath);
        }
      } else {
        LOGGER.error("No email found in the mailbox matching given message Id.");
        throw new RuntimeException("No email found in the mailbox matching given message Id.");
      }
      LOGGER.debug("DownloadEmailUsingIMAP Process Completed");
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
