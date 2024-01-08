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
import java.util.Objects;
import javax.mail.*;
import javax.mail.search.MessageIDTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveEmailService implements IMAPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(MoveEmailService.class);
  // Inputs
  @NotBlank private String messageId;
  @NotBlank private String targetFolderPath;
  private Boolean createTargetFolder;

  // Output
  IMAPResponse<?> moveEmailResponse;

  @Override
  public Response invoke(Store store, Folder folder) {
    if (createTargetFolder == null) createTargetFolder = false;
    LOGGER.debug("MoveEmailUsingIMAP process started");
    moveEmailResponse = new IMAPResponse<>("Failed to move email for messageId: " + messageId);
    try {
      LOGGER.info("Fetching messages from folder");
      Message[] msgArr = folder.search(new MessageIDTerm(stripMessageId(messageId)));
      if (msgArr == null || msgArr.length == 0)
        throw new RuntimeException("No matching message found for given message ID in the folder");
      else {
        Folder destinationFolder = setTargetFolder(store, targetFolderPath, createTargetFolder);
        if (destinationFolder == null)
          throw new RuntimeException("Unable to find targetFolderPath");
        folder.copyMessages(msgArr, destinationFolder);
        for (Message msg : msgArr) {
          msg.setFlag(Flags.Flag.DELETED, true);
          moveEmailResponse =
              new IMAPResponse<>("Email moved successfully to folder: " + targetFolderPath);
        }
      }
      LOGGER.debug("MoveEmailUsingIMAP Process Completed");
    } catch (MessagingException e) {
      LOGGER.error(e.getMessage());
      if (e.getClass().getName().contains("MailConnectException")) {
        throw new RuntimeException(
            "ConnectionError: Error Connecting to Mail Server. Please check hostName and portNumber. "
                + e.getMessage());
      }
      throw new RuntimeException("MessagingException: " + e.getMessage());
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
    if (moveEmailResponse != null)
      LOGGER.info("Move Email Status: {}", moveEmailResponse.getResponse());
    return moveEmailResponse;
  }

  public String stripMessageId(String messageId) {
    if (messageId == null) return null;
    return messageId.trim().replaceAll("[<>]", "");
  }

  /**
   * @return the destinationFolder from chained folder paths for moving email
   */
  public Folder setTargetFolder(Store store, String folderPath, Boolean createTargetFolder)
      throws MessagingException {
    if (folderPath != null) {
      String[] folderParts;
      if (folderPath.contains("\\.")) folderParts = folderPath.split("\\.");
      else folderParts = folderPath.split("/");
      Folder fod = store.getDefaultFolder();
      for (String part : folderParts) {
        fod = fod.getFolder(part);
        if (!fod.exists()) {
          if (createTargetFolder) {
            if (!fod.create(Folder.HOLDS_MESSAGES))
              throw new RuntimeException(
                  "FolderNotFound: The given folder path "
                      + folderPath
                      + " doesn't exists and creation failed");
            fod.setSubscribed(true);
            LOGGER.info("Target folder created successfully");
          } else {
            throw new RuntimeException(
                "FolderNotFound: The given folder path " + folderPath + " doesn't exists");
          }
        }
      }
      return fod;
    }
    return null;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public void setTargetFolderPath(String targetFolderPath) {
    this.targetFolderPath = targetFolderPath;
  }

  public void setCreateTargetFolder(Boolean createTargetFolder) {
    this.createTargetFolder = createTargetFolder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MoveEmailService that = (MoveEmailService) o;
    return Objects.equals(messageId, that.messageId)
        && Objects.equals(targetFolderPath, that.targetFolderPath)
        && Objects.equals(createTargetFolder, that.createTargetFolder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageId, targetFolderPath, createTargetFolder);
  }

  @Override
  public String toString() {
    return "MoveEmailService{"
        + ", messageId='"
        + messageId
        + '\''
        + ", targetFolderPath='"
        + targetFolderPath
        + '\''
        + ", createTargetFolder="
        + createTargetFolder
        + '}';
  }
}
