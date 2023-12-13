/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mysql.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.db.mysql.model.response.MySQLResponse;
import com.infosys.camundaconnectors.db.mysql.service.AlterTableService;
import com.infosys.camundaconnectors.db.mysql.service.CreateDatabaseService;
import com.infosys.camundaconnectors.db.mysql.service.CreateTableService;
import com.infosys.camundaconnectors.db.mysql.service.DeleteDataService;
import com.infosys.camundaconnectors.db.mysql.service.InsertDataService;
import com.infosys.camundaconnectors.db.mysql.service.ReadDataService;
import com.infosys.camundaconnectors.db.mysql.service.UpdateDataService;
import com.infosys.camundaconnectors.db.mysql.utility.DatabaseClient;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import jakarta.validation.constraints.*;

public class MySQLRequest<T extends MySQLRequestData> {
  @NotNull private DatabaseConnection databaseConnection;
  @JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
	      property = "operation")
	  @JsonSubTypes(
	      value = {
	        @JsonSubTypes.Type(value= UpdateDataService.class , name = "mysql.update-data"),
	        @JsonSubTypes.Type(value = ReadDataService.class, name = "mysql.read-data"),
	        @JsonSubTypes.Type(
	            value = InsertDataService.class,
	            name = "mysql.insert-data"),
	        @JsonSubTypes.Type(
	            value = DeleteDataService.class,
	            name = "mysql.delete-data"),
	        @JsonSubTypes.Type(
	            value = CreateTableService.class,
	            name = "mysql.create-table"),
	        @JsonSubTypes.Type(
	            value = CreateDatabaseService.class,
	            name = "mysql.create-database"),
	        @JsonSubTypes.Type(
	            value = AlterTableService.class,
	            name = "mysql.alter-table"),
	     
	      })
  @NotNull private T data;

  public MySQLResponse invoke(DatabaseClient databaseClient) throws SQLException {
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
	MySQLRequest other = (MySQLRequest) obj;
	return Objects.equals(data, other.data) && Objects.equals(databaseConnection, other.databaseConnection);
}

@Override
public String toString() {
	return "MySQLRequest [databaseConnection=" + databaseConnection + ", data=" + data + "]";
}

  
}
