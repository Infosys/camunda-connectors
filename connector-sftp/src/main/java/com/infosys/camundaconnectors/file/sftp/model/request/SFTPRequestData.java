/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.model.request;

import com.infosys.camundaconnectors.file.sftp.model.response.Response;
import net.schmizz.sshj.sftp.SFTPClient;

public interface SFTPRequestData {
  Response invoke(final SFTPClient sftpClient);
}
