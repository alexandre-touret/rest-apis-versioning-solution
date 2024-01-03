# How to upgrade your API without versioning?

At this point we have our first customer : **John Doe** who uses our API with the current specification.

## TL;DR: What will you learn in this chapter?

This chapter covers the following topics:

1. How to start the platform
2. Adding a non-breaking change and see how it doesn't impact the API Contract 

## Prerequisites

You must start three new shells and run [rest-book](../rest-book), [rest-number](../rest-number) and [the gateway](../gateway) modules.
As mentioned earlier, you must be at the root of the project (i.e., ``rest-apis-versioning-workshop``).

In the first shell, run:

```jshelllanguage
./gradlew bootRun -p rest-book
```

In the second one:

```jshelllanguage
./gradlew bootRun -p rest-number
```


And in the last one:

```jshelllanguage
./gradlew bootRun -p gateway
```

_You can disable unit and integration tests by adding the option ``-x test`` at the end of the command ;-)._


## The current status

### Getting the OpenAPI Documentation

You can now reach the current API documentation by running these commands:

For the books API:

```jshelllanguage
http :8082/v1/v3/api-docs
```
For the numbers API:

```jshelllanguage
http :8081/v1/v3/api-docs
```

You can also check the documentation by browsing these endpoints:

* http://localhost:8082/v1/swagger-ui/index.html
* http://localhost:8081/v1/swagger-ui/index.html

You can also use the scripts located in the [bin](../bin) folder.

Here are some examples of the functionalities provided:

* Get a Random Book

You can get a random book by running this command:

```jshelllanguage
. ./bin/randomBook.sh
```
* Create a book

```jshelllanguage
. ./bin/createBook.sh
```

Now you can stop this service (i.e., [rest-book](../rest-book)) now by typing CTRL+C on the shell you started the rest-book module.

## Adding new data

In this chapter, we will update the [Book schema in the OpenAPI spec file](../rest-book/src/main/resources/openapi.yml) adding the attribute ``excerpt``.

This attribute is (only for the workshop) the beginning of the [description attribute](../rest-book/src/main/resources/openapi.yml).
We will extract the first 100 characters.

1. Update the [OpenAPI spec file](../rest-book/src/main/resources/openapi.yml)
   of [the rest-book module]((../rest-book/src/main/resources/openapi.yml)) , add the ``excerpt`` attribute:

```yaml
    Book:
       required:
          - title
       type: object
       properties:
          excerpt:
             readOnly: true
             type: string
```
2. Build the application again

```jshelllanguage
./gradlew build -p rest-book
```

The build and tests should success. In the meantime, you would get this warning message:

```jshelllanguage
...mapper/BookMapper.java:13: warning: Unmapped target property: "excerpt".
   BookDto toBookDto(Book book);

```

It is *"normal"* because the POJO (*Plain Old Java Object*) used to persist data has not been modified yet.

3. Normally you can see now this new attribute in
   the [BookDto class](../rest-book/build/generated/src/main/java/info/touret/bookstore/spring/book/generated/dto/BookDto.java)
   .
4. In the [Book entity class](../rest-book/src/main/java/info/touret/bookstore/spring/book/entity/Book.java), add a
   transient attribute as below by uncommenting the following code.

```java

@Transient
private transient String excerpt;


// getter

public String getExcerpt(){
        return this.excerpt;
        }


@PostLoad
public void initFields(){
        if(description!=null) {
        this.excerpt = description.substring(0, 100);
        }
}
```
You can now rebuild the application.

Before creating unit and integration tests, we can run them to see if this modification is blocking.

```jshelllanguage
./gradlew build -p rest-book
```

:question: See what happens: Is it blocking or not?


5. Now, let's get a random book with an excerpt

Restart your rest-book service

```jshelllanguage
./gradlew bootRun -p rest-book
```

Check it manually by running the following command:

```jshelllanguage
http :8082/v1/books/1098 --print b | jq .excerpt 
```

You can also do that through the API Gateway:

```jshelllanguage
http :8080/v1/books/1098 --print b | jq .excerpt 
```
## Adding a new operation

