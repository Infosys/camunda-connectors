/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.postgresql.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.db.postgresql.model.response.PostgreSQLResponse;
import com.infosys.camundaconnectors.db.postgresql.service.AlterTableService;
import com.infosys.camundaconnectors.db.postgresql.service.CreateDatabaseService;
import com.infosys.camundaconnectors.db.postgresql.service.CreateTableService;
import com.infosys.camundaconnectors.db.postgresql.service.DeleteDataService;
import com.infosys.camundaconnectors.db.postgresql.service.InsertDataService;
import com.infosys.camundaconnectors.db.postgresql.service.ReadDataService;
import com.infosys.camundaconnectors.db.postgresql.service.UpdateDataService;
import com.infosys.camundaconnectors.db.postgresql.utility.DatabaseClient;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import jakarta.validation.constraints.*;
import jakarta.validation.*;

public class PostgreSQLRequest<T extends PostgreSQLRequestData> {
  @NotNull @Valid private DatabaseConnection databaseConnection;
  @JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
	      property = "operation")
	  @JsonSubTypes(
	      value = {
	        @JsonSubTypes.Type(value =UpdateDataService.class , name = "postgresql.update-data"),
	        @JsonSubTypes.Type(value = ReadDataService.class, name = "postgresql.read-data"),
	        @JsonSubTypes.Type(
	            value = InsertDataService.class,
	            name = "postgresql.insert-data"),
	        @JsonSubTypes.Type(
	            value = DeleteDataService.class,
	            name = "postgresql.delete-data"),
	        @JsonSubTypes.Type(
	            value = CreateTableService.class,
	            name = "postgresql.create-table"),
	        @JsonSubTypes.Type(
	            value = CreateDatabaseService.class,
	            name = "postgresql.create-database"),
	        @JsonSubTypes.Type(
	            value = AlterTableService.class,
	            name = "postgresql.alter-table"),
	     
	      })
  @Valid @NotNull private T data;

  public PostgreSQLResponse invoke(DatabaseClient databaseClient) throws SQLException {
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
	PostgreSQLRequest other = (PostgreSQLRequest) obj;
	return Objects.equals(data, other.data) && Objects.equals(databaseConnection, other.databaseConnection);
}

@Override
public String toString() {
	return "PostgreSQLRequest [databaseConnection=" + databaseConnection + ", data=" + data + "]";
}

  
}
