/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.postgresql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infosys.camundaconnectors.db.postgresql.model.request.PostgreSQLRequest;
import com.infosys.camundaconnectors.db.postgresql.service.*;

public final class GsonSupplier {
  private static final PostgreSQLRequestDeserializer DESERIALIZER =
      new PostgreSQLRequestDeserializer("operation")
          .registerType("postgresql.alter-table", AlterTableService.class)
          .registerType("postgresql.create-database", CreateDatabaseService.class)
          .registerType("postgresql.create-table", CreateTableService.class)
          .registerType("postgresql.delete-data", DeleteDataService.class)
          .registerType("postgresql.insert-data", InsertDataService.class)
          .registerType("postgresql.read-data", ReadDataService.class)
          .registerType("postgresql.update-data", UpdateDataService.class);
  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(PostgreSQLRequest.class, DESERIALIZER).create();

  private GsonSupplier() {}

  public static Gson getGson() {
    return GSON;
  }
}
