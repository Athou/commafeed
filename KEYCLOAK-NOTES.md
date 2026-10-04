# Keycloak User Directory

## Authentication Flow

CommaFeed protects the directory endpoints with the existing `ADMIN` role. An authenticated CommaFeed administrator calls the backend, and `KeycloakAdminService` uses a configured service-account client to obtain an access token from Keycloak's OpenID Connect token endpoint. The service then sends that bearer token to the Keycloak Admin REST API; Keycloak credentials and tokens never reach the browser.

The flow is:

1. An authenticated CommaFeed admin requests `/rest/admin/keycloak/users` or `/rest/admin/keycloak/users/{id}`.
2. The backend validates the `ADMIN` role.
3. The backend requests a client-credentials token from the configured Keycloak realm.
4. The backend calls the Keycloak Admin REST API and returns a limited user DTO.
5. Missing configuration, authentication failures, malformed responses, and upstream failures return `503 Service Unavailable`; user details are not exposed in error responses.

## Required Keycloak Admin API

The service calls these endpoints:

- `GET /admin/realms/{realm}/users` to list users.
- `GET /admin/realms/{realm}/users/{id}` to retrieve one user's details.
- `{base-url}/realms/{realm}/protocol/openid-connect/token` to obtain the service-account access token.

The service-account client must be allowed to query users in the target realm, typically through the `realm-management` client roles `query-users` and `view-users`.

## Environment Variables and Roles

Configure the backend with:

- `KEYCLOAK_BASE_URL`: Keycloak server base URL, for example `https://keycloak.example.com`.
- `KEYCLOAK_REALM`: realm containing the users.
- `KEYCLOAK_CLIENT_ID`: confidential client with service accounts enabled.
- `KEYCLOAK_CLIENT_SECRET`: secret for that client.

Directory access requires both:

- A logged-in CommaFeed user with the application `ADMIN` role.
- A Keycloak service account with sufficient realm-management permissions (`query-users` and `view-users`).

The browser only calls CommaFeed endpoints. It does not receive `KEYCLOAK_CLIENT_SECRET`, Keycloak access tokens, or direct administrative access.
