/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.oracle.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.db.oracle.model.response.OracleDBResponse;
import com.infosys.camundaconnectors.db.oracle.service.AlterTableService;
import com.infosys.camundaconnectors.db.oracle.service.CreateTableService;
import com.infosys.camundaconnectors.db.oracle.service.DeleteDataService;
import com.infosys.camundaconnectors.db.oracle.service.InsertDataService;
import com.infosys.camundaconnectors.db.oracle.service.ReadDataService;
import com.infosys.camundaconnectors.db.oracle.service.UpdateDataService;
import com.infosys.camundaconnectors.db.oracle.utility.DatabaseClient;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import jakarta.validation.constraints.*;
import jakarta.validation.*;

public class OracleDBRequest<T extends OracleDBRequestData> {
  @NotNull @Valid private DatabaseConnection databaseConnection;
  @JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
	      property = "operation")
	  @JsonSubTypes(
	      value = {
	        @JsonSubTypes.Type(value= UpdateDataService.class , name = "oracledb.update-data"),
	        @JsonSubTypes.Type(value = ReadDataService.class, name = "oracledb.read-data"),
	        @JsonSubTypes.Type(
	            value = InsertDataService.class,
	            name = "oracledb.insert-data"),
	        @JsonSubTypes.Type(
	            value = DeleteDataService.class,
	            name = "oracledb.delete-data"),
	        @JsonSubTypes.Type(
	            value = CreateTableService.class,
	            name = "oracledb.create-table"),
	        @JsonSubTypes.Type(
	            value = AlterTableService.class,
	            name = "oracledb.alter-table"),
	     
	      })
  @Valid @NotNull private T data;

  public OracleDBResponse invoke(DatabaseClient databaseClient) throws SQLException {
    Connection connection =
        databaseClient.getConnectionObject(databaseConnection, data.getDatabaseName());
    return data.invoke(connection);
  }

  public DatabaseConnection getDatabaseConnection() {
    return databaseConnection;
  }

  public void setDatabaseConnection(DatabaseConnection databaseConnection) {
    this.databaseConnection = databaseConnection;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

@Override
public int hashCode() {
	return Objects.hash(data, databaseConnection);
}

@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	OracleDBRequest other = (OracleDBRequest) obj;
	return Objects.equals(data, other.data) && Objects.equals(databaseConnection, other.databaseConnection);
}

@Override
public String toString() {
	return "OracleDBRequest [databaseConnection=" + databaseConnection + ", data=" + data + "]";
}

  
}
