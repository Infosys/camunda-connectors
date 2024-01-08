/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.service.CopyFileService;
import com.infosys.camundaconnectors.files.ftp.service.CopyFolderService;
import com.infosys.camundaconnectors.files.ftp.service.CreateFolderService;
import com.infosys.camundaconnectors.files.ftp.service.DeleteFileService;
import com.infosys.camundaconnectors.files.ftp.service.DeleteFolderService;
import com.infosys.camundaconnectors.files.ftp.service.ListFilesService;
import com.infosys.camundaconnectors.files.ftp.service.ListFoldersService;
import com.infosys.camundaconnectors.files.ftp.service.MoveFileService;
import com.infosys.camundaconnectors.files.ftp.service.MoveFolderService;
import com.infosys.camundaconnectors.files.ftp.service.ReadFileService;
import com.infosys.camundaconnectors.files.ftp.service.WriteFileService;
import com.infosys.camundaconnectors.files.ftp.utility.FTPServerClient;
import java.util.Objects;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import org.apache.commons.net.ftp.FTPClient;

public class FTPRequest<T extends FTPRequestData> {
  @NotNull @Valid private Authentication authentication;
  @NotBlank private String operation;
  @JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
	      property = "operation")
	  @JsonSubTypes(
	      value = {
	        @JsonSubTypes.Type(value = CopyFileService.class, name = "ftp.copy-file"),
	        @JsonSubTypes.Type(value = CopyFolderService.class, name = "ftp.copy-folder"),
	        @JsonSubTypes.Type(value = CreateFolderService.class, name = "ftp.create-folder"),
	        @JsonSubTypes.Type(value = DeleteFileService.class, name = "ftp.delete-file"),
	        @JsonSubTypes.Type(value = DeleteFolderService.class, name = "ftp.delete-folder"),
	        @JsonSubTypes.Type(value = ListFilesService.class, name = "ftp.list-files"),
	        @JsonSubTypes.Type(value = ListFoldersService.class, name = "ftp.list-folders"),
	        @JsonSubTypes.Type(value = MoveFileService.class, name = "ftp.move-file"),
	        @JsonSubTypes.Type(value = MoveFolderService.class, name = "ftp.move-folder"),
	        @JsonSubTypes.Type(value = ReadFileService.class, name = "ftp.read-file"),
	        @JsonSubTypes.Type(value = WriteFileService.class, name = "ftp.write-file")
	      })  
  @Valid @NotNull private T data;
  
  public FTPResponse<String> invoke(FTPServerClient ftpServerClient) {
    FTPResponse<String> resp;
    try {
      FTPClient client = ftpServerClient.loginFTP(authentication);
      if (operation.equalsIgnoreCase("ftp.copy-file")
          || operation.equalsIgnoreCase("ftp.copy-folder")) {
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
