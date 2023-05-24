/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.utility;

import com.infosys.camundaconnectors.file.sftp.model.request.Authentication;
import java.io.File;
import java.nio.file.Path;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SftpServerClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(SftpServerClient.class);
  SSHClient client = null;

  public SSHClient loginSftp(final Authentication authentication) throws Exception {
    client = new SSHClient();

    Path knownHostsFile = Path.of(authentication.getKnownHostsPath());
    client.loadKnownHosts();
    client.addHostKeyVerifier(new OpenSSHKnownHosts(new File(knownHostsFile.toString())));
    LOGGER.info("Connecting to sftp server" + authentication.getUsername());
    client.connect(authentication.getHostname(), Integer.parseInt(authentication.getPortNumber()));
    client.authPassword(authentication.getUsername(), authentication.getPassword());

    LOGGER.info("Connected to sftp server");
    return client;
  }

  public void logoutSftp() throws Exception {
    if (client != null) client.close();
  }
}
