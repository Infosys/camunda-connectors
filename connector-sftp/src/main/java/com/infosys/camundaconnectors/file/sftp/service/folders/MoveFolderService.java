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

public class MoveFolderService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(MoveFolderService.class);

  @NotBlank private String sourceDirectory;
  @NotBlank private String targetDirectory;
  @NotBlank private String actionIfFileExists;
  RenameFolderUtil renameFolderUtil = new RenameFolderUtil();

  SFTPResponse<?> moveFolderResponse;

  public Response invoke(SFTPClient sftpClient) {
    if (sourceDirectory == null || sourceDirectory.isBlank())
      throw new RuntimeException("Please Provide a valid sourceDirectory");
    if (targetDirectory == null || sourceDirectory.isBlank())
      throw new RuntimeException("Please Provide a valid targetDirectory");
    if (actionIfFileExists.isBlank()
        || (!actionIfFileExists.equalsIgnoreCase("rename")
            && !actionIfFileExists.equalsIgnoreCase("replace")
            && !actionIfFileExists.equalsIgnoreCase("skip"))) {
      throw new RuntimeException(
          "Incorrect actionIfFileExists. It should be rename,replace or skip");
    }
    Path sourceDirectoryPath = Path.of(sourceDirectory);
    Path targetDirectoryPath = Path.of(targetDirectory);
    try {
      boolean targetFolderExists = false;
      LOGGER.info("Checking if source directory already exists or not");
      boolean sourceFilePathFlag = checkIfFileExists(sftpClient, sourceDirectoryPath);
      if (!sourceFilePathFlag) throw new IOException("Source file does not exists");
      LOGGER.info("Checking if target folder already exists or not");
      targetFolderExists = checkIfFileExists(sftpClient, targetDirectoryPath);
      Boolean movedFolder = false;
      if (!targetFolderExists) {
        LOGGER.info("Target Directory does not exists, Creating a Target Directory");
        sftpClient.mkdir(targetDirectoryPath.toString());
      } else {
        Path newPath =
            Path.of(
                targetDirectory.toString()
                    + File.separator
                    + sourceDirectoryPath.getFileName().toString());
        LOGGER.info(
            "Checking if the folder with the same name as source folder is already exists in target folder");
        movedFolder = checkIfFileExists(sftpClient, newPath);
      }
      if (!movedFolder) {
        LOGGER.info("Create new Folder with the same name as sourceDirectory");
        Path newPath =
            Path.of(
                targetDirectory.toString()
                    + File.separator
                    + sourceDirectoryPath.getFileName().toString());
        LOGGER.info("Moving folder");
        sftpClient.rename(sourceDirectoryPath.toString(), newPath.toString());
        LOGGER.info("Folder moved successfully");
        moveFolderResponse = new SFTPResponse<>("Folder Moved Successfully");
      } else {
        LOGGER.info("Target folder already exists.");
        if (actionIfFileExists == null || actionIfFileExists.isBlank()) {

          throw new Exception("Action file should be rename,replace or skip");
        }
        if (actionIfFileExists.equalsIgnoreCase("rename")) {
          LOGGER.info("rename flow started");
          String newFolderName = sourceDirectoryPath.getFileName().toString();
          newFolderName = newFolderName + " - Copy";
          Path newPath = Path.of(targetDirectory.toString() + File.separator + newFolderName);
          Integer count = 2;
          newPath = renameFolderUtil.renameUtil(sftpClient, newPath, count);
          sftpClient.rename(sourceDirectoryPath.toString(), newPath.toString());
          LOGGER.info(
              "Folder name successfully changed and folder is moved to its targetDirectory"
                  + newPath.toString());
          moveFolderResponse =
              new SFTPResponse<>(
                  "Folder renamed and Moved Successfully with new name as" + newPath.toString());
        } else if (actionIfFileExists.equalsIgnoreCase("replace")) {
          LOGGER.info("Replacing started");
          Path newPath =
              Path.of(
                  targetDirectory.toString()
                      + File.separator
                      + sourceDirectoryPath.getFileName().toString());
          deleteDirectoryRecursively(sftpClient, newPath);
          if (checkIfFileExists(sftpClient, newPath)) {
            sftpClient.rmdir(newPath.toString());
          }
          LOGGER.info("Folder Removed Successfully");
          sftpClient.rename(sourceDirectoryPath.toString(), newPath.toString());
          LOGGER.info("Folder Replaced Successfully");
          moveFolderResponse = new SFTPResponse<>("Folder replaced and copied successfully");
        } else if (actionIfFileExists.equalsIgnoreCase("skip")) {
          LOGGER.info("Folder Skipped Successfully");
          moveFolderResponse = new SFTPResponse<>("Folder Skipped successfully");
        }
      }

      return moveFolderResponse;

    } catch (Exception e) {
      throw new RuntimeException(e.getLocalizedMessage());
    } finally {
      try {
        if (sftpClient != null) sftpClient.close();
      } catch (IOException e) {
        LOGGER.error("Some error occurred while trying to close SFTPClient");
      }
    }
  }

  public boolean checkIfFileExists(SFTPClient sftpClient, Path newFilePath) {
    try {
      if (sftpClient.stat(newFilePath.toString()) == null) {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public void deleteDirectoryRecursively(SFTPClient sftpClient, Path directory) throws IOException {
    sftpClient
        .ls(directory.toString())
        .forEach(
            file -> {
              Path fullAdd = Path.of(directory + File.separator + file.getName());
              if (file.isDirectory()) {
                try {
                  deleteDirectoryRecursively(sftpClient, fullAdd);
                  sftpClient.rmdir(Path.of(directory + File.separator + file.getName()).toString());
                } catch (IOException e) {
                  LOGGER.error("Some error occurred while trying to remove a folder");
                }
              } else {
                try {
                  sftpClient.rm(fullAdd.toString());
                } catch (IOException e) {
                  throw new RuntimeException(
                      "Some error occurred while trying to delete directory");
                }
              }
            });
  }

  public String getSourceDirectory() {
    return sourceDirectory;
  }

  public void setSourceDirectory(String sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  public String getTargetDirectory() {
    return targetDirectory;
  }

  public void setTargetDirectory(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  public String getActionIfFileExists() {
    return actionIfFileExists;
  }

  public void setActionIfFileExists(String actionIfFileExists) {
    this.actionIfFileExists = actionIfFileExists;
  }

  @Override
  public int hashCode() {
    return Objects.hash(actionIfFileExists, sourceDirectory, targetDirectory);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MoveFolderService other = (MoveFolderService) obj;
    return Objects.equals(actionIfFileExists, other.actionIfFileExists)
        && Objects.equals(sourceDirectory, other.sourceDirectory)
        && Objects.equals(targetDirectory, other.targetDirectory);
  }

  @Override
  public String toString() {
    return "MoveFolderService [sourceDirectory="
        + sourceDirectory
        + ", targetDirectory="
        + targetDirectory
        + ", actionIfFileExists="
        + actionIfFileExists
        + "]";
  }
}
