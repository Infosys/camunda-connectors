/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.service.folders;

import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import com.infosys.camundaconnectors.file.sftp.utility.*;
import java.io.File;
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

public class CopyFolderService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CopyFolderService.class);

  @NotBlank private String sourceDirectory;
  @NotBlank private String targetDirectory;
  @NotBlank private String actionIfFileExists;
  SFTPResponse<?> copyFolderResponse;

  RenameFolderUtil renameFolderUtil = new RenameFolderUtil();

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
      boolean targetFolderExists = true;
      LOGGER.info("Checking if source directory already exists or not");
      boolean sourceFilePathFlag = checkIfFileExists(sftpClient, sourceDirectoryPath);
      if (!sourceFilePathFlag) throw new IOException("Source file does not exists");
      LOGGER.info("Checking if target folder already exists or not");
      targetFolderExists = checkIfFileExists(sftpClient, targetDirectoryPath);
      Boolean copiedFolder = false;
      if (!targetFolderExists) {
        sftpClient.mkdir(targetDirectoryPath.toString());
      } else {
        Path newPath =
            Path.of(
                targetDirectory.toString()
                    + File.separator
                    + sourceDirectoryPath.getFileName().toString());
        copiedFolder = checkIfFileExists(sftpClient, newPath);
      }
      if (!copiedFolder) {
        LOGGER.info("Create new Folder with the same name as sourceDirectory");
        Path newPath =
            Path.of(
                targetDirectory.toString()
                    + File.separator
                    + sourceDirectoryPath.getFileName().toString());
        sftpClient.mkdir(newPath.toString());
        LOGGER.info("Copying folder");
        copyFolder(sftpClient, sourceDirectoryPath, newPath);
        LOGGER.info("Folder copied successfully");
        copyFolderResponse = new SFTPResponse<>("Folder copied Successfully");
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
          sftpClient.mkdir(newPath.toString());
          copyFolder(sftpClient, sourceDirectoryPath, newPath);
          LOGGER.info(
              "Folder name successfully changed and file is copied to its targetDirectory"
                  + newPath.toString());
          copyFolderResponse =
              new SFTPResponse<>("Folder renamed and copied Successfully with new name as ");
        } else if (actionIfFileExists.equalsIgnoreCase("replace")) {
          LOGGER.info("Replacing started");
          Path newPath =
              Path.of(
                  targetDirectory.toString()
                      + File.separator
                      + sourceDirectoryPath.getFileName().toString());
          deleteDirectoryRecursively(sftpClient, newPath);
          sftpClient.rmdir(newPath.toString());
          LOGGER.info("Folder Removed Successfully");
          sftpClient.mkdir(newPath.toString());
          copyFolder(sftpClient, sourceDirectoryPath, newPath);
          LOGGER.info("Folder replaced successfully");
          copyFolderResponse = new SFTPResponse<>("File replaced and copied successfully");
        } else if (actionIfFileExists.equalsIgnoreCase("skip")) {
          LOGGER.info("Folder Skipped Successfully");
          copyFolderResponse = new SFTPResponse<>("Folder Skipped successfully");
        }
      }

    } catch (Exception e) {
      throw new RuntimeException(e.getLocalizedMessage());
    } finally {
      try {
        if (sftpClient != null) sftpClient.close();
      } catch (IOException e) {
        LOGGER.error("Some Error occurred while trying to close the SFTPClient");
      }
    }
    return copyFolderResponse;
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

  public void copyFolder(SFTPClient sftpClient, Path sourceDirectoryPath, Path targetDirectoryPath)
      throws IOException {
    sftpClient
        .ls(sourceDirectoryPath.toString())
        .forEach(
            file -> {
              if (file.isDirectory()) {
                Path subSourcePath = Path.of(sourceDirectoryPath.toString(), file.getName());
                Path subDestPath = Path.of(targetDirectoryPath.toString(), file.getName());

                try {
                  sftpClient.mkdir(subDestPath.toString());
                } catch (IOException e) {
                  throw new RuntimeException(
                      "Some error occurred while trying to create a directory");
                }

                try {
                  copyFolder(sftpClient, subSourcePath, subDestPath);
                } catch (IOException e) {
                  throw new RuntimeException(
                      "Some error occurred while trying to copy a directory");
                }
              } else {

                Path sourceFile = Path.of(sourceDirectoryPath.toString(), file.getName());
                Path destPathFile = (Path.of(targetDirectoryPath.toString()));
                try {
                  copyFile(sftpClient, sourceFile, destPathFile);
                } catch (IOException e) {
                  throw new RuntimeException("Some error occurred while trying to copy a file");
                }
              }
            });
  }

  public void copyFile(SFTPClient sftpClient, Path sourceFile, Path destPathFile)
      throws IOException {

    InputStream inputStream = readFile(sftpClient, sourceFile);
    OutputStream outputStream = writeFile(sftpClient, destPathFile, sourceFile);
    IOUtils.copy(inputStream, outputStream);
  }

  // Read file into InputStream
  public InputStream readFile(SFTPClient sftpClient, Path sourceFilePath) throws IOException {
    RemoteFile file =
        sftpClient.getSFTPEngine().open(sourceFilePath.toString(), EnumSet.of(OpenMode.READ));
    //
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
            .open(targetFile.toString(), EnumSet.of(OpenMode.CREAT, OpenMode.WRITE));
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
                }
              } else {
                try {
                  sftpClient.rm(fullAdd.toString());
                } catch (IOException e) {
                  throw new RuntimeException("Some error occurred while trying to delete folder");
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
    CopyFolderService other = (CopyFolderService) obj;
    return Objects.equals(actionIfFileExists, other.actionIfFileExists)
        && Objects.equals(sourceDirectory, other.sourceDirectory)
        && Objects.equals(targetDirectory, other.targetDirectory);
  }

  @Override
  public String toString() {
    return "CopyFolderService [sourceDirectory="
        + sourceDirectory
        + ", targetDirectory="
        + targetDirectory
        + ", actionIfFileExists="
        + actionIfFileExists
        + "]";
  }
}
