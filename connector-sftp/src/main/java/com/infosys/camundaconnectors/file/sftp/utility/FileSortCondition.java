/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.utility;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.schmizz.sshj.sftp.RemoteResourceInfo;

/**
 * This will take the list of files and sort based on - size,last Modified File, last Accessed file,
 * name of file
 */
public class FileSortCondition {
  /**
   * @param File object array
   * @param sortBy Map having keys as sortOn and order e.g {'sortOn':'Name', 'order': 'asc'}
   */
  public void sortFiles(List<RemoteResourceInfo> filesArr, Map<String, String> sortBy) {
    if (filesArr == null || filesArr.size() == 0)
      throw new RuntimeException("SortException: files array is empty or null.");
    String sortField = sortBy.get("sortOn").toLowerCase();
    String order = sortBy.get("order").toLowerCase();
    if (!(order.contains("asc")
        || order.contains("desc")
        || order.contains("dsc")
        || order.contains("ascending")
        || order.contains("descending")))
      throw new RuntimeException(
          "SortException: Sorted order error. It should be - ascending or descending.");
    if (order.contains("ascending")) order = "asc";
    if (order.contains("dsc") || order.contains("descending")) order = "desc";
    Comparator<RemoteResourceInfo> fileComparator;

    if (sortField.contains("size")) fileComparator = sizeTerm(order);
    else if (sortField.contains("last modified")
        || sortField.contains("date modified")
        || (sortField.contains("date") || sortField.contains("time")))
      fileComparator = fileModifiedTerm(order);
    else if (sortField.contains("last file accessed") || sortField.contains("last date accessed"))
      fileComparator = fileLastAccessedTerm(order);
    else if (sortField.contains("name")) fileComparator = fileNameTerm(order);
    else fileComparator = null;
    if (fileComparator == null)
      throw new RuntimeException(
          "Unable to sort files. You can sort message on size,"
              + "size,name,last modified file,last file accessed");

    filesArr.sort(fileComparator);
  }

  private Comparator<RemoteResourceInfo> sizeTerm(String order) {
    return (RemoteResourceInfo m1, RemoteResourceInfo m2) -> {
      long res;
      try {
        res = m1.getAttributes().getSize() - m2.getAttributes().getSize();
      } catch (Exception e) {
        throw new RuntimeException("Unable to sort files on size: " + e.getLocalizedMessage());
      }
      return (int) ((order.contains("asc")) ? res : (-1 * res));
    };
  }

  private Comparator<RemoteResourceInfo> fileModifiedTerm(String order) {
    return (RemoteResourceInfo m1, RemoteResourceInfo m2) -> {
      int res;
      try {
        res = (int) ((m1.getAttributes().getMtime() - m2.getAttributes().getMtime()));
        return ((order.contains("asc")) ? res : (-1 * res));
      } catch (Exception e) {
        throw new RuntimeException(
            "Unable to sort files on last modified: " + e.getLocalizedMessage());
      }
    };
  }

  private Comparator<RemoteResourceInfo> fileNameTerm(String order) {
    return (RemoteResourceInfo m1, RemoteResourceInfo m2) -> {
      int res;
      try {
        if (m1.getName() == null || m2.getName() == null) return 0;
        res = m1.getName().compareTo(m2.getName());
        return ((order.contains("asc")) ? res : (-1 * res));
      } catch (Exception e) {
        throw new RuntimeException("Unable to sort files on Name: " + e.getLocalizedMessage());
      }
    };
  }

  private Comparator<RemoteResourceInfo> fileLastAccessedTerm(String order) {
    return (RemoteResourceInfo m1, RemoteResourceInfo m2) -> {
      long res;
      try {
        res = m1.getAttributes().getAtime() - m2.getAttributes().getAtime();
      } catch (Exception e) {
        throw new RuntimeException(
            "Unable to sort files on last accessed time: " + e.getLocalizedMessage());
      }
      return (int) ((order.contains("asc")) ? res : (-1 * res));
    };
  }
}
