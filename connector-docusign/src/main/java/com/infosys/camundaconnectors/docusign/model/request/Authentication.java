/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.docusign.model.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class Authentication {

  private String accountId;

  @NotBlank private String accessToken;

  private String baseUri;

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, accountId, baseUri);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Authentication other = (Authentication) obj;
    return Objects.equals(accessToken, other.accessToken)
        && Objects.equals(accountId, other.accountId)
        && Objects.equals(baseUri, other.baseUri);
  }

  @Override
  public String toString() {
    return "Authentication [accountId="
        + accountId
        + ", accessToken="
        + accessToken
        + ", baseUri="
        + baseUri
        + "]";
  }
}
