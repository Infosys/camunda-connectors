/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.model.request;

import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.utility.FTPServerClient;
import io.camunda.connector.api.annotation.Secret;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.net.ftp.FTPClient;

public class FTPRequest<T extends FTPRequestData> {
  @NotNull @Valid @Secret private Authentication authentication;
  @NotBlank private String operation;
  @Valid @NotNull private T data;

  public FTPResponse<String> invoke(FTPServerClient ftpServerClient)  {
	FTPResponse<String> resp;	
    try {
		FTPClient client = ftpServerClient.loginFTP(authentication);
		if(operation.equalsIgnoreCase("ftp.copy-file") || operation.equalsIgnoreCase("ftp.copy-folder")) {
			FTPClient client2 = ftpServerClient.loginFTP(authentication);
    		return data.invoke(client, client2);
    	}
		return data.invoke(client);
	} catch (Exception e) {
		resp = new FTPResponse<>("Login Failed!!");
		return resp;
	}
    
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FTPRequest<?> that = (FTPRequest<?>) o;
    return Objects.equals(authentication, that.authentication)
        && Objects.equals(operation, that.operation)
        && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authentication, operation, data);
  }

  @Override
  public String toString() {
    return "FTPRequest{"
        + " authentication="
        + authentication
        + ", operation='"
        + operation
        + '\''
        + ", data="
        + data
        + '}';
  }
}
