/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.ssh.model.request;

import com.infosys.camundaconnectors.ssh.model.response.Response;
import net.schmizz.sshj.SSHClient;

public interface SSHRequestData {
  Response invoke(final SSHClient sshClient, final String operatingSystem);
}