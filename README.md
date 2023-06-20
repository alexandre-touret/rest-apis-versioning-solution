
# REST APIs Versioning: Hands-on !

This workshop aims to introduce different ways to handle and propose several versions of a same API to your customers.

## :dart: Big picture

During this workshop we will strive with API versioning on a (small) microservice application.
Here is a short description of it.

This platform aims to store and get books of a bookstore.

### System View

```mermaid
C4Context
      title System Context diagram for Bookstore System
      Person(customerA, "Bookstore Customer", "A customer of the bookstore") 
      Person(adminA, "Bookstore Administrator", "An administrator of <br/> the bookstore") 
      Enterprise_Boundary(b0, "Bookstore Boundary") {
        System(bookstoreSystem, "Bookstore System", "Allows Book <br/> creation, search,...")  
        System(iamSystem, "Bookstore IAM", "Allows Identification <br/> & authorization...")  
      }
      Rel(customerA, bookstoreSystem, "Uses")
      Rel(adminA, bookstoreSystem, "Uses & manage users")
      Rel(customerA, iamSystem, "identifies & authorizes")
      Rel(adminA, iamSystem, "identifies & authorizes")
```

#### Explanations

Here we have two main kind of users:
* Customer : He can browse and create books
* Administrator: He can create books and activate/deactivate the maintenance mode

Within our platform, we have two main systems:

* Bookstore system which operate all the book related operations
* Bookstore IAM which is responsible for identifying and authorizing users

### Container view

```mermaid
C4Container
      title Container Context diagram for Bookstore System


      Person(customerA, "Bookstore Customer", "A customer of the bookstore") 
      Person(adminA, "Bookstore Administrator", "An administrator <br/> of the bookstore") 

      Enterprise_Boundary(b0, "Bookstore Boundary") {
        Container_Boundary(b2,"Bookstore IAM"){
          Container(iam,"IAM","Provides a JWT token with roles in claims")

               }
        Container_Boundary(b1,"Bookstore System"){
            Container(bookstoreApi,"Bookstore API","Spring Boot, Cloud","Exposes the Bookstore APIs")
            Container(gateway,"API Gateway","Spring Cloud Gateway","Exposes the APIs")
            ContainerDb(database, "Database", "PostgreSQL Database", "Stores bookstore")
            Container(isbnApi,"ISBN","Spring Boot, Cloud","Exposes the ISBN APIs")
            Container(configuration,"Configuration Server","Spring Cloud Config","Exposes the configuration")
            Container(zipkin,"Zipkin","Zipkin","Gathers and <br/> provides distributed tracing")
        }
      }

      Rel(customerA,gateway, "Uses")
      Rel(adminA, gateway, "Uses & manage users")
      Rel(customerA, iam, "identifies & authorizes")
      Rel(adminA, iam, "identifies & authorizes")
      Rel(gateway, iam, "verify token")
      Rel(gateway, bookstoreApi, "exposes")
      Rel(gateway, isbnApi, "exposes")
      Rel(bookstoreApi, isbnApi, "uses")
      Rel(bookstoreApi, database, "stores data")
      UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

#### Explanations

This diagram digs into the systems exposed above in the system view.

The Bookstore system is composed of:
* The API Gateway which exposes our APIs
* The Bookstore API which exposes all the related book APIs and stores data to a PostgreSQL database
* The ISBN API which provides random ISBN numbers
* A Configuration server which centralizes all the configuration files

The Bookstore IAM is composed of:
* A mock server which provides JWT token with appropriate roles and information.

### :straight_ruler: Stack
Here is a summary of the stack used in this workshop for this architecture:

| Container | Tools                                                        | Comments |
|---|--------------------------------------------------------------|---|
| API Gateway | Spring Cloud Gateway 2022.0.3                                |  |
| Bookstore API | JAVA 17,Spring Boot 3.1.X                                    |  |
| ISBN API | JAVA 17,Spring Boot 3.1.X                                    |  |
| Configuration Server | Spring Cloud Config 2022.0.3                                 |  |
| Database | PostgreSQL                                                   |  |
| Authorization Server | JAVA 17,Spring Boot 3.1.X, Spring Authorization Server 1.1.0 |  |


### Customers

## Our API Roadmap

```mermaid
%%{init: { 'logLevel': 'debug', 'theme': 'base', 'gitGraph': {'rotateCommitLabel': true}} }%%
gitGraph:
    commit id:"Init"
    commit id: "new features" tag:"Adding excerpt attribute & operation"
    branch V1
    checkout V1
    commit id:"add URI PATH versions"
    commit id: "add HTTP Header versions"
    commit id: "add accept HTTP Header versions"
    checkout main
    branch V2
    commit id: "revamping"
    commit id: "Add author list feature"
    checkout V1
    commit id: "Add fallback behaviour in V1"
    checkout V2
    commit id: "Authorization management"
    merge V1
    commit id: "Deprecating V1"
