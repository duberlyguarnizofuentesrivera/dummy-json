# Dummy JSON

A REST API to create and return dummy JSON objects for testing and mocking,
by [Duberly Guarnizo](https://duberlyguarnizo.com/)

## Advantages

- Multilingual (
  translations [easily created](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.internationalization)
  adding a translation properties file)
- REST error descriptions (via [ProblemDetail](https://datatracker.ietf.org/doc/html/rfc7807) specification)
- JSON Web Tokens (JWT: no need for csrf)
- User roles and endpoints permissions.
- Auditable entities
- Multi-session (Tokens with expiration and revoking flags stored on db per session)
- OpenAPI compatible (via [SpringDoc](https://springdoc.org/))

## Dependencies

- Spring Boot 3.1
- Gradle
- Docker (& [TestContainers](https://testcontainers.com/) for testing)
- Mapstruct
- OpenAPI (Swagger UI for frontend)
- [JJWT](https://github.com/jwtk/jjwt)

## Structure

Dummy Json is designed to return a valid JSON object (previously created by the user) or an error description (
implemented with Spring's ProblemDetail interface). The user creates an account, logs in sending a JSON body with
username and password, and gets a JSON body with a JWT token. Session is created when such JWT is added to the requests
headers, and is validated. With an open session, user creates a new JsonContent entity, and gets an url to query this
content. Any internet user can utilize that URL to get the same JsonContent (that endpoint is not secured). Abuse is
avoided by IP filtering and limiting number of queries in a space of time.

### Environment Variables

You need to configure the following environment variables for the program to work properly:

- FIRST_ADMIN_USERNAME: The username for the first admin created.
- FIRST_ADMIN_PASSWORD: The password for the first admin created.
- JWT_KEY: Your key to encode the JSON Web Tokens. Keep this secret!
- HOST_NAME: Your domain name, ie: [duberlyguarnizo.com](https://duberlyguarnizo.com/)

The environment variables related to the first admin created are only used for the first boot. If any user exists with
role ADMIN, the creation is skipped. Still, the presence of those environment variables is required.
### User

A user is either an admin, a supervisor, or a "client" (user who creates an account and uploads new JsonContent).It has
a role and permissions associated.

### JsonContent

JsonContent is an object that contains a text-formatted JSON object (not validated, yet).

### JwtToken

Represents a JWT token saved to validate if the request's token is expired (in case of abuse), revoked (log out), and
the associated user, so multiple concurrent tokens can be created per user (i.e.: when using multiple devices).

### Custom exceptions

To be thrown and managed with ProblemDetails API.

## Run

### Swagger

You can access the Swagger UI at: http://localhost:8080/swagger-ui/index.html where you will find all API definitions.

### Authentication

Send an AuthenticationRequest body, via POST request to http://localhost:8080/api/v1/auth/authenticate
with username and password fields. You will get a JWT token that you must attach in your headers as a bearer token.
The body must be formed like this:

```json
{
  "username": "your-user-name",
  "password": "your-password"
}
```

And the response, if correct credentials have been submitted:

```json
{
  "jwt": "your-long-jwt-auth-token"
}
```

## TODO

-[ ] Validate JSON content uploaded by user
-[ ] Implement frontend
-[ ] Add HATEOAS support
-[ ] Manage some ugly Spring RequestFilter exceptions