You can then add a new operation ``getBookExcerpt``.

In the [OpenAPI spec file](../rest-book/src/main/resources/openapi.yml), add a new operation:

For instance:

```yaml
  /books/{id}/excerpt:
     get:
        tags:
           - book-controller
        summary: Gets a book's excerpt from its ID
        operationId: getBookExcerpt
        parameters:
           - name: id
             in: path
             required: true
             schema:
                type: integer
                format: int64
        responses:
           '200':
              description: Found book excerpt
              content:
                 application/json:
                    schema:
                       type: string
           '408':
              description: Request Timeout
              content:
                 "*/*":
                    schema:
                       "$ref": "#/components/schemas/APIError"
           '418':
              description: I'm a teapot
              content:
                 "*/*":
                    schema:
                       "$ref": "#/components/schemas/APIError"
           '500':
              description: Internal Server Error
              content:
                 "*/*":
                    schema:
                       "$ref": "#/components/schemas/APIError"

```


You can now generate the corresponding Java code.

```jshelllanguage
./gradlew  openApiGenerate -p rest-book
```

Now, let us create the corresponding method in [BookController](../rest-book/src/main/java/info/touret/bookstore/spring/book/controller/BookController.java):

Uncomment the following method:

```java
   @Override
public ResponseEntity<String> getBookExcerpt(Long id) {
        var optionalBook = bookService.findBookById(id);
        if (optionalBook.isPresent()) {
        return ResponseEntity.ok(optionalBook.get().getExcerpt());
        } else {
        return ResponseEntity.notFound().build();
        }
}
```

Build it again:

```jshelllanguage
./gradlew build -p rest-book 
```

You have now added new data and functionality to your API without any version :exclamation:

## What about backward compatibility?

Let's check
the [OldBookControllerIT](../rest-book/src/test/java/info/touret/bookstore/spring/book/controller/OldBookControllerIT.java)
integration test.
It uses
the [good old BookDto definition](../rest-book/src/test/java/info/touret/bookstore/spring/book/generated/dto/BookDto.java)
which represents the previous definition
of [BookDto](../rest-book/build/generated/src/main/java/info/touret/bookstore/spring/book/generated/dto/BookDto.java) (
i.e., without the ``excerpt`` functionality.
This class is based on the
first [BookDto definition](../rest-book/build/generated/src/main/java/info/touret/bookstore/spring/book/generated/dto/BookDto.java) (
i.e., without the ``exceprt`` attribute).

Run it, check the log output provided by [LogBook](https://github.com/zalando/logbook/).

```jshelllanguage
./gradlew -p rest-book test
```
Check the [test log file](../rest-book/build/test-results/test/TEST-info.touret.bookstore.spring.book.controller.OldBookControllerIT.xml) and search the HTTP logs

For instance:

```json
 {
  "origin" : "local",
  "type" : "response",
  "correlation" : "acc9e76fa90e42ed",
  "duration" : 36,
  "protocol" : "HTTP/1.1",
  "status" : 200,
  "headers" : {
    "Connection" : [ "keep-alive" ],
    "Content-Type" : [ "application/json" ],
    "Date" : [ "Mon, 12 Jun 2023 15:54:16 GMT" ],
    "Keep-Alive" : [ "timeout=60" ],
    "Transfer-Encoding" : [ "chunked" ]
  },
  "body" : {
  "excerpt" : "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut l",
  "title" : "la case de l oncle tom",
  "isbn13" : "1234567899123",
  "isbn10" : "1234567890",
  "author" : "Harriet Beecher Stowe",
  "yearOfPublication" : 1852,
  "nbOfPages" : 613,
  "rank" : 4,
  "price" : null,
  "smallImageUrl" : null,
  "mediumImageUrl" : null,
  "description" : "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
  "id" : 100
}
}

```

> **Note**
>
> See what happens, compare the data with
> the [good old BookDto definition](../rest-book/src/test/java/info/touret/bookstore/spring/book/generated/dto/BookDto.java)
> and **explain it** :exclamation:
>
> [Go then to chapter 2](./02-first_version.md)

