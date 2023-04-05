/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.email.imap;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.infosys.camundaconnectors.email.imap.model.request.IMAPRequest;
import com.infosys.camundaconnectors.email.imap.model.request.IMAPRequestData;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IMAPRequestDeserializer
    implements JsonDeserializer<IMAPRequest<? extends IMAPRequestData>> {

  private String typeElementName;
  private Gson gson;
  private Map<String, Class<? extends IMAPRequestData>> typeRegistry;

  public IMAPRequestDeserializer(String typeElementName) {
    this.typeElementName = typeElementName;
    this.gson = new Gson();
    this.typeRegistry = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  private static TypeToken<IMAPRequest<? extends IMAPRequestData>> getTypeToken(
      Class<? extends IMAPRequestData> requestDataClass) {
    return (TypeToken<IMAPRequest<? extends IMAPRequestData>>)
        TypeToken.getParameterized(IMAPRequest.class, requestDataClass);
  }

  public IMAPRequestDeserializer registerType(
      String typeName, Class<? extends IMAPRequestData> requestType) {
    typeRegistry.put(typeName, requestType);
    return this;
  }

  @Override
  public IMAPRequest<? extends IMAPRequestData> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return getTypeElementValue(jsonElement)
        .map(typeRegistry::get)
        .map(IMAPRequestDeserializer::getTypeToken)
        .map(typeToken -> getIMAPRequest(jsonElement, typeToken))
        .orElse(null);
  }

  private Optional<String> getTypeElementValue(JsonElement jsonElement) {
    JsonObject asJsonObject = jsonElement.getAsJsonObject();
    JsonElement element = asJsonObject.get(typeElementName);
    return Optional.ofNullable(element).map(JsonElement::getAsString);
  }

  private IMAPRequest<? extends IMAPRequestData> getIMAPRequest(
      JsonElement jsonElement, TypeToken<IMAPRequest<? extends IMAPRequestData>> typeToken) {
    return gson.fromJson(jsonElement, typeToken.getType());
  }
}
