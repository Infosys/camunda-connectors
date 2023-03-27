/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.smtp.model.request;

import io.camunda.connector.api.annotation.Secret;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class SMTPRequest {
  @Valid @NotNull @Secret private Authentication authentication;
  @NotBlank private String smtpEmailMailBoxName;
  @NotEmpty private List<String> smtpEmailToRecipients;
  private List<String> smtpEmailCcRecipients;
  private List<String> smtpEmailBccRecipients;
  @NotBlank private String smtpEmailSubject;

  @NotBlank
  @Pattern(
      regexp = "(?i)(text|html|txt|htm)",
      message = "'contentType' should be either 'html' or 'text'")
  private String smtpEmailContentType;

  @NotBlank private String smtpEmailContent;
  private List<String> smtpEmailAttachments;
  private Boolean smtpEmailImportance;
  private Boolean smtpEmailReadReceipt;
  private String smtpEmailFollowUp;
  private List<String> smtpEmailDirectReplyTo;
  private String smtpEmailSensitivity;

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }

  public String getSmtpEmailMailBoxName() {
    return smtpEmailMailBoxName;
  }

  public void setSmtpEmailMailBoxName(String smtpEmailMailBoxName) {
    this.smtpEmailMailBoxName = smtpEmailMailBoxName;
  }

  public List<String> getSmtpEmailToRecipients() {
    return smtpEmailToRecipients;
  }

  public void setSmtpEmailToRecipients(List<String> smtpEmailToRecipients) {
    this.smtpEmailToRecipients = smtpEmailToRecipients;
  }

  public List<String> getSmtpEmailCcRecipients() {
    return smtpEmailCcRecipients;
  }

  public void setSmtpEmailCcRecipients(List<String> smtpEmailCcRecipients) {
    this.smtpEmailCcRecipients = smtpEmailCcRecipients;
  }

  public List<String> getSmtpEmailBccRecipients() {
    return smtpEmailBccRecipients;
  }

  public void setSmtpEmailBccRecipients(List<String> smtpEmailBccRecipients) {
    this.smtpEmailBccRecipients = smtpEmailBccRecipients;
  }

  public String getSmtpEmailSubject() {
    return smtpEmailSubject;
  }

  public void setSmtpEmailSubject(String smtpEmailSubject) {
    this.smtpEmailSubject = smtpEmailSubject;
  }

  public String getSmtpEmailContentType() {
    return smtpEmailContentType;
  }

  public void setSmtpEmailContentType(String smtpEmailContentType) {
    this.smtpEmailContentType = smtpEmailContentType;
  }

  public String getSmtpEmailContent() {
    return smtpEmailContent;
  }

  public void setSmtpEmailContent(String smtpEmailContent) {
    this.smtpEmailContent = smtpEmailContent;
  }

  public List<String> getSmtpEmailAttachments() {
    return smtpEmailAttachments;
  }

  public void setSmtpEmailAttachments(List<String> smtpEmailAttachments) {
    this.smtpEmailAttachments = smtpEmailAttachments;
  }

  public Boolean getSmtpEmailImportance() {
    return smtpEmailImportance;
  }

  public void setSmtpEmailImportance(Boolean smtpEmailImportance) {
    this.smtpEmailImportance = smtpEmailImportance;
  }

  public Boolean getSmtpEmailReadReceipt() {
    return smtpEmailReadReceipt;
  }

  public void setSmtpEmailReadReceipt(Boolean smtpEmailReadReceipt) {
    this.smtpEmailReadReceipt = smtpEmailReadReceipt;
  }

  public String getSmtpEmailFollowUp() {
    return smtpEmailFollowUp;
  }

  public void setSmtpEmailFollowUp(String smtpEmailFollowUp) {
    this.smtpEmailFollowUp = smtpEmailFollowUp;
  }

  public List<String> getSmtpEmailDirectReplyTo() {
    return smtpEmailDirectReplyTo;
  }

  public void setSmtpEmailDirectReplyTo(List<String> smtpEmailDirectReplyTo) {
    this.smtpEmailDirectReplyTo = smtpEmailDirectReplyTo;
  }

  public String getSmtpEmailSensitivity() {
    return smtpEmailSensitivity;
  }

  public void setSmtpEmailSensitivity(String smtpEmailSensitivity) {
    this.smtpEmailSensitivity = smtpEmailSensitivity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SMTPRequest request = (SMTPRequest) o;
    return Objects.equals(authentication, request.authentication)
        && Objects.equals(smtpEmailMailBoxName, request.smtpEmailMailBoxName)
        && Objects.equals(smtpEmailToRecipients, request.smtpEmailToRecipients)
        && Objects.equals(smtpEmailCcRecipients, request.smtpEmailCcRecipients)
        && Objects.equals(smtpEmailBccRecipients, request.smtpEmailBccRecipients)
        && Objects.equals(smtpEmailSubject, request.smtpEmailSubject)
        && Objects.equals(smtpEmailContentType, request.smtpEmailContentType)
        && Objects.equals(smtpEmailContent, request.smtpEmailContent)
        && Objects.equals(smtpEmailAttachments, request.smtpEmailAttachments)
        && Objects.equals(smtpEmailImportance, request.smtpEmailImportance)
        && Objects.equals(smtpEmailReadReceipt, request.smtpEmailReadReceipt)
        && Objects.equals(smtpEmailFollowUp, request.smtpEmailFollowUp)
        && Objects.equals(smtpEmailDirectReplyTo, request.smtpEmailDirectReplyTo)
        && Objects.equals(smtpEmailSensitivity, request.smtpEmailSensitivity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        authentication,
        smtpEmailMailBoxName,
        smtpEmailToRecipients,
        smtpEmailCcRecipients,
        smtpEmailBccRecipients,
        smtpEmailSubject,
        smtpEmailContentType,
        smtpEmailContent,
        smtpEmailAttachments,
        smtpEmailImportance,
        smtpEmailReadReceipt,
        smtpEmailFollowUp,
        smtpEmailDirectReplyTo,
        smtpEmailSensitivity);
  }
}
