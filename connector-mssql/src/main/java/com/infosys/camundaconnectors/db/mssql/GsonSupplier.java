/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infosys.camundaconnectors.db.mssql.model.request.MSSQLRequest;
import com.infosys.camundaconnectors.db.mssql.service.*;

public final class GsonSupplier {
  private static final MSSQLRequestDeserializer DESERIALIZER =
          new MSSQLRequestDeserializer("operation")
                  .registerType("mssql.alter-table", AlterTableService.class)
                  .registerType("mssql.create-database", CreateDatabaseService.class)
                  .registerType("mssql.create-table", CreateTableService.class)
                  .registerType("mssql.delete-data", DeleteDataService.class)
                  .registerType("mssql.insert-data", InsertDataService.class)
                  .registerType("mssql.read-data", ReadDataService.class)
                  .registerType("mssql.update-data", UpdateDataService.class);
  private static final Gson GSON =
          new GsonBuilder().registerTypeAdapter(MSSQLRequest.class, DESERIALIZER).create();

  private GsonSupplier() {}

  public static Gson getGson() {
    return GSON;
  }
}
