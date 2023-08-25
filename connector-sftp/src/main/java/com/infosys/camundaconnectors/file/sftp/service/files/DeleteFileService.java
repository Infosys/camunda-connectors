/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.service.files;

import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import java.io.IOException;
import java.util.Objects;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFileService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFileService.class);
  private String folderPath;
  private String fileName;
  SFTPResponse<?> deleteFileResponse;

  public Response invoke(SFTPClient sftpClient) {

    LOGGER.info("DeleteFileUsingSFTP process started");
    try {
      if (fileName == null || fileName.isBlank())
        throw new RuntimeException("Please Provide a valid fileName");
      if (folderPath == null || folderPath.isBlank() || folderPath.isEmpty())
        throw new RuntimeException("Please provide a valid folderPath");
      String fileToBeDeleted = folderPath.replace("\\", "/") + "/" + fileName;
      sftpClient.rm(fileToBeDeleted);
      deleteFileResponse = new SFTPResponse<>("File deleted successfully");
      LOGGER.info("DeleteFileUsingSFTP Process Completed");
    } catch (IOException e) {
      LOGGER.info("Some error occurred while trying to delete file");
      throw new RuntimeException("IOException" + e.getLocalizedMessage());
    } finally {
      try {
        if (sftpClient != null) {
          sftpClient.close();
          LOGGER.info("SFTPClient close successfully");
        }
      } catch (IOException e) {
        LOGGER.error("Some Error occurred while trying to close the SFTPClient");
      }
    }
    return deleteFileResponse;
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(fileName, folderPath);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DeleteFileService other = (DeleteFileService) obj;
    return Objects.equals(fileName, other.fileName) && Objects.equals(folderPath, other.folderPath);
  }

  @Override
  public String toString() {
    return "DeleteFileService [folderPath=" + folderPath + ", fileName=" + fileName + "]";
  }
}
