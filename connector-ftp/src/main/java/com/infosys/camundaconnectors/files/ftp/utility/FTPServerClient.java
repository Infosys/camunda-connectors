/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.utility;

import com.infosys.camundaconnectors.files.ftp.model.request.Authentication;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPServerClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(FTPServerClient.class);

  public FTPServerClient() {}

  private void connectToFTPServer(String host, Integer port, FTPClient client) throws IOException {
    client.connect(host, port);
  }

  public FTPClient loginFTP(Authentication auth) throws Exception {
    String host = auth.getHost();
    String port = auth.getPort();
    String username = auth.getUsername();
    String password = auth.getPassword();
    FTPClient client = new FTPClient();
    try {
      connectToFTPServer(host, Integer.parseInt(port), client);
      int replyCode = client.getReplyCode();
      if (!FTPReply.isPositiveCompletion(replyCode)) {
        throw new IOException(Integer.toString(replyCode));
      }
      LOGGER.info("FTP server connected successfully!!");
    } catch (IOException e) {
      LOGGER.error("Connection failed!!" + e.getMessage());
      throw new RuntimeException("Connection failed. Server reply code: " + e.getMessage());
    }
    try {
      boolean isLoggedIn = client.login(username, password);
      if (!isLoggedIn) throw new IOException("Invalid username or password!!");
      LOGGER.info("Login Successful!!");
    } catch (IOException e) {
      LOGGER.error("Login failed, " + e.getMessage());
      throw new RuntimeException("Login Failed!!");
    }
    return client;
  }
}
