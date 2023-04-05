/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.utility.search;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;

public class SubjectFieldSearchTerm extends SearchTerm {
  private String searchContent;
  private String searchType;

  public SubjectFieldSearchTerm(String searchContent, String searchType) {
    this.searchContent = searchContent;
    this.searchType = searchType;
  }

  @Override
  public boolean match(Message message) {
    try {
      String subject = message.getSubject();
      if (searchType.equalsIgnoreCase("partial")) {
        if (subject.toLowerCase().contains(searchContent.toLowerCase())) return true;
      } else {
        if (subject.equalsIgnoreCase(searchContent)) return true;
      }
    } catch (MessagingException ex) {
      throw new RuntimeException(
          "Error in declaring search condition: " + ex.getLocalizedMessage());
    }
    return false;
  }
}
