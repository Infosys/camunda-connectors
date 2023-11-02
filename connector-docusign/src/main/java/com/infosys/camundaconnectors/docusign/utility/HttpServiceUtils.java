package com.infosys.camundaconnectors.docusign.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServiceUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServiceUtils.class);
  CloseableHttpClient httpClient = null;

  //  public HttpServiceUtils(CloseableHttpClient httpClient) {
  //    this.httpClient = httpClient;
  //  }

  public Map<String, Object> postRequest(
      String basePath, String jsonPayLoad, Authentication authentication) {

    Map<String, Object> res = null;
    try {
      HttpPost httpPost = new HttpPost(basePath);
      httpPost.setHeader("Authorization", "Bearer " + authentication.getAccessToken());
      httpPost.setHeader("Content-Type", "application/json");
      httpPost.setEntity(new StringEntity(jsonPayLoad, ContentType.APPLICATION_JSON));
      httpClient = HttpClients.createDefault();
      String httpResponse =
          httpClient.execute(
              httpPost,
              response -> {
                InputStream responseBody = response.getEntity().getContent();
                String text = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                int statusCode = response.getCode();
                LOGGER.info(text);
                if (statusCode != 201) {
                  throw new HttpResponseException(statusCode, text);
                }
                return text;
              });
      ObjectMapper objectMapper = new ObjectMapper();
      res = objectMapper.readValue(httpResponse, Map.class);
      return res;
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    } finally {
      try {
        httpClient.close();
        LOGGER.info("HttpClient closed successfully");
      } catch (IOException e) {
        LOGGER.error("There is some problem in closing the httpClient " + e.getMessage());
      }
    }
  }

  public Map<String, Object> getRequest(String basePath, Authentication authentication) {
    //     CloseableHttpClient httpClient = null;
    Map<String, Object> res = null;
    try {
      httpClient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet(basePath);
      httpGet.setHeader("Authorization", "Bearer " + authentication.getAccessToken());
      String httpResponse =
          httpClient.execute(
              httpGet,
              response -> {
                int statusCode = response.getCode();
                LOGGER.info(Integer.toString(statusCode));
                InputStream responseBody = response.getEntity().getContent();
                String phrase = response.getReasonPhrase();
                String jsonString = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                LOGGER.info(jsonString);
                if (statusCode != 200) {
                  throw new HttpResponseException(statusCode, jsonString);
                }
                return jsonString;
              });
      ObjectMapper objectMapper = new ObjectMapper();
      res = objectMapper.readValue(httpResponse, Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    } finally {
      try {
        httpClient.close();
        LOGGER.info("HttpClient closed successfully");
      } catch (IOException e) {
        LOGGER.error("There is some problem in closing the httpClient " + e.getMessage());
      }
    }
    return res;
  }

  public Map<String, Object> putRequest(
      String basePath, String jsonPayLoad, Authentication authentication) {
    //    CloseableHttpClient httpClient = null;
    String httpResponse = null;
    Map<String, Object> res = null;

    try {
      HttpPut httpPut = new HttpPut(basePath);
      httpPut.setHeader("Authorization", "Bearer " + authentication.getAccessToken());
      httpPut.setHeader("Content-Type", "application/json");
      httpPut.setEntity(new StringEntity(jsonPayLoad, ContentType.APPLICATION_JSON));
      // httpClient = HttpClients.createDefault();
      httpResponse =
          httpClient.execute(
              httpPut,
              response -> {
                InputStream responseBody = response.getEntity().getContent();
                String text = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                int statusCode = response.getCode();
                if (statusCode != 200) {
                  throw new HttpResponseException(statusCode, text);
                }
                return text;
              });
      ObjectMapper objectMapper = new ObjectMapper();
      res = objectMapper.readValue(httpResponse, Map.class);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    } finally {
      try {
        httpClient.close();
        LOGGER.info("HttpClient closed successfully");
      } catch (IOException e) {
        LOGGER.error("There is some problem in closing the httpClient " + e.getMessage());
      }
    }
    return res;
  }

  public Map<String, Object> deleteRequest(
      String basePath, String jsonPayLoad, Authentication authentication) {
    // CloseableHttpClient httpClient = null;
    String httpResponse = null;
    Map<String, Object> res = null;
    try {
      HttpDelete httpDelete = new HttpDelete(basePath);
      httpDelete.setHeader("Authorization", "Bearer " + authentication.getAccessToken());
      httpDelete.setHeader("Content-Type", "application/json");
      httpDelete.setEntity(new StringEntity(jsonPayLoad, ContentType.APPLICATION_JSON));
      httpClient = HttpClients.createDefault();
      httpResponse =
          httpClient.execute(
              httpDelete,
              response -> {
                InputStream responseBody = response.getEntity().getContent();
                String text = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                int statusCode = response.getCode();
                if (statusCode != 200) {
                  throw new HttpResponseException(statusCode, text);
                }
                return text;
              });
      ObjectMapper objectMapper = new ObjectMapper();
      res = objectMapper.readValue(httpResponse, Map.class);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    } finally {
      try {
        httpClient.close();
        LOGGER.info("HttpClient closed successfully");
      } catch (IOException e) {
        LOGGER.error("There is some problem in closing the httpClient " + e.getMessage());
      }
    }
    return res;
  }
}
