/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.files.ftp.model.request;

import org.apache.commons.net.ftp.FTPClient;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;

public interface FTPRequestData {
  FTPResponse<String> invoke(FTPClient client);
  FTPResponse<String> invoke(FTPClient client1, FTPClient client2);
}