/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.ssh.service;

import com.infosys.camundaconnectors.ssh.model.request.SSHRequestData;
import com.infosys.camundaconnectors.ssh.model.response.Response;
import com.infosys.camundaconnectors.ssh.model.response.SSHResponse;
import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteCommandService implements SSHRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCommandService.class);
  @NotNull private List<String> commands;
  @NotNull private String commandLine;
  @NotNull private String outputType;
  private String timeout;
  SSHResponse<?> executeCommandResponse;

  @Override
  public Response invoke(SSHClient sshClient, String operatingSystem) {
    LOGGER.info("ExecuteCommand process started");
    try {
      if (commands == null || commands.isEmpty() || commands.size() == 0)
        throw new RuntimeException("commands can not be empty");
      if (!commandLine.equals("single") && !commandLine.equals("multiple"))
        throw new RuntimeException("commandLine should be single or multiple");
      if (!outputType.equals("statusCode") && !outputType.equals("commandOutput"))
        throw new RuntimeException("outputType should be statusCode or commandOutput");
      if (!operatingSystem.equals("linux") && !operatingSystem.equals("windows"))
        throw new RuntimeException("operatingSystem should be linux or windows");
      if (timeout == null || timeout.equals("")) timeout = "30";

      if (operatingSystem.equals("linux")) {
        executeCommandsOnLinux(sshClient);
      } else executeCommandsOnWindows(sshClient);
    } finally {
      try {
        sshClient.close();
        LOGGER.info("SSH Connection closed");
      } catch (IOException e) {
        LOGGER.error("There was a problem while closing the SSH connection");
      }
    }
    LOGGER.info("ExecuteCommand process Completed");
    return executeCommandResponse;
  }

  public String readInputString(InputStream inputStream) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int byteRead;
    while ((byteRead = inputStream.read(buffer)) != -1) {
      output.write(buffer, 0, byteRead);
    }
    return output.toString(StandardCharsets.UTF_8);
  }

  public CompletableFuture<String> executeCommand(
      SSHClient sshClient, String command, String outputType) {
    return CompletableFuture.supplyAsync(
        () -> {
          Session session = null;
          String commandOutputStream = "1";
          try {
            session = sshClient.startSession();

            Session.Command cmd = session.exec(command);
            cmd.join();
            if (outputType.equals("commandOutput"))
              commandOutputStream = readInputString(cmd.getInputStream());

            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) {
              throw new RuntimeException(
                  "While executing the command encountered an error. Exit status: " + exitStatus);
            }
          } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
          } finally {
            try {
              session.close();
            } catch (TransportException | ConnectionException e) {
              LOGGER.error(e.getMessage());
              throw new RuntimeException("There was an error while closing the connection " + e);
            }
          }
          return commandOutputStream;
        });
  }

  public Response executeCommandsOnLinux(SSHClient sshClient) {
    List<String> commandsWithTimeout = new ArrayList<>();
    for (String command : commands) {
      String cmdWithTimeout = "timeout " + timeout + "s " + command;
      commandsWithTimeout.add(cmdWithTimeout);
    }
    if (commandLine.equals("single")) {
      String combineCommand = "";
      int i = 0;
      for (String command : commandsWithTimeout) {
        combineCommand += command;
        i++;
        if (i != commands.size()) combineCommand += " ; ";
      }
      String output = "1";
      try {
        Session session = sshClient.startSession();
        Session.Command cmd = session.exec(combineCommand);
        cmd.join();
        if (outputType.equals("commandOutput")) output = readInputString(cmd.getInputStream());
        int exitStatus = cmd.getExitStatus();
        if (exitStatus != 0) {
          throw new RuntimeException(
              "While executing the command encountered an error. Exit status: " + exitStatus);
        }
        session.close();
        byte[] contentInBytes = output.getBytes("utf-8");
        if (contentInBytes.length > 32766) {
          LOGGER.error("Output  size is greater than 32766 byte");
          throw new RuntimeException("Output size should be less than 32766 byte");
        }
        executeCommandResponse = new SSHResponse<String>(output);
      } catch (IOException e2) {
        LOGGER.error(e2.getMessage());
        throw new RuntimeException(e2);
      }
    } else {
      Map<String, Object> commandOutputs = new TreeMap<>();
      List<Map<String, Object>> allCommandsoutput = new ArrayList<>();
      List<CompletableFuture<Void>> commandFutures = new ArrayList<>();
      try {
        for (String command : commandsWithTimeout) {
          CompletableFuture<Void> commandFuture =
              executeCommand(sshClient, command, outputType)
                  .handle(
                      (result, ex) -> {
                        if (ex != null)
                          throw new RuntimeException(
                              "While executing command \"" + command + "\" encountered an error");
                        commandOutputs.put(command, result);
                        return null;
                      });
          commandFutures.add(commandFuture);
        }
        CompletableFuture<Void> allCommandsFuture =
            CompletableFuture.allOf(commandFutures.toArray(new CompletableFuture[0]));
        allCommandsFuture.join();
        allCommandsoutput.add(commandOutputs);
        executeCommandResponse =
            new SSHResponse<>(allCommandsoutput.stream().collect((Collectors.toList())));
        checkResponseSize(executeCommandResponse);

      } catch (RuntimeException e) {
        LOGGER.error(e.getMessage());
        throw new RuntimeException(e);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return executeCommandResponse;
  }

  public Response executeCommandsOnWindows(SSHClient sshClient) {

    Map<String, Object> commandOutputs = new TreeMap<>();
    if (commandLine.equals("single")) {
      List<CompletableFuture<Void>> commandFutures = new ArrayList<>();
      String combineCommand = "";
      int i = 0;
      for (String command : commands) {
        combineCommand += command;
        i++;
        if (i != commands.size()) combineCommand += " && ";
      }
      try {
        CompletableFuture<Void> commandFuture =
            executeCommand(sshClient, combineCommand, outputType)
                .completeOnTimeout("Timed out", Long.parseLong(timeout + 15), TimeUnit.SECONDS)
                .handle(
                    (result, ex) -> {
                      if (ex != null)
                        throw new RuntimeException("While executing command encountered an error");
                      if (result.equals("Timed out"))
                        throw new RuntimeException("While executing command's timed out");
                      commandOutputs.put("command", result);
                      return null;
                    });

        commandFutures.add(commandFuture);
        CompletableFuture<Void> allCommandsFuture =
            CompletableFuture.allOf(commandFutures.toArray(new CompletableFuture[0]));
        allCommandsFuture.join();
        byte[] contentInBytes = ((String) commandOutputs.get("command")).getBytes("utf-8");
        if (contentInBytes.length > 32766) {
          LOGGER.error("Output  size is greater than 32766 byte");
          throw new RuntimeException("Output size should be less than 32766 byte");
        }
        executeCommandResponse = new SSHResponse<String>((String) commandOutputs.get("command"));

      } catch (RuntimeException e2) {
        LOGGER.error(e2.getMessage());
        throw new RuntimeException(e2);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }

    } else {
      List<Map<String, Object>> allCommandsoutput = new ArrayList<>();
      List<CompletableFuture<Void>> commandFutures = new ArrayList<>();
      try {
        for (String command : commands) {
          CompletableFuture<Void> commandFuture =
              executeCommand(sshClient, command, outputType)
                  .completeOnTimeout("Command timed out", Long.parseLong(timeout), TimeUnit.SECONDS)
                  .handle(
                      (result, ex) -> {
                        if (ex != null)
                          throw new RuntimeException(
                              "While executing command \"" + command + "\" encountered an error");
                        if (result.equals("Command timed out"))
                          throw new RuntimeException(
                              "While executing command \"" + command + "\" command timed out");
                        commandOutputs.put(command, result);
                        return null;
                      });

          commandFutures.add(commandFuture);
        }
        CompletableFuture<Void> allCommandsFuture =
            CompletableFuture.allOf(commandFutures.toArray(new CompletableFuture[0]));
        allCommandsFuture.join();
        allCommandsoutput.add(commandOutputs);
        executeCommandResponse =
            new SSHResponse<>(allCommandsoutput.stream().collect((Collectors.toList())));
        checkResponseSize(executeCommandResponse);
      } catch (RuntimeException e) {
        LOGGER.error(e.getMessage());
        throw new RuntimeException(e);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return executeCommandResponse;
  }

  public void checkResponseSize(SSHResponse<?> listFilesResponse) throws Exception {
    byte[] listFileResponseInByte = listFilesResponse.toString().getBytes();
    long responseSize = listFileResponseInByte.length;
    if (responseSize > 32766) {
      LOGGER.error("Response size is greater than 32766 bytes");
      throw new RuntimeException("Response size is greater than 32766");
    }
  }

  public List<String> getCommands() {
    return commands;
  }

  public void setCommands(List<String> commands) {
    this.commands = commands;
  }

  public String getCommandLine() {
    return commandLine;
  }

  public void setCommandLine(String commandLine) {
    this.commandLine = commandLine;
  }

  public String getOutputType() {
    return outputType;
  }

  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  public SSHResponse<?> getExecuteCommandResponse() {
    return executeCommandResponse;
  }

  public void setExecuteCommandResponse(SSHResponse<?> executeCommandResponse) {
    this.executeCommandResponse = executeCommandResponse;
  }

  @Override
  public int hashCode() {
    return Objects.hash(commandLine, commands, executeCommandResponse, outputType);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ExecuteCommandService other = (ExecuteCommandService) obj;
    return Objects.equals(commandLine, other.commandLine)
        && Objects.equals(commands, other.commands)
        && Objects.equals(executeCommandResponse, other.executeCommandResponse)
        && Objects.equals(outputType, other.outputType);
  }

  @Override
  public String toString() {
    return "ExecuteCommandService [commands="
        + commands
        + ", commandLine="
        + commandLine
        + ", outputType="
        + outputType
        + ", executeCommandResponse="
        + executeCommandResponse
        + "]";
  }
}