```

## :traffic_light: Prerequisites

### :mortar_board: Skills

| Skill                                                                                                                                                                                                                                                                                   | Level | 
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---|
| [REST API](https://google.aip.dev/general)                                                                                                                                                                                                                                              | proficient |
| [Java](https://www.oracle.com/java/)                                                                                                                                                                                                                                                    | novice |   
| [Gradle](https://gradle.org/)                                                                                                                                                                                                                                                           | novice |
| [Spring Framework](https://spring.io/projects/spring-framework), [Boot](https://spring.io/projects/spring-boot), [Cloud Config](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_quick_start), [Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/docs/current/reference/html/index.html)| novice |
| [OpenID Connect](https://openid.net/connect)                                                                                                                                                                                                                                            | novice |]
| [Docker](https://docs.docker.com/)                                                                                                                                                                                                                                                      | novice |

### :wrench: Tools
#### If you want to execute this workshop locally
You **MUST** have set up these tools first:
* [Java 17+](https://adoptium.net/temurin/releases/?version=17)
* [Gradle 7.5+](https://gradle.org/)
* [Docker](https://docs.docker.com/) & [Docker compose](https://docs.docker.com/compose/)
* Any IDE ([IntelliJ IDEA](https://www.jetbrains.com/idea), [VSCode](https://code.visualstudio.com/), [Netbeans](https://netbeans.apache.org/),...) you want
* [cURL](https://curl.se/), [jq](https://stedolan.github.io/jq/), [HTTPie](https://httpie.io/) or any tool to call your REST APIs


Here are commands to validate your environment:

**Java**

```jshelllanguage
java -version
    openjdk version "17.0.5" 2022-10-18
    OpenJDK Runtime Environment Temurin-17.0.5+8 (build 17.0.5+8)
    OpenJDK 64-Bit Server VM Temurin-17.0.5+8 (build 17.0.5+8, mixed mode, sharing)

```

**Gradle**

If you use the wrapper, you won't have troubles. Otherwise...:

```jshelllanguage
gradle --version

    Welcome to Gradle 7.6!
```

**Docker Compose**

```jshelllanguage
docker compose version
    Docker Compose version v2.12.2
```



#### :rocket: If you don't want to bother with a local setup

##### With Gitpod (recommended)
You can use [Gitpod](https://gitpod.io).
You must create an account first.
You then can open this project in either your local VS Code or directly in your browser:

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#github.com/alexandre-touret/rest-apis-versioning-workshop.git)

##### With Github Codespaces
You can also [use Github Codespaces](https://docs.github.com/en/codespaces/).
You can create a new one by [running "Code > Create codespace on main"](https://docs.github.com/en/codespaces/developing-in-codespaces/creating-a-codespace-for-a-repository#creating-a-codespace-for-a-repository).

You have then to run the command in the shell:

```jshelllanguage
pip install httpie
    sdk install java 17.0.5-tem
    sdk default java 17.0.5-tem
```

## :boom: Ready ?

> **Warning**
>
> I **strongly** suggest to fork this project into your personal GitHub namespace (aka your GitHub account).

You then can change the URL mentioned above to link GitHub and Gitpod:

```markdown
[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#github.com/%%MY_NAMESPACE%%/rest-apis-versioning-workshop.git)
```

or you can directly browse this URL (think to change the ``%%MY_NAMESPACE%%`` prefix):

``https://gitpod.io/#github.com/%%MY_NAMESPACE%%/rest-apis-versioning-workshop.git``


> **Note**
>
> Now, you can start [the workshop](./docs/index.md) :tada:.
  
