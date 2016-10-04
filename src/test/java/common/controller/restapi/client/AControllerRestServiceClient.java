/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package common.controller.restapi.client;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.controller.ui.modal.ControllerInfo;
import common.support.Base64;
import common.controller.DetailedResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Base class for all UI Service Clients containing HTTP Connection calls using get and post requests.
 * Also has options to deserialize Json Object and a Json Object Array.
 */
public abstract class AControllerRestServiceClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AControllerRestServiceClient.class);
    private static final String RESTUI = "restui";
    private static final long SESSION_TIMEOUT = 300000;
    private static final int NUM_ATTEMPTS = 3;

    private String controllerLogin;
    private String password;
    private boolean isSSLEnabled;
    private String controllerBasePath;
    private Gson gson;
    private String sessionCookie;
    private long lastSessionCreateTime = 0;

    public AControllerRestServiceClient() {
        this(ControllerInfo.DEFAULT_CONTROLLER_CLIENT);
    }

    public AControllerRestServiceClient(ControllerInfo controllerInfo) {
        String httpProtocol = controllerInfo.isControllerSslEnabled() ? "https" : "http";
        controllerBasePath =
                httpProtocol + "://" + controllerInfo.getControllerHost() + ":" + controllerInfo.getControllerPort()
                        + "/controller/";
        controllerLogin = controllerInfo.getControllerLogin();
        password = controllerInfo.getControllerPassword();
        isSSLEnabled = controllerInfo.isControllerSslEnabled();
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").serializeNulls().create();
    }

    /**
     * Authenticate (login) with the controller and get a session cookie (JSESSIONID)
     * This is needed when talking to the restui API
     */
    protected synchronized String getSessionCookie() {
        return getSessionCookie(controllerLogin, password);
    }

    protected synchronized String getSessionCookie(String controllerLogin, String password) {
            final String url = controllerBasePath + "auth?action=login";
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(url);
            }
            for (int i = 0; i < NUM_ATTEMPTS; i++) {
                try {
                    String unEncodedAuthString = controllerLogin + ":" + password;
                    String authorizationString = "Basic " + new String(Base64.encode(unEncodedAuthString.getBytes()));
                    URL urlObj = new URL(url);

                    if (isSSLEnabled) {
                        HttpsURLConnection uc = (HttpsURLConnection) urlObj.openConnection();
                        uc.setHostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String arg0, SSLSession arg1) {
                                return true;
                            }
                        });
                        uc.setRequestProperty("Authorization", authorizationString);
                        uc.disconnect(); // disconnect the connection.
                        final int serverResponseCode = uc.getResponseCode();
                        if (HttpURLConnection.HTTP_OK == serverResponseCode) {
                            String cookie = null;
                            for (String s : uc.getHeaderFields().get("Set-Cookie")) {
                                if (s.startsWith("JSESSIONID")) {
                                    cookie = s;
                                    break;
                                }
                            }
                            sessionCookie = cookie;
                            lastSessionCreateTime = System.currentTimeMillis();
                            LOGGER.info("Created new session cookie " + sessionCookie);
                            break;
                        } else {
                            LOGGER.error("Failed to get a valid login session from the controller at " + url
                                                 + "\nauthorizationString = " + authorizationString
                                                 + "\nServer response code: " + serverResponseCode
                                                 + "\nServer response message: " + uc.getResponseMessage());
                            sessionCookie = null;
                        }
                    } else {
                        HttpURLConnection uc = (HttpURLConnection) urlObj.openConnection();
                        uc.setRequestProperty("Authorization", authorizationString);
                        uc.disconnect(); // disconnect the connection.
                        final int serverResponseCode = uc.getResponseCode();
                        if (HttpURLConnection.HTTP_OK == serverResponseCode) {
                            String cookie = null;
                            for (String s : uc.getHeaderFields().get("Set-Cookie")) {
                                if (s.startsWith("JSESSIONID")) {
                                    cookie = s;
                                    break;
                                }
                            }
                            sessionCookie = cookie;
                            lastSessionCreateTime = System.currentTimeMillis();
                            LOGGER.info("Created new session cookie " + sessionCookie);
                            break;
                        } else {
                            LOGGER.error("Failed to get a valid login session from the controller at " + url
                                                 + "\nauthorizationString = " + authorizationString
                                                 + "\nServer response code: " + serverResponseCode
                                                 + "\nServer response message: " + uc.getResponseMessage());
                            sessionCookie = null;
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.error("Failed getSessionCookie " + url);
                    throw new RuntimeException(e);
                }
            }
        return sessionCookie;
    }

    public abstract String getServiceUrl();

    protected String getBaseUrl() {
        return RESTUI;
    }

    /**
     * Method to be used when performing a GET request to the restui API
     * @param serviceSuffix a relative path from the controller base URL to the rest endpoint
     * @param dtoClass DTO class to de-serialize
     * @return Detailed response or the numeric HTTP response code or RuntimeException on error.
     */
    protected Object readGetUrl(String serviceSuffix, Object dtoClass) {
        DetailedResponse detailedResponse;
        String url = getURL(serviceSuffix);
        LOGGER.info("Sending GET request to url: " + url);
        try {
            URL urlObj = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) urlObj.openConnection();
            uc.setUseCaches(false);
            uc.setRequestProperty(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            uc.setRequestProperty("Cookie", getSessionCookie());
            uc.connect();
            String errorMessage;
            final int responseCode = uc.getResponseCode();
            LOGGER.info("Got Response from controller: Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream stream = uc.getInputStream();
                String jsonText;
                Object json;
                if (dtoClass instanceof JsonObject) {
                    jsonText = IOUtils.toString(stream, "UTF-8");
                    json = new JsonParser().parse(jsonText).getAsJsonObject();
                } else if (dtoClass instanceof JsonArray) {
                    jsonText = IOUtils.toString(stream, "UTF-8");
                    json = new JsonParser().parse(jsonText).getAsJsonArray();
                } else {
                    json = readJson(stream, dtoClass);
                    jsonText = json.toString();
                }
                detailedResponse = new DetailedResponse(responseCode, uc.getResponseMessage(),
                        jsonText, json);
            } else if (responseCode == HTTP_NO_CONTENT) {
                detailedResponse = new DetailedResponse(responseCode, "No Content", "No Content");
            } else {
                InputStream errorStream = uc.getErrorStream();
                ByteBuffer buffer = ByteBuffer.allocate(errorStream.available());
                byte[] byteArray = buffer.array();
                errorStream.read(byteArray);
                errorMessage = new String(byteArray);
                detailedResponse = new DetailedResponse(responseCode, uc.getResponseMessage(), errorMessage);
            }
        } catch (Throwable e) {
            LOGGER.error("Failed readUrl to " + url);
            throw new RuntimeException(e);
        }
        return getRestResponse(detailedResponse, dtoClass == null);
    }

    private Object getRestResponse(DetailedResponse detailedResponse, boolean noContentIsValid) {
        if (detailedResponse.statusCode() == HttpURLConnection.HTTP_OK) {
            if (detailedResponse.responseObject() != null) {
                return detailedResponse.responseObject();
            } else {
                LOGGER.error("REST call successful but no object returned !!");
            }
        } else if (detailedResponse.statusCode() == HTTP_NO_CONTENT) {
            if (noContentIsValid) {
                LOGGER.info("REST call successful with HTTP status code " + detailedResponse.statusCode() + " !!");
                return detailedResponse.statusCode();
            } else {
                LOGGER.error("REST call un-successful due to HTTP status code " + detailedResponse.statusCode() + " !!");
            }
        } else {
            LOGGER.error("Error in get request: Status code: " + detailedResponse.statusCode()
                    + "Detailed Message: " + detailedResponse.detailedMessage());
        }
        return null;
    }

    /**
     * Method to be used when performing a GET request to the restui API
     * @param serviceSuffix a relative path from the controller base URL to the rest endpoint
     * @param dtoClass Object array to be used for de-serialization.
     * @return Detailed response or the numeric HTTP response code or RuntimeException on error.
     */
    protected Object readGetUrl(String serviceSuffix, Object[] dtoClass) {
        DetailedResponse detailedResponse;
        String url = getURL(serviceSuffix);
        LOGGER.info("Sending GET request to url: " + url);
        try {
            URL urlObj = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) urlObj.openConnection();
            uc.setUseCaches(false);
            uc.setRequestProperty(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            uc.setRequestProperty("Cookie", getSessionCookie());
            uc.connect();
            String errorMessage;
            final int responseCode = uc.getResponseCode();
            LOGGER.info("Got Response from controller: Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream stream = uc.getInputStream();
                Object[] json = readJson(stream, dtoClass);
                detailedResponse = new DetailedResponse(responseCode, uc.getResponseMessage(), json.toString(), json);
            } else if (responseCode == HTTP_NO_CONTENT) {
                detailedResponse = new DetailedResponse(responseCode, "No Content", "No Content");
            } else {
                InputStream errorStream = uc.getErrorStream();
                ByteBuffer buffer = ByteBuffer.allocate(errorStream.available());
                byte[] byteArray = buffer.array();
                errorStream.read(byteArray);
                errorMessage = new String(byteArray);
                detailedResponse = new DetailedResponse(responseCode, uc.getResponseMessage(), errorMessage);
            }
        } catch (Throwable e) {
            LOGGER.error("Failed readUrl to " + url);
            throw new RuntimeException(e);
        }
        return getRestResponse(detailedResponse, dtoClass == null);
    }

    /**
     * Method to be used when performing a POST request to the restPrefix API
     * @param serviceSuffix a relative path from the controller base URL to the rest endpoint
     * @param object the message body to be serialized in json format
     * @param dtoClass Object to de-serialize
     * @return Detailed response or the numeric HTTP response code or RuntimeException on error.
     */
    protected Object readPostUrl(String serviceSuffix, Object object, Object dtoClass) {
        DetailedResponse detailedResponse = new DetailedResponse(0, null, null);
        String url = getURL(serviceSuffix);
        LOGGER.info("Sending POST request to url: " + url);
        try {
            URL urlObj = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) urlObj.openConnection();
            uc.setRequestMethod("POST");
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.setUseCaches(false);
            uc.setRequestProperty(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            uc.setRequestProperty(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            uc.setRequestProperty("charset", "utf-8");
            uc.setRequestProperty("Cookie", getSessionCookie());
            String jsonBody;
            if (object instanceof JsonObject || object instanceof JsonArray) {
                JsonElement jsonElement = (JsonElement) object;
                jsonBody = jsonElement.getAsString();
            } else
                jsonBody = serializeObjects(object);
            byte[] postDataBytes = jsonBody.getBytes("UTF-8");
            uc.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            uc.getOutputStream().write(postDataBytes);
            uc.getOutputStream().flush();
            uc.disconnect(); // disconnect the connection.
            String errorMessage;
            final int responseCode = uc.getResponseCode();

            if (HttpURLConnection.HTTP_OK == responseCode) {
                InputStream stream = uc.getInputStream();
                String jsonText;
                Object json;
                if (dtoClass instanceof JsonObject) {
                    jsonText = IOUtils.toString(stream, "UTF-8");
                    json = new JsonParser().parse(jsonText).getAsJsonObject();
                } else if (dtoClass instanceof JsonArray) {
                    jsonText = IOUtils.toString(stream, "UTF-8");
                    json = new JsonParser().parse(jsonText).getAsJsonArray();
                } else {
                    json = readJson(stream, dtoClass);
                    jsonText = json.toString();
                }
                detailedResponse = new DetailedResponse(responseCode, uc.getResponseMessage(), jsonText, json);
            } else if (responseCode == HTTP_NO_CONTENT) {
                detailedResponse = new DetailedResponse(responseCode, "No Content", "No Content");
            } else {
                InputStream errorStream = uc.getErrorStream();
                ByteBuffer buffer = ByteBuffer.allocate(errorStream.available());
                byte[] byteArray = buffer.array();
                errorStream.read(byteArray);
                errorMessage = new String(byteArray);
                detailedResponse = new DetailedResponse(responseCode, uc.getResponseMessage(), errorMessage);
            }
        } catch (IOException e) {
            LOGGER.error("Failed POST to " + url);
        }

        return getRestResponse(detailedResponse, dtoClass == null);
    }

    /**
     * Method to be used when performing a POST request to the restui API
     * @param serviceSuffix a relative path from the controller base URL to the rest endpoint
     * @param objects the message body to be sent in json format
     * @param dtoClass Object array to de-serialize
     * @return Detailed response or the numeric HTTP response code or RuntimeException on error.
     */
    protected Object readPostUrl(String serviceSuffix, Object objects, Object[] dtoClass) {
        DetailedResponse detailedResponse = new DetailedResponse(0, null, null);
        String url = getURL(serviceSuffix);
        LOGGER.info("Sending POST request to url: " + url);
        try {
            URL urlObj = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) urlObj.openConnection();
            uc.setRequestMethod("POST");
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.setUseCaches(false);
            uc.setRequestProperty(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            uc.setRequestProperty(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            uc.setRequestProperty("charset", "utf-8");
            uc.setRequestProperty("Cookie", getSessionCookie());
            String jsonBody = serializeObjects(objects);
            byte[] postDataBytes = jsonBody.getBytes("UTF-8");
            uc.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            uc.getOutputStream().write(postDataBytes);
            uc.getOutputStream().flush();
            uc.disconnect(); // disconnect the connection.
            String errorMessage;
            final int responseCode = uc.getResponseCode();

            if (HttpURLConnection.HTTP_OK == responseCode) {
                if (getServiceUrl().equals("private")) {
                    detailedResponse = new DetailedResponse(responseCode, "Properties Updated", "Properties Updated",
                            "Properties Updated");
                } else {
                    InputStream stream = uc.getInputStream();
                    Object[] json = readJson(stream, dtoClass);
                    detailedResponse = new DetailedResponse(responseCode, uc.getResponseMessage(), json.toString(), json);
                }
            } else if (responseCode == HTTP_NO_CONTENT) {
                detailedResponse = new DetailedResponse(responseCode, "No Content", "No Content");
            } else {
                InputStream errorStream = uc.getErrorStream();
                ByteBuffer buffer = ByteBuffer.allocate(errorStream.available());
                byte[] byteArray = buffer.array();
                errorStream.read(byteArray);
                errorMessage = new String(byteArray);
                detailedResponse = new DetailedResponse(responseCode, uc.getResponseMessage(), errorMessage);
            }
        } catch (IOException e) {
            LOGGER.error("Failed POST to " + url);
        }

        return getRestResponse(detailedResponse, dtoClass == null);
    }

    private <T> T readJson(InputStream is, T dtoClass) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonText = IOUtils.toString(is, "UTF-8");
            return (T) mapper.readValue(jsonText, dtoClass.getClass());
        } finally {
            is.close();
        }
    }

    private <T> T[] readJson(InputStream is, T[] dtoClass) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonText = IOUtils.toString(is, "UTF-8");
            return (T[]) mapper.readValue(jsonText, dtoClass.getClass());
        } finally {
            is.close();
        }
    }

    private <T> String serializeObjects(T dtoClass) {
        return gson.toJson(dtoClass);
    }

    private String getURL(String serviceSuffix) {
        StringBuilder urlBuilder = new StringBuilder(controllerBasePath);
        if (!getBaseUrl().isEmpty())
            urlBuilder.append(getBaseUrl()).append("/");
        if (!getServiceUrl().isEmpty())
            urlBuilder.append(getServiceUrl()).append("/");
        urlBuilder.append(serviceSuffix);
        return urlBuilder.toString();
    }
}
