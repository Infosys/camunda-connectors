/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.utility.search;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;
import org.apache.commons.mail.util.MimeMessageParser;

public class BodySearchTerm extends SearchTerm {
  private String searchContent;
  private String searchType;

  public BodySearchTerm(String searchContent, String searchType) {
    this.searchContent = searchContent;
    this.searchType = searchType;
  }

  @Override
  public boolean match(Message message) {
    try {
      MimeMessageParser mmp = new MimeMessageParser((MimeMessage) message).parse();
      String messageContent;
      messageContent = mmp.getPlainContent();
      if (messageContent == null) return false;
      if (searchType.equalsIgnoreCase("partial")) {
        if (messageContent.toLowerCase().contains(searchContent.toLowerCase())) return true;
      } else {
        if (messageContent.equalsIgnoreCase(searchContent)) return true;
      }
    } catch (Exception ex) {
	    throw new RuntimeException(
			    "Error in declaring search condition: " + ex.getLocalizedMessage());
    }
    return false;
  }
}
