/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.files.ftp.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import com.infosys.camundaconnectors.files.ftp.model.request.Authentication;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FTPServerClientTest {
  @Mock private Authentication authentication;
  @Mock private FTPClient ftpClient;
  @Mock private FTPServerClient ftpServerClient;

  void ShouldReturnClient() throws Exception {
	  when(authentication.getUsername()).thenReturn("curation.bot");
	  when(authentication.getPort()).thenReturn("21");
	  when(ftpServerClient.loginFTP(authentication)).thenReturn(ftpClient);
	  assertThat(ftpServerClient.loginFTP(authentication)).isInstanceOf(FTPClient.class);
  }
}
