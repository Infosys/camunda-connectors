/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.service.folders;

import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFolderService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFolderService.class);
  private String folderPath;
  SFTPResponse<?> deleteFolderResponse;

  public Response invoke(SFTPClient sftpClient) {
    LOGGER.info("DeleteFolderUsingSFTP process started");
    try {
      if (folderPath == null || folderPath.isBlank())
        throw new RuntimeException("Please provide a valid folderPath");
      Path folderPath_ = Path.of(folderPath);
      deleteDirectoryRecursively(sftpClient, folderPath_);
      sftpClient.rmdir(folderPath_.toString().replace("\\", "/"));
      deleteFolderResponse = new SFTPResponse<>("Folder deleted successfully");
      LOGGER.info("DeleteFolderUsingSFTP Process Completed");
    } catch (IOException e) {
      LOGGER.info("Some error occurred while trying to delete folder");
      throw new RuntimeException("IOException" + e.getLocalizedMessage());
    } finally {

      try {
        if (sftpClient != null) sftpClient.close();
      } catch (IOException e) {
        LOGGER.error("Some Error occurred while trying to close the SFTPClient");
      }
    }
    return deleteFolderResponse;
  }

  public void deleteDirectoryRecursively(SFTPClient sftpClient, Path directory) throws IOException {
    sftpClient
        .ls(directory.toString().replace("\\", "/"))
        .forEach(
            file -> {
              Path fullAdd = Path.of(directory + File.separator + file.getName());
              if (file.isDirectory()) {
                try {
                  deleteDirectoryRecursively(sftpClient, fullAdd);
                  sftpClient.rmdir(
                      Path.of(directory + File.separator + file.getName())
                          .toString()
                          .replace("\\", "/"));
                } catch (IOException e) {
                }
              } else {
                try {
                  sftpClient.rm(fullAdd.toString().replace("\\", "/"));
                } catch (IOException e) {
                }
              }
            });
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
  }

  @Override
  public int hashCode() {
    return Objects.hash(folderPath);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DeleteFolderService other = (DeleteFolderService) obj;
    return Objects.equals(folderPath, other.folderPath);
  }

  @Override
  public String toString() {
    return "DeleteFolderService [folderPath=" + folderPath + "]";
  }
}
