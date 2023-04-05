/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.service;

import com.infosys.camundaconnectors.email.pop3.model.request.POP3RequestData;
import com.infosys.camundaconnectors.email.pop3.model.response.POP3Response;
import com.infosys.camundaconnectors.email.pop3.model.response.Response;
import com.infosys.camundaconnectors.email.pop3.utility.SearchCondition;
import com.infosys.camundaconnectors.email.pop3.utility.SortCondition;
import java.util.*;
import java.util.stream.Collectors;
import javax.mail.*;
import javax.mail.search.SearchTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListEmailsService implements POP3RequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ListEmailsService.class);
  private Map<String, Object> filters;
  private Map<String, String> sortBy;
  private Integer maxResults;
  private String outputType;
  // Output
  List<String> messageIdList = new ArrayList<>();
  List<Map<String, Object>> headerDetails = new ArrayList<>();
  POP3Response<?> listEmailsResponse;

  @Override
  public Response invoke(Store store, Folder folder) {
    LOGGER.debug("ListEmailsUsingPOP3 process started");
    if (maxResults == null || maxResults < 0) maxResults = 1000;
    if (outputType == null || outputType.isBlank()) outputType = "messageIds";
    /* Filter / Search Conditions */
    try {
      Message[] msgArr;
      if (filters != null && !filters.isEmpty()) {
        SearchTerm st = new SearchCondition().convertToSearchTerm(filters);
        LOGGER.info("Fetching filtered messages from INBOX folder.");
        msgArr = folder.search(st);
      } else {
        LOGGER.info("Fetching all messages from INBOX folder.");
        msgArr = folder.getMessages();
      }
      if (msgArr != null && msgArr.length > 0) {
        LOGGER.info("Number of emails fulfilled search criteria: {}", msgArr.length);
        /* Sort By */
        if (sortBy != null
            && !sortBy.isEmpty()
            && (sortBy.get("sortOn") != null)
            && (sortBy.get("order") != null)) {
          SortCondition sortCond = new SortCondition();
          sortCond.sortMessages(msgArr, sortBy);
        }
        if (outputType.equalsIgnoreCase("headerDetails")) {
          for (Message message : msgArr) {
            String mID = stripMessageId((message.getHeader("Message-ID")[0]));
            Map<String, Object> emailHeaderDetail = new TreeMap<>();
            emailHeaderDetail.put("MessageID", mID);
            Address[] fromAddress = message.getFrom();
            if (fromAddress != null && fromAddress.length > 0)
              emailHeaderDetail.put("From", fromAddress[0]);
            emailHeaderDetail.put("Subject", message.getSubject());
            emailHeaderDetail.put("Size", message.getSize());
            // ReceivedDate - as getReceivedDate() returns null in POP3
            String[] receivedParts = message.getHeader("Received");
            if (receivedParts != null && receivedParts.length > 0) {
              String[] receivedDataParts = receivedParts[0].split(";");
              emailHeaderDetail.put(
                  "ReceivedDate", receivedDataParts[receivedDataParts.length - 1].strip());
            } else emailHeaderDetail.put("ReceivedDate", null);
            headerDetails.add(emailHeaderDetail);
          }
        } else if (outputType.equalsIgnoreCase("messageIds")) {
          for (Message message : msgArr)
            messageIdList.add(stripMessageId((message.getHeader("Message-ID")[0])));
        } else throw new RuntimeException("outputType should be messageIds or headerDetails");
      } else LOGGER.info("Number of emails fulfilled search criteria: 0");
      LOGGER.debug("ListEmailsUsingPOP3 Process Completed");
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
    if (outputType.equalsIgnoreCase("headerDetails")) {
      listEmailsResponse =
          new POP3Response<>(headerDetails.stream().limit(maxResults).collect(Collectors.toList()));
    } else if (outputType.equalsIgnoreCase("messageIds")) {
      listEmailsResponse =
          new POP3Response<>(messageIdList.stream().limit(maxResults).collect(Collectors.toList()));
    }
    if (listEmailsResponse != null)
      LOGGER.info("List Emails Status: {}", listEmailsResponse.getResponse());
    return listEmailsResponse;
  }

  public String stripMessageId(String messageId) {
    if (messageId == null) return null;
    return messageId.trim().replaceAll("[<>]", "");
  }

  public Map<String, Object> getFilters() {
    return filters;
  }

  public void setFilters(Map<String, Object> filters) {
    this.filters = filters;
  }

  public Map<String, String> getSortBy() {
    return sortBy;
  }

  public void setSortBy(Map<String, String> sortBy) {
    this.sortBy = sortBy;
  }

  public Integer getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }

  public String getOutputType() {
    return outputType;
  }

  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ListEmailsService that = (ListEmailsService) o;
    return Objects.equals(filters, that.filters)
        && Objects.equals(sortBy, that.sortBy)
        && Objects.equals(maxResults, that.maxResults)
        && Objects.equals(outputType, that.outputType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filters, sortBy, maxResults, outputType);
  }

  @Override
  public String toString() {
    return "ListEmailsService{"
        + "filters="
        + filters
        + ", sortBy="
        + sortBy
        + ", maxResults="
        + maxResults
        + ", outputType='"
        + outputType
        + '\''
        + '}';
  }
}
