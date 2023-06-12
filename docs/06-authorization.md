# Last but not least : what about security and authorization impacts?

While versioning secured APIs, there is usually one impact we miss at the beginning: security, especially authorization.
If you apply authorization policies on your whole platform using for instance, ABAC or RBAC mechanisms, you have to take care about your authorization.
They could indeed evolve over your versions.

If you use [OAUTHv2](https://www.rfc-editor.org/rfc/rfc6749.html) or [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html) (_what else?_), you could restrict the usage of a version to specific clients or end users using scopes stored in claims.

You can declare scopes stored in claims such as: ``bookv1:write`` or ``numberv2:read`` to specify both the authorised action and the corresponding version.

We will see in this chapter how a standard [``credential flow`` authorization mechanism](https://www.rfc-editor.org/rfc/rfc6749#section-4.4) could handle versioning.

> **Note**
>
> * In the same way as for version handling, we will apply the security only in the gateway using the authorization server.
> * **In this chapter, we will only authorise URI Path service versions.**

## Enabling security

Before starting, please stop the [gateway](../gateway) and the [authorization server](../authorization-server).

### Authorization server:

In the [``application.properties`` file](../authorization-server/src/main/resources/application.properties), verify that we have specified the action in every scope:

```properties
server.port=8009
spring.application.name=authorization-server
authorization.url=http://localhost:${server.port}
authorization.clients.customer1.clientId=customer1
authorization.clients.customer1.clientSecret=secret1
authorization.clients.customer1.scopes=bookv1:read,bookv1:write,numberv1:read
authorization.clients.customer2.clientId=customer2
authorization.clients.customer2.clientSecret=secret2
authorization.clients.customer2.scopes=bookv2:read,bookv2:write,numberv2:read
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
http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer1" client_secret="secret1" scope="openid bookv1:write bookv1:write numberv1:read"
```

```jshelllanguage
http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer2" client_secret="secret2" scope="openid bookv2:write bookv2:read numberv2:read"
```

Verify you have the corresponding scopes.

```json
{
    "access_token": "eyJraWQiOiIxNTk4NjZlMC0zNWRjLTQ5MDMtYmQ5MC1hMTM5ZDdjMmYyZjciLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcjIiLCJhdWQiOiJjdXN0b21lcjIiLCJuYmYiOjE2NzI1MDQ0MTQsInNjb3BlIjpbImJvb2t2Mjp3cml0ZSIsIm51bWJlcnYyOnJlYWQiLCJvcGVuaWQiLCJib29rdjI6cmVhZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwMDkiLCJleHAiOjE2Nz
I1MDQ3MTQsImlhdCI6MTY3MjUwNDQxNH0.gAaDcOaORse0NPIauMVK_rhFATqdKCTvLl41HSr2y80JEj_EHN9bSO5kg2pgkz6KIiauFQ6CT1NJPUlqWO8jc8-e5rMjwWuscRb8flBeQNs4-AkJjbevJeCoQoCi_bewuJy7Y7jqOXiGxglgMBk-0pr5Lt85dkepRaBSSg9vgVnF_X6fyRjXVSXNIDJh7DQcQQ-Li0z5EkeHUIUcXByh19IfiFuw-HmMYXu9EzeewofYj9Gsb_7qI0Ubo2x7y6W2tvzmr2PxkyWbmoioZdY9K0
nP6btskFz2hLjkL_aS9fHJnhS6DS8Sz1J_t95SRUtUrBN8VjA6M-ofbYUi5Pb97Q",
    "expires_in": 299,
    "scope": "bookv2:write numberv2:read openid bookv2:read",
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

Finally, if you don't know how to create OIDC requests by your own, you can use https://oidcdebugger.com/.


### Declare routes and corresponding scopes in the gateway

In [the gateway's configuration](../gateway/src/main/resources/application.yml), enable first the security uncommenting this lines:

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
        /* Defaut configuration for OAUTH authorization (TO BE ADDED during the workshop) */
        http.csrf().disable().cors().disable()
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
                .oauth2ResourceServer().jwt(Customizer.withDefaults());

        /* If the previous configuration is applied, you would remove this following line (and the other way around)
        http.csrf().disable().cors().disable().authorizeExchange().anyExchange().permitAll();*/
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

Now restart the gateway:

```jshelllanguage
./gradlew bootRun -p gateway
```

#### Test it

Update the scripts with the appropriate version numbers in scopes and the corresponding ``client_id`` and ``client_secret``.

For instance, in the [``secureCountBooks.sh`` script file](../bin/secureCountBooks.sh), modify it:

from:

```jshelllanguage
#! /bin/bash


access_token=`http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer1" client_secret="secret1" scope="openid book:read" -p b | jq -r '.access_token'`

http :8080/v1/books/count "Authorization: Bearer ${access_token}"

```

to:

```jshelllanguage
#! /bin/bash


access_token=`http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer1" client_secret="secret1" scope="openid bookv1:read" -p b | jq -r '.access_token'`

http :8080/v1/books/count "Authorization: Bearer ${access_token}"

```

Try them all!
