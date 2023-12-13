/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.db.mssql.model.response.MSSQLResponse;
import com.infosys.camundaconnectors.db.mssql.service.AlterTableService;
import com.infosys.camundaconnectors.db.mssql.service.CreateDatabaseService;
import com.infosys.camundaconnectors.db.mssql.service.CreateTableService;
import com.infosys.camundaconnectors.db.mssql.service.DeleteDataService;
import com.infosys.camundaconnectors.db.mssql.service.InsertDataService;
import com.infosys.camundaconnectors.db.mssql.service.ReadDataService;
import com.infosys.camundaconnectors.db.mssql.service.UpdateDataService;
import com.infosys.camundaconnectors.db.mssql.utility.DatabaseClient;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import jakarta.validation.constraints.*;
import jakarta.validation.*;

public class MSSQLRequest<T extends MSSQLRequestData> {
  @NotNull @Valid private DatabaseConnection databaseConnection;
  @JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
	      property = "operation")
	  @JsonSubTypes(
	      value = {
	        @JsonSubTypes.Type(value= UpdateDataService.class , name = "mssql.update-data"),
	        @JsonSubTypes.Type(value = ReadDataService.class, name = "mssql.read-data"),
	        @JsonSubTypes.Type(
	            value = InsertDataService.class,
	            name = "mssql.insert-data"),
	        @JsonSubTypes.Type(
	            value = DeleteDataService.class,
	            name = "mssql.delete-data"),
	        @JsonSubTypes.Type(
	            value = CreateTableService.class,
	            name = "mssql.create-table"),
	        @JsonSubTypes.Type(
	            value = CreateDatabaseService.class,
	            name = "mssql.create-database"),
	        @JsonSubTypes.Type(
	            value = AlterTableService.class,
	            name = "mssql.alter-table"),
	     
	      })
  @Valid @NotNull private T data;

  public MSSQLResponse invoke(DatabaseClient databaseClient) throws SQLException {
    return data.invoke(databaseClient,databaseConnection,data.getDatabaseName());
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
	MSSQLRequest other = (MSSQLRequest) obj;
	return Objects.equals(data, other.data) && Objects.equals(databaseConnection, other.databaseConnection);
}

@Override
public String toString() {
	return "MSSQLRequest [databaseConnection=" + databaseConnection + ", data=" + data + "]";
}
  
}
