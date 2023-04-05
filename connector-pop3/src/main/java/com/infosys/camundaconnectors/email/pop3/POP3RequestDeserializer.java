/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.pop3;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.infosys.camundaconnectors.email.pop3.model.request.POP3Request;
import com.infosys.camundaconnectors.email.pop3.model.request.POP3RequestData;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class POP3RequestDeserializer
    implements JsonDeserializer<POP3Request<? extends POP3RequestData>> {

  private String typeElementName;
  private Gson gson;
  private Map<String, Class<? extends POP3RequestData>> typeRegistry;

  public POP3RequestDeserializer(String typeElementName) {
    this.typeElementName = typeElementName;
    this.gson = new Gson();
    this.typeRegistry = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  private static TypeToken<POP3Request<? extends POP3RequestData>> getTypeToken(
      Class<? extends POP3RequestData> requestDataClass) {
    return (TypeToken<POP3Request<? extends POP3RequestData>>)
        TypeToken.getParameterized(POP3Request.class, requestDataClass);
  }

  public POP3RequestDeserializer registerType(
      String typeName, Class<? extends POP3RequestData> requestType) {
    typeRegistry.put(typeName, requestType);
    return this;
  }

  @Override
  public POP3Request<? extends POP3RequestData> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return getTypeElementValue(jsonElement)
        .map(typeRegistry::get)
        .map(POP3RequestDeserializer::getTypeToken)
        .map(typeToken -> getPOP3Request(jsonElement, typeToken))
        .orElse(null);
  }

  private Optional<String> getTypeElementValue(JsonElement jsonElement) {
    JsonObject asJsonObject = jsonElement.getAsJsonObject();
    JsonElement element = asJsonObject.get(typeElementName);
    return Optional.ofNullable(element).map(JsonElement::getAsString);
  }

  private POP3Request<? extends POP3RequestData> getPOP3Request(
      JsonElement jsonElement, TypeToken<POP3Request<? extends POP3RequestData>> typeToken) {
    return gson.fromJson(jsonElement, typeToken.getType());
  }
}
