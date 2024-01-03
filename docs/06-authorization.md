# Last but not least : what about security and authorization impacts?

## TL;DR: What are you going to learn in this chapter?

This chapter covers the following topics:

1. Pinpointing the impacts on authorization
2. Enforcing API versions restrictions with OAUTHv2 scopes

While versioning secured APIs, there is usually one impact we miss at the beginning: security, especially authorization.
If you apply authorization policies on your whole platform using for instance, [ABAC](https://en.wikipedia.org/wiki/Attribute-based_access_control) or [RBAC](https://en.wikipedia.org/wiki/Role-based_access_control) approaches, you must take care about it.
They could indeed evolve over your versions.

If you use [OAUTHv2](https://www.rfc-editor.org/rfc/rfc6749.html) or [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html) (_what else?_), you would restrict the usage of a version to specific clients or end users using [scopes](https://auth0.com/docs/get-started/apis/scopes) stored in [claims](https://auth0.com/docs/secure/tokens/json-web-tokens/json-web-token-claims).

You can declare scopes stored in claims such as: ``book:v1:write`` or ``number:v2:read`` to specify both the authorised
action and the corresponding version.

We will see in this chapter how a standard [``credential flow`` authorization mechanism](https://www.rfc-editor.org/rfc/rfc6749#section-4.4) can handle versioning.

> **Note**
>
> * In the same way as for version handling, we will apply the security only in the gateway using the authorization server.
> * **In this chapter, we will only authorise URI Path service versions.**

## Enabling security

Before starting, please stop the [gateway](../gateway) and the [authorization server](../authorization-server).

### Authorization server

In the [``application.properties`` file](../authorization-server/src/main/resources/application.properties), update the configuration with the good scopes:

```properties
server.port=8009
spring.application.name=authorization-server
authorization.url=http://localhost:${server.port}
authorization.clients.customer1.clientId=customer1
authorization.clients.customer1.clientSecret=secret1
authorization.clients.customer1.scopes=book:v1:read,book:v1:write,number:v1:read
authorization.clients.customer2.clientId=customer2
authorization.clients.customer2.clientSecret=secret2
authorization.clients.customer2.scopes=book:v2:read,book:v2:write,number:v2:read
authorization.clients.gateway.clientId=gateway
authorization.clients.gateway.clientSecret=secret3
authorization.clients.gateway.scopes=gateway
logging.level.org.springframework.web:INFO
logging.level.org.springframework.security:INFO
logging.level.org.springframework.security.oauth2:INFO
spring.security.oauth2.resourceserver.jwt.issuer-uri=${authorization.url}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${authorization.url}
spring.zipkin.base-url=http://localhost:9411
spring.zipkin.sender.type=web
management.tracing.sampling.probability=1.0
management.endpoints.web.exposure.include=prometheus
```

In this example, we declared the ``customer1`` can use the version 1 and the ``customer2`` the v2.

You can start it now:

```jshelllanguage
./gradlew bootRun -p authorization-server
```

#### Test it

You can now try to generate token as either the ``customer1`` or ``customer2``:

For ``customer1``:

```jshelllanguage
http --form  :8009/oauth2/token grant_type="client_credentials" client_id="customer1" ="secret1" scope="openid book:v1:write book:v1:write number:v1:read"
```

```jshelllanguage
http --form  :8009/oauth2/token grant_type="client_credentials" client_id="customer2" client_secret="secret2" scope="openid book:v2:write book:v2:read number:v2:read"
```

Verify you have the corresponding scopes.
Here is the customer2's token:

```json
{
  "access_token": "eyJraWQiOiJiMmI5NjFjYi1lM2VlLTQ5OGMtOGIxNi01YmFmZTRjYzZmOWEiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcjIiLCJhdWQiOiJjdXN0b21lcjIiLCJuYmYiOjE2OTYyNjA1NjQsInNjb3BlIjpbIm9wZW5pZCIsImJvb2s6djI6cmVhZCIsImJvb2s6djI6d3JpdGUiLCJudW1iZXI6djI6cmVhZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwMDkiLCJleHAiOjE2OTYyNjA4NjQsImlhdCI6MTY5NjI2MDU2NH0.bC-2X4Zfz7TRPZ45zPhhKVPpOg6rZH0FSskL8Z7cIq-iAUiSwoSK60kUKcgEKVgjlfZfBge2B0yvSExCM16Bf_7HhbKppbUjLJ7dO3to_oh1TJVdpdG54l_2hIRI3SGFVxaKk9NpkXbiPq4-nT2HdVbrtd6JlB0R0ticKqhjOJElosA7jGQ-YoCVSJxpdrlcahI-1I0kX_0vqD_iN58XU-saqGG3cG9hG-NjR_NCj5DYG4AEUWu-wFQlRrG8IBwJJmlS3ibM-uVU9jG2mLNrJsCMTJccVnoQ9J17T3L5twEyXg511qlCyqJFvDXSg03pxPFYxex_Yz1GpIcvjnyn_A",
  "expires_in": 299,
  "scope": "openid book:v2:read book:v2:write number:v2:read",
  "token_type": "Bearer"
}

```

You can also try using inappropriate scopes (e.g., using ``bookv1:read`` scope for ``customer2``).

You MUST have this error:

```json
{
    "error": "invalid_scope"
}

```

If you want you can also verify the ``access_token`` and the claims on [jwt.io](https://jwt.io/) website.

After copying/pasting the access token, you can see the following output with the corresponding roles:

```json
{
  "sub": "customer2",
  "aud": "customer2",
  "nbf": 1696260564,
  "scope": [
    "openid",
    "book:v2:read",
    "book:v2:write",
    "number:v2:read"
  ],
  "iss": "http://localhost:8009",
  "exp": 1696260864,
  "iat": 1696260564
}
```


Finally, if you don't know how to create [OIDC requests](https://openid.net/developers/how-connect-works/) by your own, you can use https://oidcdebugger.com/.

### Declare routes and corresponding scopes in the gateway

In [the gateway's configuration](../gateway/src/main/resources/application.yml), enable first the security uncommenting these lines:

```yaml
# SECURITY CONFIGURATION TO BE APPLIED (remove comments to apply it)
  security:
    oauth2:
      client:
        registration:
          login-client:
            provider: authz
            client-id: gateway
            client-secret: secret3
            authorization-grant-type: client_credentials
            redirect-uri-template: "{baseUrl}/"
            scope: gateway
        provider:
          authz:
            authorization-uri: http://localhost:8009/oauth2/authorize
            token-uri: http://localhost:8009/oauth2/token
            user-info-uri: http://localhost:8009/oauth2/userinfo
            user-name-attribute: sub
            jwk-set-uri: http://localhost:8009/oauth2/token_keys
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8009
```

Uncomment block codes in the [gateway application](../gateway/src/main/java/info/touret/bookstore/spring/gateway/GatewayApplication.java) to get the following content:

```java
  @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {

            http.csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
            .pathMatchers(GET, "/v1/books/count").hasAuthority("SCOPE_bookv1:read")
            .pathMatchers(GET, "/v1/books/random").hasAuthority("SCOPE_bookv1:read")
            .pathMatchers(POST, "/v1/books").hasAuthority("SCOPE_bookv1:write")
            .pathMatchers(GET, "/v1/books").hasAuthority("SCOPE_bookv1:read")
            .pathMatchers("/v1/isbns").hasAuthority("SCOPE_numberv1:read")
            .pathMatchers(GET, "/v2/books/count").hasAuthority("SCOPE_bookv2:read")
            .pathMatchers(GET, "/v2/books/random").hasAuthority("SCOPE_bookv2:read")
            .pathMatchers(POST, "/v2/books").hasAuthority("SCOPE_bookv2:write")
            .pathMatchers(GET, "/v2/books").hasAuthority("SCOPE_bookv2:read")
            .pathMatchers("/v2/isbns").hasAuthority("SCOPE_numberv2:read")
            .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec.jwt(Customizer.withDefaults()));
        /* If the previous configuration is applied, you would remove this following line (and the other way around)
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.anyExchange().permitAll());*/
            return http.build();
            }

    /* If the security is enabled, you MUST uncomment the following factories */
    @Bean
    JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
        return NimbusJwtDecoder.withJwkSetUri(properties.getJwt().getJwkSetUri()).build();

    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String issuerUrl) {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUrl);
    }
```

Update then the import statements:

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

```

Now restart the gateway:

```jshelllanguage
./gradlew bootRun -p gateway
```

#### Test it

Update the scripts with the appropriate version numbers in scopes and the corresponding ``client_id`` and ``client_secret``.

For instance, in the [``secureCountBooks.sh`` script file](../bin/secureCountBooks.sh), check you have the good scopes:

```jshelllanguage
#! /bin/bash


access_token=`http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer1" client_secret="secret1" scope="openid bookv1:read" -p b | jq -r '.access_token'`

http :8080/v1/books/count "Authorization: Bearer ${access_token}"

```

Try them all!
