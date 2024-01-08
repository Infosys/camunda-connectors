/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.utility;

import org.apache.commons.net.ftp.FTPFile;

public class FTPFilePath {

  private FTPFile file;
  private String path;

  public void setFTPFile(FTPFile file) {
    this.file = file;
  }

  public FTPFile getFTPFile() {
    return this.file;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return this.path;
  }
}
