/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequest;
import com.infosys.camundaconnectors.files.ftp.service.*;

public final class GsonSupplier {
  private static final FTPRequestDeserializer DESERIALIZER =
      new FTPRequestDeserializer("operation")
      .registerType("ftp.copy-file", CopyFileService.class)
      .registerType("ftp.copy-folder", CopyFolderService.class)
      .registerType("ftp.create-folder", CreateFolderService.class)
      .registerType("ftp.delete-file", DeleteFileService.class)
      .registerType("ftp.delete-folder", DeleteFolderService.class)
      .registerType("ftp.list-files", ListFilesService.class)
      .registerType("ftp.list-folders", ListFoldersService.class)
      .registerType("ftp.move-file", MoveFileService.class)
      .registerType("ftp.move-folder", MoveFolderService.class)
      .registerType("ftp.read-file", ReadFileService.class)
      .registerType("ftp.write-file", WriteFileService.class);

  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(FTPRequest.class, DESERIALIZER).create();

  private GsonSupplier() {
	  
  }

  public static Gson getGson() {
    return GSON;
  }
}
