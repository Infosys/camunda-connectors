/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.ssh.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

public class Authentication {
  @NotBlank(message = "Please provide a valid hostName for reomte server")
  private String hostname;

  @NotBlank(message = "Please provide a valid port for remote server")
  @Pattern(regexp = "^\\d+$", message = "Port must be a number")
  private String portNumber;

  @NotBlank(message = "Please provide a valid username for remote server")
  private String username;

  @NotBlank(message = "Please provide a valid password for remote server")
  private String password;

  @NotBlank(message = "Please provide a valid known hosts path")
  private String knownHostsPath;

  @NotBlank(message = "Please provide a valid OS")
  private String operatingSystem;

  @Override
  public String toString() {
    return "Authentication [hostname="
        + hostname
        + ", portNumber="
        + portNumber
        + ", username="
        + username
        + ", password="
        + password
        + ", knownHostsPath="
        + knownHostsPath
        + ", operatingSystem="
        + operatingSystem
        + "]";
  }

  public String getOperatingSystem() {
    return operatingSystem;
  }

  public void setOperatingSystem(String operatingSystem) {
    this.operatingSystem = operatingSystem;
  }

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
    return Objects.hash(hostname, knownHostsPath, operatingSystem, password, portNumber, username);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Authentication other = (Authentication) obj;
    return Objects.equals(hostname, other.hostname)
        && Objects.equals(knownHostsPath, other.knownHostsPath)
        && Objects.equals(operatingSystem, other.operatingSystem)
        && Objects.equals(password, other.password)
        && Objects.equals(portNumber, other.portNumber)
        && Objects.equals(username, other.username);
  }
}
