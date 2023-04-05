/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.imap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infosys.camundaconnectors.email.imap.model.request.IMAPRequest;
import com.infosys.camundaconnectors.email.imap.service.*;

public final class GsonSupplier {
  private static final IMAPRequestDeserializer DESERIALIZER =
      new IMAPRequestDeserializer("operation")
          .registerType("imap.list-emails", ListEmailsService.class)
          .registerType("imap.search-emails", SearchEmailsService.class)
          .registerType("imap.delete-email", DeleteEmailService.class)
          .registerType("imap.download-email", DownloadEmailService.class)
          .registerType("imap.move-email", MoveEmailService.class)
          .registerType("imap.read-email", ReadEmailService.class);
  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(IMAPRequest.class, DESERIALIZER).create();

  private GsonSupplier() {}

  public static Gson getGson() {
    return GSON;
  }
}
