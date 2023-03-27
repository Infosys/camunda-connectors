/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.smtp;

import com.infosys.camundaconnectors.email.smtp.model.SMTPResponse;
import com.infosys.camundaconnectors.email.smtp.model.request.Authentication;
import com.infosys.camundaconnectors.email.smtp.model.request.SMTPRequest;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMailService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SendMailService.class);
  private static final String SENSITIVITY = "Sensitivity";

  public SMTPResponse execute(final Session session, final SMTPRequest request) {
    // Create email's Message object
    MimeMessage message = new MimeMessage(session);
    // Set From address
    try {
      message.setFrom(new InternetAddress(request.getSmtpEmailMailBoxName()));
    } catch (MessagingException e) {
      throw new RuntimeException("Invalid mailbox or from email address", e);
    }
    try {
      // Set Subject
      message.setSubject(request.getSmtpEmailSubject());
      // Add 'to'  recipients' addresses
      addToRecipients(request.getSmtpEmailToRecipients(), message);
      // Add 'cc' recipients' addresses
      addCcRecipients(request.getSmtpEmailCcRecipients(), message);
      // Add 'bcc' recipients' addresses
      addBccRecipients(request.getSmtpEmailBccRecipients(), message);
      // Add 'reply to' email addresses
      addDirectReplyTo(request.getSmtpEmailDirectReplyTo(), message);
      // Add follow up
      addFollowUp(request.getSmtpEmailFollowUp(), message);
      // Set mail 'importance'
      if (request.getSmtpEmailImportance() != null
          && request.getSmtpEmailImportance().equals(true)) {
        message.setHeader("X-Priority", "1");
      }
      // Add sensitivity
      addSensitivity(request.getSmtpEmailSensitivity(), message);
      // Add read receipt
      if (request.getSmtpEmailReadReceipt() != null
          && request.getSmtpEmailReadReceipt().equals(true)) {
        message.setHeader("Disposition-Notification-To", request.getSmtpEmailMailBoxName());
      }
      // Create mail 'body'
      Multipart multipart =
          setMailBody(request.getSmtpEmailContentType(), request.getSmtpEmailContent());
      // add attachments
      addAttachments(request.getSmtpEmailAttachments(), multipart);
      // set mail content
      message.setContent(multipart);
      message.saveChanges();
      LOGGER.debug("Message create successfully");
      LOGGER.debug("Getting transport ready for SMTP");
      // Prepare transport and send mail
      try (Transport transport = session.getTransport("smtp")) {
        Authentication auth = request.getAuthentication();
        transport.connect(auth.getHostname(), auth.getUsername(), auth.getPassword());
        transport.sendMessage(message, message.getAllRecipients());
      }
      LOGGER.info("Status: Mail sent successfully");
      return new SMTPResponse("Mail sent successfully");
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }

  private void addToRecipients(List<String> toRecipients, MimeMessage message) {
    if (toRecipients != null && !toRecipients.isEmpty()) {
      InternetAddress[] toRecipientAddress =
          toRecipients.stream()
              .filter(Objects::nonNull)
              .map(
                  recipient -> {
                    try {
                      return new InternetAddress(recipient);
                    } catch (AddressException e) {
                      throw new RuntimeException("Invalid toEmailAddress", e);
                    }
                  })
              .toArray(InternetAddress[]::new);
      try {
        message.setRecipients(Message.RecipientType.TO, toRecipientAddress);
      } catch (MessagingException e) {
        throw new RuntimeException("Invalid 'toRecipients' address", e);
      }
    }
  }

  private void addCcRecipients(List<String> ccRecipients, MimeMessage message) {
    if (ccRecipients != null && !ccRecipients.isEmpty()) {
      InternetAddress[] ccRecipientAddress =
          ccRecipients.stream()
              .filter(Objects::nonNull)
              .map(
                  recipient -> {
                    try {
                      return new InternetAddress(recipient);
                    } catch (AddressException e) {
                      throw new RuntimeException("Invalid CcEmailAddress", e);
                    }
                  })
              .toArray(InternetAddress[]::new);
      try {
        message.setRecipients(Message.RecipientType.CC, ccRecipientAddress);
      } catch (MessagingException e) {
        throw new RuntimeException("Invalid 'ccRecipients' address", e);
      }
    }
  }

  private void addBccRecipients(List<String> bccRecipients, MimeMessage message) {
    if (bccRecipients != null && !bccRecipients.isEmpty()) {
      InternetAddress[] bccRecipientAddress =
          bccRecipients.stream()
              .filter(Objects::nonNull)
              .map(
                  recipient -> {
                    try {
                      return new InternetAddress(recipient);
                    } catch (AddressException e) {
                      throw new RuntimeException("Invalid BccEmailAddress", e);
                    }
                  })
              .toArray(InternetAddress[]::new);
      try {
        message.setRecipients(Message.RecipientType.BCC, bccRecipientAddress);
      } catch (MessagingException e) {
        throw new RuntimeException("Invalid 'bccRecipients' address", e);
      }
    }
  }

  private void addDirectReplyTo(List<String> directReplyTo, MimeMessage message) {
    if (directReplyTo != null && !directReplyTo.isEmpty()) {
      InternetAddress[] replyToAddress =
          directReplyTo.stream()
              .filter(Objects::nonNull)
              .map(
                  recipient -> {
                    try {
                      return new InternetAddress(recipient);
                    } catch (AddressException e) {
                      throw new RuntimeException("Invalid directReplyTo email address", e);
                    }
                  })
              .toArray(InternetAddress[]::new);
      try {
        message.setReplyTo(replyToAddress);
      } catch (MessagingException e) {
        throw new RuntimeException("Invalid 'directReplyTo' address", e);
      }
    }
  }

  private void addFollowUp(String followUp, MimeMessage message) {
    if (followUp != null && !followUp.isBlank()) {
      final long Reply_By_MS;
      if (followUp.equalsIgnoreCase("today")) {
        Reply_By_MS = 0L;
      } else if (followUp.equalsIgnoreCase("tomorrow")) {
        Reply_By_MS = 24L * 60L * 60L * 1000L;
      } else {
        LOGGER.error("InvalidDate : please select valid follow up value");
        throw new RuntimeException("InvalidDate : please select valid follow up value");
      }
      try {
        message.setHeader("X-Message-Flag", "Follow up");
        MailDateFormat md = new MailDateFormat();
        final long now = System.currentTimeMillis();
        message.setHeader("Reply-By", md.format(new Date(now + Reply_By_MS)));
        message.setSentDate(new Date(now));
      } catch (MessagingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void addSensitivity(String sensitivity, MimeMessage message) {
    if (sensitivity != null && !sensitivity.isBlank()) {
      try {
        if (sensitivity.equalsIgnoreCase("Confidential")) {
          message.addHeader(SENSITIVITY, "Company-Confidential");
        } else if (sensitivity.equalsIgnoreCase("Private")) {
          message.addHeader(SENSITIVITY, "Private");
        } else if (sensitivity.equalsIgnoreCase("Personal")) {
          message.addHeader(SENSITIVITY, "Personal");
        } else {
          message.addHeader(SENSITIVITY, "Normal");
        }
      } catch (MessagingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Multipart setMailBody(String contentType, String content) throws MessagingException {
    Multipart multipart = new MimeMultipart();
    MimeBodyPart messageBodyPart = new MimeBodyPart();
    if (contentType.equalsIgnoreCase("html")) {
      File htmlFilePath = new File(content);
      if (htmlFilePath.isFile()) {
        StringBuilder sb = readHTMLFile(content);
        messageBodyPart.setContent(sb.toString(), "text/html; charset=utf-8");
      } else {
        LOGGER.error("Invalid html file path in content or not a valid file");
        throw new RuntimeException("Invalid html file path in content or not a valid file");
      }
    } else if (contentType.equalsIgnoreCase("text") || contentType.equalsIgnoreCase("txt")) {
      content = content.replace("\\n", "<br>");
      content = content.replace("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
      messageBodyPart.setContent(content, "text/html; charset=utf-8");

    } else {
      LOGGER.error("InvalidContentType : Content Type should be either text or html");
      throw new RuntimeException("InvalidContentType : Content Type should be either text or html");
    }
    multipart.addBodyPart(messageBodyPart);
    return multipart;
  }

  private StringBuilder readHTMLFile(String content) {
    StringBuilder sb = new StringBuilder(1024);
    String s;
    try (FileReader fr = new FileReader(content);
        BufferedReader br = new BufferedReader(fr)) {
      while ((s = br.readLine()) != null) {
        sb.append(s);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    LOGGER.debug(sb.toString());
    return sb;
  }

  private void addAttachments(List<String> attachments, Multipart multipart) {
    if (attachments != null && !attachments.isEmpty()) {
      for (String attachmentFilePaths : attachments) {
        try {
          MimeBodyPart attachmentBodyPart = new MimeBodyPart();
          attachmentBodyPart.attachFile(new File(attachmentFilePaths));
          multipart.addBodyPart(attachmentBodyPart);
        } catch (IOException | MessagingException e) {
          throw new RuntimeException(
              "InvalidAttachments : system could not find attachment or provided path is invalid",
              e);
        }
      }
    }
  }
}
