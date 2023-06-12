# Dealing with breaking changes

## Preamble

Now it is time to move on.

We just deprecated our [first version](../rest-book) and add new features for our new customers while bringing them wisely to our existing ones!

How to migrate your customers who use the V1 to the V2 ?
Good question!

The first thing to do is to communicate on a regular basis the roadmap and the planned your product End Of Life (EoL) milestones.

By the way, our customer wants having several authors for a same book.
Currently, one book could only have one author.
This functionality could be considered as a [breaking change](https://en.wiktionary.org/wiki/breaking_change).

Beyond the API definition, this new functionality impacts the whole application. From the OpenAPI description to database schema.
How could we do that maintaining two versions of our API for our customers?


## V2 API Changes

Stop the [rest-book-2](../rest-book-2) and [rest-book](../rest-book) modules.

In the [rest-books-2 OpenAPI description file](../rest-book-2/src/main/resources/openapi.yml), update the definition of the ``Book``. Rename the fied ``author`` to ``authors`` and define it like this:

```yaml
    authors:
      type: array
      items:
        $ref: '#/components/schemas/Author'
```

Now, create the Author object below the ``Maintenance`` object:

````yaml
    Author:
      type: object
      properties:
        lastname:
          type: string
        firstname:
          type: string
        id:
          type: integer
          format: int64

````

Regenerate the corresponding classes:

```jshelllanguage
./gradlew openApiGenerate -p rest-book-2 
```

You should see in the [generated sources folder](../rest-book-2/build/generated/src/main) the new ``Author`` class.

If you build your application, the build will fail.

```jshelllanguage
./gradlew clean build -p rest-book-2
```
## What's next?
Regarding the use case, we should apply this new relationship between the ``Book`` and ``Author`` objects into the whole application, from the API to the database.

This new feature is really a breaking change. How to add this feature without disturbing the existing customers? We have few ways:

* By isolating the different tenants in a dedicated database/schema. It means the database schema could be also versioned.
* By mixing the features in the same schema (adding a field author and an author list)
* By anticipating the depreciation of the v1, upgrading the database schema and updating it to keep return only one author to the author list.

You got it: there is no free lunch!

Although the first solution is the smartest, it implies several impacts: database data migrations,lack of loose coupling between the API and the database, painful version upgrades and such like.

In this workshop, we will apply the last option:

## Create the new functionality in the V2

We will implement in this chapter this new functionality.

### JPA Entities

Create an ``Author`` entity with the following content:


<details>
<summary>Click to expand</summary>

```java
package info.touret.bookstore.spring.book.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
public class Author implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID publicId;


    private String lastname;

    private String firstname;

    @ManyToMany(mappedBy = "authors")
    private List<Book> books;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
}

```
</details>

Modify the [``Book``](../rest-book-2/src/main/java/info/touret/bookstore/spring/book/entity/Book.java) adding a new field ``authors``:

```java
@ManyToMany(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH,CascadeType.DETACH})
private List<Author> authors;
```

Add then the getters and setters:

```java
    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
```

### Spring Data repository

Create the repository ``AuthorRepository`` in the package [``info.touret.bookstore.spring.book.repository``](../rest-book-2/src/main/java/info/touret/bookstore/spring/book/repository):

```java
public interface AuthorRepository extends CrudRepository<Author,Long> {
    Optional<Author> findByPublicId(UUID uuid);

}

```

### Service layer implementation

Here is how I implemented this new feature in the service layer.
It impacts the ``persistBook`` and ``updateBook`` methods:

```java
public Book updateBook(@Valid Book book) {
        return bookRepository.save(updateBookGettingOrCreatingNewAuthors(book));
        }
[...]

private Book updateBookGettingOrCreatingNewAuthors(Book book){
        book.setAuthors(book.getAuthors().stream().map(author ->
        authorRepository.findByPublicId(author.getPublicId()).orElseGet(() -> {
        author.setBooks(List.of(book));
        return author;
        })
        ).toList());
        return book;
        }

private Book persistBook(Book book) {
        var isbnNumbers = restTemplate.getForEntity(isbnServiceURL, IsbnNumbers.class).getBody();
        if (isbnNumbers != null) {
        book.setIsbn13(isbnNumbers.getIsbn13());
        book.setIsbn13(isbnNumbers.getIsbn10());
        }
        return bookRepository.save(updateBookGettingOrCreatingNewAuthors(book));
}
```

You have to inject the ``AuthorRepository`` class in the constructor:

```java
 public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       RestTemplate restTemplate,
                       @Value("${booknumbers.api.url}") String isbnServiceURL,
                       @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") CircuitBreakerFactory circuitBreakerFactory,
                       @Value("${book.find.limit:10}") Integer findLimit) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.restTemplate = restTemplate;
        this.isbnServiceURL = isbnServiceURL;

        this.circuitBreakerFactory = circuitBreakerFactory;
        this.findLimit = findLimit;
```

and add a field ``authorRepository``:

```java
private final AuthorRepository authorRepository;
```

### Import Data

You can remove all the data located in [``import.sql``](../rest-book-2/src/main/resources/import.sql) and replace it by:

<details>
<summary>Click to expand</summary>

```sql
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1000,'Antonio','Concalves','7c11e1bf-1c74-4280-812b-cbc6038b7d21');
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1001,'Roger','Kitain','b7896f56-3168-4d45-aca7-745e2071bca2');
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1002,'Kinman','Chung','0138897a-a867-4f0a-8063-9744a289df15');
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1003,'Lincoln','Baxter','6122e45a-4880-4014-96ef-162f380c06f3');
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1004,'Antoine','Sabot-Durand','d61c3c68-bb34-4ebe-bce0-76240bf64361');
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1005,'Adam','Bien','8c8fd370-a17b-419c-b175-8ca50010ee88');
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1006,'Nigel','Deakin','640db75b-15e8-48d5-8ec7-9992d7ca4e05');
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1007,'Ed','Burns','00ce3ab4-bce3-42f2-b1dd-2eec75b62b30');
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1008,'Arun','Gupta','640db75b-15e8-48d5-8ec7-9992d7ca4e05');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 997, '9781980399025', 'Understanding Bean Validation', 9, 'https://images-na.ssl-images-amazon.com/images/I/31fHenHChZL._SL160_.jpg', 'https://images-na.ssl-images-amazon.com/images/I/31fHenHChZL.jpg', 9.99, 129, 2018,  'In this fascicle will you will learn Bean Validation and use its different APIs to apply constraints on a bean, validate all sorts of constraints and write your own constraints');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 998, '9781093918977', 'Understanding JPA', 9, 'https://images-na.ssl-images-amazon.com/images/I/3122s2sjOtL._SL160_.jpg', 'https://images-na.ssl-images-amazon.com/images/I/3122s2sjOtL.jpg', 9.99, 246, 2019,  'In this fascicle, you will learn Java Persistence API, its annotations for mapping entities, as well as the Java Persistence Query Language and entity life cycle');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1002, '1931182311', 'Advanced Java EE Development for Rational Application Developer 7.5: Developers'' Guidebook Author: Robert McChesney Nov-2011', 3, 'http://ecx.images-amazon.com/images/I/51pri75YOPL._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/51pri75YOPL._SL160_.jpg', 9.99, 543, 2011,  'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1001, '1931182310', 'Advanced Java EE Development for Rational Application Developer 7.5: Developers'' Guidebook', 2, 'http://ecx.images-amazon.com/images/I/51bjnhlGbeL._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/51bjnhlGbeL._SL160_.jpg', 79.95, 752, 2011, 'Written by IBM senior field engineers and senior product development experts, this advanced book provides a solid look at the development of a range of core Java EE technologies, as well as an in-depth description of the development facilities provided by IBM Rational Application Developer version 7.5. Since the Java EE developmental platform incorporates a wide range of technologies from disparate and myriad sources, this up-to-date guidebook helps developers triumph over the complexity and depth of knowledge required to build successful architectures. Senior developers, engineers, and architects—especially those who work with Rational Application Developer and those seeking certification at the Sun-certified Java master-tier level or the IBM Rational Application Developer certified professional and certified advanced professional levels—will appreciate this convenient, single reference point.');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1003, '1931182312', 'Advanced Java EE Development for Rational Application Developer 7.5: Developers'' Guidebook 2nd , Seco edition by McChesney, Robert, Cole, Kameron, Raszka, Richard (2011) Paperback', 3, 'http://ecx.images-amazon.com/images/I/511pqkJd3UL._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/511pqkJd3UL._SL160_.jpg', 9.99, 987, 2005,  'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1004, '1514210959', 'Advanced Java EE Development with WildFly', 3, 'http://ecx.images-amazon.com/images/I/51f7V8CEb7L._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/51f7V8CEb7L._SL160_.jpg', 44.99, 416, 2015, 'Your one-stop guide to developing Java® EE applications with the Eclipse IDE, Maven, and WildFly® 8.1 About This Book •Develop Java EE 7 applications using the WildFly platform •Discover how to use EJB 3.x, JSF 2.x, Ajax, JAX-RS, JAX-WS, and Spring with WildFly 8.1 •A practical guide filled with easy-to-understand programming examples to help you gain hands-on experience with Java EE development using WildFly Who This Book Is For This book is for professional WildFly developers. If you are already using JBoss or WildFly but don''t use the Eclipse IDE and Maven for development, this book will show you how the Eclipse IDE and Maven facilitate the development of Java EE applications with WildFly 8.1. This book does not provide a beginner-level introduction to Java EE as it is written as an intermediate/advanced course in Java EE development with WildFly 8.1. In Detail This book starts with an introduction to EJB 3 and how to set up the environment, including the configuration of a MySQL database for use with WildFly. We will then develop object-relational mapping with Hibernate 4, build and package the application with Maven, and then deploy it in WildFly 8.1, followed by a demonstration of the use of Facelets in a web application. Moving on from that, we will create an Ajax application in the Eclipse IDE, compile and package it using Maven, and run the web application on WildFly 8.1 with a MySQL database. In the final leg of this book, we will discuss support for generating and parsing JSON with WildFly 8.1.');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1005, '8894038912', 'Advanced Jax-Ws Web Services', 2, 'http://ecx.images-amazon.com/images/I/31lRGN%2BfvDL._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/31lRGN%2BfvDL._SL160_.jpg', 22.90, 154, 2014,  'In this book you''ll learn the concepts of Soap based Web Services architecture and get practical advice on building and deploying Web Services in the enterprise. Starting from the basics and the best practices for setting up a development environment, this book enters into the inner details of the Jax-Ws in a clear and concise way. You will also learn about the major toolkits available for creating, compiling and testing Soap Web Services and how to address common issues such as debugging data and securing its content. What you will learn: Move your first steps with Soap Web Services. Developing Web Services using top-down and bottom-up approach. Using Maven archetypes to speed up Web Services creation. Getting into the details of Jax-Ws types: Java to Xml mapping and Xml to Java Developing Soap Web Services on WildFly 8 and Tomcat. Running native Apache Cxf on WildFly Securing Web Services. Applying authentication policies to your services. Encrypting the communication.');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1006, '0071763929', 'Mike Meyers'' Guide to Supporting Windows 7 for CompTIA A+ Certification (Exams 701 & 702) (All-in-One)', 9, 'http://ecx.images-amazon.com/images/I/51Dk-Zq2I7L._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/51Dk-Zq2I7L._SL160_.jpg', 30.00, 240, 2011, 'Mike Meyers'' Guide to Supporting Windows 7 for CompTIA A+ Certification, Exams 220-701 & 220-702 Get the latest information on the new Windows 7 topics and questions added to CompTIA A+ exams 220-701 and 220-702. A must-have companion to CompTIA A+ All-in-One Exam Guide, Seventh Edition andMike Meyers'' CompTIA A+ Guide to Managing and Troubleshooting PCs, Third Edition, this book focuses on the new exam objectives. Mike Meyers'' Guide to Supporting Windows 7 for CompTIA A+ Certification provides learning objectives at the beginning of each chapter, exam tips, practice exam questions, and in-depth explanations. Written by the leading authority on CompTIA A+ certification and training, this essential resource provides the up-to-date coverage you need to pass the exams with ease. Mike Meyers, CompTIA A+, CompTIA Network+, CompTIA Security+, MCP, is the industry''s leading authority on CompTIA A+ certification and the bestselling author of seven editions of CompTIA A+ All-in-One Exam Guide. He is the president of PC and network repair seminars for thousands of organizations throughout the world, and a member of CompTIA.');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1007, '1849512442', 'Apache Maven 3 Cookbook (Quick Answers to Common Problems)', 1, 'http://ecx.images-amazon.com/images/I/41uiGX%2BdFRL._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/41uiGX%2BdFRL._SL160_.jpg', 39.99, 224, 2011, 'Take Flash to the next dimension by creating detailed, animated, and interactive 3D worlds with Away3D OverviewCreate stunning 3D environments with highly detailed texturesAnimate and transform all types of 3D objects, including 3D TextEliminate the need for expensive hardware with proven Away3D optimization techniques, without compromising on visual appeal Written in a practical and illustrative style, which will appeal to Away3D beginners and Flash developers alike In DetailAway3D is one of the most popular real-time 3D engines for Flash. Besides creating various detailed 3D environments you can also create animated 3D scenes, use various special effects, integrate third-party libraries, and much more. The possibilities of using this engine are endless. But will you be able take full advantage of all these features and make a 3D application that is picture perfect?. This is the best book for guiding you through Away3D, and the possibilities it opens up for the Flash platform. You''ll be able to create basic 3D objects, display lifelike animated characters, construct complex 3D scenes in stunning detail, and much more with this practical hands-on guide. Starting with the very basics, this book will walk you through creating your first Away3D application, and then move on to describe and demonstrate the many features that are available within Away3D such as lighting, shading, animation, 3D text, model loading and more. With the help of this comprehensive guide to all the information you ever needed to use Away3D, you''ll find yourself creating incredibly detailed 3D environments in no time. You begin with an overview of downloading the Away3D source code and configuring various authoring tools like Flex Builder, Flash Builder, FlashDevelop, and Flash CS4. Next you ease your way through creating your first primitive 3D object from scratch, then move on to creating stunning 3D environments with incredibly detailed textures and animations. You will make applications react to mouse events, with the click of a mouse – literally, learn ways to focus your camera and perfect your creation by viewing it from all angles, and take your Away3D application to the next level by overcoming the limitations in default Away3D algorithms. You will also learn optimization techniques to obtain the best performance from Away3D, without compromising on visual appeal. Create stunning real-world 3D Flash applications, right from displaying your first sphere to creating entire 3D cities, with plenty of tips to help you avoid common pitfalls. What you will learn from this bookDraw primitive shapes such as cubes, cones, spheres, and planes without having to manually construct them from their basic elements Add eye-catching special effects to your Away3D applicationWarp, curve, modify, and bend 3D text to your willFocus the Camera and view 3D objects from all anglesImprove mouse interactivity in your 3D applicationIntegrate third-party libraries such as TweenLite and Stardust with Away3D to animate 3D objects and create particle effects Use sprites and sprite classesUtilize the power of Pixel Bender for image processingExport 3D models from 3D modeling applications such as 3ds Max, Blender, MilkShape, and Sketch-Up Get practical tips on achieving maximum performance in your 3D applications');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1008, '2746062399', 'Apache Tomcat 7 - Guide d''administration du serveur Java EE 6 sous Windows et Linux', 3, 'http://ecx.images-amazon.com/images/I/5159Mv-pl3L._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/5159Mv-pl3L._SL160_.jpg', 9.99, 234, 1999,  'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1009, '2746086336', 'Apache Tomcat 8 - Guide d''administration du serveur Java EE 7 sous Windows et Linux', 3, 'http://ecx.images-amazon.com/images/I/413b2IfJmsL._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/413b2IfJmsL._SL160_.jpg', 9.99, 123, 2009,  'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1010, '1492201448', 'Apache TomEE Cookbook: Apache TomEE Administrator Cookbook', 1, 'http://ecx.images-amazon.com/images/I/61fIAAa6poL._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/61fIAAa6poL._SL160_.jpg', 35.00, 196, 2013, 'This cookbook is written for learning TomEE internals by server administrators and Java EE developers. Administrators will learn how to configure TomEE for using it in a production environment. Developers will learn how to create web applications using the Java™ Platform, Enterprise Edition 6 (Java EE 6) technologies provided by TomEE runtime and to deploy these enterprise applications into TomEE. Chapters of the book: Chapter 1 : Introduction Chapter 2 : Getting Started Chapter 3 : TomEE Architecture Chapter 4 : TomEE Web Server, Apache Tomcat Chapter 5 : TomEE EJB Lite Server, Apache OpenEJB Chapter 6 : Deployments in TomEE Chapter 7 : JavaEE Technologies Used in TomEE Chapter 8 : TomEE Security Chapter 9 : JNDI Naming In TomEE Chapter 10 : Transactions in TomEE Chapter 11 : TomEE Clustering Features Chapter 12 : TomEE WebSocket Protocol Support Chapter 13 : TomEE GUI Chapter 14 : Testing Techniques in TomEE Chapter 15 : TomEE Embedded Usage Chapter 16 : Useful References Chapter 17 : ASF License');
INSERT INTO Book(id, isbn_13, title, rank, small_image_url, medium_image_url, price, nb_of_pages, year_of_publication, description) VALUES ( 1011, '148885162X', 'Application Server 131 Success Secrets: 131 Most Asked Questions On Application Server - What You Need To Know', 3, 'http://ecx.images-amazon.com/images/I/51s-9pleKyL._SL75_.jpg', 'http://ecx.images-amazon.com/images/I/51s-9pleKyL._SL160_.jpg', 29.95, 60, 2014,  'A fresh Application Server approach. An program server may be whichever a code model that delivers a simplified tactic to generating an application-server effectuation, short of heed to what the program purposes are, either the server part of a concrete effectuation example. In whichever instance, the server''s purpose is committed to the effectual implementation of methods (programs, procedures, scripts) for helping its affected applications. There has never been a Application Server Guide like this. It contains 131 answers, much more than you can imagine; comprehensive answers and extensive details and references, with insights that have never before been offered in print. Get the information you need--fast! This all-embracing guide offers a thorough view of key knowledge and detailed insight. This Guide introduces what you want to know about Application Server. A quick look inside of some of the subjects covered: Web application server - URL mapping, Web application server - Web services, WebSphere Application Server - Version 8.5, Comparison of application servers - Haskell, Application server Advantages of application servers, Oracle Application Server 10g, WebSphere Application Server - Version 6.0, Application server Other platforms, Oracle Application Server 10g - Components, Base4 Application Server - History, Application server - Advantages of application servers, Application server - Java application servers, Comparison of application servers - Ruby, Web application server - Ajax, WebSphere Application Server for z/OS - WAS z/OS Platform Exploitation, Comparison of application servers - Smalltalk, TNAPS Application Server - Hosting, WebLogic Application Server, Sun Java System Application Server - Releases, WebSphere Application Server - Version 7.0, WebLogic Application Server - Components, SAP NetWeaver Application Server - Authentication, and much more…');

INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (997,1000);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (998,1000);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1001,1001);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1002,1002);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1003,1003);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1004,1004);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1005,1000);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1006,1003);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1007,1005);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1008,1000);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1009,1006);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1010,1007);
INSERT INTO BOOK_AUTHORS(books_id,authors_id) values (1011,1008);
```
</details>

### Tests

Your tests are currently failing.
It is time to fix them up!

Delete the [OldBookControllerIT](../rest-book-2/src/test/java/info/touret/bookstore/spring/book/controller/OldBookControllerIT.java) and the [OldBookDto](../rest-book/src/test/java/info/touret/bookstore/spring/book/dto/OldBookDto.java) class.
We don't need them anymore.

#### BookServiceTest

In [this class](../rest-book-2/src/test/java/info/touret/bookstore/spring/book/service/BookServiceTest.java), replace the following statements

```java
book.setAuthor("author");
```

by

```java
var author = new Author();
author.setFirstname("firstname");
author.setLastname("lastname");
author.setPublicId(UUID.randomUUID());
book.setAuthors(List.of(author));
```

Update the ``setUp`` method:

```java
    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository,authorRepository, restTemplate, "URL", circuitBreakerFactory,10);
    }
```

In the ``should_update_book``, modify the last assertion:

```java
@Test
void should_update_book(){
        [...]
        assertEquals(book.getAuthors(),updateBook.getAuthors());
        }
```

Don't forget to mock your new repository:

```java
@MockBean
private AuthorRepository authorRepository;
```

#### BookControllerIT

Replace in the same way than above the ``authors`` field initialization. You can also to that in a fluent way

```java
var authorDto = new AuthorDto().firstname("George").lastname("Orwell").publicId("6ce999fa-31bd-4a52-9692-22f55d2a1d2f");
book.setAuthors(List.of(authorDto));
```

You can also add an assertion in the ``should_find_a_book()`` method:

```java
assertEquals("7c11e1bf-1c74-4280-812b-cbc6038b7d21", bookDto.getAuthors().get(0).getPublicId());
```

You have to update the [``books-data.sql``](../rest-book-2/src/test/resources/books-data.sql) file:

```sql
delete from book_authors ;
delete from author ;
delete from book ;
commit;
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1000,'Harriet','Beecher Stowe','7c11e1bf-1c74-4280-812b-cbc6038b7d21');
insert into book (id,title,isbn_13,isbn_10,year_of_publication,nb_of_pages,rank,small_image_url,medium_image_url,description) values (100,'la case de l oncle tom','1234567899123','1234567890',1852,613,4.2,null,null,'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.');
insert into book_authors(books_id, authors_id) VALUES (100,1000);
```

### Test it

Run the following command:

```jshelllanguage
./gradlew clean build -p rest-book-2
```

Check new if both your infrastructure and your config server are up.

Run the application now:

```jshelllanguage
./gradlew clean build -p rest-book-2
```

Try this request and verify you have a list of authors in every book

For instance:

```jshelllanguage
http :8083/v2/books 
```

```json
[...]
 "authors": [
            {
                "firstname": "Antonio",
                "lastname": "Concalves",
                "publicId": "7c11e1bf-1c74-4280-812b-cbc6038b7d21"
            }
        ],

```

## Dealing with changes for existing customers in the v1

Now, the database is not usable as is for the V1.

You have to modify it without impacting the service contract (i.e., your OpenAPI definition).
For this workshop, we will do the translation in the [mappers](../rest-book/src/main/java/info/touret/bookstore/spring/book/mapper).

### JPA entities

Copy/paste [the entities modified in the v2](../rest-book-2/src/main/java/info/touret/bookstore/spring/book/entity).

### Spring Data repository
Nothing to do here.

### Service layer
Nothing to do here too

### Mapper layer
Create a class ``AuthorMapper`` in the package ``info.touret.bookstore.spring.book.mapper``:

```java
package info.touret.bookstore.spring.book.mapper;

import info.touret.bookstore.spring.book.entity.Author;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface AuthorMapper {

    default String toString(List<Author> authors) {
        if (authors != null && authors.get(0) != null) {
            return authors.get(0).getFirstname() + " " + authors.get(0).getLastname();
        }
        return null;
    }

    default List<Author> toAuthorList(String author) {
        if (author != null) {
            Author newAuthor = new Author();
            newAuthor.setLastname(author);
            return List.of(newAuthor);
        }
        return null;
    }

}

```

Yes [``Null`` sucks](https://en.wikipedia.org/wiki/Tony_Hoare) but it is the MapStruct _normal_ way .

In the [``BookMapper`` class](../rest-book/src/main/java/info/touret/bookstore/spring/book/mapper/BookMapper.java), declare the [``AuthorMapper``](../rest-book/src/main/java/info/touret/bookstore/spring/book/mapper/AuthorMapper.java) as a dependency:

```java
@Mapper(uses = AuthorMapper.class)
public interface BookMapper {
    [...]
}
```

### Import Data

Copy paste the [V2 ``import.sql``](../rest-book-2/src/main/resources/import.sql) content into [the V1](../rest-book/src/main/resources/import.sql).

### Tests

In the same way than for the V1, delete the [OldBookControllerIT](../rest-book-2/src/test/java/info/touret/bookstore/spring/book/controller/OldBookControllerIT.java) and the [OldBookDto](../rest-book/src/test/java/info/touret/bookstore/spring/book/dto/OldBookDto.java) class.
We don't need them anymore.

#### BookServiceTest

Fix your test as earlier your ``author`` field declaration:

```java
   var author = new Author();
   author.setFirstname("Harriet");
   author.setLastname("Beecher Stowe");
   author.setPublicId(UUID.randomUUID());
   book.setAuthors(List.of(author));
```

#### BookControllerIT

Add an assertion in [the ``should_find_a_book()`` method](../rest-book/src/test/java/info/touret/bookstore/spring/book/controller/BookControllerIT.java):

```java
assertEquals("Harriet Beecher Stowe",bookDto.getAuthor());
```

### Test it

Run the following command first:

```jshelllanguage
./gradlew clean build -p rest-book 
```

Then, start the rest-book module:

```jshelllanguage
./gradlew bootRun -p rest-book
```

Reach then the API and check the author attribute:

```jshelllanguage
http :8082/v1/books
```


> **Note**
>
> We have seen in this chapter the potential issues of breaking changes.
> Adding new features creating new versions usually affect the previous ones.
> That's why it is recommended to only propose **TWO** alive versions to your customers. The V1 is deprecated and the V2 is the target version.
>
> Sometimes the workaround presented here is not possible. You have then to deal with database duplication and versioning. You can use [FlywayDB](https://flywaydb.org/) or [Liquibase](http://www.liquibase.org/) for that purpose.
>
> [Go then to chapter 6](06-authorization.md)
