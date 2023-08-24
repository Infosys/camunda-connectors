package com.infosys.camundaconnectors.agile.jira.utility;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import kong.unirest.Cookies;
import kong.unirest.Headers;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestParsingException;

public class CustomHttpResponseTest implements HttpResponse<JsonNode> {
  private int statusCode;
  private String statusText;
  private JsonNode body;

  public CustomHttpResponseTest(JsonNode body, int statusCode, String statusText) {
    this.body = body;
    this.statusCode = statusCode;
    this.statusText = statusText;
  }

  @Override
  public int getStatus() {
    return statusCode;
  }

  @Override
  public String getStatusText() {
    return statusText;
  }

  @Override
  public Headers getHeaders() {
    return null;
  }

  @Override
  public JsonNode getBody() {
    return body;
  }

  @Override
  public Optional<UnirestParsingException> getParsingError() {
    return Optional.empty();
  }

  @Override
  public <V> V mapBody(Function<JsonNode, V> func) {
    return null;
  }

  @Override
  public <V> HttpResponse<V> map(Function<JsonNode, V> func) {
    return null;
  }

  @Override
  public HttpResponse<JsonNode> ifSuccess(Consumer<HttpResponse<JsonNode>> consumer) {
    return null;
  }

  @Override
  public HttpResponse<JsonNode> ifFailure(Consumer<HttpResponse<JsonNode>> consumer) {
    return null;
  }

  @Override
  public <E> HttpResponse<JsonNode> ifFailure(
      Class<? extends E> errorClass, Consumer<HttpResponse<E>> consumer) {
    return null;
  }

  @Override
  public boolean isSuccess() {
    return false;
  }

  @Override
  public <E> E mapError(Class<? extends E> errorClass) {
    return null;
  }

  @Override
  public Cookies getCookies() {
    return null;
  }
}
