/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.service.files;

import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import com.infosys.camundaconnectors.file.sftp.utility.FileSortCondition;

import jakarta.validation.constraints.NotBlank;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListFilesService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ListFilesService.class);
  private Map<String, String> sortBy;
  private String fileNamePattern;
  private String modifiedBefore;
  private String modifiedAfter;
 private String searchSubFoldersAlso;
  private String maxNumberOfFiles;
  private String maxDepth;
  private String outputType;
  @NotBlank private String filePath;
  private boolean searchSubFolders;
  SFTPResponse<?> listFilesResponse;
  List<String> files = new ArrayList<>();
  List<Map<String, Object>> fileDetails = new ArrayList<>();
  List<RemoteResourceInfo> filesArr = new ArrayList<>();

  //  @SuppressWarnings("unchecked")
  @Override
  public Response invoke(SFTPClient sftpClient) {
    if (fileNamePattern == null || fileNamePattern.isBlank()) fileNamePattern = ".*";
    if (searchSubFoldersAlso == null) searchSubFolders = false;
    else if (searchSubFoldersAlso.equalsIgnoreCase("True")) searchSubFolders = true;
    else searchSubFolders = false;
    if (outputType == null
        || !outputType.equalsIgnoreCase("filePaths") && !outputType.equalsIgnoreCase("fileDetails"))
      outputType = "filePaths";
    if (maxDepth == null || maxDepth.isBlank()) maxDepth = Integer.toString(1);
    if (maxNumberOfFiles == null || maxNumberOfFiles.isBlank()) maxNumberOfFiles = "100";
    if (modifiedAfter == null || modifiedAfter.isBlank() || modifiedAfter.isEmpty())
      modifiedAfter = null;
    if (modifiedBefore == null || modifiedBefore.isBlank() || modifiedBefore.isEmpty())
      modifiedBefore = null;
    filePath = Path.of(filePath).toString().replace("\\", "/");
    Date modifiedAfterDate = new Date();
    Date modifiedBeforeDate = new Date();
    SimpleDateFormat df = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
    df.setTimeZone(TimeZone.getTimeZone("IST"));

    if (modifiedAfter != null) {
      try {
        modifiedAfterDate = df.parse(modifiedAfter);
      } catch (Exception e) {
        throw new RuntimeException("InvalidDateModifiedAfter: InvalidDateModifiedAfter");
      }
    }
    if (modifiedBefore != null) {
      try {
        modifiedBeforeDate = df.parse(modifiedBefore);
      } catch (Exception e) {
        throw new RuntimeException("InvalidDateModifiedBefore: InvalidDateModifiedBefore");
      }
    }
    if (modifiedAfter != null && modifiedBefore != null) {
      try {

        ListFilesAccordingToModifiedAfterAndModifiedBefore(
            sftpClient,
            filePath,
            modifiedBeforeDate,
            modifiedAfterDate,
            Integer.parseInt(maxDepth));
      } catch (NumberFormatException | IOException | ParseException e) {
        throw new RuntimeException("Some error occurred while trying to list files-1");
      }
    } else if (modifiedAfter != null) {
      try {
        ListFilesAccordingToModifiedAfter(
            sftpClient, filePath, modifiedAfterDate, Integer.parseInt(maxDepth));
      } catch (NumberFormatException | IOException | ParseException e) {
        throw new RuntimeException("Some error occurred while trying to list files-2");
      }
    } else if (modifiedBefore != null) {
      try {
        ListFilesAccordingToModifiedBefore(
            sftpClient, filePath, modifiedBeforeDate, Integer.parseInt(maxDepth));
      } catch (NumberFormatException | IOException | ParseException e) {
        throw new RuntimeException("Some error occurred while trying to list files-3");
      }
    } else {
      try {
        ListFiles(sftpClient, filePath, Integer.parseInt(maxDepth));
      } catch (NumberFormatException | IOException | ParseException e) {
        throw new RuntimeException(e.getMessage());
      }
    }
    try {
      if (sortBy != null
          && !sortBy.isEmpty()
          && (sortBy.get("sortOn") != null)
          && (sortBy.get("order") != null)) {
        FileSortCondition sortCond = new FileSortCondition();
        sortCond.sortFiles(filesArr, sortBy);
      }

    } catch (Exception e) {
      LOGGER.debug("Some error occurred while trying to sort files");
      throw new RuntimeException("IOException" + e.getLocalizedMessage());
    }

    if (outputType.equalsIgnoreCase("fileDetails")) {
      for (RemoteResourceInfo remoteResourceInfo : filesArr) {
        Map<String, Object> fileDetail = new TreeMap<>();
        fileDetail.put("FileName", remoteResourceInfo.getName());
        fileDetail.put("Parent", remoteResourceInfo.getParent());
        fileDetail.put("Path", remoteResourceInfo.getPath());
        fileDetail.put("Size", remoteResourceInfo.getAttributes().getSize());
        fileDetail.put("Time", getDateFromEpoch(remoteResourceInfo.getAttributes().getMtime()));
        fileDetails.add(fileDetail);
      }
    } else {
      for (RemoteResourceInfo remoteResourceInfo : filesArr) {
        files.add(remoteResourceInfo.getName());
      }
    }
    try {
      if (outputType.equalsIgnoreCase("fileDetails")) {
        listFilesResponse =
            new SFTPResponse<>(
                fileDetails.stream()
                    .limit(Long.parseLong(maxNumberOfFiles))
                    .collect(Collectors.toList()));
        checkResponseSize(listFilesResponse);
      } else if (outputType.equalsIgnoreCase("filePaths"))
        listFilesResponse =
            new SFTPResponse<>(
                files.stream()
                    .limit(Long.parseLong(maxNumberOfFiles))
                    .collect(Collectors.toList()));
      checkResponseSize(listFilesResponse);

    } catch (Exception e) {
      throw new RuntimeException(e.getLocalizedMessage());
    } finally {

      try {
        if (sftpClient != null) sftpClient.close();
      } catch (IOException e) {
        LOGGER.error("Some error occurred while trying to close SFTPClient");
      }
    }
    if (listFilesResponse != null)
      LOGGER.info("List Files Status: {}", listFilesResponse.getResponse());

    return listFilesResponse;
  }

  public void ListFilesAccordingToModifiedAfterAndModifiedBefore(
      SFTPClient sftpClient,
      String folderPath,
      Date modifiedBefore,
      Date modifiedAfter,
      int maxDepth)
      throws IOException, ParseException {

    Queue<RemoteResourceInfo> q = new LinkedList<>();
    for (RemoteResourceInfo remoteResourceInfo : sftpClient.ls(folderPath)) {
      Date date = new Date(remoteResourceInfo.getAttributes().getMtime() * 1000);
      SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
      sdf.setTimeZone(TimeZone.getTimeZone("IST"));
      Date ModifiedFileDate = sdf.parse(sdf.format(date));
      if (ModifiedFileDate.compareTo(modifiedAfter) > 0
          && ModifiedFileDate.compareTo(modifiedBefore) < 0) q.add(remoteResourceInfo);
    }

    while (!q.isEmpty()) {
      int n = q.size();
      if (maxDepth == 0) break;
      for (int i = 0; i < n; i++) {
        RemoteResourceInfo remoteResourceInfo = q.poll();
        if (remoteResourceInfo.isDirectory()) {
          for (RemoteResourceInfo subFolders : sftpClient.ls(remoteResourceInfo.getPath())) {
            Date date = new Date(subFolders.getAttributes().getMtime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
            Date ModifiedFileDate = sdf.parse(sdf.format(date));
            if (ModifiedFileDate.compareTo(modifiedAfter) > 0
                && ModifiedFileDate.compareTo(modifiedBefore) < 0) q.add(subFolders);
          }
        } else {
          if (remoteResourceInfo.getName().matches(fileNamePattern))
            filesArr.add(remoteResourceInfo);
        }
      }
      maxDepth--;
    }
  }

  public void ListFilesAccordingToModifiedAfter(
      SFTPClient sftpClient, String folderPath, Date modifiedAfter, int maxDepth)
      throws IOException, ParseException {

    Queue<RemoteResourceInfo> q = new LinkedList<>();
    for (RemoteResourceInfo remoteResourceInfo : sftpClient.ls(folderPath)) {
      Date date = new Date(remoteResourceInfo.getAttributes().getMtime() * 1000);
      SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
      sdf.setTimeZone(TimeZone.getTimeZone("IST"));
      Date ModifiedFileDate = sdf.parse(sdf.format(date));
      if (ModifiedFileDate.compareTo(modifiedAfter) > 0) q.add(remoteResourceInfo);
    }

    while (!q.isEmpty()) {
      int n = q.size();
      if (maxDepth == 0) break;
      for (int i = 0; i < n; i++) {
        RemoteResourceInfo remoteResourceInfo = q.poll();
        if (remoteResourceInfo.isDirectory()) {
          for (RemoteResourceInfo subFolders : sftpClient.ls(remoteResourceInfo.getPath())) {
            Date date = new Date(subFolders.getAttributes().getMtime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
            Date ModifiedFileDate = sdf.parse(sdf.format(date));
            if (ModifiedFileDate.compareTo(modifiedAfter) > 0) q.add(subFolders);
          }
        } else {
          if (remoteResourceInfo.getName().matches(fileNamePattern))
            filesArr.add(remoteResourceInfo);
        }
      }
      maxDepth--;
    }
  }

  public void ListFilesAccordingToModifiedBefore(
      SFTPClient sftpClient, String folderPath, Date modifiedBefore, int maxDepth)
      throws IOException, ParseException {

    Queue<RemoteResourceInfo> q = new LinkedList<>();
    for (RemoteResourceInfo remoteResourceInfo : sftpClient.ls(folderPath)) {
      Date date = new Date(remoteResourceInfo.getAttributes().getMtime() * 1000);
      SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
      sdf.setTimeZone(TimeZone.getTimeZone("IST"));
      Date ModifiedFileDate = sdf.parse(sdf.format(date));
      if (ModifiedFileDate.compareTo(modifiedBefore) < 0) q.add(remoteResourceInfo);
    }

    while (!q.isEmpty()) {
      int n = q.size();
      if (maxDepth == 0) break;
      for (int i = 0; i < n; i++) {
        RemoteResourceInfo remoteResourceInfo = q.poll();
        if (remoteResourceInfo.isDirectory()) {
          for (RemoteResourceInfo subFolders : sftpClient.ls(remoteResourceInfo.getPath())) {
            Date date = new Date(subFolders.getAttributes().getMtime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
            Date ModifiedFileDate = sdf.parse(sdf.format(date));
            if (ModifiedFileDate.compareTo(modifiedBefore) < 0) q.add(subFolders);
          }
        } else {
          if (remoteResourceInfo.getName().matches(fileNamePattern))
            filesArr.add(remoteResourceInfo);
        }
      }
      maxDepth--;
    }
  }

  public void ListFiles(SFTPClient sftpClient, String folderPath, int maxDepth)
      throws IOException, ParseException {

    Queue<RemoteResourceInfo> q = new LinkedList<>();
    for (RemoteResourceInfo remoteResourceInfo : sftpClient.ls(folderPath)) {
      Date date = new Date(remoteResourceInfo.getAttributes().getMtime() * 1000);
      SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
      sdf.setTimeZone(TimeZone.getTimeZone("IST"));
      Date ModifiedFileDate = sdf.parse(sdf.format(date));
      q.add(remoteResourceInfo);
    }

    while (!q.isEmpty()) {
      int n = q.size();
      if (maxDepth == 0) break;
      for (int i = 0; i < n; i++) {
        RemoteResourceInfo remoteResourceInfo = q.poll();
        if (remoteResourceInfo.isDirectory()) {
          for (RemoteResourceInfo subFolders : sftpClient.ls(remoteResourceInfo.getPath())) {
            Date date = new Date(subFolders.getAttributes().getMtime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
            Date ModifiedFileDate = sdf.parse(sdf.format(date));
            q.add(subFolders);
          }
        } else {
          if (remoteResourceInfo.getName().matches(fileNamePattern))
            filesArr.add(remoteResourceInfo);
        }
      }
      maxDepth--;
    }
  }

  public String getDateFromEpoch(long epochSeconds) {
    Date date = new Date(epochSeconds * 1000L);
    DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    String formatted = format.format(date);
    return formatted;
  }

  public void checkResponseSize(SFTPResponse<?> listFilesResponse) throws Exception {
    byte[] listFileResonposeInByte = listFilesResponse.toString().getBytes();
    long responseSize = listFileResonposeInByte.length;
    if (responseSize > 32766) {
      LOGGER.error("Response size is greater than 32766 bytes");
      throw new Exception("Response size is greater than 32766");
    }
  }

  public Map<String, String> getSortBy() {
    return sortBy;
  }

  public void setSortBy(Map<String, String> sortBy) {
    this.sortBy = sortBy;
  }

  public String getFileNamePattern() {
    return fileNamePattern;
  }

  public void setFileNamePattern(String fileNamePattern) {
    this.fileNamePattern = fileNamePattern;
  }

  public String getModifiedBefore() {
    return modifiedBefore;
  }

  public void setModifiedBefore(String modifiedBefore) {
    this.modifiedBefore = modifiedBefore;
  }

  public String getModifiedAfter() {
    return modifiedAfter;
  }

  public void setModifiedAfter(String modifiedAfter) {
    this.modifiedAfter = modifiedAfter;
  }

  public String getSearchSubFoldersAlso() {
    return searchSubFoldersAlso;
  }

  public void setSearchSubFoldersAlso(String searchSubFoldersAlso) {
    this.searchSubFoldersAlso = searchSubFoldersAlso;
  }

  public String getMaxNumberOfFiles() {
    return maxNumberOfFiles;
  }

  public void setMaxNumberOfFiles(String maxNumberOfFiles) {
    this.maxNumberOfFiles = maxNumberOfFiles;
  }

  public String getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(String maxDepth) {
    this.maxDepth = maxDepth;
  }

  public String getOutputType() {
    return outputType;
  }

  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        fileNamePattern,
        filePath,
        maxDepth,
        maxNumberOfFiles,
        modifiedAfter,
        modifiedBefore,
        outputType,
        searchSubFoldersAlso,
        sortBy);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ListFilesService other = (ListFilesService) obj;
    return Objects.equals(fileNamePattern, other.fileNamePattern)
        && Objects.equals(filePath, other.filePath)
        && Objects.equals(maxDepth, other.maxDepth)
        && Objects.equals(maxNumberOfFiles, other.maxNumberOfFiles)
        && Objects.equals(modifiedAfter, other.modifiedAfter)
        && Objects.equals(modifiedBefore, other.modifiedBefore)
        && Objects.equals(outputType, other.outputType)
        && Objects.equals(searchSubFoldersAlso, other.searchSubFoldersAlso)
        && Objects.equals(sortBy, other.sortBy);
  }

  @Override
  public String toString() {
    return "ListFilesService [sortBy="
        + sortBy
        + ", fileNamePattern="
        + fileNamePattern
        + ", modifiedBefore="
        + modifiedBefore
        + ", modifiedAfter="
        + modifiedAfter
        + ", searchSubFoldersAlso="
        + searchSubFoldersAlso
        + ", maxNumberOfFiles="
        + maxNumberOfFiles
        + ", maxDepth="
        + maxDepth
        + ", outputType="
        + outputType
        + ", folderPath="
        + filePath
        + ", searchSubFolders="
        + searchSubFolders
        + ", listFilesResponse="
        + listFilesResponse
        + ", files="
        + files
        + ", fileDetails="
        + fileDetails
        + ", filesArr="
        + filesArr
        + "]";
  }
}
