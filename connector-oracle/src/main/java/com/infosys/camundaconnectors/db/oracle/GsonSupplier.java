/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.oracle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infosys.camundaconnectors.db.oracle.model.request.OracleDBRequest;
import com.infosys.camundaconnectors.db.oracle.service.*;

public final class GsonSupplier {
  private static final OracleDBRequestDeserializer DESERIALIZER =
      new OracleDBRequestDeserializer("operation")
          .registerType("oracledb.alter-table", AlterTableService.class)
          .registerType("oracledb.create-table", CreateTableService.class)
          .registerType("oracledb.delete-data", DeleteDataService.class)
          .registerType("oracledb.insert-data", InsertDataService.class)
          .registerType("oracledb.read-data", ReadDataService.class)
          .registerType("oracledb.update-data", UpdateDataService.class);
  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(OracleDBRequest.class, DESERIALIZER).create();

  private GsonSupplier() {}

  public static Gson getGson() {
    return GSON;
  }
}
