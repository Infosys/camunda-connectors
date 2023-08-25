package com.infosys.camundaconnectors.file.sftp.utility;

import java.io.File;
import java.nio.file.Path;
import net.schmizz.sshj.sftp.SFTPClient;

public class RenameFileUtil {

  public Path renameFileUtil(SFTPClient sftpClient, Path newFilePath, Integer count) {
    try {
      if (sftpClient.stat(newFilePath.toString().replace("\\", "/")) == null) {
        return newFilePath;
      }
    } catch (Exception e) {
      return newFilePath;
    }
    String newFileName = newFilePath.getFileName().toString();
    String fileName = newFileName.split("\\.")[0];
    String extension = newFileName.split("\\.")[1];
    if (count == 2) {

      newFileName = fileName + "(" + Integer.toString(count) + ")" + "." + extension;

    } else {
      int indexOfCount = fileName.lastIndexOf(Integer.toString(count - 1));
      newFileName =
          fileName.substring(0, indexOfCount) + Integer.toString(count) + ")" + "." + extension;
    }
    newFilePath = Path.of(newFilePath.getParent().toString() + File.separator + newFileName);
    return renameFileUtil(sftpClient, newFilePath, count + 1);
  }
}
