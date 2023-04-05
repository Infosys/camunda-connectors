/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.utility.search;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceivedDateSearchTerm extends SearchTerm {
  private final Logger log = LoggerFactory.getLogger(ReceivedDateSearchTerm.class);
  private String searchContent;
  private LocalDateTime sentAfter;

  public ReceivedDateSearchTerm(String searchContent) {
    this.searchContent = searchContent;
    this.sentAfter = parseString(this.searchContent);
  }

  // Parsing date
  LocalDateTime parseString(String searchContent) {
    LocalDateTime date = null;
    List<String> formatStrings =
        Arrays.asList(
            "dd/MM/yyyy hh:mm:ss a",
            "dd/MM/yyyy HH:mm:ss",
            "E MMM dd HH:mm:ss Z yyyy",
            "EEEE MMMM d yyyy",
            "MMMM d yyyy",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-d HH:mm:ss",
            "yyyy-MM-dd",
            "dd MMM yyyy");
    for (String formatString : formatStrings) {
      try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString);
        date = LocalDateTime.parse(searchContent, formatter);
      } catch (Exception e) {
        log.error(e.getLocalizedMessage());
      }
      if (date != null) {
        log.info("Parsed date string value: {}", date);
        break;
      }
    }
    return date;
  }

  @Override
  public boolean match(Message message) {
    if (this.sentAfter == null) return false;
    try {
      log.info("Filter emails after time: {}", this.sentAfter);
      if (LocalDateTime.ofInstant(message.getReceivedDate().toInstant(), ZoneId.systemDefault())
          .isAfter(this.sentAfter)) {
        return true;
      }
    } catch (MessagingException ex) {
      throw new RuntimeException(
          "Error in declaring search condition: " + ex.getLocalizedMessage());
    }
    return false;
  }
}
