/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.utility;

public class FileSortByComparator {

  public int sortByName(FTPFilePath fp1, FTPFilePath fp2) {
    return fp1.getFTPFile()
        .getName()
        .toLowerCase()
        .compareTo(fp2.getFTPFile().getName().toLowerCase());
  }

  public int sortBySize(FTPFilePath fp1, FTPFilePath fp2) {
    if (fp1.getFTPFile().getSize() > fp2.getFTPFile().getSize()) return 1;
    return -1;
  }

  public int sortByDate(FTPFilePath fp1, FTPFilePath fp2) {
    return fp1.getFTPFile()
        .getTimestamp()
        .getTime()
        .compareTo(fp2.getFTPFile().getTimestamp().getTime());
  }
}
