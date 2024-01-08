/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import com.infosys.camundaconnectors.email.imap.model.request.Authentication;
import com.infosys.camundaconnectors.email.imap.model.response.IMAPResponse;
import com.infosys.camundaconnectors.email.imap.model.response.Response;
import com.infosys.camundaconnectors.email.imap.service.MoveEmailService;
import com.infosys.camundaconnectors.email.imap.utility.MailServerClient;
import com.infosys.camundaconnectors.email.imap.utility.SearchCondition;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IMAPFunctionTest extends BaseTest {
  @Mock private Store store;
  @Mock private Folder folder;
  @Mock private Message message;
  @Mock private SearchTerm searchTerm;
  @Mock private MailServerClient mailServerClient;
  @Mock private SearchCondition searchCondition;
  @InjectMocks @Spy private MoveEmailService moveEmailService;
  private IMAPFunction imapFunction;

  @BeforeEach
  void init() throws Exception {
    //		mimeMessageParserFunction.apply(mimeMessage);
    imapFunction = new IMAPFunction(mailServerClient);
    Mockito.when(mailServerClient.getStore(any(Authentication.class))).thenReturn(store);
    Mockito.when(mailServerClient.getFolder(any(Store.class), any(Authentication.class)))
        .thenReturn(folder);
    Mockito.when(folder.search(any(SearchTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(folder.search(any(MessageIDTerm.class))).thenReturn(new Message[] {message});
    Mockito.when(store.getDefaultFolder()).thenReturn(folder);
    Mockito.when(folder.getFolder(anyString())).thenReturn(folder);
    Mockito.when(folder.exists()).thenReturn(true);
  }

  private void assertThatItsValid(Object executeResponse, String validateAgainst) {
    assertThat(executeResponse).isInstanceOf(Response.class);
    @SuppressWarnings("unchecked")
    IMAPResponse<String> response = (IMAPResponse<String>) executeResponse;
    assertThat(response.getResponse()).isNotNull();
    assertThat(response)
        .extracting("response")
        .asInstanceOf(InstanceOfAssertFactories.STRING)
        .contains(validateAgainst);
  }
}
