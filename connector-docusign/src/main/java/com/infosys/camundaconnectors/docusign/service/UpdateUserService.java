/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.request.DocuSignRequestData;
import com.infosys.camundaconnectors.docusign.model.response.DocuSignResponse;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.ErrorHandler;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateUserService implements DocuSignRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUserService.class);
  private String payloadType;
  private String userId;
  private String firstName;
  private String lastName;
  private String middleName;
  private String jobTitle;
  private String company;
  private String language;
  private List<String> groups;
  private String address1;
  private String address2;
  private String city;
  private String postalCode;
  private String phone;
  private Map<String, Object> payload;
  private String countryRegion;

  DocuSignResponse<?> updateUserResponse;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpRequest)
      throws IOException {
    if (userId == null || userId.equals("")) throw new RuntimeException("UserId can not be null");
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/users/"
            + userId;
    String jsonPayload = "";
    if (payloadType.equalsIgnoreCase("basicfields")) jsonPayload = getPayloadFromBasicFields();
    else jsonPayload = getPayloadFromJsonString();
    Map<String, Object> response = httpRequest.putRequest(basePath, jsonPayload, authentication);
    errorHandler.checkForErrorDetails(response);
    updateUserResponse = new DocuSignResponse<Map<String, Object>>(response);
    LOGGER.info("Response", updateUserResponse);
    return updateUserResponse;
  }

  private String getPayloadFromJsonString() {
    if (payload == null || payload.size() == 0)
      throw new RuntimeException("payload can not be null");
    Gson gson = new Gson();
    return gson.toJson(payload, Map.class);
  }

  private String getPayloadFromBasicFields() {
    JsonObject userInfoObject = new JsonObject();
    JsonObject homeAddressObject = new JsonObject();
    JsonArray groupsArray = new JsonArray();
    if (!isNullStr(firstName)) userInfoObject.addProperty("firstName", firstName);
    if (!isNullStr(lastName)) userInfoObject.addProperty("lastName", lastName);
    if (!isNullStr(middleName)) userInfoObject.addProperty("middleName", middleName);
    if (!isNullStr(jobTitle)) userInfoObject.addProperty("jobTitle", jobTitle);
    if (!isNullStr(address1)) homeAddressObject.addProperty("address1", address1);
    if (!isNullStr(address2)) homeAddressObject.addProperty("address2", address2);
    if (!isNullStr(city)) homeAddressObject.addProperty("city", city);
    if (!isNullStr(countryRegion)) homeAddressObject.addProperty("country", countryRegion);
    if (!isNullStr(postalCode)) homeAddressObject.addProperty("postalCode", postalCode);
    if (!isNullStr(phone)) homeAddressObject.addProperty("phone", phone);
    userInfoObject.add("workAddress", homeAddressObject);
    if (!isNullList(groups)) {
      for (String groupId : groups) {
        JsonObject groupObject = new JsonObject();
        groupObject.addProperty("groupId", groupId);
        groupsArray.add(groupObject);
      }
      userInfoObject.add("groupList", groupsArray);
    }
    Gson gson = new Gson();
    String jsonPayLoad = gson.toJson(userInfoObject);
    return jsonPayLoad;
  }

  public boolean isNullStr(String str) {
    return str == null || str.equals("") || str.equals("None");
  }

  public boolean isNullList(List<String> list) {
    return list == null || list.size() == 0;
  }

  public String getPayloadType() {
    return payloadType;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public void setPayload(Map<String, Object> payload) {
    this.payload = payload;
  }

  public String getCountryRegion() {
    return countryRegion;
  }

  public void setCountryRegion(String countryRegion) {
    this.countryRegion = countryRegion;
  }

  public void setPayloadType(String payloadType) {
    this.payloadType = payloadType;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  public String getAddress1() {
    return address1;
  }

  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  public String getAddress2() {
    return address2;
  }

  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        address1,
        address2,
        city,
        company,
        countryRegion,
        firstName,
        groups,
        jobTitle,
        language,
        lastName,
        middleName,
        payload,
        payloadType,
        phone,
        postalCode,
        updateUserResponse,
        userId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    UpdateUserService other = (UpdateUserService) obj;
    return Objects.equals(address1, other.address1)
        && Objects.equals(address2, other.address2)
        && Objects.equals(city, other.city)
        && Objects.equals(company, other.company)
        && Objects.equals(countryRegion, other.countryRegion)
        && Objects.equals(firstName, other.firstName)
        && Objects.equals(groups, other.groups)
        && Objects.equals(jobTitle, other.jobTitle)
        && Objects.equals(language, other.language)
        && Objects.equals(lastName, other.lastName)
        && Objects.equals(middleName, other.middleName)
        && Objects.equals(payload, other.payload)
        && Objects.equals(payloadType, other.payloadType)
        && Objects.equals(phone, other.phone)
        && Objects.equals(postalCode, other.postalCode)
        && Objects.equals(updateUserResponse, other.updateUserResponse)
        && Objects.equals(userId, other.userId);
  }

  @Override
  public String toString() {
    return "UpdateUserService [payloadType="
        + payloadType
        + ", userId="
        + userId
        + ", firstName="
        + firstName
        + ", lastName="
        + lastName
        + ", middleName="
        + middleName
        + ", jobTitle="
        + jobTitle
        + ", company="
        + company
        + ", language="
        + language
        + ", groups="
        + groups
        + ", address1="
        + address1
        + ", address2="
        + address2
        + ", city="
        + city
        + ", postalCode="
        + postalCode
        + ", phone="
        + phone
        + ", payload="
        + payload
        + ", countryRegion="
        + countryRegion
        + ", updateUserResponse="
        + updateUserResponse
        + "]";
  }
}
