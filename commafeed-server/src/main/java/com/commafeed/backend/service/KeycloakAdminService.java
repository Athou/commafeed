package com.commafeed.backend.service;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.frontend.rest.resources.model.KeycloakUserModel;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Slf4j
public class KeycloakAdminService {

    private static final String BASE_URL_ENV = "KEYCLOAK_BASE_URL";
    private static final String REALM_ENV = "KEYCLOAK_REALM";
    private static final String CLIENT_ID_ENV = "KEYCLOAK_CLIENT_ID";
    private static final String CLIENT_SECRET_ENV = "KEYCLOAK_CLIENT_SECRET";

    private final HttpClient httpClient;
    private final CommaFeedConfiguration config;

    public KeycloakAdminService(CommaFeedConfiguration config) {
        this.config = config;
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(config.httpClient().connectTimeout())
                        .build();
    }

    public List<KeycloakUserModel> findUsers() throws KeycloakUnavailableException {
        return parseUsers(sendAdminRequest("/users"));
    }

    public KeycloakUserModel findUser(String id)
            throws KeycloakUnavailableException, KeycloakUserNotFoundException {
        try {
            JsonObject userJson = new JsonObject(sendAdminRequest("/users/" + encodePath(id)));
            return toModel(userJson);
        } catch (KeycloakUnavailableException e) {
            if (e.notFound()) {
                throw new KeycloakUserNotFoundException();
            }
            throw e;
        }
    }

    private String sendAdminRequest(String path) throws KeycloakUnavailableException {
        KeycloakConfiguration configuration = getConfiguration();
        String token = requestToken(configuration);
        String baseUrl = removeTrailingSlash(configuration.baseUrl());
        String endpoint = baseUrl + "/admin/realms/" + encodePath(configuration.realm()) + path;

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(config.httpClient().responseTimeout())
                        .header("Authorization", "Bearer " + token)
                        .header("Accept", "application/json")
                        .GET()
                        .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Keycloak request interrupted");
            throw new KeycloakUnavailableException(false);
        } catch (IOException | RuntimeException e) {
            log.warn("Keycloak request failed");
            throw new KeycloakUnavailableException(false);
        }
        if (response.statusCode() == 404) {
            throw new KeycloakUnavailableException(true);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new KeycloakUnavailableException(false);
        }
        return response.body();
    }

    private String requestToken(KeycloakConfiguration configuration)
            throws KeycloakUnavailableException {
        String endpoint =
                removeTrailingSlash(configuration.baseUrl())
                        + "/realms/"
                        + encodePath(configuration.realm())
                        + "/protocol/openid-connect/token";
        String body =
                "grant_type=client_credentials"
                        + "&client_id="
                        + encode(configuration.clientId())
                        + "&client_secret="
                        + encode(configuration.clientSecret());
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(config.httpClient().responseTimeout())
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new KeycloakUnavailableException(false);
            }
            String token = new JsonObject(response.body()).getString("access_token");
            if (StringUtils.isBlank(token)) {
                throw new KeycloakUnavailableException(false);
            }
            return token;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Keycloak token request interrupted");
            throw new KeycloakUnavailableException(false);
        } catch (IOException | RuntimeException e) {
            log.warn("Keycloak token request failed");
            throw new KeycloakUnavailableException(false);
        }
    }

    private KeycloakConfiguration getConfiguration() throws KeycloakUnavailableException {
        String baseUrl = System.getenv(BASE_URL_ENV);
        String realm = System.getenv(REALM_ENV);
        String clientId = System.getenv(CLIENT_ID_ENV);
        String clientSecret = System.getenv(CLIENT_SECRET_ENV);
        if (StringUtils.isAnyBlank(baseUrl, realm, clientId, clientSecret)) {
            throw new KeycloakUnavailableException(false);
        }
        return new KeycloakConfiguration(baseUrl, realm, clientId, clientSecret);
    }

    private List<KeycloakUserModel> parseUsers(String responseBody)
            throws KeycloakUnavailableException {
        try {
            JsonArray users = new JsonArray(responseBody);
            List<KeycloakUserModel> result = new ArrayList<>(users.size());
            for (Object user : users) {
                result.add(toModel((JsonObject) user));
            }
            return result;
        } catch (RuntimeException e) {
            log.warn("Invalid Keycloak users response");
            throw new KeycloakUnavailableException(false);
        }
    }

    private KeycloakUserModel toModel(JsonObject user) {
        return new KeycloakUserModel(
                user.getString("id"),
                user.getString("username"),
                user.getString("email"),
                user.getString("firstName"),
                user.getString("lastName"),
                Boolean.TRUE.equals(user.getBoolean("enabled")),
                user.getLong("createdTimestamp"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String encodePath(String value) {
        return encode(value).replace("+", "%20");
    }

    private static String removeTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record KeycloakConfiguration(
            String baseUrl, String realm, String clientId, String clientSecret) {}

    public static class KeycloakUnavailableException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final boolean notFound;

        public KeycloakUnavailableException(boolean notFound) {
            this.notFound = notFound;
        }

        public boolean notFound() {
            return notFound;
        }
    }

    public static class KeycloakUserNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
