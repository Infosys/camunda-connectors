/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.model.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.service.files.CopyFileService;
import com.infosys.camundaconnectors.file.sftp.service.files.DeleteFileService;
import com.infosys.camundaconnectors.file.sftp.service.files.ListFilesService;
import com.infosys.camundaconnectors.file.sftp.service.files.MoveFileService;
import com.infosys.camundaconnectors.file.sftp.service.files.ReadFileService;
import com.infosys.camundaconnectors.file.sftp.service.files.WriteFileService;
import com.infosys.camundaconnectors.file.sftp.service.folders.CopyFolderService;
import com.infosys.camundaconnectors.file.sftp.service.folders.CreateFolderService;
import com.infosys.camundaconnectors.file.sftp.service.folders.DeleteFolderService;
import com.infosys.camundaconnectors.file.sftp.service.folders.ListFoldersService;
import com.infosys.camundaconnectors.file.sftp.service.folders.MoveFolderService;
import com.infosys.camundaconnectors.file.sftp.utility.SftpServerClient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import java.util.Objects;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SFTPRequest<T extends SFTPRequestData> {
@Valid  private Authentication authentication;
  @JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
	      property = "operation")
	  @JsonSubTypes(
	      value = {
	        @JsonSubTypes.Type(value =CopyFileService.class , name = "sftp.copy-file"),
	        @JsonSubTypes.Type(value = CopyFolderService.class, name = "sftp.copy-folder"),
	        @JsonSubTypes.Type(
	            value = CreateFolderService.class,
	            name = "sftp.create-folder"),
	        @JsonSubTypes.Type(
	            value = DeleteFileService.class,
	            name = "sftp.delete-file"),
	        @JsonSubTypes.Type(
	            value = DeleteFolderService.class,
	            name = "sftp.delete-folder"),
	        @JsonSubTypes.Type(
	            value = ListFilesService.class,
	            name = "sftp.list-files"),
	        @JsonSubTypes.Type(
	            value = ListFoldersService.class,
	            name = "sftp.list-folders"),
	        @JsonSubTypes.Type(
	            value = MoveFileService.class,
	            name = "sftp.move-file"),
	        @JsonSubTypes.Type(
	            value = MoveFolderService.class,
	            name = "sftp.move-folder"),
	        @JsonSubTypes.Type(value = ReadFileService.class, name = "sftp.read-file"),
	        @JsonSubTypes.Type(value = WriteFileService.class, name = "sftp.write-file")
	      })
	  @Valid
	  private T data;
  private static final Logger LOGGER = LoggerFactory.getLogger(SftpServerClient.class);

  public Response invoke(final SftpServerClient sftpServerClient) throws Exception {
    SSHClient client = sftpServerClient.loginSftp(authentication);
    SFTPClient sftpClient = client.newSFTPClient();
    LOGGER.info("Connected to the sftp server");
    return data.invoke(sftpClient);
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }


  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

@Override
public int hashCode() {
	return Objects.hash(authentication, data);
}

@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	SFTPRequest other = (SFTPRequest) obj;
	return Objects.equals(authentication, other.authentication) && Objects.equals(data, other.data);
}

@Override
public String toString() {
	return "SFTPRequest [authentication=" + authentication + ", data=" + data + "]";
}

  
}
