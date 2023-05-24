/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.utility;

import java.io.File;
import java.nio.file.Path;
import net.schmizz.sshj.sftp.SFTPClient;

public class RenameFolderUtil {

  public Path renameUtil(SFTPClient sftpClient, Path newFilePath, Integer count) {
    try {
      if (sftpClient.stat(newFilePath.toString()) == null) {
        return newFilePath;
      }
    } catch (Exception e) {
      return newFilePath;
    }
    String fileName = newFilePath.getFileName().toString();
    String newFileName = new String();
    if (count == 2) {
      newFileName = fileName + "(" + Integer.toString(count) + ")";
    } else {
      int indexOfCount = fileName.lastIndexOf(Integer.toString(count - 1));
      newFileName = fileName.substring(0, indexOfCount) + Integer.toString(count) + ")";
    }
    newFilePath = Path.of(newFilePath.getParent() + File.separator + newFileName);
    return renameUtil(sftpClient, newFilePath, count + 1);
  }
}
