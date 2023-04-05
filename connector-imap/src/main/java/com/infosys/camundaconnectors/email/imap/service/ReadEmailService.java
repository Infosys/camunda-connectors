/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.service;

import com.infosys.camundaconnectors.email.imap.model.request.IMAPRequestData;
import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.MessageIDTerm;
import javax.validation.constraints.NotBlank;
import org.apache.commons.mail.util.MimeMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadEmailService implements IMAPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReadEmailService.class);
  Function<MimeMessage, MimeMessageParser> mimeMessageParserFunction;

  public ReadEmailService() {
    mimeMessageParserFunction = mimeMessage -> new MimeMessageParser(mimeMessage);
  }

  public ReadEmailService(Function<MimeMessage, MimeMessageParser> mimeMessageParserFunction) {
    this.mimeMessageParserFunction = mimeMessageParserFunction;
  }

  // Inputs
  @NotBlank private String messageId;
  private String postReadAction;
  private String outputType;
  private Boolean readLatestResponse;

  // Output
  Map<String, Object> header = new TreeMap<>();
  Map<String, Object> detailedHeader = new TreeMap<>();
  IMAPResponse<Map<String, Object>> readEmailResponse;

  @Override
  public Response invoke(Store store, Folder folder) {
    LOGGER.debug("Fetching messages from folder");
    try {
      Message[] msgArr = folder.search(new MessageIDTerm(stripMessageId(messageId)));
      if (msgArr == null || msgArr.length == 0)
        throw new RuntimeException("No matching message found for given message ID in the folder");
      else {
        Message mailToRead = msgArr[0];
        if (outputType == null || outputType.equalsIgnoreCase("detailedHeader")) {
          extractMessageHeader(mailToRead, detailedHeader);
          if (readLatestResponse == null) readLatestResponse = false;
          extractMessageContent(mailToRead, detailedHeader, readLatestResponse);
        } else if (outputType.equalsIgnoreCase("header")) {
          extractMessageHeader(mailToRead, header);
        } else throw new RuntimeException("outputType should be header or detailedHeader");
        mailToRead.setFlag(
            Flags.Flag.SEEN,
            (postReadAction == null || !postReadAction.equalsIgnoreCase("unread")));
      }
      LOGGER.debug("ReadEmailUsingIMAP Process Completed");
    } catch (MessagingException e) {
      LOGGER.error(e.getLocalizedMessage());
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
    if (outputType == null || outputType.equalsIgnoreCase("detailedHeader")) {
      readEmailResponse = new IMAPResponse<>(detailedHeader);
    } else if (outputType.equalsIgnoreCase("header")) {
      readEmailResponse = new IMAPResponse<>(header);
    } else throw new RuntimeException("outputType should be detailedHeader or header");
    LOGGER.info("Read Email Status: {}", readEmailResponse.getResponse());
    return readEmailResponse;
  }

  /** Parse message and extract Email Header Details */
  public void extractMessageHeader(Message message, Map<String, Object> headers) {
    try {
      MimeMessage mimeMessage = (MimeMessage) message;
      MimeMessageParser mmp = mimeMessageParserFunction.apply(mimeMessage).parse();
      headers.put("SenderEmailAddress", mmp.getFrom());
      headers.put("ToList", getInternetAddress(mmp.getTo()));
      headers.put("CcList", getInternetAddress(mmp.getCc()));
      headers.put("BccList", getInternetAddress(mmp.getBcc()));
      headers.put("Subject", mmp.getSubject());
      headers.put("Size", mimeMessage.getSize());
      headers.put("ReceivedDate", mimeMessage.getReceivedDate());
    } catch (Exception ex) {
      throw new RuntimeException("Exception in parsing email: " + ex.getLocalizedMessage());
    }
  }

  /** Parse message and extract Email Header Details */
  public void extractMessageContent(
      Message message, Map<String, Object> headers, Boolean readLatestResponse) {
    try {
      MimeMessageParser mmp = mimeMessageParserFunction.apply((MimeMessage) message).parse();
      String plainBody = mmp.getPlainContent();
      if (plainBody == null) plainBody = "";
      String htmlBody = mmp.getHtmlContent();
      if (htmlBody == null) htmlBody = "";
      if (readLatestResponse) {
        headers.put("PlainContent", plainBody.split("\r\n\r\nFrom:")[0]);
        headers.put("HtmlContent", htmlBody.split("From:</b>")[0]);
      } else {
        headers.put("PlainContent", plainBody);
        headers.put("HtmlContent", htmlBody);
      }
      headers.put("AttachmentsCount", mmp.getAttachmentList().size());
      Map<String, Object> attachmentMap = new HashMap<>();
      for (DataSource attachment : mmp.getAttachmentList()) {
        attachmentMap.put(attachment.getName(), attachment.getInputStream());
      }
      headers.put("AttachmentDetails", attachmentMap);
    } catch (Exception ex) {
      throw new RuntimeException("Exception in parsing email: " + ex.getLocalizedMessage());
    }
  }

  public String stripMessageId(String messageId) {
    if (messageId == null) return null;
    return messageId.trim().replaceAll("[<>]", "");
  }

  /** Extract Internet address / email ID */
  public List<String> getInternetAddress(List<Address> addressList) {
    return addressList.stream()
        .map((Address x) -> ((InternetAddress) x).getAddress())
        .collect(Collectors.toList());
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public void setPostReadAction(String postReadAction) {
    this.postReadAction = postReadAction;
  }

  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  public void setReadLatestResponse(Boolean readLatestResponse) {
    this.readLatestResponse = readLatestResponse;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ReadEmailService that = (ReadEmailService) o;
    return Objects.equals(messageId, that.messageId)
        && Objects.equals(postReadAction, that.postReadAction)
        && Objects.equals(outputType, that.outputType)
        && Objects.equals(readLatestResponse, that.readLatestResponse);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageId, postReadAction, outputType, readLatestResponse);
  }

  @Override
  public String toString() {
    return "ReadEmailService{"
        + "messageId='"
        + messageId
        + '\''
        + ", postReadAction='"
        + postReadAction
        + '\''
        + ", outputType='"
        + outputType
        + '\''
        + ", readLatestResponse="
        + readLatestResponse
        + '}';
  }
}
