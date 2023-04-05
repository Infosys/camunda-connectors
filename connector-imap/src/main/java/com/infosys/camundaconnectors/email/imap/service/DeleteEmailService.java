/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.service;

import com.infosys.camundaconnectors.email.imap.model.request.IMAPRequestData;
import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import java.util.Objects;
import javax.mail.*;
import javax.mail.search.MessageIDTerm;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteEmailService implements IMAPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteEmailService.class);
  // Input
  @NotBlank private String messageId;
  // Output
  IMAPResponse<?> deleteEmailResponse;

  @Override
  public Response invoke(Store store, Folder folder) {
    LOGGER.debug("DeleteEmailUsingIMAP process started");
    try {
      LOGGER.info("Fetching messages from folder.");
      if (messageId == null || messageId.isBlank())
        throw new RuntimeException("Please provide a valid message ID");
      Message[] msgArr = folder.search(new MessageIDTerm(stripMessageId(messageId)));
      if (msgArr != null && msgArr.length > 0) {
        for (Message msg : msgArr) {
          msg.setFlag(Flags.Flag.DELETED, true);
          deleteEmailResponse = new IMAPResponse<>("Email deleted successfully");
        }
      } else {
        LOGGER.error("No email found in the mailbox matching given message Id");
        throw new RuntimeException("No email found in the mailbox matching given message Id");
      }
      LOGGER.debug("DeleteEmailUsingIMAP Process Completed");
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
    if (deleteEmailResponse != null)
      LOGGER.info("Delete Email Status: {}", deleteEmailResponse.getResponse());
    return deleteEmailResponse;
  }

  public String stripMessageId(String messageId) {
    if (messageId == null) return null;
    return messageId.trim().replaceAll("[<>]", "");
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DeleteEmailService that = (DeleteEmailService) o;
    return Objects.equals(messageId, that.messageId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageId);
  }

  @Override
  public String toString() {
    return "DeleteEmailService{" + "messageId='" + messageId + '\'' + '}';
  }
}
