/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.utility.search;

import java.io.IOException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.search.SearchTerm;

public class AttachmentFieldSearchTerm extends SearchTerm {
  @Override
  public boolean match(Message message) {
    try {
      if (hasAttachments(message)) return true;
    } catch (MessagingException | IOException ex) {
      throw new RuntimeException(
          "Error in declaring search condition: " + ex.getLocalizedMessage());
    }
    return false;
  }

  /** Check if email have attachment */
  boolean hasAttachments(Message message) throws MessagingException, IOException {
    if (message.isMimeType("multipart/mixed")) {
      Multipart mp = (Multipart) message.getContent();
      if (mp.getCount() > 1) return true;
    }
    return false;
  }
}
