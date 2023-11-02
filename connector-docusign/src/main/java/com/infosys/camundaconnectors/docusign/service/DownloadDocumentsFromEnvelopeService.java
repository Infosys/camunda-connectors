/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.docusign.service;

import com.infosys.camundaconnectors.docusign.model.request.Authentication;
import com.infosys.camundaconnectors.docusign.model.request.DocuSignRequestData;
import com.infosys.camundaconnectors.docusign.model.response.DocuSignResponse;
import com.infosys.camundaconnectors.docusign.model.response.Response;
import com.infosys.camundaconnectors.docusign.utility.ErrorHandler;
import com.infosys.camundaconnectors.docusign.utility.HttpServiceUtils;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadDocumentsFromEnvelopeService implements DocuSignRequestData {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(DownloadDocumentsFromEnvelopeService.class);
  @NotNull private String envelopeId;
  @NotNull private String documentId;
  private String path;
  private String documentName;
  private String certificate;
  private String showChanges;
  private String language;
  DocuSignResponse<?> downloadDocumentResponse;
  ErrorHandler errorHandler = new ErrorHandler();

  @Override
  public Response invoke(Authentication authentication, HttpServiceUtils httpService)
      throws IOException {
    String baseUri = authentication.getBaseUri();
    String basePath =
        baseUri
            + (baseUri.charAt(baseUri.length() - 1) != '/' ? "/" : "")
            + "restapi/v2.1/accounts/"
            + authentication.getAccountId()
            + "/envelopes"
            + "/"
            + envelopeId
            + "/documents"
            + "/"
            + documentId;
    CloseableHttpClient httpClient = null;
    String httpResponse = "";
    try {
      if (certificate != null
          && certificate.equalsIgnoreCase("true")
          && !documentId.equalsIgnoreCase("combined"))
        throw new RuntimeException(
            "Certificate can not be \"true\" when documentId is \"combined\"");
      URIBuilder uri = new URIBuilder(basePath);
      if (!isNullStr(certificate) && certificate.equalsIgnoreCase("true"))
        uri.addParameter("certificate", certificate);
      if (!isNullStr(showChanges) && showChanges.equalsIgnoreCase("true"))
        uri.addParameter("show_changes", showChanges);
      if (!isNullStr(language)) uri.addParameter("language", language);
      if (documentName == null || documentName.equals("")) documentName = envelopeId + "_Documents";
      String documentPath = path + "/" + documentName;
      basePath = uri.build().toString();
      httpClient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet(basePath);
      httpGet.setHeader("Authorization", "Bearer " + authentication.getAccessToken());
      httpResponse =
          httpClient.execute(
              httpGet,
              response -> {
                int statusCode = response.getCode();
                LOGGER.info(Integer.toString(statusCode));
                InputStream responseBody = response.getEntity().getContent();
                File file = null;
                String contentType = response.getFirstHeader("Content-Type").getValue();
                if (contentType.equalsIgnoreCase("application/pdf")) {
                  String documentUri = documentPath + "." + "pdf";
                  file = new File(documentUri);
                } else {
                  String documentUri = documentPath + "." + "zip";
                  file = new File(documentUri);
                }
                FileUtils.copyInputStreamToFile(responseBody, file);
                if (statusCode == 400) {
                  throw new HttpResponseException(
                      statusCode, "Enter correct documentId or value" + documentId);
                } else if (statusCode != 200) {
                  throw new HttpResponseException(statusCode, "Some problem occured");
                }

                return "File Downloaded Successfully";
              });
    } catch (IOException | URISyntaxException e) {
      LOGGER.error(e.getMessage().toString());
      throw new RuntimeException(e.getMessage());
    } finally {
      httpClient.close();
      LOGGER.info("HttpClient closed successfully");
    }
    downloadDocumentResponse = new DocuSignResponse<String>(httpResponse);
    LOGGER.info("Response", downloadDocumentResponse);
    return downloadDocumentResponse;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getDocumentName() {
    return documentName;
  }

  public void setDocumentName(String documentName) {
    this.documentName = documentName;
  }

  private boolean isNullStr(String str) {
    return str == null || str.equals("");
  }

  @Override
  public String toString() {
    return "DownloadDocumentsFromEnvelopeService [envelopeId="
        + envelopeId
        + ", documentId="
        + documentId
        + ", path="
        + path
        + ", documentName="
        + documentName
        + ", certificate="
        + certificate
        + ", showChanges="
        + showChanges
        + ", language="
        + language
        + ", downloadDocumentResponse="
        + downloadDocumentResponse
        + ", errorHandler="
        + errorHandler
        + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        certificate,
        documentId,
        documentName,
        downloadDocumentResponse,
        envelopeId,
        errorHandler,
        language,
        path,
        showChanges);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DownloadDocumentsFromEnvelopeService other = (DownloadDocumentsFromEnvelopeService) obj;
    return Objects.equals(certificate, other.certificate)
        && Objects.equals(documentId, other.documentId)
        && Objects.equals(documentName, other.documentName)
        && Objects.equals(downloadDocumentResponse, other.downloadDocumentResponse)
        && Objects.equals(envelopeId, other.envelopeId)
        && Objects.equals(errorHandler, other.errorHandler)
        && Objects.equals(language, other.language)
        && Objects.equals(path, other.path)
        && Objects.equals(showChanges, other.showChanges);
  }

  public String getEnvelopeId() {
    return envelopeId;
  }

  public String getDocumentId() {
    return documentId;
  }

  public void setEnvelopeId(String envelopeId) {
    this.envelopeId = envelopeId;
  }

  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }
}
