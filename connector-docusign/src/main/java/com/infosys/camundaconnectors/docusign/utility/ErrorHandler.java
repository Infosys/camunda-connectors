package com.infosys.camundaconnectors.docusign.utility;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hc.client5.http.HttpResponseException;

public class ErrorHandler {
  public void checkForErrorDetails(Map<String, Object> res)
      throws HttpResponseException, NumberFormatException {
    if (res.containsKey("errorDetails")) {
      Map<String, String> errorDetail = (Map<String, String>) res.get("errorDetails");
      throw new HttpResponseException(
          Integer.parseInt(errorDetail.get("errorCode")), errorDetail.get("message"));
    }
    for (Entry<String, Object> entry : res.entrySet()) {
      if (entry.getValue() instanceof List) {
        for (Map<String, Object> fields : (List<Map<String, Object>>) entry.getValue()) {
          if (fields.containsKey("errorDetails")) {
            Map<String, String> errorDetail = (Map<String, String>) fields.get("errorDetails");
            throw new HttpResponseException(
                Integer.parseInt(errorDetail.get("errorCode")), errorDetail.get("message"));
          }
        }
      }
    }
  }
}
