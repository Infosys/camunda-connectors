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
import java.util.Objects;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateFolderService implements FTPRequestData {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateFolderService.class);
  @NotBlank private String parentFolderPath;
  @NotBlank private String folderName;
  @NotBlank private String actionIfFolderExists;

  public String getParentFolderPath() {
    return this.parentFolderPath;
  }

  public void setParentFolderPath(String path) {
    this.parentFolderPath = path;
  }

  public String getFolderName() {
    return this.folderName;
  }

  public void setFolderName(String name) {
    this.folderName = name;
  }

  public String getActionIfFolderExists() {
    return this.actionIfFolderExists;
  }

  public void setActionIfFolderExists(String path) {
    this.actionIfFolderExists = path;
  }

  private boolean checkFolderExists(String folderPath, FTPClient client) throws IOException {
    return client.changeWorkingDirectory(folderPath);
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

  public FTPResponse<String> invoke(FTPClient client) {
    try {
      client.changeWorkingDirectory(File.separator);
      String remoteFolderPath;
      if (checkFolderExists(parentFolderPath + File.separator + folderName, client)) {
        if (actionIfFolderExists.equals(null) || actionIfFolderExists.equals("rename")) {
          String name = folderName;
          int cnt = 1;
          String remoteFolderName = name + "(" + Integer.toString(cnt) + ")";
          while (checkFolderExists(parentFolderPath + File.separator + remoteFolderName, client)) {
            cnt++;
            remoteFolderName = name + "(" + Integer.toString(cnt) + ")";
          }
          remoteFolderPath = parentFolderPath + File.separator + remoteFolderName;
        } else if (actionIfFolderExists.equals("replace")) {
          remoteFolderPath = parentFolderPath + File.separator + folderName;
          client.changeWorkingDirectory(File.separator);
          removeFolder(remoteFolderPath, client);
        } else {
          LOGGER.info("Operation skipped!!");
          return new FTPResponse<>("Operation skipped!!");
        }
      } else {
        remoteFolderPath = parentFolderPath + File.separator + folderName;
      }
      boolean isCreated = client.makeDirectory(remoteFolderPath);
      LOGGER.info("Created ? : " + isCreated);
      LOGGER.info("Folder Successfully created!!");
      return new FTPResponse<>("New Folder Successfully created!!");
    } catch (Exception e) {
      LOGGER.error("Error in creating new folder : " + e.getMessage());
      throw new RuntimeException("Error: " + e.getMessage());
    } finally {
      try {
        if (client.isConnected()) {
          client.logout();
          client.disconnect();
          LOGGER.debug("Successfully disconnected from the server");
        }
      } catch (IOException ex) {
        LOGGER.error("Failed to disconnect from the server!!");
      }
    }
  }

  @Override
  public FTPResponse<String> invoke(FTPClient client1, FTPClient client2) {
    return null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(actionIfFolderExists, parentFolderPath, folderName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CreateFolderService other = (CreateFolderService) obj;
    return Objects.equals(actionIfFolderExists, other.actionIfFolderExists)
        && Objects.equals(parentFolderPath, other.parentFolderPath)
        && Objects.equals(folderName, other.folderName);
  }
}
