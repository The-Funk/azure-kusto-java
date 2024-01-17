package com.microsoft.azure.kusto.data;

import com.azure.core.http.*;
import com.azure.core.util.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.kusto.data.exceptions.*;
import com.microsoft.azure.kusto.data.http.CloseParentResourcesStream;
import com.microsoft.azure.kusto.data.http.HttpRequestBuilder;
import com.microsoft.azure.kusto.data.http.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.EofSensorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

public abstract class BaseClient implements Client, StreamingClient {

    private static final int MAX_REDIRECT_COUNT = 1;

    // Make logger available to implementations
    protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HttpClient httpClient;

    public BaseClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    protected String post(HttpRequest request) throws DataServiceException {

        // Todo: Add async version of this method

        // Execute and get the response
        try (HttpResponse response = httpClient.sendSync(request, Context.NONE)) {
            String responseBody = response.getBodyAsBinaryData().toString();

            if (responseBody != null) {
                switch (response.getStatusCode()) {
                    case HttpStatus.OK:
                        return responseBody;
                    case HttpStatus.TOO_MANY_REQS:
                        throw new ThrottleException(request.getUrl().toString());
                    default:
                        throw createExceptionFromResponse(request.getUrl().toString(), response, null, responseBody);
                }
            }
        }

        return null;
    }

    protected InputStream postToStreamingOutput(HttpRequest request) throws DataServiceException {
        return postToStreamingOutput(request, 0);
    }

    protected InputStream postToStreamingOutput(HttpRequest request, int redirectCount) throws DataServiceException {

        boolean returnInputStream = false;
        String errorFromResponse = null;

        HttpResponse httpResponse = null;
        try {

            // Todo: Implement async version of this method

            httpResponse = httpClient.sendSync(request, Context.NONE);

            int responseStatusCode = httpResponse.getStatusCode();

            if (responseStatusCode == HttpStatus.OK) {
                InputStream contentStream = new EofSensorInputStream(new CloseParentResourcesStream(httpResponse), null);
                Optional<HttpHeader> contentEncoding = Optional.ofNullable(httpResponse.getHeaders().get(HttpHeaderName.CONTENT_ENCODING));
                if (contentEncoding.isPresent()) {
                    if (contentEncoding.get().getValue().contains("gzip")) {
                        GZIPInputStream gzipInputStream = new GZIPInputStream(contentStream);
                        returnInputStream = true;
                        return gzipInputStream;
                    } else if (contentEncoding.get().getValue().contains("deflate")) {
                        DeflaterInputStream deflaterInputStream = new DeflaterInputStream(contentStream);
                        returnInputStream = true;
                        return deflaterInputStream;
                    }
                }
                returnInputStream = true;
                return contentStream;
            }

            errorFromResponse = httpResponse.getBodyAsBinaryData().toString();
            // Ideal to close here (as opposed to finally) so that if any data can't be flushed, the exception will be properly thrown and handled
            httpResponse.close();

            if (shouldPostToOriginalUrlDueToRedirect(redirectCount, responseStatusCode)) {
                Optional<HttpHeader> redirectLocation = Optional.ofNullable(httpResponse.getHeaders().get(HttpHeaderName.LOCATION));
                if (redirectLocation.isPresent() && !redirectLocation.get().getValue().equals(request.getUrl().toString())) {
                    HttpRequest redirectRequest = HttpRequestBuilder
                            .fromExistingRequest(request)
                            .withURL(redirectLocation.get().getValue())
                            .build();
                    return postToStreamingOutput(redirectRequest, redirectCount + 1);
                }
            }
        } catch (IOException ex) {
            throw new DataServiceException(request.getUrl().toString(),
                    "postToStreamingOutput failed to get or decompress response stream", ex, false);
        } catch (Exception ex) {
            throw createExceptionFromResponse(request.getUrl().toString(), httpResponse, ex, errorFromResponse);
        } finally {
            closeResourcesIfNeeded(returnInputStream, httpResponse);
        }

        throw createExceptionFromResponse(request.getUrl().toString(), httpResponse, null, errorFromResponse);
    }

    public static DataServiceException createExceptionFromResponse(String url, HttpResponse httpResponse, Exception thrownException, String errorFromResponse) {
        if (httpResponse == null) {
            return new DataServiceException(url, "POST failed to send request", thrownException, false);
        }
        /*
         * TODO: When we add another streaming API that returns a KustoOperationResult, we'll need to handle the 2 types of content errors this API can return:
         * (1) Inline error (engine identifies error after it starts building the json result), or (2) in the KustoOperationResult's QueryCompletionInformation,
         * both of which present with "200 OK". See .Net's DataReaderParser.
         */
        String activityId = determineActivityId(httpResponse);
        String message = errorFromResponse;
        WebException formattedException = new WebException(errorFromResponse, httpResponse, thrownException);
        boolean isPermanent = false;
        if (!StringUtils.isBlank(errorFromResponse)) {
            try {
                JsonNode jsonObject = Utils.getObjectMapper().readTree(errorFromResponse);
                if (jsonObject.has("error")) {
                    formattedException = new DataWebException(errorFromResponse, httpResponse, thrownException);
                    OneApiError apiError = ((DataWebException) formattedException).getApiError();
                    message = apiError.getDescription();
                    isPermanent = apiError.isPermanent();
                } else if (jsonObject.has("message")) {
                    message = jsonObject.get("message").asText();
                }
            } catch (JsonProcessingException e) {
                // It's not ideal to use an exception here for control flow, but we can't know if it's a valid JSON until we try to parse it
                LOGGER.debug("json processing error happened while parsing errorFromResponse {}", e.getMessage(), e);
            }
        } else {
            message = String.format("Http StatusCode='%s'", httpResponse.getStatusCode());
        }

        return new DataServiceException(
                url,
                String.format("%s, ActivityId='%s'", message, activityId),
                formattedException,
                isPermanent);
    }

    private static void closeResourcesIfNeeded(boolean returnInputStream, HttpResponse httpResponse) {
        // If we close the resources after returning the InputStream to the caller, they won't be able to read from it
        if (!returnInputStream) {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    private static boolean shouldPostToOriginalUrlDueToRedirect(int redirectCount, int status) {
        return (status == HttpStatus.FOUND || status == HttpStatus.TEMP_REDIRECT) && redirectCount + 1 <= MAX_REDIRECT_COUNT;
    }

    private static String determineActivityId(HttpResponse httpResponse) {
        String activityId = "";

        Optional<HttpHeader> activityIdHeader = Optional.ofNullable(
                httpResponse.getHeaders().get(HttpHeaderName.fromString("x-ms-activity-id")));
        if (activityIdHeader.isPresent()) {
            activityId = activityIdHeader.get().getValue();
        }
        return activityId;
    }

}
