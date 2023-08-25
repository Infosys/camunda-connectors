/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.service.folders;

import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import com.infosys.camundaconnectors.file.sftp.model.response.SFTPResponse;
import com.infosys.camundaconnectors.file.sftp.utility.FolderSortCondition;
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

public class ListFoldersService implements SFTPRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ListFoldersService.class);
  private Map<String, String> sortBy;
  private String namePattern;
  private String modifiedBefore;
  private String modifiedAfter;
  private String searchSubFoldersAlso;
  private String maxNumberOfFiles;
  private String maxDepth;
  private String outputType;
  private String folderPath;
  boolean searchSubFolders;
  SFTPResponse<?> listFoldersResponse;
  List<String> files = new ArrayList<>();
  List<Map<String, Object>> fileDetails = new ArrayList<>();
  List<RemoteResourceInfo> filesArr = new ArrayList<>();
  //  @SuppressWarnings("unchecked")
  @Override
  public Response invoke(SFTPClient sftpClient) {
    LOGGER.info("Listing Folders started");
    if (namePattern == null || namePattern.isBlank()) namePattern = ".*";
    if (searchSubFoldersAlso.equalsIgnoreCase("True")) searchSubFolders = true;
    else searchSubFolders = false;
    if (outputType == null) outputType = "folderPaths";
    if (maxDepth == null || maxDepth.isBlank()) maxDepth = Integer.toString(1);
    if (maxNumberOfFiles == null || maxNumberOfFiles.isBlank()) maxNumberOfFiles = "1000";
    if (modifiedAfter == null || modifiedAfter.isBlank() || modifiedAfter.isEmpty())
      modifiedAfter = null;
    if (modifiedBefore == null || modifiedBefore.isBlank()) modifiedBefore = null;
    Date modifiedAfterDate = new Date();
    Date modifiedBeforeDate = new Date();
    SimpleDateFormat df = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
    df.setTimeZone(TimeZone.getTimeZone("IST"));
    folderPath = Path.of(folderPath).toString().replace("\\", "/");
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
        ListFoldersAccordingToModifiedAfterAndModifiedBefore(
            sftpClient,
            folderPath,
            modifiedBeforeDate,
            modifiedAfterDate,
            Integer.parseInt(maxDepth));
      } catch (NumberFormatException | IOException | ParseException e) {
        throw new RuntimeException("Some error occurred while trying to list folders-1");
      }
    } else if (modifiedAfter != null) {
      try {
        ListFoldersAccordingToModifiedAfter(
            sftpClient, folderPath, modifiedAfterDate, Integer.parseInt(maxDepth));
      } catch (NumberFormatException | IOException | ParseException e) {
        throw new RuntimeException("Some error occurred while trying to list folders-2");
      }
    } else if (modifiedBefore != null) {
      try {
        ListFoldersAccordingToModifiedBefore(
            sftpClient, folderPath, modifiedBeforeDate, Integer.parseInt(maxDepth));
      } catch (NumberFormatException | IOException | ParseException e) {
        throw new RuntimeException("Some error occurred while trying to list folders-3");
      }
    } else {
      try {
        ListFolders(
            sftpClient, folderPath, Integer.parseInt(maxDepth), Integer.parseInt(maxNumberOfFiles));
      } catch (NumberFormatException | IOException e) {
        throw new RuntimeException(e);
      }
    }

    try {
      if (sortBy != null
          && !sortBy.isEmpty()
          && (sortBy.get("sortOn") != null)
          && (sortBy.get("order") != null)) {
        FolderSortCondition sortCond = new FolderSortCondition();
        sortCond.sortFolders(filesArr, sortBy, sftpClient);
      }
    } catch (Exception e) {
      LOGGER.debug("Some error occurred while trying to sort folders");
      throw new RuntimeException("IOException" + e.getLocalizedMessage());
    }
    try {
      if (outputType.equalsIgnoreCase("folderDetails")) {
        for (RemoteResourceInfo remoteResourceInfo : filesArr) {
          Map<String, Object> fileDetail = new TreeMap<>();
          fileDetail.put("FolderName", remoteResourceInfo.getName());
          fileDetail.put("Parent", remoteResourceInfo.getParent());
          fileDetail.put("Path", remoteResourceInfo.getPath());
          fileDetail.put("SizeInBytes", getSizeOfFolder(folderPath, sftpClient));
          fileDetail.put("Time", getDateFromEpoch(remoteResourceInfo.getAttributes().getMtime()));
          fileDetails.add(fileDetail);
        }
      } else {
        for (RemoteResourceInfo remoteResourceInfo : filesArr) {
          String fileName = remoteResourceInfo.getName();
          files.add(fileName);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot list folders" + e.getLocalizedMessage());
    }
    try {
      if (outputType.equalsIgnoreCase("folderDetails")) {
        listFoldersResponse =
            new SFTPResponse<>(
                fileDetails.stream()
                    .limit(Integer.parseInt(maxNumberOfFiles))
                    .collect(Collectors.toList()));
        checkResponseSize(listFoldersResponse);
      } else if (outputType.equalsIgnoreCase("folderPaths")) {

        listFoldersResponse =
            new SFTPResponse<>(
                files.stream()
                    .limit(Integer.parseInt(maxNumberOfFiles))
                    .collect(Collectors.toList()));
        checkResponseSize(listFoldersResponse);
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

    if (listFoldersResponse != null)
      LOGGER.info("List Folder Status: {}", listFoldersResponse.getResponse());
    return listFoldersResponse;
  }

  public void ListFoldersAccordingToModifiedAfterAndModifiedBefore(
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
      if (remoteResourceInfo.isDirectory()
          && ModifiedFileDate.compareTo(modifiedAfter) > 0
          && ModifiedFileDate.compareTo(modifiedBefore) < 0
          && remoteResourceInfo.getName().matches(namePattern)) q.add(remoteResourceInfo);
    }

    while (!q.isEmpty()) {
      int n = q.size();
      if (maxDepth == 0) break;
      for (int i = 0; i < n; i++) {
        RemoteResourceInfo remoteResourceInfo = q.poll();
        filesArr.add(remoteResourceInfo);
        if (remoteResourceInfo.isDirectory()) {
          for (RemoteResourceInfo subFolders : sftpClient.ls(remoteResourceInfo.getPath())) {
            Date date = new Date(subFolders.getAttributes().getMtime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
            Date ModifiedFileDate = sdf.parse(sdf.format(date));
            if (subFolders.isDirectory()
                && ModifiedFileDate.compareTo(modifiedAfter) > 0
                && ModifiedFileDate.compareTo(modifiedBefore) < 0
                && subFolders.getName().matches(namePattern)) q.add(subFolders);
          }
        }
      }
      maxDepth--;
    }
  }

  public void ListFoldersAccordingToModifiedAfter(
      SFTPClient sftpClient, String folderPath, Date modifiedAfter, int maxDepth)
      throws IOException, ParseException {
    Queue<RemoteResourceInfo> q = new LinkedList<>();
    for (RemoteResourceInfo remoteResourceInfo : sftpClient.ls(folderPath)) {
      Date date = new Date(remoteResourceInfo.getAttributes().getMtime() * 1000);
      SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
      sdf.setTimeZone(TimeZone.getTimeZone("IST"));
      Date ModifiedFileDate = sdf.parse(sdf.format(date));
      if (remoteResourceInfo.isDirectory()
          && ModifiedFileDate.compareTo(modifiedAfter) > 0
          && remoteResourceInfo.getName().matches(namePattern)) q.add(remoteResourceInfo);
    }

    while (!q.isEmpty()) {
      int n = q.size();
      if (maxDepth == 0) break;
      for (int i = 0; i < n; i++) {
        RemoteResourceInfo remoteResourceInfo = q.poll();
        filesArr.add(remoteResourceInfo);
        if (remoteResourceInfo.isDirectory()) {
          for (RemoteResourceInfo subFolders : sftpClient.ls(remoteResourceInfo.getPath())) {
            Date date = new Date(subFolders.getAttributes().getMtime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
            Date ModifiedFileDate = sdf.parse(sdf.format(date));
            if (subFolders.isDirectory()
                && ModifiedFileDate.compareTo(modifiedAfter) > 0
                && subFolders.getName().matches(namePattern)) q.add(subFolders);
          }
        }
      }
      maxDepth--;
    }
  }

  public void ListFoldersAccordingToModifiedBefore(
      SFTPClient sftpClient, String folderPath, Date modifiedBefore, int maxDepth)
      throws IOException, ParseException {
    Queue<RemoteResourceInfo> q = new LinkedList<>();
    for (RemoteResourceInfo remoteResourceInfo : sftpClient.ls(folderPath)) {
      Date date = new Date(remoteResourceInfo.getAttributes().getMtime() * 1000);
      SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
      sdf.setTimeZone(TimeZone.getTimeZone("IST"));
      Date ModifiedFileDate = sdf.parse(sdf.format(date));
      if (remoteResourceInfo.isDirectory()
          && ModifiedFileDate.compareTo(modifiedBefore) < 0
          && remoteResourceInfo.getName().matches(namePattern)) q.add(remoteResourceInfo);
    }

    while (!q.isEmpty()) {
      int n = q.size();
      if (maxDepth == 0) break;
      for (int i = 0; i < n; i++) {
        RemoteResourceInfo remoteResourceInfo = q.poll();
        filesArr.add(remoteResourceInfo);
        if (remoteResourceInfo.isDirectory()) {
          for (RemoteResourceInfo subFolders : sftpClient.ls(remoteResourceInfo.getPath())) {
            Date date = new Date(subFolders.getAttributes().getMtime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
            Date ModifiedFileDate = sdf.parse(sdf.format(date));
            if (subFolders.isDirectory()
                && ModifiedFileDate.compareTo(modifiedBefore) < 0
                && subFolders.getName().matches(namePattern)) q.add(subFolders);
          }
        }
      }
      maxDepth--;
    }
  }

  public void ListFolders(
      SFTPClient sftpClient, String folderPath, int maxDepth, int maxNumberOfFiles)
      throws IOException {

    Queue<RemoteResourceInfo> q = new LinkedList<>();
    for (RemoteResourceInfo remoteResourceInfo : sftpClient.ls(folderPath)) {
      if (remoteResourceInfo.isDirectory()) q.add(remoteResourceInfo);
    }

    while (!q.isEmpty()) {
      int n = q.size();
      if (maxDepth == 0) break;
      for (int i = 0; i < n; i++) {
        RemoteResourceInfo remoteResourceInfo = q.poll();
        filesArr.add(remoteResourceInfo);
        if (remoteResourceInfo.isDirectory() && searchSubFolders) {

          for (RemoteResourceInfo subFolders : sftpClient.ls(remoteResourceInfo.getPath())) {
            q.add(subFolders);
          }
          ;
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

  public long getSizeOfFolder(String path, SFTPClient sftpClient) throws IOException {
    long size = 0;
    for (RemoteResourceInfo remoteResourceInfo : sftpClient.ls(path)) {
      if (remoteResourceInfo.isRegularFile()) {
        size += remoteResourceInfo.getAttributes().getSize();
      } else if (remoteResourceInfo.isDirectory()) {
        size += getSizeOfFolder(path + "/" + remoteResourceInfo.getName(), sftpClient);
      }
    }
    return size;
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

  public String getNamePattern() {
    return namePattern;
  }

  public void setNamePattern(String namePattern) {
    this.namePattern = namePattern;
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

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        folderPath,
        maxDepth,
        maxNumberOfFiles,
        modifiedAfter,
        modifiedBefore,
        namePattern,
        outputType,
        searchSubFoldersAlso,
        sortBy);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ListFoldersService other = (ListFoldersService) obj;
    return Objects.equals(folderPath, other.folderPath)
        && Objects.equals(maxDepth, other.maxDepth)
        && Objects.equals(maxNumberOfFiles, other.maxNumberOfFiles)
        && Objects.equals(modifiedAfter, other.modifiedAfter)
        && Objects.equals(modifiedBefore, other.modifiedBefore)
        && Objects.equals(namePattern, other.namePattern)
        && Objects.equals(outputType, other.outputType)
        && Objects.equals(searchSubFoldersAlso, other.searchSubFoldersAlso)
        && Objects.equals(sortBy, other.sortBy);
  }

  @Override
  public String toString() {
    return "ListFoldersService [sortBy="
        + sortBy
        + ", namePattern="
        + namePattern
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
        + folderPath
        + "]";
  }
}
