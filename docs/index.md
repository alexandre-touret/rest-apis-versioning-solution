# Hand's on

## Repository organisation

Here is how this repository is organised and a short explanation:

```jshelllanguage
    ├── authorization-server    -->OAUTH2 Spring Authorization Server
    ├── config-server           -->Spring Cloud Configuration Server
    ├── docs                    -->Workshop documentation
    ├── gateway                 -->Spring Cloud Gateway
    ├── gradle                  -->Gradle configuration files and wrapper binaries
    ├── infrastructure          -->Docker compose based infrastructure(database,zipkin)
    ├── rest-book               -->Spring Boot Bookstore application
    └── rest-number             -->Spring Boot Number application
```

## :running: Warm up

### :computer: Infrastructure

:warning: The required infrastructure is available by
running [Docker containers](https://www.docker.com/resources/what-container/).

You can start the whole by running [Docker compose](https://docs.docker.com/compose/).

```bash
cd infrastructure
docker compose up -d
```

If you start this command on Gitpod, [you will be asked to make public or not some network ports](https://www.gitpod.io/docs/configure/workspaces/ports).  
You can make them public.

You can then check the running containers by running this command:

```jshelllanguage
docker compose ps
```

```bash
NAME                COMMAND                  SERVICE             STATUS              PORTS
books-database      "docker-entrypoint.s…"   database            running             0.0.0.0:5432->5432/tcp
books-zipkin        "start-zipkin"           zipkin              running (healthy)   9410/tcp, 0.0.0.0:9411->9411/tcp

```

## :information_desk_person: Spring services to be started before

:warning: You **MUST** also start the [config-server](../config-server) service before.

Start a new shell and run the following command at the root of the project (i.e., ``rest-apis-versioning-workshop``)

```jshelllanguage
./gradlew bootRun -p config-server
```

You should have the following output indicating the service is ready:

```
 [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8888 (http) with context path ''
 [  restartedMain] i.t.a.c.ConfigServerApplication          : Started ConfigServerApplication in 2.986 seconds (JVM running for 3.328)
```

:warning: You can ignore the message below and start using the API

```jshelllanguage
<==========---> 80% EXECUTING [13s]
```

To check it is effectively ready, you can reach the API by running this command:

```jshelllanguage
http http://localhost:8888/rest-number/default 
```

You should have such an output:

```json
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Thu, 15 Dec 2022 19:04:45 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
"label": null,
"name": "rest-number",
"profiles": [
"default"
],
"propertySources": [
{
"name": "classpath:/config/rest-number.yml",
"source": {
"logging.level.org.springframework.web": "info",
"management.auditevents.enabled": true,
"management.endpoint.health.enabled": true,
"management.endpoint.health.probes.enabled": true,
"management.endpoint.health.show-details": "always",
"management.endpoint.metrics.enabled": true,
"management.endpoint.prometheus.enabled": true,
"management.endpoint.shutdown.enabled": true,
"management.endpoints.enabled-by-default": true,
"management.endpoints.jmx.exposure.include": "*",
"management.endpoints.web.exposure.include": "*",
"management.health.livenessstate.enabled": true,
"management.health.readinessstate.enabled": true,
"management.metrics.distribution.percentiles-histogram.http.server.requests": true,
"management.tracing.sampling.probability": 1.0,
"resilience4j.bulkhead.configs.default.maxConcurrentCalls": 100,
"resilience4j.bulkhead.instances.book-numbers.maxConcurrentCalls": 10,
"resilience4j.thread-pool-bulkhead.configs.default.coreThreadPoolSize": 2,
"resilience4j.thread-pool-bulkhead.configs.default.maxThreadPoolSize": 4,
"resilience4j.thread-pool-bulkhead.configs.default.queueCapacity": 2,
"resilience4j.thread-pool-bulkhead.instances.book-numbers.baseConfig": "default",
"resilience4j.timelimiter.configs.default.cancelRunningFuture": false,
"resilience4j.timelimiter.configs.default.timeoutDuration": "2s",
"resilience4j.timelimiter.instances.book-numbers.baseConfig": "default",
"server.port": 8081,
"spring.application.name": "rest-number",
"spring.cloud.circuitbreaker.resilience4j.enabled": true,
"spring.zipkin.base-url": "http://localhost:9411",
"spring.zipkin.sender.type": "web",
"time.to.sleep": 1000
}
}
],
"state": null,
"version": null
}

```


:warning: In the same way, you **MUST** also start the [authorization-server](../authorization-server).
You can run it by running the following command:

```jshelllanguage
./gradlew bootRun -p authorization-server
```

You should get such an output:

```jshelllanguage
2022-11-29 10:10:45.775  INFO 13729 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8009 (http) with context path ''
2022-11-29 10:10:45.789  INFO 13729 --- [  restartedMain] i.t.b.s.a.AuthorizationServerApplication : Started AuthorizationServerApplication in 4.657 seconds (JVM running for 4.972)
```

You can test it by running this command:

```jshelllanguage
http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer1" client_secret="secret1" -p b
```

You should then have an access token in the response:

```json
{
    "access_token": "eyJraWQiOiJlZjY5MTRkMC0wM2UwLTRlZmUtYjg5NS00MzMyOTlkMmE5ODAiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJhdWQiOiJjdXN0b21lcjEiLCJuYmYiOjE2Njk3MTMxNjYsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODAwOSIsImV4cCI6MTY2OTcxMzQ2NiwiaWF0IjoxNjY5NzEzMTY2fQ.E6_tfUxoULlAPUf91OYSyAu3YG0ZLCBIzNgaOq8cH7MKo5ZMTjUmGMycChtRQZPn7BAyseqQy8e8nwwkzcx9aIFOakQvKTg5GSJBwwwNUvpqvc91NhUfXy-KpuzLnDph4YIP5PjnUQwByBU5rsK_ALVQlcY5AePgErlcUszPx0VgZoLBrp46ld520BccAa9Tz20TaNz5wMlqURqrz7bwp-Q65iCVy3TrLaiT4qrdNLsCsxlJA-0HIrlBTU8HBt0Xv0oh-8P6iTFZvH7s1qtwby1fSZ11eGOOA5_SZ7JJ-9oC5L7-bdA0LBSQxDJtEJJOZBG1Ellypj8NWPRPFZt_UA",
    "expires_in": 299,
    "token_type": "Bearer"
}
```

## :sparkles: Ready? Let's deep dive into versioning!

Here are the chapters covered by this workshop:

1. [Dealing with updates without versioning](./01-without_versioning.md)
2. [Our first version](./02-first_version.md)
3. [Adding new customers and a new functionalities](./03-second_version.md)
4. [Configuration management](./04-scm.md)
5. [Dealing with conflicts](./05-conflicts.md)
6. [Authorization issues](./06-authorization.md)
