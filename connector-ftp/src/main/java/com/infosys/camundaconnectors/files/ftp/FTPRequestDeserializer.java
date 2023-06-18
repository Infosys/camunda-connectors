/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequest;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequestData;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FTPRequestDeserializer
    implements JsonDeserializer<FTPRequest<? extends FTPRequestData>> {
  private String typeElementName;
  private Gson gson;
  private Map<String, Class<? extends FTPRequestData>> typeRegistry;
  public FTPRequestDeserializer(String typeElementName) {
    this.typeElementName = typeElementName;
    this.gson = new Gson();
    this.typeRegistry = new HashMap<>();
  }
  @SuppressWarnings("unchecked")
  private static TypeToken<FTPRequest<? extends FTPRequestData>> getTypeToken(
      Class<? extends FTPRequestData> requestDataClass) {
    return (TypeToken<FTPRequest<? extends FTPRequestData>>)
        TypeToken.getParameterized(FTPRequest.class, requestDataClass);
  }
  public FTPRequestDeserializer registerType(
      String typeName, Class<? extends FTPRequestData> requestType) {
    typeRegistry.put(typeName, requestType);
    return this;
  }
  @Override
  public FTPRequest<? extends FTPRequestData> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return getTypeElementValue(jsonElement)
        .map(typeRegistry::get)
        .map(FTPRequestDeserializer::getTypeToken)
        .map(typeToken -> getFTPRequest(jsonElement, typeToken))
        .orElse(null);
  }
  private Optional<String> getTypeElementValue(JsonElement jsonElement) {
    JsonObject asJsonObject = jsonElement.getAsJsonObject();
    JsonElement element = asJsonObject.get(typeElementName);
    return Optional.ofNullable(element).map(JsonElement::getAsString);
  }
  private FTPRequest<? extends FTPRequestData> getFTPRequest(
      JsonElement jsonElement, TypeToken<FTPRequest<? extends FTPRequestData>> typeToken) {
    return gson.fromJson(jsonElement, typeToken.getType());
  }
}
