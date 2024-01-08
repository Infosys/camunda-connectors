/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.service;

import com.infosys.camundaconnectors.email.imap.model.request.IMAPRequestData;
import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import com.infosys.camundaconnectors.email.imap.utility.search.*;
import jakarta.validation.constraints.NotBlank;
import java.util.*;
import javax.mail.*;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchEmailsService implements IMAPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchEmailsService.class);
  private final List<String> validSearchFields =
      List.of("subject", "sender", "from", "body", "content", "receiveddate", "attachment", "all");
  // Inputs
  private String searchType;
  @NotBlank private String searchField;
  @NotBlank private String searchContent;
  private String outputType;
  // Output
  List<String> messageIdList = new ArrayList<>();
  List<Map<String, Object>> headerDetails = new ArrayList<>();
  IMAPResponse<?> searchEmailsResponse;

  @Override
  public Response invoke(Store store, Folder folder) {
    LOGGER.debug("SearchEmailsUsingIMAP process started");
    try {
      if (searchType == null
          || searchType.isBlank()
          || !(searchType.equalsIgnoreCase("complete") || searchType.equalsIgnoreCase("exact"))) {
        searchType = "partial";
      }
      if (outputType == null || outputType.isBlank()) outputType = "messageIds";
      searchType = searchType.toLowerCase();
      if (!validSearchFields.contains(searchField.toLowerCase())) {
        throw new RuntimeException(
            "searchField should be one of the following: "
                + "subject, sender, from, body, content, receivedDate, attachment, or "
                + "all (to search in subject/from/body)");
      }
      searchField = searchField.toLowerCase();
      LOGGER.debug("Constructing search field");
      SearchTerm searchCondition;
      if (searchField.contains("subject")) {
        searchCondition = new SubjectFieldSearchTerm(searchContent, searchType);
      } else if (searchField.contains("sender") || searchField.contains("from")) {
        searchCondition = new FromFieldSearchTerm(searchContent, searchType);
      } else if (searchField.contains("body") || searchField.contains("content")) {
        searchCondition = new BodySearchTerm(searchContent, searchType);
      } else if (searchField.contains("received")
          && (searchField.contains("date") || searchField.contains("time"))) {
        searchCondition = new ReceivedDateSearchTerm(searchContent);
      } else if (searchField.contains("attachment")) {
        searchCondition = new AttachmentFieldSearchTerm();
      } else {
        SearchTerm[] commonTerms = {
          new SubjectFieldSearchTerm(searchContent, searchType),
          new FromFieldSearchTerm(searchContent, searchType),
          new BodySearchTerm(searchContent, searchType)
        };
        searchCondition = new OrTerm(commonTerms);
      }
      LOGGER.info("Fetching messages from folder.");
      Message[] msgArr = folder.search(searchCondition);
      if (msgArr == null || msgArr.length == 0) {
        LOGGER.info("Number of emails fulfilled search criteria: 0");
        messageIdList = new ArrayList<>();
        headerDetails = new ArrayList<>();
      } else {
        LOGGER.info("Number of emails fulfilled search criteria: {}", msgArr.length);
        if (outputType.equalsIgnoreCase("headerDetails")) {
          for (Message message : msgArr) {
            String mID = stripMessageId((message.getHeader("Message-ID")[0]));
            Map<String, Object> emailHeaderDetail = new TreeMap<>();
            emailHeaderDetail.put("MessageID", mID);
            emailHeaderDetail.put("ReceivedDate", message.getReceivedDate());
            Address[] fromAddress = message.getFrom();
            if (fromAddress != null && fromAddress.length > 0)
              emailHeaderDetail.put("From", fromAddress[0].toString());
            emailHeaderDetail.put("Subject", message.getSubject());
            headerDetails.add(emailHeaderDetail);
          }
        } else if (outputType.equalsIgnoreCase("messageIds")) {
          for (Message message : msgArr) {
            messageIdList.add(stripMessageId((message.getHeader("Message-ID")[0])));
          }
        } else throw new RuntimeException("outputType should be messageIds or headerDetails");
      }
      LOGGER.debug("SearchEmailsUsingIMAP Process Completed");
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
    if (outputType.equalsIgnoreCase("headerDetails")) {
      searchEmailsResponse = new IMAPResponse<>(headerDetails);
    } else if (outputType.equalsIgnoreCase("messageIds")) {
      searchEmailsResponse = new IMAPResponse<>(messageIdList);
    }
    if (searchEmailsResponse != null)
      LOGGER.info("Search Emails Status: {}", searchEmailsResponse.getResponse());
    return searchEmailsResponse;
  }

  public String stripMessageId(String messageId) {
    if (messageId == null) return null;
    return messageId.trim().replaceAll("[<>]", "");
  }

  public void setSearchContent(String searchContent) {
    this.searchContent = searchContent;
  }

  public void setSearchField(String searchField) {
    this.searchField = searchField;
  }

  public void setSearchType(String searchType) {
    this.searchType = searchType;
  }

  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SearchEmailsService that = (SearchEmailsService) o;
    return Objects.equals(searchContent, that.searchContent)
        && Objects.equals(searchField, that.searchField)
        && Objects.equals(searchType, that.searchType)
        && Objects.equals(outputType, that.outputType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(searchContent, searchField, searchType, outputType);
  }

  @Override
  public String toString() {
    return "SearchEmailsService{"
        + "searchContent='"
        + searchContent
        + '\''
        + ", searchField='"
        + searchField
        + '\''
        + ", searchType='"
        + searchType
        + '\''
        + ", outputType='"
        + outputType
        + '\''
        + '}';
  }
}
