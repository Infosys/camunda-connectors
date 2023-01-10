/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mysql;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.infosys.camundaconnectors.db.mysql.model.request.MySQLRequest;
import com.infosys.camundaconnectors.db.mysql.model.request.MySQLRequestData;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MySQLRequestDeserializer
    implements JsonDeserializer<MySQLRequest<? extends MySQLRequestData>> {

  private String typeElementName;
  private Gson gson;
  private Map<String, Class<? extends MySQLRequestData>> typeRegistry;

  public MySQLRequestDeserializer(String typeElementName) {
    this.typeElementName = typeElementName;
    this.gson = new Gson();
    this.typeRegistry = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  private static TypeToken<MySQLRequest<? extends MySQLRequestData>> getTypeToken(
      Class<? extends MySQLRequestData> requestDataClass) {
    return (TypeToken<MySQLRequest<? extends MySQLRequestData>>)
        TypeToken.getParameterized(MySQLRequest.class, requestDataClass);
  }

  public MySQLRequestDeserializer registerType(
      String typeName, Class<? extends MySQLRequestData> requestType) {
    typeRegistry.put(typeName, requestType);
    return this;
  }

  @Override
  public MySQLRequest<? extends MySQLRequestData> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return getTypeElementValue(jsonElement)
        .map(typeRegistry::get)
        .map(MySQLRequestDeserializer::getTypeToken)
        .map(typeToken -> getMySQLRequest(jsonElement, typeToken))
        .orElse(null);
  }

  private Optional<String> getTypeElementValue(JsonElement jsonElement) {
    JsonObject asJsonObject = jsonElement.getAsJsonObject();
    JsonElement element = asJsonObject.get(typeElementName);
    return Optional.ofNullable(element).map(JsonElement::getAsString);
  }

  private MySQLRequest<? extends MySQLRequestData> getMySQLRequest(
      JsonElement jsonElement, TypeToken<MySQLRequest<? extends MySQLRequestData>> typeToken) {
    return gson.fromJson(jsonElement, typeToken.getType());
  }
}
