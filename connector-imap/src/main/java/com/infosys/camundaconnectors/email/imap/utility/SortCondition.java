/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.utility;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * This will take the list of messages and sort based on - size, from, subject, message number,
 * messageID, sent date or received date
 */
public class SortCondition {
  /**
   * Sort messages array on message field
   *
   * @param messages Message object array
   * @param sortBy Map having keys as sortOn and order e.g {'sortOn':'Subject', 'order':
   *     'Ascending'}
   */
  public void sortMessages(Message[] messages, Map<String, String> sortBy) {
    if (messages == null || messages.length == 0)
      throw new RuntimeException("SortException: messages array is empty or null.");
    String sortField = sortBy.get("sortOn").toLowerCase();
    String order = sortBy.get("order").toLowerCase();
    if (!(order.contains("asc") || order.contains("desc")))
      throw new RuntimeException(
          "SortException: Sorted order error. It should be - ascending or descending.");

    Comparator<Message> messageComparator;

    if (sortField.contains("size")) messageComparator = sizeTerm(order);
    else if (sortField.contains("from")) messageComparator = fromStringTerm(order);
    else if (sortField.contains("subject")) messageComparator = subjectTerm(order);
    else if (sortField.contains("message") && sortField.contains("number"))
      messageComparator = messageNumberTerm(order);
    else if (sortField.contains("sent")
        && (sortField.contains("date") || sortField.contains("time")))
      messageComparator = sentDateTerm(order);
    else if (sortField.contains("message") && sortField.contains("id"))
      messageComparator = messageIDTerm(order);
    else if (sortField.contains("received")
        && (sortField.contains("date") || sortField.contains("time")))
      messageComparator = receivedDateTerm(order);
    else messageComparator = null;
    if (messageComparator == null)
      throw new RuntimeException(
          "Unable to sort message. You can sort message on size,"
              + " from, subject, message number, messageID, sent date and received date.");

    Arrays.sort(messages, messageComparator);
  }

  private Comparator<Message> sizeTerm(String order) {
    return (Message m1, Message m2) -> {
      int res;
      try {
        res = m1.getSize() - m2.getSize();
      } catch (MessagingException e) {
        throw new RuntimeException("Unable to sort messages on size: " + e.getLocalizedMessage());
      }
      return (order.contains("asc")) ? res : (-1 * res);
    };
  }

  private Comparator<Message> fromStringTerm(String order) {
    return (Message m1, Message m2) -> {
      try {
        String m1From = m1.getFrom()[0].toString();
        String m2From = m2.getFrom()[0].toString();
        if (m1From == null || m2From == null) return 0;
        int res = m1From.compareTo(m2From);
        return (order.contains("asc")) ? res : (-1 * res);
      } catch (MessagingException e) {
        throw new RuntimeException(
            "Unable to sort messages on sender email: " + e.getLocalizedMessage());
      }
    };
  }

  private Comparator<Message> subjectTerm(String order) {
    return (Message m1, Message m2) -> {
      try {
        if (m1.getSubject() == null || m2.getSubject() == null) return 0;
        int res = m1.getSubject().compareTo(m2.getSubject());
        return (order.contains("asc")) ? res : (-1 * res);
      } catch (MessagingException e) {
        throw new RuntimeException(
            "Unable to sort messages on subject: " + e.getLocalizedMessage());
      }
    };
  }

  private Comparator<Message> messageNumberTerm(String order) {
    return (Message m1, Message m2) -> {
      int res = m1.getMessageNumber() - m2.getMessageNumber();
      return (order.contains("asc")) ? res : (-1 * res);
    };
  }

  private Comparator<Message> sentDateTerm(String order) {
    return (Message m1, Message m2) -> {
      try {
        if (m1.getSentDate() == null || m2.getSentDate() == null) return 0;
        int res = m1.getSentDate().compareTo(m2.getSentDate());
        return (order.contains("asc")) ? res : (-1 * res);
      } catch (MessagingException e) {
        throw new RuntimeException(
            "Unable to sort messages on sent date: " + e.getLocalizedMessage());
      }
    };
  }

  private Comparator<Message> messageIDTerm(String order) {
    return (Message m1, Message m2) -> {
      try {
        String m1ID = stripMessageId((m1.getHeader("Message-ID")[0]));
        String m2ID = stripMessageId((m2.getHeader("Message-ID")[0]));
        if (m1ID == null || m2ID == null) return 0;
        int res = m1ID.compareTo(m2ID);
        return (order.contains("asc")) ? res : (-1 * res);
      } catch (MessagingException e) {
        throw new RuntimeException(
            "Unable to sort messages on message ID: " + e.getLocalizedMessage());
      }
    };
  }

  private Comparator<Message> receivedDateTerm(String order) {
    return (Message m1, Message m2) -> {
      try {
        if (m1.getReceivedDate() == null || m2.getReceivedDate() == null) return 0;
        int res = m1.getReceivedDate().compareTo(m2.getReceivedDate());
        return (order.contains("asc")) ? res : (-1 * res);
      } catch (MessagingException e) {
        throw new RuntimeException(
            "Unable to sort messages on received date: " + e.getLocalizedMessage());
      }
    };
  }

  public String stripMessageId(String messageId) {
    if (messageId == null) return null;
    return messageId.trim().replaceAll("[<>]", "");
  }
}
