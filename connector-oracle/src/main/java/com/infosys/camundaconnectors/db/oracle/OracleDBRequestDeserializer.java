/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.oracle;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.infosys.camundaconnectors.db.oracle.model.request.OracleDBRequest;
import com.infosys.camundaconnectors.db.oracle.model.request.OracleDBRequestData;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OracleDBRequestDeserializer
    implements JsonDeserializer<OracleDBRequest<? extends OracleDBRequestData>> {

  private String typeElementName;
  private Gson gson;
  private Map<String, Class<? extends OracleDBRequestData>> typeRegistry;

  public OracleDBRequestDeserializer(String typeElementName) {
    this.typeElementName = typeElementName;
    this.gson = new Gson();
    this.typeRegistry = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  private static TypeToken<OracleDBRequest<? extends OracleDBRequestData>> getTypeToken(
      Class<? extends OracleDBRequestData> requestDataClass) {
    return (TypeToken<OracleDBRequest<? extends OracleDBRequestData>>)
        TypeToken.getParameterized(OracleDBRequest.class, requestDataClass);
  }

  public OracleDBRequestDeserializer registerType(
      String typeName, Class<? extends OracleDBRequestData> requestType) {
    typeRegistry.put(typeName, requestType);
    return this;
  }

  @Override
  public OracleDBRequest<? extends OracleDBRequestData> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return getTypeElementValue(jsonElement)
        .map(typeRegistry::get)
        .map(OracleDBRequestDeserializer::getTypeToken)
        .map(typeToken -> getOracleDBRequest(jsonElement, typeToken))
        .orElse(null);
  }

  private Optional<String> getTypeElementValue(JsonElement jsonElement) {
    JsonObject asJsonObject = jsonElement.getAsJsonObject();
    JsonElement element = asJsonObject.get(typeElementName);
    return Optional.ofNullable(element).map(JsonElement::getAsString);
  }

  private OracleDBRequest<? extends OracleDBRequestData> getOracleDBRequest(
      JsonElement jsonElement,
      TypeToken<OracleDBRequest<? extends OracleDBRequestData>> typeToken) {
    return gson.fromJson(jsonElement, typeToken.getType());
  }
}
