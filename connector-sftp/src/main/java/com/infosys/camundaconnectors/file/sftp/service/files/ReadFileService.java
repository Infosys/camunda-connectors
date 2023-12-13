/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.service.files;

import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;

import jakarta.validation.constraints.NotBlank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadFileService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReadFileService.class);
  @NotBlank private String sourceFilePath;
  Map<String, Object> fileDetail = new TreeMap<>();
  List<Map<String, Object>> response = new ArrayList<>();
  SFTPResponse<?> readFileResponse;

  public Response invoke(SFTPClient sftpClient) {
    try {
      if (sourceFilePath == null || sourceFilePath.isBlank() || sourceFilePath.isEmpty())
        throw new IOException("Source file is null");
      String sourceDirectoryPath = sourceFilePath.replace("\\", "/");
      LOGGER.info("Checking if source file already exists or not");
      boolean sourceFilePathFlag = checkIfFileExists(sftpClient, sourceDirectoryPath);
      if (!sourceFilePathFlag) throw new IOException("Source file does not exists");
      if (sftpClient.size(sourceDirectoryPath.toString()) > 32766) {
        LOGGER.error("cannot read a file having size greater than 32766 bytes");
        throw new RuntimeException("File size is greater than 32766 bytes");
      }

      InputStream inputStream = readFile(sftpClient, sourceDirectoryPath);
      InputStreamReader isr = new InputStreamReader(inputStream);
      BufferedReader br = new BufferedReader(isr);
      StringBuffer sb = new StringBuffer();
      String str;
      while ((str = br.readLine()) != null) {
        sb.append(str);
      }
      String readFile = sb.toString();
      readFileResponse = new SFTPResponse<>(readFile);
    } catch (Exception e) {
      throw new RuntimeException(e.getLocalizedMessage());
    } finally {
      try {
        if (sftpClient != null) sftpClient.close();
      } catch (IOException e) {
        LOGGER.error("Some Error occurred while trying to close the SFTPClient");
      }
    }
    return readFileResponse;
  }

  public boolean checkIfFileExists(SFTPClient sftpClient, String newFilePath) {
    try {
      if (sftpClient.stat(newFilePath) == null) {}
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public InputStream readFile(SFTPClient sftpClient, String sourceFilePath) throws IOException {
    RemoteFile file = sftpClient.getSFTPEngine().open(sourceFilePath, EnumSet.of(OpenMode.READ));
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

  public String getSourceFilePath() {
    return sourceFilePath;
  }

  public void setSourceFilePath(String sourceFilePath) {
    this.sourceFilePath = sourceFilePath;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceFilePath);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ReadFileService other = (ReadFileService) obj;
    return Objects.equals(sourceFilePath, other.sourceFilePath);
  }

  @Override
  public String toString() {
    return "ReadFileService [sourceFilePath=" + sourceFilePath + "]";
  }
}
