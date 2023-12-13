/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp.model.request;

import java.util.Objects;
import jakarta.validation.constraints.*;

public class Authentication {
  @NotBlank(message = "Please provide a valid hostName for sftp server")
  private String hostname;

  @NotBlank(message = "Please provide a valid port for sftp server")
  @Pattern(regexp = "^\\d+$", message = "Port must be a number")
  private String portNumber;

  @NotBlank(message = "Please provide a valid username for sftp server")
  private String username;

  @NotBlank(message = "Please provide a valid password for sftp server")
  private String password;

  @NotBlank(message = "Please provide a valid known hosts path")
  private String knownHostsPath;

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getPortNumber() {
    return portNumber;
  }

  public void setPortNumber(String portNumber) {
    this.portNumber = portNumber;
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

  public String getKnownHostsPath() {
    return knownHostsPath;
  }

  public void setKnownHostsPath(String knownHostsPath) {
    this.knownHostsPath = knownHostsPath;
  }

  @Override
  public int hashCode() {
    return Objects.hash(hostname, knownHostsPath, password, portNumber, username);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Authentication other = (Authentication) obj;
    return Objects.equals(hostname, other.hostname)
        && Objects.equals(knownHostsPath, other.knownHostsPath)
        && Objects.equals(password, other.password)
        && Objects.equals(portNumber, other.portNumber)
        && Objects.equals(username, other.username);
  }
}
