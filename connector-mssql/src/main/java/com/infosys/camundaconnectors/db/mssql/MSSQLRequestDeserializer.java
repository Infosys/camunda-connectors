/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.infosys.camundaconnectors.db.mssql.model.request.MSSQLRequest;
import com.infosys.camundaconnectors.db.mssql.model.request.MSSQLRequestData;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MSSQLRequestDeserializer
    implements JsonDeserializer<MSSQLRequest<? extends MSSQLRequestData>> {

  private String typeElementName;
  private Gson gson;
  private Map<String, Class<? extends MSSQLRequestData>> typeRegistry;

  public MSSQLRequestDeserializer(String typeElementName) {
    this.typeElementName = typeElementName;
    this.gson = new Gson();
    this.typeRegistry = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  private static TypeToken<MSSQLRequest<? extends MSSQLRequestData>> getTypeToken(
      Class<? extends MSSQLRequestData> requestDataClass) {
    return (TypeToken<MSSQLRequest<? extends MSSQLRequestData>>)
        TypeToken.getParameterized(MSSQLRequest.class, requestDataClass);
  }

  public MSSQLRequestDeserializer registerType(
      String typeName, Class<? extends MSSQLRequestData> requestType) {
    typeRegistry.put(typeName, requestType);
    return this;
  }

  @Override
  public MSSQLRequest<? extends MSSQLRequestData> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return getTypeElementValue(jsonElement)
        .map(typeRegistry::get)
        .map(MSSQLRequestDeserializer::getTypeToken)
        .map(typeToken -> getMSSQLRequest(jsonElement, typeToken))
        .orElse(null);
  }

  private Optional<String> getTypeElementValue(JsonElement jsonElement) {
    JsonObject asJsonObject = jsonElement.getAsJsonObject();
    JsonElement element = asJsonObject.get(typeElementName);
    return Optional.ofNullable(element).map(JsonElement::getAsString);
  }

  private MSSQLRequest<? extends MSSQLRequestData> getMSSQLRequest(
      JsonElement jsonElement, TypeToken<MSSQLRequest<? extends MSSQLRequestData>> typeToken) {
    return gson.fromJson(jsonElement, typeToken.getType());
  }
}
