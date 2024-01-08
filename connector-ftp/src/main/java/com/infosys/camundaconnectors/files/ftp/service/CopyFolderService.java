/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequestData;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyFolderService implements FTPRequestData {

  private static final Logger LOGGER = LoggerFactory.getLogger(CopyFolderService.class);
  @NotBlank private String sourcePath;
  @NotBlank private String targetPath;
  @NotBlank private String actionIfFolderExists;
  @NotBlank private String createTargetFolder;

  public String getSourcePath() {
    return this.sourcePath;
  }

  public void setSourcePath(String path) {
    this.sourcePath = path;
  }

  public String getTargetPath() {
    return this.targetPath;
  }

  public void setTargetPath(String path) {
    this.targetPath = path;
  }

  public String getActionIfFolderExists() {
    return this.actionIfFolderExists;
  }

  public void setActionIfFolderExists(String action) {
    this.actionIfFolderExists = action;
  }

  public String getCreateTargetFolder() {
    return this.createTargetFolder;
  }

  public void setCreateTargetFolder(String path) {
    this.createTargetFolder = path;
  }

  private boolean checkValidPath(String folderPath, FTPClient client) throws IOException {
    return client.changeWorkingDirectory(folderPath);
  }

  private boolean checkFolderExists(String path, String srcFolderName, FTPClient client)
      throws IOException {
    return checkValidPath(path + File.separator + srcFolderName, client);
  }

  private static void copyFolder(
      FTPClient srcClient, FTPClient destClient, String srcFolderPath, String targetFolderPath)
      throws IOException, RuntimeException {
    FTPFile[] files = srcClient.listFiles(srcFolderPath);
    for (FTPFile file : files) {
      String srcFilePath = srcFolderPath + File.separator + file.getName();
      String targetFilePath = targetFolderPath + File.separator + file.getName();
      if (file.isFile()) {
        InputStream inputStream = srcClient.retrieveFileStream(srcFilePath);
        OutputStream outputStream = destClient.storeFileStream(targetFilePath);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
        boolean startSuccess = srcClient.completePendingCommand();
        boolean endSuccess = destClient.completePendingCommand();
        if (!startSuccess || !endSuccess) {
          throw new RuntimeException("Failed to copy Folder.");
        }
      } else if (file.isDirectory()) {
        srcClient.makeDirectory(targetFilePath);
        copyFolder(srcClient, destClient, srcFilePath, targetFilePath);
      }
    }
  }

  private void removeFolder(String folderPath, FTPClient client) throws IOException {
    FTPFile[] files = client.listFiles(folderPath);
    for (FTPFile file : files) {
      if (file.isFile()) {
        client.deleteFile(folderPath + File.separator + file.getName());
      } else {
        removeFolder(folderPath + File.separator + file.getName(), client);
      }
    }
    client.removeDirectory(folderPath);
  }

  public FTPResponse<String> invoke(FTPClient srcClient, FTPClient destClient) {

    try {
      // System.out.println(File.separator);
      srcClient.changeWorkingDirectory(File.separator);
      destClient.changeWorkingDirectory(File.separator);
      LOGGER.info(sourcePath);
      LOGGER.info(targetPath);
      if (!checkValidPath(sourcePath, srcClient)) {
        throw new RuntimeException("Invalid sourcePath!!");
      } else if (!checkValidPath(targetPath, destClient)) {
        if (createTargetFolder.equals("true")) {
          destClient.changeWorkingDirectory(File.separator);
          destClient.makeDirectory(targetPath);
        } else {
          throw new RuntimeException("Target Folder doesn't exist!!");
        }
      } else {
        LOGGER.info("All Set!!");
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException("Error: " + e.getMessage());
    }
    try {
      srcClient.enterLocalPassiveMode();
      srcClient.setFileType(FTP.BINARY_FILE_TYPE);
      destClient.enterLocalPassiveMode();
      destClient.setFileType(FTP.BINARY_FILE_TYPE);
      String remoteTargetPath;
      String srcFolderName = "";
      for (int i = 0; i < sourcePath.length(); i++) {
        if (Character.toString(sourcePath.charAt(i)).equals(File.separator)) {
          srcFolderName = "";
        } else {
          srcFolderName += sourcePath.charAt(i);
        }
      }
      try {
        if (checkFolderExists(targetPath, srcFolderName, destClient)) {
          if (actionIfFolderExists.equals(null) || actionIfFolderExists.equals("rename")) {
            String folderName = srcFolderName;
            int cnt = 1;
            String remoteFolderName = folderName + "(" + Integer.toString(cnt) + ")";
            while (checkFolderExists(targetPath, remoteFolderName, destClient)) {
              cnt++;
              remoteFolderName = folderName + "(" + Integer.toString(cnt) + ")";
            }
            remoteTargetPath = targetPath + File.separator + remoteFolderName;
            destClient.makeDirectory(remoteTargetPath);
          } else if (actionIfFolderExists.equals("replace")) {
            remoteTargetPath = targetPath + File.separator + srcFolderName;
            destClient.changeWorkingDirectory(File.separator);
            removeFolder(remoteTargetPath, destClient);
            destClient.makeDirectory(remoteTargetPath);
          } else {
            LOGGER.info("Operation skipped!!");
            return new FTPResponse<>("Operation skipped!!");
          }
        } else {
          remoteTargetPath = targetPath + File.separator + srcFolderName;
          destClient.makeDirectory(remoteTargetPath);
        }
      } catch (Exception e) {
        throw new RuntimeException("Error: Couldn't process!!");
      }
      if (!srcClient.changeWorkingDirectory(sourcePath)) {
        // LOGGER.error("Source folder doesn't exist");
        throw new RuntimeException("Source folder doesn't exist!!");
      }
      if (!destClient.changeWorkingDirectory(targetPath)) {
        destClient.makeDirectory(targetPath);
      }
      copyFolder(srcClient, destClient, sourcePath, remoteTargetPath);
      boolean startSuccess = srcClient.completePendingCommand();
      boolean endSuccess = destClient.completePendingCommand();
      LOGGER.info("strt:" + startSuccess + "target:" + endSuccess);
      if (startSuccess && endSuccess) {
        LOGGER.info("Folder copied successfully.");
        return new FTPResponse<>("Folder copied successfully.");
      } else {
        throw new RuntimeException("Failed to copy file.");
      }
    } catch (Exception ex) {
      LOGGER.error("Error: " + ex.getMessage());
      throw new RuntimeException("Error: " + ex.getMessage());
    } finally {
      try {
        LOGGER.info("" + srcClient.isConnected());
        if (srcClient.isConnected()) {
          srcClient.logout();
          srcClient.disconnect();
          LOGGER.debug("Successfully disconnected from the server");
        }
        LOGGER.info("" + destClient.isConnected());
        if (destClient.isConnected()) {
          destClient.logout();
          destClient.disconnect();
          LOGGER.debug("Successfully disconnected from the server");
        }
      } catch (IOException ex) {
        LOGGER.error("Failed to disconnect from the server!!");
      }
    }
  }

  @Override
  public FTPResponse<String> invoke(FTPClient client) {
    return null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(actionIfFolderExists, createTargetFolder, sourcePath, targetPath);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CopyFolderService other = (CopyFolderService) obj;
    return Objects.equals(actionIfFolderExists, other.actionIfFolderExists)
        && Objects.equals(createTargetFolder, other.createTargetFolder)
        && Objects.equals(sourcePath, other.sourcePath)
        && Objects.equals(targetPath, other.targetPath);
  }
}
