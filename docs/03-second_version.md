# And now something completely different : a second version

## TL;DR: What are you going to learn in this chapter?

This chapter covers the following topics:

1. Creating a new version (it will be a copy of the rest-book module)
2. Add a new breaking change functionality to the last version

## A new functionality for a new customer

We have now a new customer. 

Good news/bad news!

The good one is our API is now famous, the bad one is we must change our API contract without impacting our existing customers.

The very bad point, is our existing customers cannot update their API clients by one year (at least).
We then decided to create a new version!

In this case, it is strongly recommended to deal with GIT long time versions.
For instance, using [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

You can also use and ship Docker images built on top of this workflow to facilitate the deployment of module's versions.
To simplify the development loop of this workshop, we will only duplicate the [rest-book](../rest-book) module.

> **Note**
>
> In this chapter, we won't be working on having two separate versions running in the same time.
> We will be doing that on the next chapter after configuring the config server.

### Pre requisites

You **MUST** stop the running [rest-book module](../rest-book) before!

### Duplicating the rest-book module

* Update the [build.gradle](../build.gradle) file uncommenting the following configuration:

```groovy
project(':rest-book-2') {
    apply plugin: 'org.openapi.generator'
    dependencies {
        implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
        runtimeOnly 'org.postgresql:postgresql'
        testImplementation 'com.h2database:h2'
        implementation 'org.springframework.boot:spring-boot-starter-web'
        implementation 'org.springframework.boot:spring-boot-starter-validation'
        implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j'
        implementation 'org.springframework.cloud:spring-cloud-starter-config'
        implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocVersion}"
        implementation 'com.fasterxml.jackson.core:jackson-annotations'
        implementation "org.mapstruct:mapstruct:${mapstructVersion}"
        implementation 'org.zalando:logbook-spring-boot-starter:3.0.0'
        annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
    }
    openApiValidate {
        inputSpec = "$projectDir/src/main/resources/openapi.yml".toString()
        recommend = true
    }
    openApiGenerate {
        generatorName = "spring"
        library = "spring-boot"
        modelNameSuffix = "Dto"
        inputSpec = "$projectDir/src/main/resources/openapi.yml".toString()
        outputDir = "$buildDir/generated".toString()
        apiPackage = "info.touret.bookstore.spring.book.generated.controller"
        invokerPackage = "info.touret.bookstore.spring.book.generated.invoker"
        modelPackage = "info.touret.bookstore.spring.book.generated.dto"
        configOptions = [
                dateLibrary          : "java8",
                java8                : "true",
                openApiNullable      : "false",
                documentationProvider: "springdoc",
                useBeanValidation    : "true",
                interfaceOnly        : "true",
                useSpringBoot3       : "true"
        ]
    }
    tasks.withType(JavaCompile) {
        options.compilerArgs = [
                '-Amapstruct.suppressGeneratorTimestamp=true',
                '-Amapstruct.suppressGeneratorVersionInfoComment=true',
                '-Amapstruct.defaultComponentModel=spring'
        ]
    }

    springBoot {
        mainClass = "info.touret.bookstore.spring.RestBookstoreApplication"
    }
    sourceSets.main.java.srcDirs += "$buildDir/generated/src/main/java".toString()
    compileJava.dependsOn 'openApiGenerate'
}

```

In the [settings.gradle](../settings.gradle) file you have to define this new module.
Uncomment this line:

```properties
include 'rest-book-2'
```

Validate your configuration by building this project:

```jshelllanguage
./gradlew build -p rest-book-2
```

You will then have to re-import the new configuration in your IDE by refreshing it (You can also only build the new module by running this command):

```jshelllanguage
./gradlew build -p rest-book-2
```

## Adding a new functionality to rest-book2

In this new service, we are to deploy new features for our new customer. 
He/She has a huge library of books, we therefore want to limit the numbers of results provided by our [``/books`` API](../rest-book-2/src/main/java/info/touret/bookstore/spring/book/controller/BookController.java) to only 10 results.

We could imagine that a search engine functionality would be more realistic.
However, for this workshop, we will only work to a books list limiter.

This limit will be applied by creating a new query which uses a [``Pageable`` parameter](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Pageable.html).

This object is really useful to paginate results. We will only use it for limiting the data queried on the database and returned by our API.

In the rest-book-2 [``BookRepository`` class](../rest-book-2/src/main/java/info/touret/bookstore/spring/book/repository/BookRepository.java),
add the following method:

```java
List<Book> findAll(Pageable pageable);
```

Add also these import declarations:

```java
import org.springframework.data.domain.Pageable;
import java.util.List;
```

In the [BookService](../rest-book-2/src/main/java/info/touret/bookstore/spring/book/service/BookService.java) class, update the [``findAllBooks`` method](../rest-book-2/src/main/java/info/touret/bookstore/spring/book/service/BookService.java):

```java
public List<Book> findAllBooks(){
        return bookRepository.findAll(PageRequest.of(0,findLimit));
}
```

Add also the corresponding import declaration:

```java
import org.springframework.data.domain.PageRequest;
```

The field ``findLimit`` is set in the constructor:

```java
    private final Integer findLimit;

public BookService(BookRepository bookRepository,
        RestTemplate restTemplate,
@Value("${booknumbers.api.url}") String isbnServiceURL,
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") CircuitBreakerFactory circuitBreakerFactory,
@Value("${book.find.limit:10}") Integer findLimit){
        this.bookRepository=bookRepository;
        this.restTemplate=restTemplate;
        this.isbnServiceURL=isbnServiceURL;

        this.circuitBreakerFactory=circuitBreakerFactory;
        this.findLimit=findLimit;
        }


```

You have then to add some config lines and the [rest-book.yml](../config-server/src/main/resources/config/rest-book.yml) file:

```yaml
book:
  find:
    limit: 10
```

## Running it

Now, you can start your new module:

```jshelllanguage
./gradlew bootRun -p rest-book-2
```

You can test it and especially the new feature:

```jshelllanguage
http :8082/v1/books --print b | jq '. | length'
```

You must only get ten elements.

> **Note**
>
> In this chapter, we added a new feature creating a new version.
> At this time, we can't run both of the two versions simultaneously. It will be done in the next chapter.
>
> [Go to chapter 4](04-scm.md)
