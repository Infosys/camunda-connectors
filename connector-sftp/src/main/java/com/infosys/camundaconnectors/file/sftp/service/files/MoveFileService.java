/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.service.files;

import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import com.infosys.camundaconnectors.file.sftp.utility.RenameFileUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveFileService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(MoveFileService.class);
  @NotBlank private String sourceFilePath;
  @NotBlank private String targetDirectory;
  @NotBlank private String actionIfFileExists;
  SFTPResponse<?> moveFileResponse;
  RenameFileUtil renameFile = new RenameFileUtil();

  public Response invoke(SFTPClient sftpClient) {
    Path sourceDirectoryPath = Path.of(sourceFilePath);
    Path targetDirectoryPath = Path.of(targetDirectory);
    try {
      if (actionIfFileExists.isBlank()
          || (!actionIfFileExists.equalsIgnoreCase("rename")
              && !actionIfFileExists.equalsIgnoreCase("replace")
              && !actionIfFileExists.equalsIgnoreCase("skip"))) {
        throw new RuntimeException(
            "Incorrect actionIfFileExists. It should be rename,replace or skip");
      }
      boolean targetFolderExists = false;
      if (sftpClient.stat(sourceDirectoryPath.toString()) == null) {
        throw new IOException("Source file does not exists");
      }
      if (sftpClient.stat(targetDirectoryPath.toString()) == null) {
        targetFolderExists = false;
      } else targetFolderExists = true;
      boolean checkSourceInTarget = false;
      if (targetFolderExists) {
        LOGGER.info("Checking if the file is already present or not");
        Path remoteSourceFile =
            Path.of(
                targetDirectoryPath.toString()
                    + File.separator
                    + sourceDirectoryPath.getFileName().toString());
        checkSourceInTarget = checkIfFileExists(sftpClient, remoteSourceFile);
      } else if (!targetFolderExists) {
        try {
          sftpClient.mkdir(targetDirectoryPath.toString());
          LOGGER.info("New Folder created successfully" + targetDirectoryPath.toString());
        } catch (IOException e) {
          LOGGER.info("Folder creation failed");
          throw new Exception("Can not create targetDirectory");
        }
      }
      if (!checkSourceInTarget) {
        Path targetFile = Path.of(targetDirectory, sourceDirectoryPath.getFileName().toString());
        sftpClient.rename(sourceDirectoryPath.toString(), targetFile.toString());
        moveFileResponse = new SFTPResponse<>("File Moved Successfully");
        LOGGER.info("File Moved Successfully");
      } else {
        if (actionIfFileExists == null || actionIfFileExists.isBlank()) {
          throw new Exception("Action file should be rename,replace or skip");
        }
        if (actionIfFileExists.equalsIgnoreCase("rename")) {
          LOGGER.info("rename flow started");
          String newFileName = sourceDirectoryPath.getFileName().toString();
          String fileName = newFileName.split("\\.")[0];
          String extension = newFileName.split("\\.")[1];
          newFileName = fileName + " - Copy." + extension;
          Path newPath = Path.of(targetDirectory.toString() + File.separator + newFileName);
          Integer count = 2;
          Path newRenamePath = renameFile.renameFileUtil(sftpClient, newPath, count);
          sftpClient.rename(sourceDirectoryPath.toString(), newRenamePath.toString());
          LOGGER.info(
              "File name successfully changed and file is moved to its targetDirectory"
                  + newRenamePath.toString());
          moveFileResponse =
              new SFTPResponse<>(
                  "File renamed and moved Successfully with new name as"
                      + newPath.getFileName().toString());

        } else if (actionIfFileExists.equalsIgnoreCase("replace")) {
          String newTargetFile =
              targetDirectory + File.separator + sourceDirectoryPath.getFileName().toString();
          sftpClient.rm(newTargetFile);
          sftpClient.rename(sourceDirectoryPath.toString(), newTargetFile);
          moveFileResponse =
              new SFTPResponse<>("File replaced successfully" + newTargetFile.toString());
        } else if (actionIfFileExists.equalsIgnoreCase("skip")) {
          LOGGER.info("File skipped successfully");
          moveFileResponse = new SFTPResponse<>("File skipped successfully");
        }
      }
      return moveFileResponse;

    } catch (Exception e) {
      throw new RuntimeException(e.getLocalizedMessage());
    } finally {
      try {
        if (sftpClient != null) sftpClient.close();
      } catch (IOException e) {
        LOGGER.error("Some Error occurred while trying to close the SFTPClient");
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

  public Path renameFileUtil(SFTPClient sftpClient, Path newFilePath) {
    try {
      if (sftpClient.stat(newFilePath.toString()) == null) {
        return newFilePath;
      }
    } catch (Exception e) {
      return newFilePath;
    }

    String newFileName = newFilePath.getFileName().toString();
    String fileName = newFileName.split("\\.")[0];
    String extension = newFileName.split("\\.")[1];
    newFileName = fileName + " - Copy." + extension;
    newFilePath = Path.of(newFilePath.getParent() + File.separator + newFileName);
    return renameFileUtil(sftpClient, newFilePath);
  }

  public String getSourceFilePath() {
    return sourceFilePath;
  }

  public void setSourceFilePath(String sourceFilePath) {
    this.sourceFilePath = sourceFilePath;
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

  public SFTPResponse<?> getMoveFileResponse() {
    return moveFileResponse;
  }

  public void setMoveFileResponse(SFTPResponse<?> moveFileResponse) {
    this.moveFileResponse = moveFileResponse;
  }

  @Override
  public int hashCode() {
    return Objects.hash(actionIfFileExists, sourceFilePath, targetDirectory);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MoveFileService other = (MoveFileService) obj;
    return Objects.equals(actionIfFileExists, other.actionIfFileExists)
        && Objects.equals(sourceFilePath, other.sourceFilePath)
        && Objects.equals(targetDirectory, other.targetDirectory);
  }

  @Override
  public String toString() {
    return "MoveFileService [sourceFilePath="
        + sourceFilePath
        + ", targetDirectory="
        + targetDirectory
        + ", actionIfFileExists="
        + actionIfFileExists
        + ", moveFileResponse="
        + moveFileResponse
        + "]";
  }
}
