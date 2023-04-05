/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.imap.model.request;

import com.infosys.camundaconnectors.email.imap.model.response.Response;
import javax.mail.Folder;
import javax.mail.Store;

public interface IMAPRequestData {
  Response invoke(final Store store, final Folder folder);
}
