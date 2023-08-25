/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.ssh.model.response;

import java.util.Objects;

public class SSHResponse<T> implements Response {
  private final T response;

  public SSHResponse(T response) {
    this.response = response;
  }

  public T getResponse() {
    return response;
  }

  @Override
  public int hashCode() {
    return Objects.hash(response);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SSHResponse other = (SSHResponse) obj;
    return Objects.equals(response, other.response);
  }

  @Override
  public String toString() {
    return "SSHresponse [response=" + response + "]";
  }
}
