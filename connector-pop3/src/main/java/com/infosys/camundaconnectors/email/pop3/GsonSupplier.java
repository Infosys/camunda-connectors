/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.pop3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infosys.camundaconnectors.email.pop3.model.request.POP3Request;
import com.infosys.camundaconnectors.email.pop3.service.*;

public final class GsonSupplier {
  private static final POP3RequestDeserializer DESERIALIZER =
      new POP3RequestDeserializer("operation")
          .registerType("pop3.list-emails", ListEmailsService.class)
          .registerType("pop3.search-emails", SearchEmailsService.class)
          .registerType("pop3.delete-email", DeleteEmailService.class)
          .registerType("pop3.download-email", DownloadEmailService.class);
  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(POP3Request.class, DESERIALIZER).create();

  private GsonSupplier() {}

  public static Gson getGson() {
    return GSON;
  }
}
