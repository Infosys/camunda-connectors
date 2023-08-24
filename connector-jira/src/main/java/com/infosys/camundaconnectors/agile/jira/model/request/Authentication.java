/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.agile.jira.model.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class Authentication {
  @NotBlank private String url;
  @NotBlank private String username;
  @NotBlank private String password;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public int hashCode() {
    return Objects.hash(password, url, username);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Authentication other = (Authentication) obj;
    return Objects.equals(password, other.password)
        && Objects.equals(url, other.url)
        && Objects.equals(username, other.username);
  }

  @Override
  public String toString() {
    return "Authentication [url=" + url + ", username=" + username + ", password=" + password + "]";
  }
}
