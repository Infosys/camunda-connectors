/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.agile.jira.utility;

import com.infosys.camundaconnectors.agile.jira.model.request.Authentication;

public class JIRAServerClient {

  public JIRAServerClient() {}

  public void loginJira(Authentication auth) throws Exception {
    String url = auth.getUrl();
    String username = auth.getUsername();
    String password = auth.getPassword();
    if (url == null || username == null || password == null)
      throw new RuntimeException("Credentials should not be empty!!");
  }
}
