/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.file.sftp;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequest;
import com.infosys.camundaconnectors.file.sftp.model.request.SFTPRequestData;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SFTPRequestDeserializer
    implements JsonDeserializer<SFTPRequest<? extends SFTPRequestData>> {

  private String typeElementName;
  private Gson gson;
  private Map<String, Class<? extends SFTPRequestData>> typeRegistry;

  public SFTPRequestDeserializer(String typeElementName) {
    this.typeElementName = typeElementName;
    this.gson = new Gson();
    this.typeRegistry = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  private static TypeToken<SFTPRequest<? extends SFTPRequestData>> getTypeToken(
      Class<? extends SFTPRequestData> requestDataClass) {
    return (TypeToken<SFTPRequest<? extends SFTPRequestData>>)
        TypeToken.getParameterized(SFTPRequest.class, requestDataClass);
  }

  public SFTPRequestDeserializer registerType(
      String typeName, Class<? extends SFTPRequestData> requestType) {
    typeRegistry.put(typeName, requestType);
    return this;
  }

  @Override
  public SFTPRequest<? extends SFTPRequestData> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return getTypeElementValue(jsonElement)
        .map(typeRegistry::get)
        .map(SFTPRequestDeserializer::getTypeToken)
        .map(typeToken -> getSFTPRequest(jsonElement, typeToken))
        .orElse(null);
  }

  private Optional<String> getTypeElementValue(JsonElement jsonElement) {
    JsonObject asJsonObject = jsonElement.getAsJsonObject();
    JsonElement element = asJsonObject.get(typeElementName);
    return Optional.ofNullable(element).map(JsonElement::getAsString);
  }

  private SFTPRequest<? extends SFTPRequestData> getSFTPRequest(
      JsonElement jsonElement, TypeToken<SFTPRequest<? extends SFTPRequestData>> typeToken) {
    return gson.fromJson(jsonElement, typeToken.getType());
  }
}
