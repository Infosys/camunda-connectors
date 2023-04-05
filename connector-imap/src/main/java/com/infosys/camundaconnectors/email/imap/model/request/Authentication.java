/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.imap.model.request;

import io.camunda.connector.api.annotation.Secret;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class Authentication {
  @NotBlank(message = "Please provide a valid hostName for email server")
  private String hostname;

  @NotBlank(message = "Please provide a valid port for email server")
  @Pattern(regexp = "^\\d+$", message = "Port must be a number")
  private String portNumber;

  @NotBlank(message = "Please provide a valid username for email ID")
  private String username;

  @NotBlank(message = "Please provide a valid password for given username")
  @Secret
  private String password;

  @NotBlank(message = "Please provide a valid domainName for email ID")
  private String domainName;

  private String keyStorePath;
  @Secret private String keyStorePassword;

  @NotBlank(message = "Please provide a valid folderPath, e.g. Inbox")
  private String folderPath;

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

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  public String getKeyStorePath() {
    return keyStorePath;
  }

  public void setKeyStorePath(String keyStorePath) {
    this.keyStorePath = keyStorePath;
  }

  public String getKeyStorePassword() {
    return keyStorePassword;
  }

  public void setKeyStorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Authentication that = (Authentication) o;
    return Objects.equals(hostname, that.hostname)
        && Objects.equals(portNumber, that.portNumber)
        && Objects.equals(username, that.username)
        && Objects.equals(password, that.password)
        && Objects.equals(domainName, that.domainName)
        && Objects.equals(keyStorePath, that.keyStorePath)
        && Objects.equals(keyStorePassword, that.keyStorePassword)
        && Objects.equals(folderPath, that.folderPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        hostname,
        portNumber,
        username,
        password,
        domainName,
        keyStorePath,
        keyStorePassword,
        folderPath);
  }
}
