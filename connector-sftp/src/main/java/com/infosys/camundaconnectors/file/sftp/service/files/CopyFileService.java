/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.service.files;

import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import com.infosys.camundaconnectors.file.sftp.utility.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyFileService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CopyFileService.class);

  @NotBlank private String createNewFolderIfNotExists;
  @NotBlank private String sourceFilePath;
  @NotBlank private String targetDirectory;
  @NotBlank private String actionIfFileExists;

  private boolean createNewFolder;
  SFTPResponse<?> moveFileResponse;
  RenameFileUtil renameFile = new RenameFileUtil();

  public Response invoke(SFTPClient sftpClient) {
    Path sourceDirectoryPath = Path.of(sourceFilePath);
    Path targetDirectoryPath = Path.of(targetDirectory);

    if (createNewFolderIfNotExists.equalsIgnoreCase("false")) createNewFolder = false;
    else createNewFolder = true;
    try {
      boolean targetFolderExists = true;
      LOGGER.info("Checking if source file already exists or not");
      boolean sourceFilePathFlag = checkIfFileExists(sftpClient, sourceDirectoryPath);
      if (!sourceFilePathFlag) throw new IOException("Source file does not exists");
      LOGGER.info("Checking if target folder already exists or not");
      targetFolderExists = checkIfFileExists(sftpClient, targetDirectoryPath);
      boolean checkSourceInTarget = true;
      if (targetFolderExists) {
        LOGGER.info(
            "Checking if the file which is getting copied is already present or not in targetDirectory");
        Path remoteSourceFile =
            Path.of(targetDirectory, sourceDirectoryPath.getFileName().toString());
        checkSourceInTarget = checkIfFileExists(sftpClient, remoteSourceFile);

      } else if (createNewFolder && !targetFolderExists) {
        try {
          sftpClient.mkdir(targetDirectoryPath.toString().replace("\\", "/"));
          LOGGER.info("New Folder created successfully" + targetDirectoryPath.toString());
        } catch (IOException e) {
          LOGGER.info("Folder creation failed");
          throw new Exception("Can not create targetDirectory");
        }
      }
      if (!checkSourceInTarget) {
        InputStream inputStream = readFile(sftpClient, sourceDirectoryPath);
        OutputStream outputStream = writeFile(sftpClient, targetDirectoryPath, sourceDirectoryPath);
        IOUtils.copy(inputStream, outputStream);
        moveFileResponse = new SFTPResponse<>("File Copied Successfully");
        LOGGER.info("File copied Successfully");
      } else {
        if (actionIfFileExists == null || actionIfFileExists.isBlank()) {
          throw new Exception("Action file should be rename,replace or skip");
        }
        if (actionIfFileExists.equalsIgnoreCase("rename")) {
          LOGGER.info("rename flow started at");
          String newFileName = sourceDirectoryPath.getFileName().toString();
          String fileName = "";
          String extension = "";
          fileName = newFileName.split("\\.")[0];
          extension = newFileName.split("\\.")[1];
          newFileName = fileName + " - Copy." + extension;
          String newFilePath = targetDirectoryPath.toString();
          Path newRenameFilePath = Path.of(newFilePath, newFileName);
          Integer count = 2;
          Path newRenameFileName = renameFile.renameFileUtil(sftpClient, newRenameFilePath, count);
          InputStream inputStream = readFile(sftpClient, sourceDirectoryPath);
          OutputStream outputStream = writeFile(sftpClient, targetDirectoryPath, newRenameFileName);
          IOUtils.copy(inputStream, outputStream);
          LOGGER.info(
              "File name successfully changed and file is copied to its targetDirectory"
                  + newRenameFilePath.toString());
          moveFileResponse =
              new SFTPResponse<>(
                  "File renamed and copied Successfully with new name as "
                      + newRenameFilePath.toString());
        } else if (actionIfFileExists.equalsIgnoreCase("replace")) {
          LOGGER.info("Replacing started");
          Path newTargetFile =
              Path.of(targetDirectory, sourceDirectoryPath.getFileName().toString());
          sftpClient.rm(newTargetFile.toString().replace("\\", "/"));
          InputStream inputStream = readFile(sftpClient, sourceDirectoryPath);
          OutputStream outputStream =
              writeFile(sftpClient, targetDirectoryPath, sourceDirectoryPath);
          IOUtils.copy(inputStream, outputStream);
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
      if (sftpClient.stat(newFilePath.toString().replace("\\", "/")) == null) {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public InputStream readFile(SFTPClient sftpClient, Path sourceFilePath) throws IOException {
    RemoteFile file;
    file =
        sftpClient
            .getSFTPEngine()
            .open(sourceFilePath.toString().replace("\\", "/"), EnumSet.of(OpenMode.READ));
    InputStream is =
        file.new RemoteFileInputStream() {

          @Override
          public void close() throws IOException {
            try {
              super.close();
            } finally {
              file.close();
            }
          }
        };
    return is;
  }

  public OutputStream writeFile(
      SFTPClient sftpClient, Path targetDirectoryPath, Path sourceDirectoryPath)
      throws IOException {
    Path targetFile =
        Path.of(targetDirectoryPath.toString(), sourceDirectoryPath.getFileName().toString());

    RemoteFile file =
        sftpClient
            .getSFTPEngine()
            .open(
                targetFile.toString().replace("\\", "/"),
                EnumSet.of(OpenMode.CREAT, OpenMode.WRITE));
    OutputStream os =
        file.new RemoteFileOutputStream(0, 10) {

          @Override
          public void close() throws IOException {
            try {
              super.close();
            } finally {
              file.close();
            }
          }
        };
    return os;
  }

  public String getCreateNewFolderIfNotExists() {
    return createNewFolderIfNotExists;
  }

  public void setCreateNewFolderIfNotExists(String createNewFolderIfNotExists) {
    this.createNewFolderIfNotExists = createNewFolderIfNotExists;
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

  public boolean isCreateNewFolder() {
    return createNewFolder;
  }

  public void setCreateNewFolder(boolean createNewFolder) {
    this.createNewFolder = createNewFolder;
  }

  public SFTPResponse<?> getMoveFileResponse() {
    return moveFileResponse;
  }

  public void setMoveFileResponse(SFTPResponse<?> moveFileResponse) {
    this.moveFileResponse = moveFileResponse;
  }

  public RenameFileUtil getRenameFile() {
    return renameFile;
  }

  public void setRenameFile(RenameFileUtil renameFile) {
    this.renameFile = renameFile;
  }

  public static Logger getLogger() {
    return LOGGER;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        actionIfFileExists, createNewFolderIfNotExists, sourceFilePath, targetDirectory);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CopyFileService other = (CopyFileService) obj;
    return Objects.equals(actionIfFileExists, other.actionIfFileExists)
        && Objects.equals(createNewFolderIfNotExists, other.createNewFolderIfNotExists)
        && Objects.equals(sourceFilePath, other.sourceFilePath)
        && Objects.equals(targetDirectory, other.targetDirectory);
  }

  @Override
  public String toString() {
    return "CopyFileService [createNewFolderIfNotExists="
        + createNewFolderIfNotExists
        + ", sourceFilePath="
        + sourceFilePath
        + ", targetDirectory="
        + targetDirectory
        + ", actionIfFileExists="
        + actionIfFileExists
        + "]";
  }
}
