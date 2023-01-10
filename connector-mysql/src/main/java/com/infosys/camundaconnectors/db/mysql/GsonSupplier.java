/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mysql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infosys.camundaconnectors.db.mysql.model.request.MySQLRequest;
import com.infosys.camundaconnectors.db.mysql.service.*;

public final class GsonSupplier {
  private static final MySQLRequestDeserializer DESERIALIZER =
      new MySQLRequestDeserializer("operation")
          .registerType("mysql.alter-table", AlterTableService.class)
          .registerType("mysql.create-database", CreateDatabaseService.class)
          .registerType("mysql.create-table", CreateTableService.class)
          .registerType("mysql.delete-data", DeleteDataService.class)
          .registerType("mysql.insert-data", InsertDataService.class)
          .registerType("mysql.read-data", ReadDataService.class)
          .registerType("mysql.update-data", UpdateDataService.class);
  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(MySQLRequest.class, DESERIALIZER).create();

  private GsonSupplier() {}

  public static Gson getGson() {
    return GSON;
  }
}
