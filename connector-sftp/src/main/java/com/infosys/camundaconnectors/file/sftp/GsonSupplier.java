/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequest;
import com.infosys.camundaconnectors.file.sftp.service.files.CopyFileService;
import com.infosys.camundaconnectors.file.sftp.service.files.DeleteFileService;
import com.infosys.camundaconnectors.file.sftp.service.files.ListFilesService;
import com.infosys.camundaconnectors.file.sftp.service.files.MoveFileService;
import com.infosys.camundaconnectors.file.sftp.service.files.ReadFileService;
import com.infosys.camundaconnectors.file.sftp.service.files.WriteFileService;
import com.infosys.camundaconnectors.file.sftp.service.folders.CopyFolderService;
import com.infosys.camundaconnectors.file.sftp.service.folders.CreateFolderService;
import com.infosys.camundaconnectors.file.sftp.service.folders.DeleteFolderService;
import com.infosys.camundaconnectors.file.sftp.service.folders.ListFoldersService;
import com.infosys.camundaconnectors.file.sftp.service.folders.MoveFolderService;

public final class GsonSupplier {
  private static final SFTPRequestDeserializer DESERIALIZER =
      new SFTPRequestDeserializer("operation")
          .registerType("sftp.list-files", ListFilesService.class)
          .registerType("sftp.delete-file", DeleteFileService.class)
          .registerType("sftp.delete-folder", DeleteFolderService.class)
          .registerType("sftp.copy-file", CopyFileService.class)
          .registerType("sftp.read-file", ReadFileService.class)
          .registerType("sftp.copy-folder", CopyFolderService.class)
          .registerType("sftp.move-folder", MoveFolderService.class)
          .registerType("sftp.move-file", MoveFileService.class)
          .registerType("sftp.list-folders", ListFoldersService.class)
          .registerType("sftp.write-file", WriteFileService.class)
          .registerType("sftp.create-folder", CreateFolderService.class);

  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(SFTPRequest.class, DESERIALIZER).create();

  private GsonSupplier() {}

  public static Gson getGson() {
    return GSON;
  }
}
