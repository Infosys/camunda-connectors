/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.agile.jira.model.request;

import com.infosys.camundaconnectors.agile.jira.model.response.Response;

public interface JIRARequestData {
  Response invoke(Authentication auth);
}
