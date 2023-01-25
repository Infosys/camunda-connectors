/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.model.request;

import io.camunda.connector.api.annotation.Secret;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class DatabaseConnection {
  @NotBlank private String host;

  @NotBlank
  @Pattern(regexp = "^\\d+$", message = "Port must be a number")
  private String port;

  @NotBlank private String username;
  @NotBlank @Secret private String password;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DatabaseConnection that = (DatabaseConnection) o;
    return Objects.equals(host, that.host)
        && Objects.equals(port, that.port)
        && Objects.equals(username, that.username)
        && Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port, username, password);
  }

  @Override
  public String toString() {
    return "DatabaseConnection{"
        + "host='"
        + host
        + '\''
        + ", port='"
        + port
        + '\''
        + ", username='"
        + username
        + '\''
        + ", password='"
        + password
        + '\''
        + '}';
  }
}
