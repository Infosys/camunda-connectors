/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.service.folders;

import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import com.infosys.camundaconnectors.file.sftp.utility.RenameFolderUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateFolderService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateFolderService.class);

  @NotBlank private String folderPath;
  @NotBlank private String newFolderName;
  @NotBlank private String actionIfFileExists;
  SFTPResponse<?> createFolderResponse;
  RenameFolderUtil renameFolderUtil = new RenameFolderUtil();

  public Response invoke(SFTPClient sftpClient) {

    if (folderPath == null || folderPath.isBlank())
      throw new RuntimeException("Please Provide a valid Path");
    if (newFolderName == null || newFolderName.isBlank())
      throw new RuntimeException("Please Provide a valid folderName");
    if (actionIfFileExists.isBlank()
        || (!actionIfFileExists.equalsIgnoreCase("rename")
            && !actionIfFileExists.equalsIgnoreCase("replace")
            && !actionIfFileExists.equalsIgnoreCase("skip"))) {
      throw new RuntimeException(
          "Incorrect actionIfFileExists. It should be rename,replace or skip");
    }
    Path newFolderPath = Path.of(folderPath, newFolderName);
    try {
      LOGGER.info("Checking if folder already exists or not");
      boolean sourceFilePathFlag = checkIfFileExists(sftpClient, newFolderPath);
      if (!sourceFilePathFlag) { // If Folder does not Exists Create folder
        sftpClient.mkdir(newFolderPath.toString().replace("\\", "/"));
        LOGGER.info("Folder created successfully");
        createFolderResponse = new SFTPResponse<>("Folder created successfully");
      } else {
        LOGGER.info("Target folder already exists.");
        if (actionIfFileExists == null || actionIfFileExists.isBlank()) {
          throw new IOException("Action file should be rename,replace or skip");
        }
        if (actionIfFileExists.equalsIgnoreCase("rename")) {
          LOGGER.info("rename flow started");
          String newFolderName = newFolderPath.getFileName().toString();
          newFolderName = newFolderName + " - Copy";
          Path newPath =
              Path.of(newFolderPath.getParent().toString() + File.separator + newFolderName);
          Integer count = 2;
          newPath = renameFolderUtil.renameUtil(sftpClient, newPath, count);
          sftpClient.mkdir(newPath.toString().replace("\\", "/"));
          LOGGER.info("Folder is created with name" + newFolderName);
          createFolderResponse =
              new SFTPResponse<>("Folder is created with name " + newPath.getNameCount());
        } else if (actionIfFileExists.equalsIgnoreCase("replace")) {
          LOGGER.info("Replacing started");
          deleteDirectoryRecursively(sftpClient, newFolderPath);
          if (checkIfFileExists(sftpClient, newFolderPath)) {
            sftpClient.rmdir(newFolderPath.toString().replace("\\", "/"));
          }
          LOGGER.info("Folder Removed Successfully");
          sftpClient.mkdir(newFolderPath.toString().replace("\\", "/"));
          LOGGER.info("Folder Replaced Successfully");
          createFolderResponse = new SFTPResponse<>("Folder created successfully");
        } else if (actionIfFileExists.equalsIgnoreCase("skip")) {
          LOGGER.info("File Skipped Successfully");
          createFolderResponse = new SFTPResponse<>("Folder skipped successfully");
        }
      }
    } catch (IOException e) {
      LOGGER.debug(
          "Some error occurred while trying to create folder. Please check your path once");
      throw new RuntimeException("IOException" + e.getLocalizedMessage());
    } finally {
      try {
        if (sftpClient != null) sftpClient.close();
      } catch (IOException e) {
        LOGGER.error("Some Error occurred while trying to close the SFTPClient");
      }
    }
    LOGGER.debug("CreateFolderUsingSFTP Process Completed");
    return createFolderResponse;
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
                  LOGGER.error("Some errors occurred while trying to remove files");
                }
              }
            });
  }

  public boolean checkIfFileExists(SFTPClient sftpClient, Path newFilePath) {
    try {
      if (sftpClient.stat(newFilePath.toString().replace("\\", "/")) == null) {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
  }

  public String getNewFolderName() {
    return newFolderName;
  }

  public void setNewFolderName(String newFolderName) {
    this.newFolderName = newFolderName;
  }

  public String getActionIfFileExists() {
    return actionIfFileExists;
  }

  public void setActionIfFileExists(String actionIfFileExists) {
    this.actionIfFileExists = actionIfFileExists;
  }

  @Override
  public int hashCode() {
    return Objects.hash(actionIfFileExists, createFolderResponse, folderPath, newFolderName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CreateFolderService other = (CreateFolderService) obj;
    return Objects.equals(actionIfFileExists, other.actionIfFileExists)
        && Objects.equals(createFolderResponse, other.createFolderResponse)
        && Objects.equals(folderPath, other.folderPath)
        && Objects.equals(newFolderName, other.newFolderName);
  }

  @Override
  public String toString() {
    return "CreateFolderService [folderPath="
        + folderPath
        + ", new folderName="
        + newFolderName
        + ", actionIfFileExists="
        + actionIfFileExists
        + ", createFolderResponse="
        + createFolderResponse
        + "]";
  }
}
