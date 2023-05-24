/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.file.sftp.model.response;

import java.util.Objects;

public class SFTPResponse<T> implements Response {
  private final T response;

  public SFTPResponse(T response) {
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
    SFTPResponse other = (SFTPResponse) obj;
    return Objects.equals(response, other.response);
  }

  @Override
  public String toString() {
    return "SFTPResponse [response=" + response + "]";
  }
}
